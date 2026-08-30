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

package io.github.pshevche.spockk.compilation.transformer.ir

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.INTERACTION_BUILDER_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructors

/**
 * Wraps `org.spockframework.mock.runtime.InteractionBuilder`, a real Spock class shaded unmodified
 * into `spockk-core` - one `irXxx` method per fluent method the rewriter needs, each returning the
 * chain's `IrCall` so calls can be nested (`receiver` is always the previous link in the chain).
 */
internal class IrInteractionBuilder private constructor(
  private val interactionBuilderClassSymbol: IrClassSymbol
) {

  fun irNew(builder: IrBuilder, line: IrExpression, column: IrExpression, text: IrExpression): IrConstructorCall {
    val constructor = interactionBuilderClassSymbol.constructors.first()
    return builder.irCallConstructor(constructor, listOf()).apply {
      arguments[0] = line
      arguments[1] = column
      arguments[2] = text
    }
  }

  fun irSetFixedCount(builder: IrBuilder, receiver: IrExpression, count: IrExpression): IrCall =
    chainedCall(builder, "setFixedCount", receiver) { arguments[1] = count }

  fun irSetRangeCount(
    builder: IrBuilder,
    receiver: IrExpression,
    minCount: IrExpression,
    maxCount: IrExpression,
    inclusive: IrExpression
  ): IrCall = chainedCall(builder, "setRangeCount", receiver) {
    arguments[1] = minCount
    arguments[2] = maxCount
    arguments[3] = inclusive
  }

  fun irAddEqualTarget(builder: IrBuilder, receiver: IrExpression, target: IrExpression): IrCall =
    chainedCall(builder, "addEqualTarget", receiver) { arguments[1] = target }

  fun irAddEqualMethodName(builder: IrBuilder, receiver: IrExpression, name: IrExpression): IrCall =
    chainedCall(builder, "addEqualMethodName", receiver) { arguments[1] = name }

  fun irAddEqualArg(builder: IrBuilder, receiver: IrExpression, arg: IrExpression): IrCall =
    chainedCall(builder, "addEqualArg", receiver) { arguments[1] = arg }

  fun irAddCodeArg(builder: IrBuilder, receiver: IrExpression, closure: IrExpression): IrCall =
    chainedCall(builder, "addCodeArg", receiver) { arguments[1] = closure }

  fun irAddConstantResponse(builder: IrBuilder, receiver: IrExpression, constant: IrExpression): IrCall =
    chainedCall(builder, "addConstantResponse", receiver) { arguments[1] = constant }

  fun irAddCodeResponse(builder: IrBuilder, receiver: IrExpression, closure: IrExpression): IrCall =
    chainedCall(builder, "addCodeResponse", receiver) { arguments[1] = closure }

  fun irBuild(builder: IrBuilder, receiver: IrExpression): IrCall =
    chainedCall(builder, "build", receiver) { }

  private fun chainedCall(
    builder: IrBuilder,
    methodName: String,
    receiver: IrExpression,
    configureArgs: IrCall.() -> Unit
  ): IrCall {
    val function = interactionBuilderClassSymbol.functionByName(methodName)
    return with(builder) {
      irCall(function).apply {
        dispatchReceiver = receiver
        configureArgs()
      }
    }
  }

  companion object {
    fun create(generatorContext: IrGeneratorContext): IrInteractionBuilder {
      val interactionBuilderClass = generatorContext.findRequiredClassSymbol(INTERACTION_BUILDER_FQN)
      return IrInteractionBuilder(interactionBuilderClass)
    }
  }
}
