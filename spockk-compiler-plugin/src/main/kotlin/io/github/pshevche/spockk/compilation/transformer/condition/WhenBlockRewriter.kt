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

package io.github.pshevche.spockk.compilation.transformer.condition

import io.github.pshevche.spockk.compilation.ir.irCatchParameter
import io.github.pshevche.spockk.compilation.ir.irTryHoistingVariables
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.WHEN_BLOCK_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
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
 * Rewrites a `when` block paired with a `then` block that contains a `thrown`/`notThrown`/
 * `noExceptionThrown` call: wraps the block's statements in a try/catch that records any thrown
 * exception on `SpecificationContext`, mirroring Spock's own
 * `SpecRewriter.rewriteWhenBlockForExceptionCondition`. A variable the `when` block declares that
 * [thenBlockStatements] (or a later `cleanup:` block, via [irTryHoistingVariables]'s own recursion
 * into an already-nested try like this one) reads is hoisted out of the try.
 */
internal class WhenBlockRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val feature: IrFunction,
  private val whenBlock: FeatureBlock,
  private val thenBlockStatements: List<IrStatement>
) : SpockkIrRewriter {

  private val builder = irBuilder(feature.symbol)

  fun rewrite(): List<IrStatement> {
    val specAccessor = feature.requiredThisParameter()
    val specificationContext = feature.parentAsClass.getSpecificationContext(rewriterContext)

    return buildList {
      add(specificationContext.irSetThrownException(builder, specAccessor, builder.irNull()))
      add(rewriterContext.spockRuntime.irCallBlockEntered(builder, specAccessor, whenBlock.ordinal))
      addAll(wrapInTryCatch(specAccessor, specificationContext))
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
