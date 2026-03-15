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

import io.github.pshevche.spockk.compilation.common.FeatureBlockLabel
import io.github.pshevche.spockk.compilation.common.FeatureBlockStatements
import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FeatureContext
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.BLOCK_KIND_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.BLOCK_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.FEATURE_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.MOCK_CONTROLLER_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPECIFICATION_CONTEXT_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPEC_INTERNALS_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPOCK_RUNTIME_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.ir.irEnumValue
import io.github.pshevche.spockk.compilation.ir.irImplicitNotNull
import io.github.pshevche.spockk.compilation.ir.irStringArray
import io.github.pshevche.spockk.compilation.ir.irType
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.transformer.fixture.CleanupBlockRewriter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
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
    val hasCleanup = context.cleanupBlocks.isNotEmpty()
    val builder = irBuilder(feature.symbol)
    val thisParam = feature.parameters.firstOrNull { it.name.asString() == "<this>" }
      ?: return rewriteFeatureStatementsLegacy(feature, context)

    val statements = if (hasCleanup) {
      val mergedFeatureBlocks = mergeBlocksWithStatements(context.featureBlocks)
      val featureStatementsWithCalls = buildBlockStatementsFromMerged(builder, thisParam, mergedFeatureBlocks)
      val cleanupStatements = context.cleanupBlocks.flatMap { it.statements }
      val allMergedBlocks = mergeBlocksWithStatements(context.blocks)
      val cleanupBlockIndex = allMergedBlocks.indexOfFirst { it.blockKind == "CLEANUP" }
      val blockCallBuilder = CleanupBlockRewriter.BlockCallBuilder { b, entered, idx ->
        if (entered) irCallBlockEntered(b, thisParam, idx) else irCallBlockExited(b, thisParam, idx)
      }
      CleanupBlockRewriter(
        this.context,
        feature,
        featureStatementsWithCalls,
        cleanupStatements,
        cleanupBlockIndex,
        blockCallBuilder
      ).rewrite()
    } else {
      // Non-cleanup path: wrap each merged block with entry/exit calls
      buildBlockStatementsWithCalls(builder, feature, context, thisParam)
    }

    feature.mutableStatements()?.clear()
    feature.mutableStatements()?.addAll(statements)
    feature.mutableStatements()?.add(irMockControllerLeaveScope(builder, feature, thisParam))
  }

  private fun rewriteFeatureStatementsLegacy(feature: IrFunction, context: FeatureContext) {
    val featureStatements = context.featureBlocks.flatMap { it.statements }
    val cleanupStatements = context.cleanupBlocks.flatMap { it.statements }
    val noOpBlockCallBuilder = CleanupBlockRewriter.BlockCallBuilder { builder, _, _ ->
      with(builder) { irBlock {} }
    }
    feature.mutableStatements()?.clear()
    feature.mutableStatements()?.addAll(
      CleanupBlockRewriter(
        this.context,
        feature,
        featureStatements,
        cleanupStatements,
        0,
        noOpBlockCallBuilder
      ).rewrite()
    )
  }

  private fun buildBlockStatementsWithCalls(
    builder: DeclarationIrBuilder,
    feature: IrFunction,
    context: FeatureContext,
    thisParam: IrValueParameter
  ): List<IrStatement> {
    val mergedBlocks = mergeBlocksWithStatements(context.featureBlocks)
    return buildBlockStatementsFromMerged(builder, thisParam, mergedBlocks)
  }

  private fun buildBlockStatementsFromMerged(
    builder: DeclarationIrBuilder,
    thisParam: IrValueParameter,
    mergedBlocks: List<MergedBlock>
  ): List<IrStatement> {
    val result = mutableListOf<IrStatement>()
    mergedBlocks.forEachIndexed { blockIndex, block ->
      result.add(irCallBlockEntered(builder, thisParam, blockIndex))
      result.addAll(block.statements)
      result.add(irCallBlockExited(builder, thisParam, blockIndex))
    }
    return result
  }

  // --- Block entry/exit IR helpers ---

  private fun irCallBlockEntered(
    builder: DeclarationIrBuilder,
    thisParam: IrValueParameter,
    blockIndex: Int
  ): IrExpression {
    val fn = findSpockRuntimeFunction("callBlockEntered")
    return with(builder) {
      irCall(fn.symbol, irBuiltIns.unitType).apply {
        arguments[0] = irGet(thisParam)
        arguments[1] = irInt(blockIndex)
      }
    }
  }

  private fun irCallBlockExited(
    builder: DeclarationIrBuilder,
    thisParam: IrValueParameter,
    blockIndex: Int
  ): IrExpression {
    val fn = findSpockRuntimeFunction("callBlockExited")
    return with(builder) {
      irCall(fn.symbol, irBuiltIns.unitType).apply {
        arguments[0] = irGet(thisParam)
        arguments[1] = irInt(blockIndex)
      }
    }
  }

  private fun irMockControllerLeaveScope(
    builder: DeclarationIrBuilder,
    feature: IrFunction,
    thisParam: IrValueParameter
  ): IrExpression {
    val specContextGetter = findSpecificationContextGetter(feature)
    val specContextCall = with(builder) {
      irCall(specContextGetter.symbol, specContextGetter.returnType).apply {
        dispatchReceiver = irGet(thisParam)
      }
    }

    val specContextReturnType = specContextGetter.returnType
    val specContextClassSymbol = (specContextReturnType as IrSimpleType).classifier as IrClassSymbol
    val notNullSpecContext = with(builder) {
      irImplicitNotNull(specContextCall, specContextClassSymbol.defaultType)
    }
    val mockControllerGetter = findFunctionInHierarchy(specContextClassSymbol, "getMockController")
    val mockControllerCall = with(builder) {
      irCall(mockControllerGetter.symbol, mockControllerGetter.returnType).apply {
        dispatchReceiver = notNullSpecContext
      }
    }

    val mockControllerClass = context.findRequiredClassSymbol(MOCK_CONTROLLER_FQN)
    val castMockController = with(builder) {
      irAs(mockControllerCall, mockControllerClass.defaultType)
    }
    val leaveScopeFn = findFunctionInHierarchy(mockControllerClass, "leaveScope")
    return with(builder) {
      irCall(leaveScopeFn.symbol, leaveScopeFn.returnType).apply {
        dispatchReceiver = castMockController
      }
    }
  }

  private fun findFunctionInHierarchy(classSymbol: IrClassSymbol, name: String): IrSimpleFunction {
    val queue = ArrayDeque<IrClass>()
    queue.add(classSymbol.owner)
    while (queue.isNotEmpty()) {
      val clazz = queue.removeFirst()
      clazz.declarations
        .filterIsInstance<IrSimpleFunction>()
        .find { fn -> fn.name.asString() == name }
        ?.let { return it }
      for (superType in clazz.superTypes) {
        val superClass = (superType as? IrSimpleType)
          ?.classifier
          ?.let { c -> (c as? IrClassSymbol)?.owner }
        if (superClass != null) queue.add(superClass)
      }
    }
    error("Cannot find function '$name' on ${classSymbol.owner.name} or its supertypes")
  }

  private fun findSpockRuntimeFunction(name: String): IrSimpleFunction {
    val spockRuntimeClass = context.findRequiredClassSymbol(SPOCK_RUNTIME_FQN)
    return spockRuntimeClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { fn -> fn.name.asString() == name }
  }

  private fun findSpecificationContextGetter(feature: IrFunction): IrSimpleFunction {
    val spec = feature.parent as IrClass
    var clazz: IrClass? = spec
    while (clazz != null) {
      val getter = clazz.declarations
        .filterIsInstance<IrSimpleFunction>()
        .find { fn -> fn.name.asString() == "getSpecificationContext" }
      if (getter != null) return getter
      clazz = clazz.superTypes.firstOrNull()?.let { superType ->
        val fqn = (superType as? IrSimpleType)
          ?.classifier
          ?.let { classifier -> (classifier as? IrClassSymbol)?.owner?.fqNameWhenAvailable }
          ?: return@let null
        context.findRequiredClassSymbol(fqn).owner
      }
    }
    val specInternalsClass = context.findRequiredClassSymbol(SPEC_INTERNALS_FQN)
    return specInternalsClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { fn -> fn.name.asString() == "getSpecificationContext" }
  }

  // --- Metadata annotation ---

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

  // --- Block merging ---

  private data class MergedBlock(
    val blockKind: String,
    val descriptions: List<String>,
    val statements: List<IrStatement> = emptyList()
  )

  private fun mergeBlocks(blocks: List<FeatureBlockStatements>): List<MergedBlock> =
    doMergeBlocks(blocks, includeStatements = false)

  private fun mergeBlocksWithStatements(blocks: List<FeatureBlockStatements>): List<MergedBlock> =
    doMergeBlocks(blocks, includeStatements = true)

  private fun doMergeBlocks(
    blocks: List<FeatureBlockStatements>,
    includeStatements: Boolean
  ): List<MergedBlock> {
    val result = mutableListOf<MergedBlock>()
    for (block in blocks) {
      val label = block.element.label
      if (label == FeatureBlockLabel.AND && result.isNotEmpty()) {
        val last = result.last()
        result[result.lastIndex] = last.copy(
          descriptions = last.descriptions + block.element.description,
          statements = if (includeStatements) last.statements + block.statements else emptyList()
        )
      } else if (label.blockKind != null) {
        result.add(
          MergedBlock(
            blockKind = label.blockKind!!,
            descriptions = listOf(block.element.description),
            statements = if (includeStatements) block.statements else emptyList()
          )
        )
      }
    }
    return result
  }
}
