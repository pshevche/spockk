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

import io.github.pshevche.spockk.compilation.ir.isAssertCall
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.ir.unwrapImplicitCoercionToUnit
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression

/**
 * Block-level entry point for `expect`/`then` blocks: bookends the block with
 * `callBlockEntered`/`callBlockExited` and delegates the actual statement rewriting to
 * [ConditionStatementsRewriter].
 */
internal class ConditionRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val builder: DeclarationIrBuilder,
  private val feature: IrFunction,
  private val blockOrdinal: Int,
  private val valueRecorderVar: IrVariable?,
  private val errorCollectorVar: IrVariable?
) : SpockkIrRewriter {

  fun rewrite(statements: List<IrStatement>): List<IrStatement> = buildList {
    add(
      rewriterContext.spockRuntime.irCallBlockEntered(
        builder,
        feature.requiredThisParameter(),
        blockOrdinal
      )
    )
    addAll(
      ConditionStatementsRewriter(rewriterContext).rewrite(
        statements = statements,
        enclosingFunction = feature,
        builder = builder,
        valueRecorderVar = valueRecorderVar,
        errorCollectorVar = errorCollectorVar,
        treatAsConditionScope = true,
        allowInteractionStatements = true
      )
    )
    add(
      rewriterContext.spockRuntime.irCallBlockExited(
        builder,
        feature.requiredThisParameter(),
        blockOrdinal
      )
    )
  }
}

/**
 * A statement is a condition when [treatAsConditionScope] is true for this statement list, and the
 * statement is an explicit `assert(...)` call or an implicit boolean expression statement.
 */
internal fun IrStatement.isConditionStatement(irBuiltIns: IrBuiltIns, treatAsConditionScope: Boolean = true): Boolean {
  if (!treatAsConditionScope) return false
  val expr = (this as? IrExpression)?.unwrapImplicitCoercionToUnit() ?: return false
  return expr.isAssertCall() || expr.type == irBuiltIns.booleanType
}
