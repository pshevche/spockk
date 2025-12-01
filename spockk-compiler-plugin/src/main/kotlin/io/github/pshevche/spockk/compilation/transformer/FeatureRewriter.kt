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

import io.github.pshevche.spockk.compilation.common.FeatureBlockStatements
import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FeatureContext
import io.github.pshevche.spockk.compilation.ir.ContextAwareIrFactory
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.name.Name

internal class FeatureRewriter(private val irFactory: ContextAwareIrFactory) {

  companion object {
    private const val FEATURE_METADATA_FQN = "org.spockframework.runtime.model.FeatureMetadata"
    private const val BLOCK_METADATA_FQN = "org.spockframework.runtime.model.BlockMetadata"
    private const val BLOCK_KIND_FQN = "org.spockframework.runtime.model.BlockKind"
  }

  fun rewrite(feature: IrFunction, context: FeatureContext) {
    annotateFeature(feature, context)
    renameFeature(feature, context)
    rewriteFeatureStatements(feature, context)
  }

  private fun annotateFeature(feature: IrFunction, context: FeatureContext) {
    feature.annotations +=
      featureMetadataAnnotation(
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

  private fun featureMetadataAnnotation(
    ordinal: Int,
    name: String,
    line: Int,
    parameterNames: List<String>,
    blocks: List<FeatureBlockStatements>
  ): IrConstructorCall =
    irFactory.constructorCall(
      FEATURE_METADATA_FQN,
      irFactory.const(ordinal),
      irFactory.const(name),
      irFactory.const(line),
      irFactory.stringArray(parameterNames),
      blockMetadataArray(blocks.filter { it.label.blockKind != null })
    )

  private fun blockMetadataArray(blocks: List<FeatureBlockStatements>): IrExpression =
    irFactory.array(
      BLOCK_METADATA_FQN,
      blocks.map { block ->
        irFactory.constructorCall(
          BLOCK_METADATA_FQN,
          irFactory.enumValue(block.label.blockKind!!, BLOCK_KIND_FQN),
          irFactory.stringArray(listOf(block.description))
        )
      }
    )
}
