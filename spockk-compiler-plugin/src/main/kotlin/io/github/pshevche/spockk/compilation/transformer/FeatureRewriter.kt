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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FeatureContext
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.name.Name

internal class FeatureRewriter(private val irFactory: SpockkIrFactory) {
  fun rewrite(feature: IrFunction, context: FeatureContext) {
    annotateFeature(feature, context)
    renameFeature(feature, context)
    rewriteFeatureStatements(feature, context)
  }

  private fun annotateFeature(feature: IrFunction, context: FeatureContext) {
    feature.annotations +=
      irFactory.featureMetadataAnnotation(
        context.ordinal,
        context.name,
        context.line,
        context.parameterNames,
        context.blocks
      )
  }

  private fun renameFeature(feature: IrFunction, context: FeatureContext) {
    // function names in Kotlin cannot start with $
    feature.name = Name.identifier("spock_feature_${context.specDepth}_${context.ordinal}")
  }

  private fun rewriteFeatureStatements(feature: IrFunction, context: FeatureContext) {
    feature.mutableStatements()?.clear()
    feature.mutableStatements()?.addAll(context.blocks.flatMap { it.statements })
  }
}
