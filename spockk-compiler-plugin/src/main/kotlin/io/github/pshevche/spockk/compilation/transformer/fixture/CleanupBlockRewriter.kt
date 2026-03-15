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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.BLOCK_INFO_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPECIFICATION_CONTEXT_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPEC_INTERNALS_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
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
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irNotEquals
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irTry
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class CleanupBlockRewriter(
  override val context: IrGeneratorContext,
  private val feature: IrFunction,
  private val featureStatements: List<IrStatement>,
  private val cleanupStatements: List<IrStatement>,
  private val cleanupBlockIndex: Int,
  private val blockCallBuilder: BlockCallBuilder
) : SpockkIrRewriter {

  fun interface BlockCallBuilder {
    fun buildBlockCall(builder: DeclarationIrBuilder, entered: Boolean, blockIndex: Int): IrExpression
  }

  fun rewrite(): List<IrStatement> {
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

    val blockInfoType = context.findRequiredClassSymbol(BLOCK_INFO_FQN).defaultType
    val nullableBlockInfoType = blockInfoType.makeNullable()

    val failedBlockVar = irVar(
      Name.identifier($$"$spock_failedBlock"),
      nullableBlockInfoType
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
          +failedBlockVar
          +irTry(
            type = irBuiltIns.unitType,
            tryResult = irBlock {
              +captureFailedBlock(builder, featureThrowableVar, failedBlockVar)
              +blockCallBuilder.buildBlockCall(builder, true, cleanupBlockIndex)
              cleanupStatements.forEach { +it }
              +blockCallBuilder.buildBlockCall(builder, false, cleanupBlockIndex)
            },
            catches = listOf(
              innerCatch(builder, featureThrowableVar)
            ),
            finallyExpression = restoreBlockFinally(builder, featureThrowableVar, failedBlockVar)
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

  private fun captureFailedBlock(
    builder: DeclarationIrBuilder,
    featureThrowableVar: IrVariable,
    failedBlockVar: IrVariable
  ): IrExpression = with(builder) {
    irIfThenElse(
      type = irBuiltIns.unitType,
      condition = irNotEquals(irGet(featureThrowableVar), irNull()),
      thenPart = irBlock {
        +irSet(failedBlockVar, irGetCurrentBlock(builder))
      },
      elsePart = irBlock(resultType = irBuiltIns.unitType) {},
      origin = IrStatementOrigin.IF
    )
  }

  private fun restoreBlockFinally(
    builder: DeclarationIrBuilder,
    featureThrowableVar: IrVariable,
    failedBlockVar: IrVariable
  ): IrExpression = with(builder) {
    irBlock {
      +irIfThenElse(
        type = irBuiltIns.unitType,
        condition = irNotEquals(irGet(featureThrowableVar), irNull()),
        thenPart = irBlock {
          +irSetCurrentBlock(builder, failedBlockVar)
        },
        elsePart = irBlock(resultType = irBuiltIns.unitType) {},
        origin = IrStatementOrigin.IF
      )
    }
  }

  private fun irGetCurrentBlock(builder: DeclarationIrBuilder): IrExpression {
    val thisParam = feature.parameters.first { it.name.asString() == "<this>" }
    val specContextClass = context.findRequiredClassSymbol(SPECIFICATION_CONTEXT_FQN)
    val getSpecCtx = findGetSpecificationContext()
    val specCtxCall = with(builder) {
      irCall(getSpecCtx.symbol, getSpecCtx.returnType).apply {
        dispatchReceiver = irGet(thisParam)
      }
    }
    val castSpecCtx = with(builder) {
      irAs(specCtxCall, specContextClass.defaultType)
    }
    val getCurrentBlockFn = specContextClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { it.name.asString() == "getCurrentBlock" }
    return with(builder) {
      irCall(getCurrentBlockFn.symbol, getCurrentBlockFn.returnType).apply {
        dispatchReceiver = castSpecCtx
      }
    }
  }

  private fun irSetCurrentBlock(
    builder: DeclarationIrBuilder,
    failedBlockVar: IrVariable
  ): IrExpression {
    val thisParam = feature.parameters.first { it.name.asString() == "<this>" }
    val specContextClass = context.findRequiredClassSymbol(SPECIFICATION_CONTEXT_FQN)
    val getSpecCtx = findGetSpecificationContext()
    val specCtxCall = with(builder) {
      irCall(getSpecCtx.symbol, getSpecCtx.returnType).apply {
        dispatchReceiver = irGet(thisParam)
      }
    }
    val castSpecCtx = with(builder) {
      irAs(specCtxCall, specContextClass.defaultType)
    }
    val setCurrentBlockFn = specContextClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { it.name.asString() == "setCurrentBlock" }
    return with(builder) {
      irCall(setCurrentBlockFn.symbol, setCurrentBlockFn.returnType).apply {
        dispatchReceiver = castSpecCtx
        arguments[1] = irGet(failedBlockVar)
      }
    }
  }

  private fun findGetSpecificationContext(): IrSimpleFunction {
    val spec = feature.parent as IrClass
    var clazz: IrClass? = spec
    while (clazz != null) {
      val getter = clazz.declarations
        .filterIsInstance<IrSimpleFunction>()
        .find { fn -> fn.name.asString() == "getSpecificationContext" }
      if (getter != null) return getter
      clazz = clazz.superTypes.firstOrNull()?.let { superType ->
        val fqn = (superType as? IrSimpleType)
          ?.classifier
          ?.let { classifier ->
            (classifier as? IrClassSymbol)
              ?.owner?.fqNameWhenAvailable
          }
          ?: return@let null
        context.findRequiredClassSymbol(fqn).owner
      }
    }
    val specInternalsClass = context.findRequiredClassSymbol(
      SPEC_INTERNALS_FQN
    )
    return specInternalsClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { fn -> fn.name.asString() == "getSpecificationContext" }
  }
}
