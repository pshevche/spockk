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

package io.github.pshevche.spockk.compilation.transformer.condition

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.ERROR_COLLECTOR_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irCatchParameter
import io.github.pshevche.spockk.compilation.ir.irTry
import io.github.pshevche.spockk.compilation.ir.irType
import io.github.pshevche.spockk.compilation.ir.irVal
import io.github.pshevche.spockk.compilation.ir.isAssertCall
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.ir.unwrapImplicitCoercionToUnit
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.CONDITION_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.VERIFY_ALL_ERROR_COLLECTOR_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.IrValueRecorder
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.file

/**
 * Block-level entry point for `expect`/`then` blocks: bookends the block with
 * `callBlockEntered`/`callBlockExited` and delegates the actual statement rewriting to
 * [ConditionStatementsRewriter], which also drives the recursive rewriting inside
 * `verify`/`verifyAll`/`verifyEach` lambda bodies and inside helper methods.
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
    // A condition statement (or a verify/verifyAll/verifyEach call) implies the feature has
    // conditions, so the shared value recorder and error collector were created once at feature
    // scope (see FeatureRewriter) - but not every expect/then block necessarily contains one, so
    // these stay nullable here and are only forced non-null where actually needed.
    addAll(
      ConditionStatementsRewriter(rewriterContext).rewrite(
        statements = statements,
        enclosingFunction = feature,
        builder = builder,
        valueRecorderVar = valueRecorderVar,
        errorCollectorVar = errorCollectorVar,
        treatAsConditionScope = true
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
 * Rewrites a flat statement list wherever conditions may appear: an `expect`/`then` block's own
 * statements, or the interior of a `verify`/`verifyAll`/`verifyEach` lambda body (recursively -
 * reached either directly from a block, or from a plain helper method's top-level statements).
 *
 * [treatAsConditionScope] governs whether a bare boolean/`assert(...)` statement at *this*
 * statement-list level is itself an implicit condition: `true` for `expect`/`then` blocks and for
 * every recursive call into a matched helper call's lambda body (regardless of the flag's value at
 * the outer level - this is what makes nesting and helper-method bodies compose correctly), `false`
 * only for a plain helper method's own top-level statements, where only calls to
 * `verify`/`verifyAll`/`verifyEach` are recognized.
 */
internal class ConditionStatementsRewriter(
  override val rewriterContext: SpockkIrRewriterContext
) : SpockkIrRewriter {

  fun rewrite(
    statements: List<IrStatement>,
    enclosingFunction: IrFunction,
    builder: DeclarationIrBuilder,
    valueRecorderVar: IrVariable?,
    errorCollectorVar: IrVariable?,
    treatAsConditionScope: Boolean
  ): List<IrStatement> = statements.map { statement ->
    val helperCall = statement.asImplicitAssertionHelperCall()
    when {
      // Only forced non-null once a statement that actually needs them is found - a block/lambda
      // body with no conditions and no helper calls never evaluates these, so `hasConditions`
      // (which gates whether they're declared at all) doesn't need to account for every block, only
      // the ones that end up here.
      helperCall != null -> {
        rewriteHelperCallLambdaBody(helperCall, valueRecorderVar!!, errorCollectorVar!!)
        statement
      }

      statement.isConditionStatement(irBuiltIns, treatAsConditionScope) ->
        rewriteConditionStatement(statement as IrExpression, enclosingFunction, builder, valueRecorderVar!!, errorCollectorVar!!)

      else -> statement
    }
  }

  private fun rewriteHelperCallLambdaBody(
    helperCall: ImplicitAssertionHelperCall,
    valueRecorderVar: IrVariable,
    errorCollectorVar: IrVariable
  ) {
    val lambda = helperCall.lambda
    val lambdaStatements = lambda.mutableStatements() ?: return
    val lambdaBuilder = irBuilder(lambda.symbol)

    when (helperCall.kind) {
      ImplicitAssertionHelperKind.VERIFY, ImplicitAssertionHelperKind.VERIFY_EACH -> {
        // Fail-fast: reuse whichever (value recorder, error collector) pair is already ambient.
        val rewritten =
          rewrite(lambdaStatements.toList(), lambda, lambdaBuilder, valueRecorderVar, errorCollectorVar, treatAsConditionScope = true)
        lambdaStatements.clear()
        lambdaStatements.addAll(rewritten)
      }

      ImplicitAssertionHelperKind.VERIFY_ALL -> {
        // Soft assertions: a fresh, collecting ErrorCollector scoped to just this lambda, declared
        // as its first statement, validated as its last - so multiple failing conditions inside are
        // reported together instead of stopping at the first one.
        val freshErrorCollectorVar = irFreshErrorCollectorVariable(lambdaBuilder, lambda)
        val rewritten =
          rewrite(lambdaStatements.toList(), lambda, lambdaBuilder, valueRecorderVar, freshErrorCollectorVar, treatAsConditionScope = true)
        lambdaStatements.clear()
        lambdaStatements.add(freshErrorCollectorVar)
        lambdaStatements.addAll(rewritten)
        lambdaStatements.add(irValidateCollectedErrorsCall(lambdaBuilder, freshErrorCollectorVar))
      }
    }
  }

  private fun irFreshErrorCollectorVariable(builder: DeclarationIrBuilder, enclosingFunction: IrFunction): IrVariable {
    val errorCollectorClass = rewriterContext.findRequiredClassSymbol(ERROR_COLLECTOR_FQN)
    val errorCollectorConstructor = errorCollectorClass.constructors.first()
    val newErrorCollectorCall = builder.irCallConstructor(errorCollectorConstructor, listOf())

    return irVal(VERIFY_ALL_ERROR_COLLECTOR_VAR, builder.irType(ERROR_COLLECTOR_FQN)).apply {
      parent = enclosingFunction
      initializer = newErrorCollectorCall
    }
  }

  private fun irValidateCollectedErrorsCall(builder: DeclarationIrBuilder, errorCollectorVar: IrVariable): IrCall {
    val errorCollectorClass = rewriterContext.findRequiredClassSymbol(ERROR_COLLECTOR_FQN)
    val validateCollectedErrors = errorCollectorClass.functionByName("validateCollectedErrors")
    return builder.irCall(validateCollectedErrors).apply {
      dispatchReceiver = builder.irGet(errorCollectorVar)
    }
  }

  private fun rewriteConditionStatement(
    statement: IrExpression,
    enclosingFunction: IrFunction,
    builder: DeclarationIrBuilder,
    valueRecorderVar: IrVariable,
    errorCollectorVar: IrVariable
  ): IrStatement {
    val irValueRecorder = IrValueRecorder.create(rewriterContext, valueRecorderVar)
    return with(builder) {
      irTry(
        tryExpressions = listOf(verifyConditionCall(builder, enclosingFunction, statement, irValueRecorder, errorCollectorVar)),
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

/**
 * A statement is a condition when [treatAsConditionScope] is true for this statement list, and the
 * statement is an explicit `assert(...)` call or an implicit boolean expression statement. Shared by
 * [ConditionStatementsRewriter] and the feature rewriter, which decides up front whether a feature
 * needs a value recorder.
 */
internal fun IrStatement.isConditionStatement(irBuiltIns: IrBuiltIns, treatAsConditionScope: Boolean = true): Boolean {
  if (!treatAsConditionScope) return false
  val expr = (this as? IrExpression)?.unwrapImplicitCoercionToUnit() ?: return false
  return expr.isAssertCall() || expr.type == irBuiltIns.booleanType
}
