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
import io.github.pshevche.spockk.compilation.shared.BehaviorStep
import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.shared.FeatureBody
import io.github.pshevche.spockk.compilation.shared.SpockkTransformationContext.FeatureContext
import io.github.pshevche.spockk.compilation.transformer.condition.ConditionRewriter
import io.github.pshevche.spockk.compilation.transformer.condition.ExceptionConditionRewriter
import io.github.pshevche.spockk.compilation.transformer.condition.containsImplicitAssertionHelperCall
import io.github.pshevche.spockk.compilation.transformer.condition.irStaticErrorCollectorDeclaration
import io.github.pshevche.spockk.compilation.transformer.condition.irValueRecorderDeclaration
import io.github.pshevche.spockk.compilation.transformer.condition.isConditionStatement
import io.github.pshevche.spockk.compilation.transformer.fixture.CleanupBlockRewriter
import io.github.pshevche.spockk.compilation.transformer.interaction.InteractionStatementsRewriter
import io.github.pshevche.spockk.compilation.transformer.interaction.irLeaveScopeStatement
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
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
    // Declared once per feature (matching Spock), shared across every condition-bearing block. A
    // literal-lambda verify/verifyAll/verifyEach call also counts, even though it isn't itself a
    // bare condition statement.
    val hasConditions = featureBody.behaviorBlocks.any { block ->
      block.statements.any { it.isConditionStatement(irBuiltIns) } ||
        block.statements.containsImplicitAssertionHelperCall()
    }
    val valueRecorderVar =
      if (hasConditions) irValueRecorderDeclaration(builder, feature).also { add(it) } else null
    val errorCollectorVar =
      if (hasConditions) irStaticErrorCollectorDeclaration(builder, feature).also { add(it) } else null

    addAll(featureBody.anonymousStatements)

    featureBody.behaviorSteps.forEach { step ->
      when (step) {
        is BehaviorStep.Plain -> addAll(rewritePlainBlock(builder, feature, step.block))

        is BehaviorStep.Condition -> {
          val conditionRewriter =
            ConditionRewriter(rewriterContext, builder, feature, step.block.ordinal, valueRecorderVar, errorCollectorVar)
          addAll(conditionRewriter.rewrite(step.block.statements))
        }

        is BehaviorStep.WhenThen -> addAll(rewriteWhenThen(builder, feature, step, valueRecorderVar, errorCollectorVar))
      }
    }
  }

  private fun rewritePlainBlock(builder: DeclarationIrBuilder, feature: IrFunction, block: FeatureBlock): List<IrStatement> =
    buildList {
      val specAccessor = feature.requiredThisParameter()
      add(rewriterContext.spockRuntime.irCallBlockEntered(builder, specAccessor, block.ordinal))
      addAll(block.statements)
      add(rewriterContext.spockRuntime.irCallBlockExited(builder, specAccessor, block.ordinal))
    }

  // Mirrors Spock's own SpecRewriter.moveInteractions: a THEN block's interactions are extracted
  // and moved ahead of its paired WHEN block's own statements (built once here, never twice - the
  // WHEN's rewrite needs the built addInteraction statements, the THEN's rewrite needs what's left).
  private fun rewriteWhenThen(
    builder: DeclarationIrBuilder,
    feature: IrFunction,
    step: BehaviorStep.WhenThen,
    valueRecorderVar: IrVariable?,
    errorCollectorVar: IrVariable?
  ): List<IrStatement> = buildList {
    val interactionSplit = if (step.thenHasInteractions) {
      InteractionStatementsRewriter(rewriterContext, feature).extractAndRewrite(step.thenBlock.statements)
    } else {
      null
    }

    addAll(
      WhenBlockRewriter(
        rewriterContext,
        feature,
        step.whenBlock,
        wrapExceptionHandling = step.thenHasExceptionCondition,
        hasInteractions = step.thenHasInteractions,
        addInteractionStatements = interactionSplit?.addInteractionStatements ?: emptyList(),
        thenBlockStatements = step.thenBlock.statements
      ).rewrite()
    )

    val rawThenStatements = if (interactionSplit != null) {
      listOf(irLeaveScopeStatement(feature, builder)) + interactionSplit.remainingStatements
    } else {
      step.thenBlock.statements
    }
    val thenStatements = ExceptionConditionRewriter(rewriterContext, feature).rewrite(rawThenStatements)
    val conditionRewriter =
      ConditionRewriter(rewriterContext, builder, feature, step.thenBlock.ordinal, valueRecorderVar, errorCollectorVar)
    addAll(conditionRewriter.rewrite(thenStatements))
  }
}
