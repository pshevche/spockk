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

package io.github.pshevche.spockk.compilation.ir

import io.github.pshevche.spockk.compilation.shared.FeatureBlockLabelIrElement
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrContainerExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.IMPLICIT_COERCION_TO_UNIT
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName

internal fun IrStatement.asIrBlockLabel(file: IrFile): FeatureBlockLabelIrElement? =
  when (this) {
    is IrTypeOperatorCall -> (this.argument as? IrGetObjectValue)?.asIrBlockLabel(file)
    is IrCall -> asIrBlockLabel(file)
    else -> null
  }

private fun IrGetObjectValue.asIrBlockLabel(file: IrFile): FeatureBlockLabelIrElement? =
  FeatureBlockLabelIrElement.from(file, this)

internal fun IrGetObjectValue.requiredFqn() = requireNotNull(symbol.owner.fqNameWhenAvailable) {
  "Missing FqName for an object reference $this"
}

private fun IrCall.asIrBlockLabel(file: IrFile): FeatureBlockLabelIrElement? =
  FeatureBlockLabelIrElement.from(file, this)

internal fun IrCall.fqName(): FqName? = symbol.owner.fqNameWhenAvailable

internal fun IrCall.requiredFqn() = requireNotNull(fqName()!!) {
  "Missing FqName for a function call $this"
}

internal fun IrStatement.isFromCall() = (this as? IrCall)
  ?.fqName()
  ?.asString()
  ?.matches(IrIdentifiers.Spockk.FROM_FQN_REGEX) ?: false

internal fun IrCall.isSingleVariableInitializer() = fqName() == IrIdentifiers.Spockk.SINGLE_VARIABLE_INIT_FQN

internal fun IrCall.isMultiVariableInitializer() = fqName() == IrIdentifiers.Spockk.MULTI_VARIABLE_INIT_FQN

internal fun IrGetValue.asFeatureVariable(feature: IrFunction): IrValueParameter? {
  val paramSymbol = symbol as? IrValueParameterSymbol
  return feature.assignableParameters().find { it.symbol == paramSymbol }
}

// when moving property access to a new method (e.g., from feature's where block to the data provider method)
// we need to rebind the dispatch receiver (i.e., '<this>') to the new target, as it includes the method reference
internal fun IrExpression.rebindDispatchReceiverReferences(
  targetParam: IrValueParameter,
  irBuilder: DeclarationIrBuilder
): IrExpression {
  val rebinder = object : IrElementTransformerVoid() {
    override fun visitGetValue(expression: IrGetValue): IrExpression {
      val paramOwner = expression.symbol.owner
      if (paramOwner is IrValueParameter &&
        paramOwner.name.asString() == "<this>" &&
        paramOwner.symbol != targetParam.symbol
      ) {
        return irBuilder.irGet(targetParam)
      }
      return super.visitGetValue(expression)
    }
  }
  return transform(rebinder, null)
}

internal fun IrExpression.unwrapImplicitCoercionToUnit(): IrExpression = (this as? IrTypeOperatorCall)?.let {
  if (it.operator == IMPLICIT_COERCION_TO_UNIT) it.argument else it
} ?: this

internal fun IrStatement.isAssertCall(): Boolean {
  val owner = (this as? IrCall)?.symbol?.owner ?: return false
  return owner.fqNameWhenAvailable == IrIdentifiers.Kotlin.ASSERT_FQN
}

private val EXCEPTION_CONDITION_FQNS = setOf(
  IrIdentifiers.Spock.THROWN_FQN,
  IrIdentifiers.Spock.NOT_THROWN_FQN,
  IrIdentifiers.Spock.NO_EXCEPTION_THROWN_FQN
)

// thrown/notThrown/noExceptionThrown are inherited from spock.lang.Specification, so a call site
// resolves to a [fake_override] declared in the user's spec subclass, whose own fqNameWhenAvailable
// points at the subclass, not Specification - unwrap to the real declaration before comparing.
private tailrec fun IrSimpleFunction.originalDeclaration(): IrSimpleFunction {
  if (!isFakeOverride) return this
  val overridden = overriddenSymbols.firstOrNull()?.owner ?: return this
  return overridden.originalDeclaration()
}

// A bare non-Unit-typed statement gets an implicit-coercion-to-Unit wrapper; a val/var initializer
// assigning thrown<T>()'s flexibly-nullable Java generic return to a non-null type gets an
// implicit-notnull wrapper instead. Unwrap through either before recognizing the call.
private tailrec fun IrExpression.unwrapForExceptionConditionDetection(): IrExpression {
  val operatorCall = this as? IrTypeOperatorCall ?: return this
  return when (operatorCall.operator) {
    IMPLICIT_COERCION_TO_UNIT, IrTypeOperator.IMPLICIT_NOTNULL -> operatorCall.argument.unwrapForExceptionConditionDetection()
    else -> this
  }
}

internal fun IrStatement.asExceptionConditionCall(): IrCall? {
  val call = (this as? IrExpression)?.unwrapForExceptionConditionDetection() as? IrCall ?: return null
  return call.takeIf { it.symbol.owner.originalDeclaration().fqNameWhenAvailable in EXCEPTION_CONDITION_FQNS }
}

internal fun IrCall.isThrownCall(): Boolean =
  symbol.owner.originalDeclaration().fqNameWhenAvailable == IrIdentifiers.Spock.THROWN_FQN

/** The mutable statement lists directly nested one level inside a try/catch/finally or block. */
internal fun IrStatement.nestedStatementLists(): List<MutableList<IrStatement>> = when (this) {
  is IrTry -> buildList {
    (tryResult as? IrContainerExpression)?.let { add(it.statements) }
    catches.forEach { (it.result as? IrContainerExpression)?.let { result -> add(result.statements) } }
    (finallyExpression as? IrContainerExpression)?.let { add(it.statements) }
  }

  is IrContainerExpression -> listOf(statements)

  else -> emptyList()
}
