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
 * A parsed interaction statement, unwrapped down to its innermost pieces: the optional cardinality
 * prefix, the real `target.method(args)` call (or a call to the [IrIdentifiers.Spockk.ANY_METHOD_FQN]
 * marker, for `target.anyMethod()`), and the optional response suffix. [statement] is the original,
 * possibly coercion-wrapped, list entry - needed to find and replace/remove it in place.
 */
internal class InteractionStatement(
  val statement: IrStatement,
  val cardinality: InteractionCardinality?,
  val methodCall: IrCall,
  val response: InteractionResponse?
)

/**
 * Recognizes an interaction statement and unwraps it down to [InteractionStatement]'s pieces.
 *
 * Kotlin's operator precedence puts the response infix functions (`does`/`did`/`returns`/`returned`)
 * *below* the `*` cardinality operator, so `1 * obj.getUsername() returned "Name"` parses as
 * `(1 * obj.getUsername()) returned "Name"` - the response wraps the cardinality call, not the other
 * way around. Unwrapping therefore proceeds outside-in: response first, then cardinality, then
 * whatever's left must be the real method call.
 *
 * [allowBareCall] accepts a statement with neither a cardinality nor a response wrapper as a
 * still-valid interaction whose innermost call is the statement itself - only correct for the
 * `Mock`/`Stub` builder-block context, where every top-level statement is guaranteed to be a call on
 * the mocked interface (see [io.github.pshevche.spockk.compilation.transformer.mock.MockingApiTransformer]);
 * `false` (the default, used for `then`/`expect` blocks) requires a recognized wrapper, so an ordinary
 * boolean condition or side-effecting call is never misidentified as an interaction.
 */
internal fun IrStatement.asInteractionStatement(allowBareCall: Boolean = false): InteractionStatement? {
  val original = this
  val topLevelCall = (this as? IrExpression)?.unwrapImplicitCoercionToUnit() as? IrCall ?: return null

  val (afterResponse, response) = topLevelCall.unwrapResponse() ?: (topLevelCall to null)
  val (methodCall, cardinality) = afterResponse.unwrapCardinality() ?: (afterResponse to null)

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

internal fun IrCall.isCaptureMarkerCall(): Boolean = fqName() == IrIdentifiers.Spockk.CAPTURE_FQN

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
