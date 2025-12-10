/*
 * Copyright 2025 the original author or authors.
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

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors

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
