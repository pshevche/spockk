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

import io.github.pshevche.spockk.compilation.ir.irCatchParameter
import io.github.pshevche.spockk.compilation.ir.irTryHoistingVariables
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.WHEN_BLOCK_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.ir.IrSpecificationContext
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import io.github.pshevche.spockk.compilation.transformer.ir.getSpecificationContext
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.parentAsClass

/**
 * Rewrites a `when` block, however its paired `then` block reads (a plain condition needs neither
 * kind of wrapping below, so such a `when` never reaches this rewriter - see `FeatureRewriter`):
 *
 * - [wrapExceptionHandling] (paired `then` has a `thrown`/`notThrown`/`noExceptionThrown` call): the
 *   block's own statements are wrapped in a try/catch that records any thrown exception on
 *   `SpecificationContext`, mirroring Spock's own `SpecRewriter.rewriteWhenBlockForExceptionCondition`.
 * - [hasInteractions] (paired `then` declares interactions): brackets the block with
 *   `mockController.enterScope()`/[addInteractionStatements] (the interaction-building statements
 *   moved out of the `then` block), mirroring Spock's own `SpecRewriter.moveInteractions`. The paired
 *   `then` block's own rewrite inserts `mockController.leaveScope()` as the first statement of its
 *   own output - always together with [hasInteractions], even for a `then` block whose only
 *   interaction statement is a zero-argument `noMoreInteractions()` (a no-op, but still a scope to
 *   balance): [hasInteractions] tracks whether the `then` block had an interaction statement at all,
 *   not whether extracting it happened to produce any [addInteractionStatements].
 *
 * Both apply together when the `then` block has both - interaction registration always runs, whether
 * or not the stimulus that follows throws, so the try/catch nests inside the scope, around the `when`
 * block's own statements only. A variable the `when` block declares that [thenBlockStatements] (or a
 * later `cleanup:` block, via [irTryHoistingVariables]'s own recursion into an already-nested try like
 * this one) reads is hoisted out of the try.
 */
internal class WhenBlockRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val feature: IrFunction,
  private val whenBlock: FeatureBlock,
  private val wrapExceptionHandling: Boolean,
  private val hasInteractions: Boolean,
  private val addInteractionStatements: List<IrStatement> = emptyList(),
  private val thenBlockStatements: List<IrStatement>
) : SpockkIrRewriter {

  private val builder = irBuilder(feature.symbol)

  fun rewrite(): List<IrStatement> {
    val specAccessor = feature.requiredThisParameter()
    val specificationContext = feature.parentAsClass.getSpecificationContext(rewriterContext)

    return buildList {
      if (wrapExceptionHandling) {
        add(specificationContext.irSetThrownException(builder, specAccessor, builder.irNull()))
      }
      add(rewriterContext.spockRuntime.irCallBlockEntered(builder, specAccessor, whenBlock.ordinal))
      if (hasInteractions) {
        val controller = specificationContext.irGetMockController(builder, specAccessor)
        add(rewriterContext.mockController.irEnterScope(builder, controller))
        addAll(addInteractionStatements)
      }
      addAll(
        if (wrapExceptionHandling) wrapInTryCatch(specAccessor, specificationContext) else whenBlock.statements
      )
      add(rewriterContext.spockRuntime.irCallBlockExited(builder, specAccessor, whenBlock.ordinal))
    }
  }

  private fun wrapInTryCatch(
    specAccessor: IrValueParameter,
    specificationContext: IrSpecificationContext
  ): List<IrStatement> {
    val catchVar = irCatchParameter(WHEN_BLOCK_THROWABLE_VAR, irBuiltIns.throwableType).apply { parent = feature }
    val catchResult = specificationContext.irSetThrownException(builder, specAccessor, builder.irGet(catchVar))

    return builder.irTryHoistingVariables(
      tryExpressions = whenBlock.statements,
      catchExpressions = listOf(builder.irCatch(catchVar, builder.irBlock { +catchResult })),
      finallyExpressions = listOf(),
      extraReaders = thenBlockStatements
    )
  }
}
