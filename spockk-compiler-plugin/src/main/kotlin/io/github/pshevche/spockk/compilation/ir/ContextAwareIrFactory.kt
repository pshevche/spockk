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

package io.github.pshevche.spockk.compilation.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.fir.backend.utils.defaultTypeWithoutArguments
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.toIrConst

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class ContextAwareIrFactory(private val pluginContext: IrPluginContext) : IrFactory(pluginContext.irFactory.stageController) {
  private val irBuiltIns = pluginContext.irBuiltIns

  internal fun constructorCall(className: String, vararg args: IrExpression): IrConstructorCall {
    val classSymbol = pluginContext.referenceClass(className)
    val constructorSymbol = classSymbol.constructors.first()
    val classType = classSymbol.defaultType
    return IrConstructorCallImpl.fromSymbolOwner(classType, constructorSymbol).apply {
      args.withIndex().forEach { arguments[it.index] = it.value }
    }
  }

  internal fun enumValue(value: String, enumClassName: String): IrGetEnumValue {
    val enumClassSymbol = pluginContext.referenceClass(enumClassName)
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

  internal fun stringArray(elements: List<String>) =
    array(irBuiltIns.stringType, elements.map { const(it) })

  internal fun array(elementClassName: String, elements: List<IrVarargElement>) =
    array(pluginContext.referenceClass(elementClassName).defaultType, elements)

  internal fun array(elementType: IrType, elements: List<IrVarargElement>): IrVararg =
    IrVarargImpl(
      SYNTHETIC_OFFSET,
      SYNTHETIC_OFFSET,
      irBuiltIns.arrayClass.typeWith(elementType),
      elementType,
      elements
    )

  internal fun const(value: Any): IrConst =
    value.toIrConst(
      pluginContext.referenceClass(value::class.qualifiedName!!).defaultTypeWithoutArguments
    )
}
