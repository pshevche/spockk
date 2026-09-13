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

package io.github.pshevche.spockk.compilation.transformer.interaction

import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import io.github.pshevche.spockk.compilation.transformer.ir.getSpecificationContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.parentAsClass

/**
 * Emits statements against the `MockController` of the spec [feature] belongs to: registering an
 * interaction, and opening and closing the scope a `when`/`then` pair verifies its interactions in.
 *
 * Reaching that controller means going through the spec's `SpecificationContext` on every call, so
 * every caller that needs one of these three statements gets it here rather than repeating the
 * lookup.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class FeatureMockController(
  override val rewriterContext: SpockkIrRewriterContext,
  private val feature: IrFunction
) : SpockkIrRewriter {

  private val builder = irBuilder(feature.symbol)
  private val specAccessor = feature.requiredThisParameter()
  private val specificationContext = feature.parentAsClass.getSpecificationContext(rewriterContext)

  fun irEnterScope(): IrStatement = rewriterContext.mockController.irEnterScope(builder, irGetController())

  fun irLeaveScope(): IrStatement = rewriterContext.mockController.irLeaveScope(builder, irGetController())

  fun irAddInteraction(interaction: IrExpression): IrStatement =
    rewriterContext.mockController.irAddInteraction(builder, irGetController(), interaction)

  // A fresh expression per statement: IR nodes are trees, not shared values.
  private fun irGetController(): IrExpression = specificationContext.irGetMockController(builder, specAccessor)
}
