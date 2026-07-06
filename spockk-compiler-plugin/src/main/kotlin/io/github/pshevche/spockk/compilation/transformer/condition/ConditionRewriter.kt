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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers
import io.github.pshevche.spockk.compilation.ir.findFieldByName
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irCatchParameter
import io.github.pshevche.spockk.compilation.ir.irImplicitNotNull
import io.github.pshevche.spockk.compilation.ir.irTry
import io.github.pshevche.spockk.compilation.ir.irType
import io.github.pshevche.spockk.compilation.ir.irVal
import io.github.pshevche.spockk.compilation.ir.isAssertCall
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.ir.unwrapImplicitCoercionToUnit
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.CONDITION_THROWABLE_VAR
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.ERROR_COLLECTOR_VAR
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.VALUE_RECORDER_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.IrValueRecorder
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.file

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class ConditionRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val builder: DeclarationIrBuilder,
  private val feature: IrFunction,
  private val blockOrdinal: Int
) : SpockkIrRewriter {

  fun rewrite(statements: List<IrStatement>): List<IrStatement> = buildList {
    val hasConditionStatements = statements.any { isConditionStatement(it) }

    if (hasConditionStatements) {
      val valueRecorderVar = initializeValueRecorderStatement().also { add(it) }
      val errorCollectorVar = initializeErrorCollectorStatement().also { add(it) }
      add(
        rewriterContext.spockRuntime.irCallBlockEntered(
          builder,
          feature.requiredThisParameter(),
          blockOrdinal
        )
      )
      statements.forEach {
        if (isConditionStatement(it)) {
          add(rewriteConditionStatement(it as IrExpression, valueRecorderVar, errorCollectorVar))
        } else {
          add(it)
        }
      }
    } else {
      add(
        rewriterContext.spockRuntime.irCallBlockEntered(
          builder,
          feature.requiredThisParameter(),
          blockOrdinal
        )
      )
      addAll(statements)
    }

    add(
      rewriterContext.spockRuntime.irCallBlockExited(
        builder,
        feature.requiredThisParameter(),
        blockOrdinal
      )
    )
  }

  private fun isConditionStatement(statement: IrStatement): Boolean {
    val expr = (statement as? IrExpression)?.unwrapImplicitCoercionToUnit() ?: return false
    return expr.isAssertCall() || hasBooleanReturnType(expr)
  }

  private fun hasBooleanReturnType(statement: IrExpression): Boolean = statement.type == irBuiltIns.booleanType

  private fun initializeErrorCollectorStatement(): IrVariable {
    val errorCollectorType = builder.irType(IrIdentifiers.Spock.ERROR_COLLECTOR_FQN)
    val errorRethrowerType = builder.irType(IrIdentifiers.Spock.ERROR_RETHROWER_FQN)
    val errorRethrowerClass = rewriterContext.findRequiredClassSymbol(IrIdentifiers.Spock.ERROR_RETHROWER_FQN)
    val instanceField = errorRethrowerClass.findFieldByName("INSTANCE")

    val instanceFieldAccess = builder.irGetField(null, instanceField).apply {
      superQualifierSymbol = errorRethrowerClass
    }
    return irVal(ERROR_COLLECTOR_VAR, errorCollectorType).apply {
      parent = feature
      initializer = builder.irImplicitNotNull(instanceFieldAccess, errorRethrowerType)
    }
  }

  private fun initializeValueRecorderStatement(): IrVariable {
    val valueRecorderClass = rewriterContext.findRequiredClassSymbol(IrIdentifiers.Spock.VALUE_RECORDER_FQN)
    val valueRecorderConstructor = valueRecorderClass.constructors.first()
    val newValueRecorderCall = builder.irCallConstructor(valueRecorderConstructor, listOf())

    return irVal(VALUE_RECORDER_VAR, builder.irType(IrIdentifiers.Spock.VALUE_RECORDER_FQN)).apply {
      parent = feature
      initializer = newValueRecorderCall
    }
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
