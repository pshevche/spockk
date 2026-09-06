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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers
import io.github.pshevche.spockk.compilation.ir.fqName
import io.github.pshevche.spockk.compilation.ir.unwrapImplicitCoercionToUnit
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.name.FqName

/** `N * target.method(args)` or `(a..b) * target.method(args)`. */
internal sealed class InteractionCardinality {
  internal class Fixed(val count: IrExpression) : InteractionCardinality()

  /** [range] evaluates to an `IntRange`; the rewriter reads its `first`/`last` at runtime. */
  internal class Range(val range: IrExpression) : InteractionCardinality()
}

/** `does`/`did` (arbitrary code) or `returns`/`returned` (a fixed value). */
internal sealed class InteractionResponse {
  internal class Code(val block: IrExpression) : InteractionResponse()

  internal class Constant(val value: IrExpression) : InteractionResponse()
}

/**
 * A parsed interaction statement, unwrapped to its optional cardinality, real `target.method(args)`
 * call, and optional response. [statement] is the original list entry, for in-place replacement.
 */
internal class InteractionStatement(
  val statement: IrStatement,
  val cardinality: InteractionCardinality?,
  val methodCall: IrCall,
  val response: InteractionResponse?
)

/**
 * Recognizes an interaction statement and unwraps it down to [InteractionStatement]'s pieces.
 * Kotlin's operator precedence puts the response infixes below `*`, so `1 * call() returned "x"`
 * parses as `(1 * call()) returned "x"` - unwrapping proceeds outside-in: response, then cardinality.
 *
 * [allowBareCall] accepts a statement with no wrapper at all as an interaction whose innermost call
 * is the statement itself - only correct for the `Mock`/`Stub` builder-block context (every top-level
 * statement there is guaranteed to be a call on the mocked interface); `then`/`expect` blocks require
 * a recognized wrapper, so an ordinary condition is never misidentified as an interaction.
 */
internal fun IrStatement.asInteractionStatement(allowBareCall: Boolean = false): InteractionStatement? {
  val original = this
  val topLevelCall = (this as? IrExpression)?.unwrapImplicitCoercionToUnit() as? IrCall ?: return null

  val (afterOuterResponse, outerResponse) = topLevelCall.unwrapResponse() ?: (topLevelCall to null)
  val (afterCardinality, cardinality) = afterOuterResponse.unwrapCardinality() ?: (afterOuterResponse to null)
  // A response can also end up nested inside the cardinality's operand rather than wrapping it, e.g.
  // an explicitly parenthesized `1 * (obj.method() returned "x")` - try unwrapping it there too.
  val (methodCall, innerResponse) = afterCardinality.unwrapResponse() ?: (afterCardinality to null)

  val response = outerResponse ?: innerResponse
  if (cardinality == null && response == null && !allowBareCall) return null
  return InteractionStatement(original, cardinality, methodCall, response)
}

private fun IrCall.unwrapResponse(): Pair<IrCall, InteractionResponse>? {
  val receiver = extensionReceiverArg() as? IrCall ?: return null
  val payload = singleRegularArg() ?: return null
  return when (fqName()) {
    IrIdentifiers.Spockk.DOES_FQN, IrIdentifiers.Spockk.DID_FQN -> receiver to InteractionResponse.Code(payload)
    IrIdentifiers.Spockk.RETURNS_FQN, IrIdentifiers.Spockk.RETURNED_FQN -> receiver to InteractionResponse.Constant(payload)
    else -> null
  }
}

private fun IrCall.unwrapCardinality(): Pair<IrCall, InteractionCardinality>? {
  if (fqName() != IrIdentifiers.Spockk.TIMES_FQN) return null
  val receiver = extensionReceiverArg() ?: return null
  val wrappedCall = singleRegularArg() as? IrCall ?: return null
  val cardinality = if (receiver.type.classFqName == INT_FQN) {
    InteractionCardinality.Fixed(receiver)
  } else {
    InteractionCardinality.Range(receiver)
  }
  return wrappedCall to cardinality
}

private val INT_FQN = FqName("kotlin.Int")

internal fun IrCall.isAnyMarkerCall(): Boolean = fqName() == IrIdentifiers.Spockk.ANY_FQN

internal fun IrCall.isAnyMethodMarkerCall(): Boolean = fqName() == IrIdentifiers.Spockk.ANY_METHOD_FQN

internal fun IrStatement.asNoMoreInteractionsCall(): IrCall? {
  val call = (this as? IrExpression)?.unwrapImplicitCoercionToUnit() as? IrCall ?: return null
  return call.takeIf { it.fqName() == IrIdentifiers.Spockk.NO_MORE_INTERACTIONS_FQN }
}

internal fun IrCall.extensionReceiverArg(): IrExpression? = parameterArg(IrParameterKind.ExtensionReceiver)

internal fun IrCall.dispatchReceiverArg(): IrExpression? = parameterArg(IrParameterKind.DispatchReceiver)

internal fun IrCall.singleRegularArg(): IrExpression? = regularArgs().singleOrNull()

internal fun IrCall.regularArgs(): List<IrExpression> =
  symbol.owner.parameters.withIndex()
    .filter { (_, parameter) -> parameter.kind == IrParameterKind.Regular }
    .mapNotNull { (index, _) -> arguments[index] }

private fun IrCall.parameterArg(kind: IrParameterKind): IrExpression? {
  val index = symbol.owner.parameters.indexOfFirst { it.kind == kind }
  return if (index >= 0) arguments[index] else null
}

internal fun IrStatement.isInteractionStatement(): Boolean = asInteractionStatement() != null

internal fun List<IrStatement>.hasInteractionStatement(): Boolean =
  any { it.isInteractionStatement() || it.asNoMoreInteractionsCall() != null }

/**
 * The result of extracting interaction statements out of a block: [addInteractionStatements] are the
 * real `mockController.addInteraction(...)` statements built from them (in original order);
 * [remainingStatements] is the block's own statement list with those removed (also in original
 * order, everything else untouched).
 */
internal class InteractionExtractionResult(
  val addInteractionStatements: List<IrStatement>,
  val remainingStatements: List<IrStatement>
)
