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

package io.github.pshevche.spockk.compilation.transformer.parametrization

import io.github.pshevche.spockk.compilation.common.BaseSpockkIrElementTransformer
import io.github.pshevche.spockk.compilation.ir.asFeatureVariable
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class PreviousFeatureVariablesAccessTracker(
  private val feature: IrFunction,
  private val referenceableVariables: Set<String>
) : BaseSpockkIrElementTransformer() {

  private val referencedFeatureVariables: MutableSet<IrValueParameter> = mutableSetOf()

  fun check(expression: IrExpression): Set<IrValueParameter> {
    expression.transform(this, null)
    return referencedFeatureVariables.toSet()
  }

  override fun visitGetValue(expression: IrGetValue): IrExpression = super.visitGetValue(expression).also {
    expression.asFeatureVariable(feature)
      ?.takeIf { referenceableVariables.contains(it.name.asString()) }
      ?.let { referencedFeatureVariables.add(it) }
  }
}
