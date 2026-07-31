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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.VALUE_RECORDER_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irCastTo
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.toIrConst

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class IrValueRecorder private constructor(
  private val valueRecorderClassSymbol: IrClassSymbol,
  private val valueRecorderVar: IrVariable
) {

  fun irGet(builder: DeclarationIrBuilder): IrGetValue = builder.irGet(valueRecorderVar)

  fun irReset(builder: DeclarationIrBuilder): IrCall {
    val reset = valueRecorderClassSymbol.functionByName("reset")
    return builder.irCall(reset).apply {
      dispatchReceiver = irGet(builder)
    }
  }

  fun irRecord(builder: DeclarationIrBuilder, idx: Int, expr: IrExpression): IrExpression {
    val record = valueRecorderClassSymbol.functionByName("record")
    val recordCall = builder.irCall(record).apply {
      dispatchReceiver = irGet(builder)
      arguments[1] = irStartRecordingValue(builder, idx)
      arguments[2] = expr
    }
    // record(...) is typed Any?; cast the result back to the recorded expression's type so the
    // recorded value can slot into statically typed positions (e.g. operands of typed operators
    // such as Int.plus or comparisons), which Kotlin's IR - unlike Groovy's AST - requires.
    return builder.irCastTo(recordCall, expr.type)
  }

  private fun irStartRecordingValue(builder: DeclarationIrBuilder, idx: Int): IrCall {
    val startRecordingValue = valueRecorderClassSymbol.functionByName("startRecordingValue")
    return builder.irCall(startRecordingValue).apply {
      dispatchReceiver = irGet(builder)
      arguments[1] = idx.toIrConst(builder.context.irBuiltIns.intType)
    }
  }

  companion object {
    fun create(generatorContext: IrGeneratorContext, valueRecorderVar: IrVariable): IrValueRecorder {
      val valueRecorderClassSymbol = generatorContext.findRequiredClassSymbol(VALUE_RECORDER_FQN)
      return IrValueRecorder(valueRecorderClassSymbol, valueRecorderVar)
    }
  }
}
