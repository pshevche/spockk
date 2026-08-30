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

@file:OptIn(InternalSymbolFinderAPI::class, UnsafeDuringIrConstructionAPI::class)

package io.github.pshevche.spockk.compilation.ir

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Kotlin.ADD_SUPPRESSED_CALLABLE_ID
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Kotlin.LIST_FQN
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irAnnotation
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTry
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrThrowImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.buildTypeProjection
import org.jetbrains.kotlin.ir.types.impl.toBuilder
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

internal fun DeclarationIrBuilder.irAnnotation(
  className: FqName,
  vararg args: IrExpression
): IrAnnotation {
  val classSymbol = context.findRequiredClassSymbol(className)
  val constructorSymbol = classSymbol.constructors.first()
  return irAnnotation(constructorSymbol).apply {
    args.withIndex().forEach { arguments[it.index] = it.value }
  }
}

internal fun DeclarationIrBuilder.irStringArray(elements: List<String>) =
  irVararg(context.irBuiltIns.stringType, elements.map { irString(it) })

internal fun DeclarationIrBuilder.irType(typeFqn: FqName): IrType =
  context.findRequiredClassSymbol(typeFqn).defaultType

internal fun DeclarationIrBuilder.irEnumValue(
  value: String,
  enumClassFqn: FqName
): IrGetEnumValue {
  val enumClassSymbol = context.findRequiredClassSymbol(enumClassFqn)
  val enumEntry =
    enumClassSymbol.owner.declarations.filterIsInstance<IrEnumEntry>().first {
      it.name.asString() == value
    }
  return IrGetEnumValueImpl(
    SYNTHETIC_OFFSET,
    SYNTHETIC_OFFSET,
    enumClassSymbol.defaultType,
    enumEntry.symbol
  )
}

internal fun DeclarationIrBuilder.irListOf(
  elementType: IrType,
  elements: List<IrExpression>
): IrFunctionAccessExpression {
  val listOfSymbol =
    context.findFunctionSymbols(IrIdentifiers.Kotlin.LIST_OF_CALLABLE_ID).first {
      val param = it.owner.parameters.single()
      if (elements.size > 1 || elements.single() is IrVararg) param.isVararg else !param.isVararg
    }
  return irCall(listOfSymbol, context.irBuiltIns.listClass.typeWith(elementType)).apply {
    typeArguments[0] = elementType
    arguments[0] = if (elements.size > 1) irVararg(irOutType(elementType), elements) else elements.first()
  }
}

internal fun DeclarationIrBuilder.irArrayOf(
  elementType: IrType,
  elements: List<IrExpression>
): IrFunctionAccessExpression {
  val arrayOfSymbol = context.findUniqueFunctionSymbol(IrIdentifiers.Kotlin.ARRAY_OF_CALLABLE_ID)
  return irCall(arrayOfSymbol, context.irBuiltIns.arrayClass.typeWith(elementType)).apply {
    typeArguments[0] = elementType
    arguments[0] = irVararg(irOutType(elementType), elements)
  }
}

private fun irOutType(type: IrType): IrTypeProjection =
  (type as? IrSimpleType)?.toBuilder()?.buildTypeProjection(Variance.OUT_VARIANCE) ?: type

private fun IrBuilder.irVararg(elementType: IrTypeProjection, values: List<IrExpression>) =
  IrVarargImpl(
    startOffset,
    endOffset,
    context.irBuiltIns.arrayClass.typeWithArguments(listOf(elementType)),
    elementType.type,
    values
  )

internal fun irVar(name: Name, type: IrType): IrVariable = irVariable(name, type, true)

internal fun irVal(name: Name, type: IrType): IrVariable = irVariable(name, type, false)

internal fun irCatchParameter(name: Name, type: IrType): IrVariable =
  IrVariableImpl(
    SYNTHETIC_OFFSET,
    SYNTHETIC_OFFSET,
    IrDeclarationOriginWrapper.CATCH_PARAMETER,
    IrVariableSymbolImpl(),
    name,
    type,
    isVar = false,
    isConst = false,
    isLateinit = false
  )

private fun irVariable(name: Name, type: IrType, isVar: Boolean): IrVariable =
  IrVariableImpl(
    SYNTHETIC_OFFSET,
    SYNTHETIC_OFFSET,
    IrDeclarationOriginWrapper.DEFINED,
    IrVariableSymbolImpl(),
    name,
    type,
    isVar = isVar,
    isConst = false,
    isLateinit = false
  )

fun IrBuilder.irListGet(
  list: IrExpression,
  idx: Int
): IrExpression {
  val getFunction = context.findRequiredClassSymbol(LIST_FQN)
    .owner
    .functions
    .single { it.name.asString() == "get" }

  val elementType = (list.type as IrSimpleType).arguments.single().typeOrFail
  return irCall(getFunction.symbol, elementType).apply {
    dispatchReceiver = list
    // arguments[0] = this is set via the dispatchReceiver
    arguments[1] = idx.toIrConst(context.irBuiltIns.intType)
  }
}

fun IrBuilder.irGetThis(thisValueParam: IrValueParameter): IrGetValue = IrGetValueImpl(
  SYNTHETIC_OFFSET,
  SYNTHETIC_OFFSET,
  thisValueParam.type,
  thisValueParam.symbol,
  IrStatementOrigin.IMPLICIT_ARGUMENT
)

internal fun IrBuilderWithScope.irAddSuppressed(
  receiver: IrExpression,
  exception: IrExpression
): IrExpression {
  val addSuppressedFun = context.findFunctionSymbols(ADD_SUPPRESSED_CALLABLE_ID).single()
  return irBlock {
    +irCall(addSuppressedFun, context.irBuiltIns.unitType).apply {
      arguments[0] = irImplicitCastTo(receiver, context.irBuiltIns.throwableType)
      arguments[1] = exception
    }
  }
}

private fun IrBuilderWithScope.irImplicitCastTo(value: IrExpression, type: IrType): IrExpression =
  IrTypeOperatorCallImpl(
    startOffset,
    endOffset,
    type,
    IrTypeOperator.IMPLICIT_CAST,
    type,
    value
  )

internal fun IrBuilderWithScope.irCastTo(value: IrExpression, type: IrType): IrExpression =
  IrTypeOperatorCallImpl(
    startOffset,
    endOffset,
    type,
    IrTypeOperator.CAST,
    type,
    value
  )

internal fun IrBuilderWithScope.irImplicitNotNull(value: IrExpression, type: IrType): IrExpression =
  IrTypeOperatorCallImpl(
    startOffset,
    endOffset,
    type,
    IrTypeOperator.IMPLICIT_NOTNULL,
    type,
    value
  )

internal fun IrBuilder.irThrow(value: IrExpression): IrExpression = IrThrowImpl(
  startOffset,
  endOffset,
  context.irBuiltIns.nothingType,
  value
)

internal fun DeclarationIrBuilder.irTry(
  tryExpressions: List<IrStatement>,
  catchExpressions: List<IrCatch>,
  finallyExpressions: List<IrStatement>
): IrTry = irTry(
  type = context.irBuiltIns.unitType,
  tryResult = irBlock { +tryExpressions },
  catches = catchExpressions,
  finallyExpression = irBlock { +finallyExpressions }
)

/**
 * Same as [irTry], but hoists any [IrVariable] declared in [tryExpressions] - at any nesting depth
 * reachable through an already-built inner try/catch/finally, e.g. one an earlier, narrower call to
 * this same function produced - that's actually referenced from [catchExpressions],
 * [finallyExpressions], or [extraReaders] (statements the caller will place after the returned list)
 * out to a declaration returned before the try, with a `SET_VAR` left in its place. A variable
 * declared inside a try's body isn't visible in its `catch`/`finally` handlers or in statements after
 * the try - the same reason a `val` assigned inside a try must be declared before it in ordinary
 * Kotlin source. Only variables actually read outside their own declaring block are hoisted. Reuses
 * the original [IrVariable] (and so its symbol) rather than declaring a new one, so every existing
 * reference elsewhere in the tree keeps resolving correctly without needing to be rewritten.
 */
internal fun DeclarationIrBuilder.irTryHoistingVariables(
  tryExpressions: List<IrStatement>,
  catchExpressions: List<IrCatch>,
  finallyExpressions: List<IrStatement>,
  extraReaders: List<IrElement> = emptyList()
): List<IrStatement> {
  val readers: List<IrElement> = catchExpressions.map { it.result } + finallyExpressions + extraReaders
  val hoisted = mutableListOf<IrVariable>()
  val rewrittenTryExpressions = hoistReferencedVariables(tryExpressions, readers, hoisted)
  return buildList {
    addAll(hoisted)
    add(irTry(rewrittenTryExpressions, catchExpressions, finallyExpressions))
  }
}

private fun DeclarationIrBuilder.hoistReferencedVariables(
  statements: List<IrStatement>,
  readers: List<IrElement>,
  hoisted: MutableList<IrVariable>
): List<IrStatement> = statements.mapNotNull { statement ->
  statement.nestedStatementLists().forEach { nested ->
    val rewrittenNested = hoistReferencedVariables(nested, readers, hoisted)
    nested.clear()
    nested.addAll(rewrittenNested)
  }
  if (statement !is IrVariable || readers.none { it.referencesValue(statement.symbol) }) return@mapNotNull statement
  hoisted += statement
  val initializer = statement.initializer ?: return@mapNotNull null
  statement.initializer = null
  irSet(statement, initializer)
}

private fun IrElement.referencesValue(symbol: IrValueSymbol): Boolean {
  var found = false
  acceptVoid(object : IrVisitorVoid() {
    override fun visitElement(element: IrElement) {
      if (!found) element.acceptChildrenVoid(this)
    }

    override fun visitGetValue(expression: IrGetValue) {
      if (expression.symbol == symbol) found = true else visitElement(expression)
    }

    override fun visitSetValue(expression: IrSetValue) {
      if (expression.symbol == symbol) found = true else visitElement(expression)
    }
  })
  return found
}
