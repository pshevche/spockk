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
import io.github.pshevche.spockk.compilation.ir.irTry
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.CONDITION_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.interaction.InteractionStatementsRewriter
import io.github.pshevche.spockk.compilation.transformer.interaction.asInteraction
import io.github.pshevche.spockk.compilation.transformer.interaction.asNoMoreInteractionsCall
import io.github.pshevche.spockk.compilation.transformer.ir.IrErrorCollector
import io.github.pshevche.spockk.compilation.transformer.ir.IrValueRecorder
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.file

/**
 * Rewrites a flat statement list wherever conditions may appear: an `expect`/`then` block's own
 * statements, or the interior of a `verify`/`verifyAll`/`verifyEach` lambda body (recursively). Used
 * by both [ConditionRewriter] (for `expect`/`then` blocks) and [HelperMethodRewriter].
 */
internal class ConditionStatementsRewriter(
  override val rewriterContext: SpockkIrRewriterContext
) : SpockkIrRewriter {

  /**
   * [treatAsConditionScope]:
   * `true` for `expect`/`then` blocks and every recursive call into a matched helper call's lambda body
   * `false` for a plain helper method's own top-level statements (only `verify`/`verifyAll`/`verifyEach` calls are recognized there).
   *
   * [allowInteractionStatements]:
   * `true` only where a genuine dispatch receiver is available to resolve interactions against (`expect`/`then` blocks, a helper method's own statements)
   * `false` inside a `verify`/`verifyAll`/`verifyEach` lambda, which captures the enclosing `this` rather than declaring its own.
   */
  fun rewrite(
    statements: List<IrStatement>,
    enclosingFunction: IrFunction,
    builder: DeclarationIrBuilder,
    recorders: ConditionRecorders?,
    treatAsConditionScope: Boolean,
    allowInteractionStatements: Boolean = false
  ): List<IrStatement> {
    // resolved lazily to prevent eager resolution of class/function symbols
    val interactionRewriter by lazy(LazyThreadSafetyMode.NONE) {
      InteractionStatementsRewriter(
        rewriterContext,
        enclosingFunction
      )
    }

    return statements.flatMap { statement ->
      val helperCall = statement.asImplicitAssertionHelperCall()
      val interaction = if (allowInteractionStatements) statement.asInteraction() else null
      val noMoreInteractionsCall = if (allowInteractionStatements) statement.asNoMoreInteractionsCall() else null
      when {
        // verify/verifyEach/verifyAll
        helperCall != null -> {
          rewriteHelperCallLambdaBody(helperCall, statement.requireRecorders(recorders))
          listOf(statement)
        }

        // 1 * obj.isValid()
        // rewritten before conditions as some of the interactions can be recognized as conditions
        interaction != null -> interactionRewriter.rewrite(interaction)

        // noMoreInteractions()
        noMoreInteractionsCall != null -> interactionRewriter.rewriteNoMoreInteractions(noMoreInteractionsCall)

        // actual == expected
        statement.isConditionStatement(irBuiltIns, treatAsConditionScope) ->
          listOf(
            rewriteConditionStatement(
              statement as IrExpression,
              enclosingFunction,
              builder,
              statement.requireRecorders(recorders)
            )
          )

        else -> listOf(statement)
      }
    }
  }

  // The recorders are declared exactly when the enclosing method has something to record, which is
  // decided by the same predicates this dispatches on - so reaching a condition without them means
  // detection and rewriting have drifted apart.
  private fun IrStatement.requireRecorders(recorders: ConditionRecorders?): ConditionRecorders =
    requireNotNull(recorders) { "condition statement found in a method whose conditions were not detected: $this" }

  private fun rewriteHelperCallLambdaBody(helperCall: ImplicitAssertionHelperCall, recorders: ConditionRecorders) {
    val lambda = helperCall.lambda
    val lambdaStatements = lambda.mutableStatements() ?: return
    val lambdaBuilder = irBuilder(lambda.symbol)

    when (helperCall.kind) {
      ImplicitAssertionHelperKind.VERIFY, ImplicitAssertionHelperKind.VERIFY_EACH -> {
        val rewritten = rewrite(lambdaStatements.toList(), lambda, lambdaBuilder, recorders, treatAsConditionScope = true)
        lambdaStatements.clear()
        lambdaStatements.addAll(rewritten)
      }

      ImplicitAssertionHelperKind.VERIFY_ALL -> {
        val freshErrorCollectorVar = irNewErrorCollectorDeclaration(lambdaBuilder, lambda)
        val freshErrorCollector = IrErrorCollector.create(rewriterContext, freshErrorCollectorVar)
        val rewritten = rewrite(
          lambdaStatements.toList(),
          lambda,
          lambdaBuilder,
          recorders.reportingTo(freshErrorCollectorVar),
          treatAsConditionScope = true
        )
        lambdaStatements.clear()
        lambdaStatements.add(freshErrorCollectorVar)
        lambdaStatements.addAll(rewritten)
        lambdaStatements.add(freshErrorCollector.irValidateCollectedErrors(lambdaBuilder))
      }
    }
  }

  private fun rewriteConditionStatement(
    statement: IrExpression,
    enclosingFunction: IrFunction,
    builder: DeclarationIrBuilder,
    recorders: ConditionRecorders
  ): IrStatement {
    val irValueRecorder = IrValueRecorder.create(rewriterContext, recorders.valueRecorder)
    val errorCollectorVar = recorders.errorCollector
    return with(builder) {
      irTry(
        tryExpressions = listOf(
          verifyConditionCall(
            builder,
            enclosingFunction,
            statement,
            irValueRecorder,
            errorCollectorVar
          )
        ),
        catchExpressions = listOf(
          conditionFailedWithAnExceptionCall(builder, enclosingFunction, statement, irValueRecorder, errorCollectorVar)
        ),
        finallyExpressions = listOf()
      )
    }
  }

  private fun verifyConditionCall(
    builder: DeclarationIrBuilder,
    enclosingFunction: IrFunction,
    statement: IrExpression,
    irValueRecorder: IrValueRecorder,
    errorCollectorVar: IrVariable
  ): IrCall = rewriterContext.spockRuntime.irVerifyCondition(
    builder,
    irValueRecorder,
    errorCollectorVar,
    statement,
    enclosingFunction.file
  )

  private fun conditionFailedWithAnExceptionCall(
    builder: DeclarationIrBuilder,
    enclosingFunction: IrFunction,
    statement: IrExpression,
    irValueRecorder: IrValueRecorder,
    errorCollectorVar: IrVariable
  ): IrCatch {
    val catchVar = irCatchParameter(
      CONDITION_THROWABLE_VAR,
      irBuiltIns.throwableType
    ).apply { parent = enclosingFunction }

    val catchResult = rewriterContext.spockRuntime.irConditionFailedWithException(
      builder,
      irValueRecorder,
      errorCollectorVar,
      statement,
      enclosingFunction.file,
      catchVar
    )

    return with(builder) {
      irCatch(catchVar, irBlock { +catchResult })
    }
  }
}
