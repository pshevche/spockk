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

import io.github.pshevche.spockk.compilation.ir.*
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.CONDITION_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.IrValueRecorder
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.file

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
    statements.forEach { statement ->
      if (statement.isConditionStatement(irBuiltIns)) {
        // A condition statement implies the feature has conditions, so the shared value recorder
        // and error collector were created once at feature scope (see FeatureRewriter).
        add(rewriteConditionStatement(statement as IrExpression, valueRecorderVar!!, errorCollectorVar!!))
      } else {
        add(statement)
      }
    }
    add(
      rewriterContext.spockRuntime.irCallBlockExited(
        builder,
        feature.requiredThisParameter(),
        blockOrdinal
      )
    )
  }

  private fun rewriteConditionStatement(
    statement: IrExpression,
    valueRecorderVar: IrVariable,
    errorCollectorVar: IrVariable
  ): IrStatement {
    val irValueRecorder = IrValueRecorder.create(rewriterContext, valueRecorderVar)
    return with(builder) {
      irTry(
        tryExpressions = listOf(verifyConditionCall(statement, irValueRecorder, errorCollectorVar)),
        catchExpressions = listOf(
          conditionFailedWithAnExceptionCall(
            statement,
            irValueRecorder,
            errorCollectorVar
          )
        ),
        finallyExpressions = listOf()
      )
    }
  }

  private fun verifyConditionCall(
    statement: IrExpression,
    irValueRecorder: IrValueRecorder,
    errorCollectorVar: IrVariable
  ): IrCall = rewriterContext.spockRuntime.irVerifyCondition(
    builder,
    irValueRecorder,
    errorCollectorVar,
    statement,
    feature.file
  )

  private fun conditionFailedWithAnExceptionCall(
    statement: IrExpression,
    irValueRecorder: IrValueRecorder,
    errorCollectorVar: IrVariable
  ): IrCatch {
    val catchVar = irCatchParameter(
      CONDITION_THROWABLE_VAR,
      irBuiltIns.throwableType
    ).apply { parent = feature }

    val catchResult = rewriterContext.spockRuntime.irConditionFailedWithException(
      builder,
      irValueRecorder,
      errorCollectorVar,
      statement,
      feature.file,
      catchVar
    )

    return with(builder) {
      irCatch(catchVar, irBlock { +catchResult })
    }
  }
}

/**
 * A statement is a condition when it is an explicit `assert(...)` call or an implicit boolean
 * expression statement. Shared by [ConditionRewriter] and the feature rewriter, which decides up
 * front whether a feature needs a value recorder.
 */
internal fun IrStatement.isConditionStatement(irBuiltIns: IrBuiltIns): Boolean {
  val expr = (this as? IrExpression)?.unwrapImplicitCoercionToUnit() ?: return false
  return expr.isAssertCall() || expr.type == irBuiltIns.booleanType
}
