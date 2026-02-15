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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.common.BaseSpockkIrElementTransformer
import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class SpockkIrTransformer(
  generationContext: IrGeneratorContext,
  private val context: SpockkTransformationContext
) : BaseSpockkIrElementTransformer() {
  private val specRewriter = SpecRewriter(generationContext)
  private val featureRewriter = FeatureRewriter(generationContext)

  override fun visitClassNew(declaration: IrClass): IrStatement =
    declaration.transformPostfix {
      context.specContext(this)?.let { specRewriter.rewrite(this, it) }
    }

  override fun visitFunctionNew(declaration: IrFunction): IrStatement =
    declaration.transformPostfix {
      // extension functions do not have a reference to a class
      maybeCurrentIrClass?.let {
        context.featureContext(it, this)?.let { ctx -> featureRewriter.rewrite(this, ctx) }
      }
    }
}
