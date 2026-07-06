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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.BLOCK_KIND_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.BLOCK_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.FEATURE_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.ir.irEnumValue
import io.github.pshevche.spockk.compilation.ir.irStringArray
import io.github.pshevche.spockk.compilation.ir.irType
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.shared.FeatureBlockLabel
import io.github.pshevche.spockk.compilation.shared.FeatureBody
import io.github.pshevche.spockk.compilation.shared.SpockkTransformationContext.FeatureContext
import io.github.pshevche.spockk.compilation.transformer.condition.ConditionRewriter
import io.github.pshevche.spockk.compilation.transformer.fixture.CleanupBlockRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.name.Name

internal class FeatureRewriter(override val rewriterContext: SpockkIrRewriterContext) : SpockkIrRewriter {

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
        context.body.allBlocks
      )
  }

  private fun featureMetadataAnnotation(
    feature: IrFunction,
    ordinal: Int,
    name: String,
    line: Int,
    parameterNames: List<String>,
    blocks: List<FeatureBlock>
  ): IrAnnotation =
    with(irBuilder(feature.symbol)) {
      irAnnotation(
        FEATURE_METADATA_FQN,
        irInt(ordinal),
        irString(name),
        irInt(line),
        irStringArray(parameterNames),
        blockMetadataArray(this, blocks)
      )
    }

  private fun blockMetadataArray(
    builder: DeclarationIrBuilder,
    blocks: List<FeatureBlock>
  ): IrExpression =
    with(builder) {
      irVararg(
        irType(BLOCK_METADATA_FQN),
        blocks.map { block ->
          irAnnotation(
            BLOCK_METADATA_FQN,
            irEnumValue(block.element.label.blockKind!!, BLOCK_KIND_FQN),
            irStringArray(block.descriptions)
          )
        }
      )
    }

  private fun renameFeature(feature: IrFunction, context: FeatureContext) {
    feature.name = Name.identifier(InternalIdentifiers.getFeatureName(context))
  }

  private fun rewriteFeatureStatements(feature: IrFunction, context: FeatureContext) {
    val builder = irBuilder(feature.symbol)
    val featureBody = context.body

    val featureStatements = feature.mutableStatements()
    featureStatements?.clear()

    val behaviorStatements = rewriteBehaviorStatements(builder, feature, featureBody)
    val newFeatureStatements = featureBody.cleanupBlock?.let {
      CleanupBlockRewriter(rewriterContext, feature, it, behaviorStatements).rewrite()
    } ?: behaviorStatements

    feature.mutableStatements()?.addAll(newFeatureStatements)
  }

  private fun rewriteBehaviorStatements(
    builder: DeclarationIrBuilder,
    feature: IrFunction,
    featureBody: FeatureBody
  ): List<IrStatement> = buildList {
    addAll(featureBody.anonymousStatements)
    featureBody.behaviorBlocks.forEach {
      if (it.element.label == FeatureBlockLabel.EXPECT || it.element.label == FeatureBlockLabel.THEN) {
        val conditionRewriter = ConditionRewriter(rewriterContext, builder, feature, it.ordinal)
        addAll(conditionRewriter.rewrite(it.statements))
      } else {
        add(
          rewriterContext.spockRuntime.irCallBlockEntered(
            builder,
            feature.requiredThisParameter(),
            it.ordinal
          )
        )
        addAll(it.statements)
        add(
          rewriterContext.spockRuntime.irCallBlockExited(
            builder,
            feature.requiredThisParameter(),
            it.ordinal
          )
        )
      }
    }
  }
}
