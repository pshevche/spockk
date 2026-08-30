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

import io.github.pshevche.spockk.compilation.ir.irCatchParameter
import io.github.pshevche.spockk.compilation.ir.irTry
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.WHEN_BLOCK_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.IrSpecificationContext
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import io.github.pshevche.spockk.compilation.transformer.ir.getSpecificationContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.parentAsClass

/**
 * Rewrites a `when` block paired with a `then` block that declares interactions - brackets it with
 * `mockController.enterScope()`/the interaction-building statements moved out of the `then` block,
 * mirroring Spock's own `SpecRewriter.moveInteractions`. The paired `then` block's own rewrite
 * inserts `mockController.leaveScope()` as the first statement of its own output.
 *
 * When the `then` block also declares an exception condition, [wrapExceptionHandling] nests the
 * exception try/catch inside the interaction scope, around the `when` block's own statements only -
 * interaction registration always runs, whether or not the stimulus that follows throws.
 */
internal class InteractionScopeRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val feature: IrFunction,
  private val whenBlock: FeatureBlock,
  private val addInteractionStatements: List<IrStatement>,
  private val wrapExceptionHandling: Boolean
) : SpockkIrRewriter {

  private val builder = irBuilder(feature.symbol)

  fun rewrite(): List<IrStatement> {
    val specAccessor = feature.requiredThisParameter()
    val specificationContext = feature.parentAsClass.getSpecificationContext(rewriterContext)
    val controller = specificationContext.irGetMockController(builder, specAccessor)

    return buildList {
      if (wrapExceptionHandling) {
        add(specificationContext.irSetThrownException(builder, specAccessor, builder.irNull()))
      }
      add(rewriterContext.spockRuntime.irCallBlockEntered(builder, specAccessor, whenBlock.ordinal))
      add(rewriterContext.mockController.irEnterScope(builder, controller))
      addAll(addInteractionStatements)
      if (wrapExceptionHandling) {
        add(wrapInTryCatch(specAccessor, specificationContext))
      } else {
        addAll(whenBlock.statements)
      }
      add(rewriterContext.spockRuntime.irCallBlockExited(builder, specAccessor, whenBlock.ordinal))
    }
  }

  private fun wrapInTryCatch(
    specAccessor: IrValueParameter,
    specificationContext: IrSpecificationContext
  ): IrStatement {
    val catchVar = irCatchParameter(WHEN_BLOCK_THROWABLE_VAR, irBuiltIns.throwableType).apply { parent = feature }
    val catchResult = specificationContext.irSetThrownException(builder, specAccessor, builder.irGet(catchVar))

    return builder.irTry(
      tryExpressions = whenBlock.statements,
      catchExpressions = listOf(builder.irCatch(catchVar, builder.irBlock { +catchResult })),
      finallyExpressions = listOf()
    )
  }
}

/**
 * `mockController.leaveScope()`, the first statement of a `then` block paired with an
 * [InteractionScopeRewriter]-wrapped `when` block - verifies the interactions registered in that scope.
 */
internal fun SpockkIrRewriter.irLeaveScopeStatement(feature: IrFunction, builder: DeclarationIrBuilder): IrStatement {
  val specAccessor = feature.requiredThisParameter()
  val specificationContext = feature.parentAsClass.getSpecificationContext(rewriterContext)
  val controller = specificationContext.irGetMockController(builder, specAccessor)
  return rewriterContext.mockController.irLeaveScope(builder, controller)
}
