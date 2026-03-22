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

package io.github.pshevche.spockk.compilation.transformer.fields

import io.github.pshevche.spockk.compilation.shared.BaseSpockkIrElementTransformer
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

/**
 * Replaces all field-access expressions in the spec's declarations with calls to the
 * generated accessor methods. Skips generated functions (init methods, DEFAULT getters/setters)
 * to avoid transforming code that must remain as-is.
 *
 * Three kinds of replacements:
 * - [IrGetField] → call to generated getter (for val/shared fields)
 * - [IrSetField] → call to generated setter (for shared fields)
 * - [IrCall] to original getter symbol → call to replacement getter
 * - [IrCall] to original setter symbol → call to replacement setter
 * - [IrCall] to a getter that became nullable → update CALL type in-place (for var fields)
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class FieldReferenceReplacer(
  override val generatorContext: IrGeneratorContext,
  private val spec: IrClass,
  private val state: FieldRewriteState
) : BaseSpockkIrElementTransformer(),
  SpockkIrRewriter {

  fun replace() {
    if (state.isEmpty()) return

    spec.declarations.forEach {
      if (it is IrFunction || it is IrProperty) {
        it.accept(this, null)
      }
    }
  }

  // Skip functions whose bodies must not be transformed (e.g. DEFAULT_PROPERTY_ACCESSORs
  // for renamed val fields, whose GET_FIELD body should remain as-is)
  override fun visitSimpleFunction(declaration: IrSimpleFunction) =
    if (state.isGenerated(declaration.symbol)) {
      declaration
    } else {
      super.visitSimpleFunction(declaration)
    }

  override fun visitGetField(expression: IrGetField): IrExpression {
    val getter = state.fieldGetter(expression.symbol) ?: return super.visitGetField(expression)
    return with(irBuilder(getter.symbol)) {
      irCall(getter.symbol, getter.returnType).apply {
        dispatchReceiver = expression.receiver
      }
    }
  }

  override fun visitSetField(expression: IrSetField): IrExpression {
    val setter = state.fieldSetter(expression.symbol) ?: return super.visitSetField(expression)
    return with(irBuilder(setter.symbol)) {
      irCall(setter.symbol, irBuiltIns.unitType).apply {
        dispatchReceiver = expression.receiver
        arguments[1] = expression.value
      }
    }
  }

  override fun visitCall(expression: IrCall): IrExpression {
    // For var fields that became nullable: update the CALL type in-place
    val updatedType = state.getterTypeUpdate(expression.symbol)
    if (updatedType != null) {
      expression.type = updatedType
      return super.visitCall(expression)
    }

    val getterReplacement = state.getterReplacement(expression.symbol)
    if (getterReplacement != null) {
      return with(irBuilder(getterReplacement.symbol)) {
        irCall(getterReplacement.symbol, getterReplacement.returnType).apply {
          dispatchReceiver = expression.dispatchReceiver
        }
      }
    }

    val setterReplacement = state.setterReplacement(expression.symbol)
    if (setterReplacement != null) {
      return with(irBuilder(setterReplacement.symbol)) {
        irCall(setterReplacement.symbol, irBuiltIns.unitType).apply {
          dispatchReceiver = expression.dispatchReceiver
          arguments[1] = expression.arguments[1]
        }
      }
    }

    return super.visitCall(expression)
  }
}
