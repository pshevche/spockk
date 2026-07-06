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

package io.github.pshevche.spockk.compilation.transformer.ir

import io.github.pshevche.spockk.compilation.ir.unwrapImplicitCoercionToUnit
import io.github.pshevche.spockk.compilation.shared.BaseSpockkIrElementTransformer
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression

internal class ConditionValueRecordingTransformer(
  private val builder: DeclarationIrBuilder,
  private val irValueRecorder: IrValueRecorder
) : BaseSpockkIrElementTransformer() {

  private var recordCount = 0

  fun transform(expr: IrExpression) = expr.unwrapImplicitCoercionToUnit().transform(this, null)

  override fun visitConst(expression: IrConst) = record(expression)

  private fun record(expression: IrExpression): IrExpression = irValueRecorder.irRecord(builder, recordCount++, expression)
}
