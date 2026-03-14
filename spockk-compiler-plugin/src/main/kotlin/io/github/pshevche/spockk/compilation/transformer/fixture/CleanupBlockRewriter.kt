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

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext
import io.github.pshevche.spockk.compilation.ir.irAddSuppressed
import io.github.pshevche.spockk.compilation.ir.irCatchParameter
import io.github.pshevche.spockk.compilation.ir.irStatementBlock
import io.github.pshevche.spockk.compilation.ir.irThrow
import io.github.pshevche.spockk.compilation.ir.irVar
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irNotEquals
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irTry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class CleanupBlockRewriter(
  override val context: IrGeneratorContext,
  private val feature: IrFunction,
  private val featureContext: SpockkTransformationContext.FeatureContext
) : SpockkIrRewriter {

  fun rewrite(): List<IrStatement> {
    val featureStatements = featureContext.featureBlocks.flatMap { it.statements }
    val cleanupStatements = featureContext.cleanupBlocks.flatMap { it.statements }
    if (cleanupStatements.isEmpty()) {
      return featureStatements
    }

    val builder = irBuilder(feature.symbol)
    val throwableType = irBuiltIns.throwableType
    val nullableThrowableType = throwableType.makeNullable()

    val featureThrowableVar = irVar(
      Name.identifier($$"$spock_feature_throwable"),
      nullableThrowableType
    ).apply {
      parent = feature
      initializer = builder.irNull()
    }

    val tryCatchFinally = with(builder) {
      irTry(
        type = irBuiltIns.unitType,
        tryResult = irStatementBlock(featureStatements),
        catches = listOf(
          outerCatch(builder, featureThrowableVar)
        ),
        finallyExpression = irBlock {
          +irTry(
            type = irBuiltIns.unitType,
            tryResult = irStatementBlock(cleanupStatements),
            catches = listOf(
              innerCatch(builder, featureThrowableVar)
            ),
            finallyExpression = null
          )
        }
      )
    }

    return listOf(featureThrowableVar, tryCatchFinally)
  }

  private fun outerCatch(builder: DeclarationIrBuilder, featureThrowableVar: IrVariable): IrCatch {
    val catchVar = irCatchParameter(
      Name.identifier($$"$spock_tmp_throwable"),
      irBuiltIns.throwableType
    ).apply { parent = feature }
    val catchResult = with(builder) {
      irBlock {
        +irSet(featureThrowableVar, irGet(catchVar))
        +irThrow(irGet(catchVar))
      }
    }
    return builder.irCatch(catchVar, catchResult)
  }

  private fun innerCatch(builder: DeclarationIrBuilder, featureThrowableVar: IrVariable): IrCatch {
    val catchVar = irCatchParameter(
      Name.identifier($$"$spock_tmp_throwable"),
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
    return builder.irCatch(catchVar, catchResult)
  }
}
