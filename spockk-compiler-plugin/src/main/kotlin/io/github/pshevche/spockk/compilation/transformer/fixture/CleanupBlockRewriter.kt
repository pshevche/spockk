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

package io.github.pshevche.spockk.compilation.transformer.fixture

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.BLOCK_INFO_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irAddSuppressed
import io.github.pshevche.spockk.compilation.ir.irCatchParameter
import io.github.pshevche.spockk.compilation.ir.irThrow
import io.github.pshevche.spockk.compilation.ir.irTry
import io.github.pshevche.spockk.compilation.ir.irVar
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.FAILED_BLOCK_VAR
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.FEATURE_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.TMP_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import io.github.pshevche.spockk.compilation.transformer.ir.getSpecificationContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irNotEquals
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.parentAsClass

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class CleanupBlockRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val feature: IrFunction,
  private val cleanupBlock: FeatureBlock,
  private val behaviorStatements: List<IrStatement>
) : SpockkIrRewriter {

  private val builder = irBuilder(feature.symbol)

  fun rewrite(): List<IrStatement> {
    val featureThrowableVar = declareFeatureThrowableVar()
    val tryBehaviorStatementsAndCleanup = builder.irTry(
      tryExpressions = behaviorStatements,
      catchExpressions = captureFeatureFailure(featureThrowableVar),
      finallyExpressions = tryCleanupStatementsAndRestoreSpecContext(featureThrowableVar)
    )
    return listOf(featureThrowableVar, tryBehaviorStatementsAndCleanup)
  }

  private fun tryCleanupStatementsAndRestoreSpecContext(featureThrowableVar: IrVariable): List<IrStatement> {
    val failedBlockVar = declareFailedBlockVar()
    return listOf(
      failedBlockVar,
      builder.irTry(
        tryExpressions = captureFailedBlockAndRunCleanup(featureThrowableVar, failedBlockVar),
        catchExpressions = suppressCleanupFailure(featureThrowableVar),
        finallyExpressions = setFailedBlockAsCurrent(builder, featureThrowableVar, failedBlockVar)
      )
    )
  }

  private fun suppressCleanupFailure(featureThrowableVar: IrVariable): List<IrCatch> {
    val catchVar = irCatchParameter(
      TMP_THROWABLE_VAR,
      irBuiltIns.throwableType
    ).apply { parent = feature }

    val catchResult = with(builder) {
      irBlock {
        +irIfThenElse(
          type = irBuiltIns.unitType,
          condition = irNotEquals(irGet(featureThrowableVar), irNull()),
          thenPart = builder.irAddSuppressed(irGet(featureThrowableVar), irGet(catchVar)),
          elsePart = irBlock(resultType = irBuiltIns.unitType) { +builder.irThrow(irGet(catchVar)) },
          origin = IrStatementOrigin.IF
        )
      }
    }

    return listOf(builder.irCatch(catchVar, catchResult))
  }

  private fun captureFailedBlockAndRunCleanup(
    featureThrowableVar: IrVariable,
    failedBlockVar: IrVariable
  ): List<IrStatement> = buildList {
    add(captureFailedBlock(featureThrowableVar, failedBlockVar))
    add(rewriterContext.spockRuntime.irCallBlockEntered(builder, feature.requiredThisParameter(), cleanupBlock.ordinal))
    addAll(cleanupBlock.statements)
    add(rewriterContext.spockRuntime.irCallBlockExited(builder, feature.requiredThisParameter(), cleanupBlock.ordinal))
  }

  private fun captureFeatureFailure(featureThrowableVar: IrVariable): List<IrCatch> {
    val catchVar = irCatchParameter(
      TMP_THROWABLE_VAR,
      irBuiltIns.throwableType
    ).apply { parent = feature }
    val catchResult = with(builder) {
      irBlock {
        +irSet(featureThrowableVar, irGet(catchVar))
        +irThrow(irGet(catchVar))
      }
    }
    return listOf(builder.irCatch(catchVar, catchResult))
  }

  private fun declareFailedBlockVar(): IrVariable {
    val blockInfoType = rewriterContext.findRequiredClassSymbol(BLOCK_INFO_FQN).defaultType
    val nullableBlockInfoType = blockInfoType.makeNullable()

    return irVar(
      FAILED_BLOCK_VAR,
      nullableBlockInfoType
    ).apply {
      parent = feature
      initializer = builder.irNull()
    }
  }

  private fun declareFeatureThrowableVar(): IrVariable {
    val throwableType = irBuiltIns.throwableType
    val nullableThrowableType = throwableType.makeNullable()

    return irVar(
      FEATURE_THROWABLE_VAR,
      nullableThrowableType
    ).apply {
      parent = feature
      initializer = builder.irNull()
    }
  }

  private fun captureFailedBlock(
    featureThrowableVar: IrVariable,
    failedBlockVar: IrVariable
  ): IrExpression {
    val specificationContext = feature.parentAsClass.getSpecificationContext(rewriterContext)
    return with(builder) {
      irIfThen(
        condition = irNotEquals(irGet(featureThrowableVar), irNull()),
        thenPart = irBlock {
          +irSet(failedBlockVar, specificationContext.irGetCurrentBlock(builder, feature.requiredThisParameter()))
        }
      ).apply {
        origin = IrStatementOrigin.IF
      }
    }
  }

  private fun setFailedBlockAsCurrent(
    builder: DeclarationIrBuilder,
    featureThrowableVar: IrVariable,
    failedBlockVar: IrVariable
  ): List<IrStatement> {
    val specificationContext = feature.parentAsClass.getSpecificationContext(rewriterContext)
    return with(builder) {
      listOf(
        irIfThen(
          condition = irNotEquals(irGet(featureThrowableVar), irNull()),
          thenPart = irBlock {
            +specificationContext.irSetCurrentBlock(builder, feature.requiredThisParameter(), failedBlockVar)
          }
        ).apply {
          origin = IrStatementOrigin.IF
        }
      )
    }
  }
}
