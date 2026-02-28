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

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

internal fun IrField.makePrivate() {
  visibility = DescriptorVisibilities.PRIVATE
}

internal fun IrField.makeMutable() {
  isFinal = false
}

// Makes the field type nullable, updates accessor types, and sets `= null` as the default.
// Also updates any GET_FIELD/GET_VAR type references inside the property's getter/setter bodies.
internal fun IrField.makeNullableWithNullDefault(builder: DeclarationIrBuilder) {
  val nullableType = type.makeNullable()
  type = nullableType

  val property = correspondingPropertySymbol?.owner

  val getter = property?.getter
  if (getter != null) {
    getter.returnType = nullableType
    getter.body?.transform(
      object : IrElementTransformerVoid() {
        override fun visitGetField(expression: IrGetField): IrExpression {
          if (expression.symbol == this@makeNullableWithNullDefault.symbol) {
            expression.type = nullableType
          }
          return super.visitGetField(expression)
        }
      },
      null
    )
  }

  val setter = property?.setter
  if (setter != null) {
    val valueParam = setter.parameters.firstOrNull { it.name.asString() != "<this>" }
    if (valueParam != null) {
      valueParam.type = nullableType
      setter.body?.transform(
        object : IrElementTransformerVoid() {
          override fun visitGetValue(expression: IrGetValue): IrExpression {
            if (expression.symbol == valueParam.symbol) {
              expression.type = nullableType
            }
            return super.visitGetValue(expression)
          }
        },
        null
      )
    }
  }

  initializer = builder.irExprBody(builder.irNull())
}
