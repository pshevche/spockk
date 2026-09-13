/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package io.github.pshevche.spockk.compilation.transformer.interaction

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Kotlin.INT_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.ANY_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.ANY_METHOD_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.DID_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.DOES_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.NO_MORE_INTERACTIONS_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.RETURNED_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.RETURNS_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.TIMES_FQN
import io.github.pshevche.spockk.compilation.ir.dispatchReceiverArg
import io.github.pshevche.spockk.compilation.ir.extensionReceiverArg
import io.github.pshevche.spockk.compilation.ir.fqName
import io.github.pshevche.spockk.compilation.ir.regularArgs
import io.github.pshevche.spockk.compilation.ir.singleRegularArg
import io.github.pshevche.spockk.compilation.ir.unwrapImplicitCoercionToUnit
import io.github.pshevche.spockk.compilation.ir.unwrapImplicitNotNull
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName

/**
 * Recognizes the statement shapes the interaction DSL compiles to and parses them into
 * [Interaction]s. Nothing here builds IR; nothing in [InteractionStatementsRewriter] parses it.
 *
 * An interaction is written as a cardinality and/or a response wrapped around an ordinary call on
 * the mock: `2 * greeter.greet("Alice") returned "hi"`. Kotlin's operator precedence puts the
 * response infixes below `*`, so that parses as `(2 * greeter.greet("Alice")) returned "hi"` and
 * unwrapping proceeds outside-in: response first, then cardinality, leaving the call itself.
 *
 * [allowBareCall] also accepts a call with no wrapper at all. That is correct only inside a
 * `Mock`/`Stub`/`Spy` builder block, where every statement is by construction a call on the mock;
 * in `then`/`expect` blocks a wrapper is required, so an ordinary boolean condition is never
 * mistaken for an interaction.
 */
internal fun IrStatement.asInteraction(allowBareCall: Boolean = false): Interaction? {
  val call = asBareCall() ?: return null

  val (afterResponse, response) = call.unwrapResponse()
  val (afterCardinality, cardinality) = afterResponse.unwrapCardinality()
  // A response can also sit inside the cardinality's operand rather than around it, when written
  // with explicit parentheses: `1 * (greeter.greet("Alice") returned "hi")`.
  val (methodCall, nestedResponse) = afterCardinality.unwrapResponse()

  if (cardinality == null && response == null && nestedResponse == null && !allowBareCall) return null
  return methodCall.toInteraction(source = this, cardinality = cardinality, response = response ?: nestedResponse)
}

/** `noMoreInteractions(a, b)`: sugar for an expected count of zero on every listed mock. */
internal fun IrStatement.asNoMoreInteractionsCall(): IrCall? =
  asBareCall()?.takeIf { it.fqName() == NO_MORE_INTERACTIONS_FQN }

internal fun List<IrStatement>.hasInteractionStatement(): Boolean =
  any { it.asInteraction() != null || it.asNoMoreInteractionsCall() != null }

private fun IrCall.toInteraction(
  source: IrStatement,
  cardinality: InteractionCardinality?,
  response: InteractionResponse?
): Interaction = if (fqName() == ANY_METHOD_FQN) {
  Interaction(
    source = source,
    cardinality = cardinality,
    target = requireNotNull(extensionReceiverArg()) { "anyMethod() is missing its receiver" },
    method = InteractionMethod.Wildcard,
    // anyMethod() promises to match any call "regardless of arguments", which is not the same as
    // matching an empty argument list - the rewriter spreads a wildcard over the whole list instead.
    args = emptyList(),
    response = response
  )
} else {
  val target = requireNotNull(dispatchReceiverArg()) { "${symbol.owner.name} is missing its dispatch receiver" }
  Interaction(
    source = source,
    cardinality = cardinality,
    // The interaction's own `target.method(...)` call is never really invoked, and the target
    // becomes a plain nullable argument to addEqualTarget, so it no longer needs the not-null check
    // dispatching a member call required.
    target = target.unwrapImplicitNotNull(),
    method = InteractionMethod.Named(symbol.owner.name.asString()),
    args = regularArgs().map { it.toInteractionArg() },
    response = response
  )
}

private fun IrExpression.toInteractionArg(): InteractionArg =
  if ((this as? IrCall)?.fqName() == ANY_FQN) InteractionArg.Wildcard else InteractionArg.EqualTo(this)

private fun IrCall.unwrapResponse(): Pair<IrCall, InteractionResponse?> {
  val wrappedCall = extensionReceiverArg() as? IrCall ?: return this to null
  val payload = singleRegularArg() ?: return this to null
  return when (fqName()) {
    DOES_FQN, DID_FQN -> wrappedCall to InteractionResponse.Code(payload)
    RETURNS_FQN, RETURNED_FQN -> wrappedCall to InteractionResponse.Constant(payload)
    else -> this to null
  }
}

private fun IrCall.unwrapCardinality(): Pair<IrCall, InteractionCardinality?> {
  if (fqName() != TIMES_FQN) return this to null
  val count = extensionReceiverArg() ?: return this to null
  val wrappedCall = singleRegularArg() as? IrCall ?: return this to null
  val cardinality = if (count.type.classFqName == INT_FQN) {
    InteractionCardinality.Fixed(count)
  } else {
    InteractionCardinality.Range(count)
  }
  return wrappedCall to cardinality
}

private fun IrStatement.asBareCall(): IrCall? = (this as? IrExpression)?.unwrapImplicitCoercionToUnit() as? IrCall
