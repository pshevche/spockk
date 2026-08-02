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

package io.github.pshevche.spockk.compilation.transformer.ir

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPOCK_RUNTIME_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.isAssertCall
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.toIrConst

internal class IrSpockRuntime private constructor(
  private val spockRuntimeClassSymbol: IrClassSymbol,
  private val sourceTextCache: SourceTextCache
) {

  fun irCallBlockEntered(builder: IrBuilder, specAccessor: IrValueParameter, blockOrdinal: Int): IrCall {
    val callBlockEntered = spockRuntimeClassSymbol.functionByName("callBlockEntered")
    return with(builder) {
      irCall(callBlockEntered).apply {
        arguments[0] = irGet(specAccessor)
        arguments[1] = irInt(blockOrdinal)
      }
    }
  }

  fun irCallBlockExited(builder: DeclarationIrBuilder, specAccessor: IrValueParameter, blockOrdinal: Int): IrCall {
    val callBlockEntered = spockRuntimeClassSymbol.functionByName("callBlockExited")
    return with(builder) {
      irCall(callBlockEntered).apply {
        arguments[0] = irGet(specAccessor)
        arguments[1] = irInt(blockOrdinal)
      }
    }
  }

  fun irVerifyCondition(
    builder: DeclarationIrBuilder,
    irValueRecorder: IrValueRecorder,
    errorCollectorVar: IrVariable,
    statement: IrExpression,
    file: IrFile
  ): IrCall {
    val verifyCondition = spockRuntimeClassSymbol.functionByName("verifyCondition")
    val startOffset = statement.effectiveStartOffset()
    val line = file.fileEntry.getLineNumber(startOffset) + 1
    val column = file.fileEntry.getColumnNumber(startOffset) + 1
    val conditionValueRecorder = ConditionValueRecordingTransformer(builder, irValueRecorder)
    return with(builder) {
      irCall(verifyCondition).apply {
        arguments[0] = irGet(errorCollectorVar)
        arguments[1] = irValueRecorder.irReset(builder)
        arguments[2] = irStatementText(builder, statement, file)
        arguments[3] = line.toIrConst(context.irBuiltIns.intType)
        arguments[4] = column.toIrConst(context.irBuiltIns.intType)
        arguments[5] = irNull()
        // Deep-copy before transforming: `transform` mutates the tree in place, and the
        // same statement IR is shared across an inherited feature's parent context and each
        // subclass fake override. Mutating the shared original corrupts sibling rewrites.
        arguments[6] = conditionValueRecorder.transform(statement.deepCopyWithSymbols())
      }
    }
  }

  fun irConditionFailedWithException(
    builder: DeclarationIrBuilder,
    irValueRecorder: IrValueRecorder,
    errorCollectorVar: IrVariable,
    statement: IrExpression,
    file: IrFile,
    conditionThrowable: IrVariable
  ): IrCall {
    val conditionFailedWithException = spockRuntimeClassSymbol.functionByName("conditionFailedWithException")
    val startOffset = statement.effectiveStartOffset()
    val line = file.fileEntry.getLineNumber(startOffset) + 1
    val column = file.fileEntry.getColumnNumber(startOffset) + 1
    return with(builder) {
      irCall(conditionFailedWithException).apply {
        arguments[0] = irGet(errorCollectorVar)
        arguments[1] = irValueRecorder.irGet(builder)
        arguments[2] = irStatementText(builder, statement, file)
        arguments[3] = line.toIrConst(context.irBuiltIns.intType)
        arguments[4] = column.toIrConst(context.irBuiltIns.intType)
        arguments[5] = irNull()
        arguments[6] = irGet(conditionThrowable)
      }
    }
  }

  private fun irStatementText(builder: DeclarationIrBuilder, statement: IrExpression, file: IrFile): IrConst = try {
    val startOffset =
      if (statement.isAssertCall()) statement.effectiveStartOffset() + "assert".length else statement.effectiveStartOffset()
    sourceTextCache.get(file)
      .substring(startOffset, statement.endOffset)
      .toIrConst(builder.context.irBuiltIns.stringType)
  } catch (_: Exception) {
    builder.irNull()
  }

  // For a bare top-level call (e.g. `str.startsWith("xyz")`), Kotlin offsets the IrCall at the
  // callee, not the receiver, so walk into receivers to find the condition's true start.
  @OptIn(UnsafeDuringIrConstructionAPI::class)
  private fun IrExpression.effectiveStartOffset(): Int = when (this) {
    is IrTypeOperatorCall -> argument.effectiveStartOffset()

    is IrCall ->
      symbol.owner.parameters
        .asSequence()
        .withIndex()
        .filter { (_, parameter) -> parameter.kind == IrParameterKind.DispatchReceiver || parameter.kind == IrParameterKind.ExtensionReceiver }
        .fold(startOffset) { minOffset, (index, _) -> minOf(minOffset, arguments[index]?.effectiveStartOffset() ?: minOffset) }

    else -> startOffset
  }

  companion object {
    fun create(generatorContext: IrGeneratorContext, sourceTextCache: SourceTextCache): IrSpockRuntime {
      val spockRuntimeClass = generatorContext.findRequiredClassSymbol(SPOCK_RUNTIME_FQN)
      return IrSpockRuntime(spockRuntimeClass, sourceTextCache)
    }
  }
}
