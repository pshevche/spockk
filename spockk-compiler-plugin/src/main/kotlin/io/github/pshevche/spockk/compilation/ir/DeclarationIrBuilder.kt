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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Kotlin.LIST_FQN
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
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
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

internal fun DeclarationIrBuilder.irAnnotation(
  className: String,
  vararg args: IrExpression
): IrConstructorCall {
  val classSymbol = context.findRequiredClassSymbol(className)
  val constructorSymbol = classSymbol.constructors.first()
  return irCallConstructor(constructorSymbol, listOf()).apply {
    args.withIndex().forEach { arguments[it.index] = it.value }
  }
}

internal fun DeclarationIrBuilder.irStringArray(elements: List<String>) =
  irVararg(context.irBuiltIns.stringType, elements.map { irString(it) })

internal fun DeclarationIrBuilder.irType(typeName: String): IrType =
  context.findRequiredClassSymbol(typeName).defaultType

internal fun DeclarationIrBuilder.irEnumValue(
  value: String,
  enumClassName: String
): IrGetEnumValue {
  val enumClassSymbol = context.findRequiredClassSymbol(enumClassName)
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

private fun irVariable(name: Name, type: IrType, isVar: Boolean): IrVariable =
  IrVariableImpl(
    SYNTHETIC_OFFSET,
    SYNTHETIC_OFFSET,
    IrDeclarationOrigin.DEFINED,
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
  val getFunction = context.findRequiredClassSymbol(LIST_FQN.asString())
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
