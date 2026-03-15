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

import io.github.pshevche.spockk.compilation.common.FeatureBlockLabel
import io.github.pshevche.spockk.compilation.common.FeatureBlockStatements
import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FeatureContext
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.BLOCK_KIND_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.BLOCK_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.FEATURE_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.ir.irEnumValue
import io.github.pshevche.spockk.compilation.ir.irStringArray
import io.github.pshevche.spockk.compilation.ir.irType
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.transformer.fixture.CleanupBlockRewriter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.name.Name

internal class FeatureRewriter(override val context: IrGeneratorContext) : SpockkIrRewriter {

  fun rewrite(feature: IrFunction, context: FeatureContext) {
    annotateFeature(feature, context)
    renameFeature(feature, context)
    rewriteFeatureStatements(feature, context)
  }

  private fun annotateFeature(feature: IrFunction, context: FeatureContext) {
    feature.annotations +=
      featureMetadataAnnotation(
        feature,
        context.ordinal,
        context.name,
        context.line,
        context.parameterNames,
        context.blocks
      )
  }

  private fun renameFeature(feature: IrFunction, context: FeatureContext) {
    feature.name = Name.identifier(InternalIdentifiers.getFeatureName(context))
  }

  private fun rewriteFeatureStatements(feature: IrFunction, context: FeatureContext) {
    feature.mutableStatements()?.clear()
    feature.mutableStatements()?.addAll(
      CleanupBlockRewriter(this.context, feature, context).rewrite()
    )
  }

  private fun featureMetadataAnnotation(
    feature: IrFunction,
    ordinal: Int,
    name: String,
    line: Int,
    parameterNames: List<String>,
    blocks: List<FeatureBlockStatements>
  ): IrConstructorCall =
    with(irBuilder(feature.symbol)) {
      irAnnotation(
        FEATURE_METADATA_FQN,
        irInt(ordinal),
        irString(name),
        irInt(line),
        irStringArray(parameterNames),
        blockMetadataArray(this, mergeBlocks(blocks))
      )
    }

  private fun blockMetadataArray(
    builder: DeclarationIrBuilder,
    blocks: List<MergedBlock>
  ): IrExpression =
    with(builder) {
      irVararg(
        irType(BLOCK_METADATA_FQN),
        blocks.map { block ->
          irAnnotation(
            BLOCK_METADATA_FQN,
            irEnumValue(block.blockKind, BLOCK_KIND_FQN),
            irStringArray(block.descriptions)
          )
        }
      )
    }

  private data class MergedBlock(
    val blockKind: String,
    val descriptions: List<String>
  )

  private fun mergeBlocks(blocks: List<FeatureBlockStatements>): List<MergedBlock> {
    val result = mutableListOf<MergedBlock>()
    for (block in blocks) {
      val label = block.element.label
      if (label == FeatureBlockLabel.AND && result.isNotEmpty()) {
        val last = result.last()
        result[result.lastIndex] = last.copy(
          descriptions = last.descriptions + block.element.description
        )
      } else if (label.blockKind != null) {
        result.add(
          MergedBlock(
            blockKind = label.blockKind!!,
            descriptions = listOf(block.element.description)
          )
        )
      }
    }
    return result
  }
}
