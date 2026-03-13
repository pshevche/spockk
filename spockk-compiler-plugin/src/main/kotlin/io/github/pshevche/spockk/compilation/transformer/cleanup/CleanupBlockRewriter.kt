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

package io.github.pshevche.spockk.compilation.transformer.cleanup

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FeatureContext
import io.github.pshevche.spockk.compilation.ir.findFunctionSymbols
import io.github.pshevche.spockk.compilation.ir.irCatchParameter
import io.github.pshevche.spockk.compilation.ir.irVar
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irNotEquals
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irTry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class CleanupBlockRewriter(
  override val context: IrGeneratorContext,
  private val feature: IrFunction,
  private val featureContext: FeatureContext
) : SpockkIrRewriter {

  fun rewrite(): List<IrStatement> {
    val builder = irBuilder(feature.symbol)
    val throwableType = irBuiltIns.throwableType
    val nullableThrowableType = throwableType.makeNullable()

    val featureThrowableVar = irVar(
      Name.identifier("\$spock_feature_throwable"),
      nullableThrowableType
    ).apply {
      parent = feature
      initializer = builder.irNull()
    }

    val featureStatements = featureContext.featureBlocks.flatMap { it.statements }
    val cleanupStatements = featureContext.cleanupBlocks.flatMap { it.statements }

    val tryCatchFinally = with(builder) {
      irTry(
        type = irBuiltIns.unitType,
        tryResult = irBlock {
          featureStatements.forEach { +it }
        },
        catches = listOf(
          outerCatch(featureThrowableVar)
        ),
        finallyExpression = irBlock {
          +irTry(
            type = irBuiltIns.unitType,
            tryResult = irBlock {
              cleanupStatements.forEach { +it }
            },
            catches = listOf(
              innerCatch(featureThrowableVar)
            ),
            finallyExpression = null
          )
        }
      )
    }

    return listOf(featureThrowableVar, tryCatchFinally)
  }

  private fun outerCatch(featureThrowableVar: IrVariable): org.jetbrains.kotlin.ir.expressions.IrCatch {
    val builder = irBuilder(feature.symbol)
    val catchVar = irCatchParameter(
      Name.identifier("\$spock_tmp_throwable"),
      irBuiltIns.throwableType
    ).apply { parent = feature }
    val catchResult = with(builder) {
      irBlock {
        +irSet(featureThrowableVar, irGet(catchVar))
        +irThrow(irGet(catchVar))
      }
    }
    return org.jetbrains.kotlin.ir.expressions.impl.IrCatchImpl(
      builder.startOffset,
      builder.endOffset,
      catchVar,
      catchResult
    )
  }

  private fun innerCatch(featureThrowableVar: IrVariable): org.jetbrains.kotlin.ir.expressions.IrCatch {
    val builder = irBuilder(feature.symbol)
    val catchVar = irCatchParameter(
      Name.identifier("\$spock_tmp_throwable"),
      irBuiltIns.throwableType
    ).apply { parent = feature }
    val catchResult = with(builder) {
      irBlock {
        +irIfThenElse(
          type = irBuiltIns.unitType,
          condition = irNotEquals(irGet(featureThrowableVar), irNull()),
          thenPart = irAddSuppressed(irGet(featureThrowableVar), irGet(catchVar)),
          elsePart = irBlock(resultType = irBuiltIns.unitType) { +irThrow(irGet(catchVar)) },
          origin = IrStatementOrigin.IF
        )
      }
    }
    return org.jetbrains.kotlin.ir.expressions.impl.IrCatchImpl(
      builder.startOffset,
      builder.endOffset,
      catchVar,
      catchResult
    )
  }

  private fun irThrow(value: IrExpression): IrExpression {
    val builder = irBuilder(feature.symbol)
    return org.jetbrains.kotlin.ir.expressions.impl.IrThrowImpl(
      builder.startOffset,
      builder.endOffset,
      irBuiltIns.nothingType,
      value
    )
  }

  private fun IrBuilderWithScope.irImplicitCastToThrowable(value: IrExpression): IrExpression {
    val throwableType = irBuiltIns.throwableType
    return IrTypeOperatorCallImpl(
      startOffset,
      endOffset,
      throwableType,
      IrTypeOperator.IMPLICIT_CAST,
      throwableType,
      value
    )
  }

  private fun IrBuilderWithScope.irAddSuppressed(
    receiver: IrExpression,
    exception: IrExpression
  ): IrExpression {
    val addSuppressedCallableId = CallableId(FqName("kotlin"), Name.identifier("addSuppressed"))
    val addSuppressedFun = context.findFunctionSymbols(addSuppressedCallableId).single()
    return irBlock {
      +irCall(addSuppressedFun, irBuiltIns.unitType).apply {
        arguments[0] = irImplicitCastToThrowable(receiver)
        arguments[1] = exception
      }
    }
  }
}
