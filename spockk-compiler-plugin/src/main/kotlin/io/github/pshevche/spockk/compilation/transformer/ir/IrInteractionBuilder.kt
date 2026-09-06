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
import io.github.pshevche.spockk.compilation.ir.irImplicitNotNull
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.functions

/**
 * Wraps `org.spockframework.mock.runtime.InteractionBuilder`, a real Spock class shaded unmodified
 * into `spockk-core` - one `irXxx` method per fluent method the rewriter needs, each returning the
 * chain's `IrCall` so calls can be nested (`receiver` is always the previous link in the chain).
 */
internal class IrInteractionBuilder private constructor(
  private val interactionBuilderClassSymbol: IrClassSymbol
) {

  fun irNew(builder: IrBuilderWithScope, line: IrExpression, column: IrExpression, text: IrExpression): IrConstructorCall {
    val constructor = interactionBuilderClassSymbol.constructors.first()
    return builder.irCallConstructor(constructor, listOf()).apply {
      arguments[0] = line
      arguments[1] = column
      arguments[2] = text
    }
  }

  fun irSetFixedCount(builder: IrBuilderWithScope, receiver: IrExpression, count: IrExpression): IrCall =
    chainedCall(builder, "setFixedCount", receiver) { arguments[1] = count }

  fun irSetRangeCount(
    builder: IrBuilderWithScope,
    receiver: IrExpression,
    minCount: IrExpression,
    maxCount: IrExpression,
    inclusive: IrExpression
  ): IrCall = chainedCall(builder, "setRangeCount", receiver) {
    arguments[1] = minCount
    arguments[2] = maxCount
    arguments[3] = inclusive
  }

  fun irAddEqualTarget(builder: IrBuilderWithScope, receiver: IrExpression, target: IrExpression): IrCall =
    chainedCall(builder, "addEqualTarget", receiver) { arguments[1] = target }

  fun irAddEqualMethodName(builder: IrBuilderWithScope, receiver: IrExpression, name: IrExpression): IrCall =
    chainedCall(builder, "addEqualMethodName", receiver) { arguments[1] = name }

  // Real Spock: InteractionBuilder.argConstraints/argNames stay null - NullPointerException on the
  // first addEqualArg/addCodeArg - until setArgListKind(isPositional, isMixed) initializes them;
  // Spockk never supports named args, so this is always setArgListKind(true, false) (positional,
  // not mixed). Two overloads share this name (a 1-arg one defaults isMixed to false), so it's
  // looked up by parameter count rather than functionByName's single-match assumption.
  fun irSetArgListKind(builder: IrBuilderWithScope, receiver: IrExpression): IrFunctionAccessExpression {
    val setArgListKind = interactionBuilderClassSymbol.owner.functions.first {
      it.name.asString() == "setArgListKind" && it.parameters.size == 3
    }
    return with(builder) {
      irCall(setArgListKind.symbol).apply {
        dispatchReceiver = nonNullReceiver(builder, receiver)
        arguments[1] = irBoolean(true)
        arguments[2] = irBoolean(false)
      }
    }
  }

  fun irAddEqualArg(builder: IrBuilderWithScope, receiver: IrExpression, arg: IrExpression): IrCall =
    chainedCall(builder, "addEqualArg", receiver) { arguments[1] = arg }

  fun irAddConstantResponse(builder: IrBuilderWithScope, receiver: IrExpression, constant: IrExpression): IrCall =
    chainedCall(builder, "addConstantResponse", receiver) { arguments[1] = constant }

  fun irAddCodeResponse(builder: IrBuilderWithScope, receiver: IrExpression, closure: IrExpression): IrCall =
    chainedCall(builder, "addCodeResponse", receiver) { arguments[1] = closure }

  fun irBuild(builder: IrBuilderWithScope, receiver: IrExpression): IrCall =
    chainedCall(builder, "build", receiver) { }

  private fun chainedCall(
    builder: IrBuilderWithScope,
    methodName: String,
    receiver: IrExpression,
    configureArgs: IrCall.() -> Unit
  ): IrCall {
    val function = interactionBuilderClassSymbol.functionByName(methodName)
    return with(builder) {
      irCall(function).apply {
        dispatchReceiver = nonNullReceiver(builder, receiver)
        configureArgs()
      }
    }
  }

  // Every InteractionBuilder fluent method returns a Java platform type (InteractionBuilder!) -
  // dereferencing it to call the next link in the chain is exactly what real hand-written Kotlin
  // source does implicitly (`x.setFixedCount(1).addEqualTarget(...)`  inserts this same coercion at
  // every `.`), so this matches what the frontend would produce rather than skipping the coercion
  // this raw IR construction would otherwise silently omit. The constructor call starting the chain
  // ([irNew]) is already non-null, so it's passed through unchanged.
  private fun nonNullReceiver(builder: IrBuilderWithScope, receiver: IrExpression): IrExpression =
    if (receiver.type.isNullable()) {
      builder.irImplicitNotNull(receiver, interactionBuilderClassSymbol.defaultType)
    } else {
      receiver
    }

  companion object {
    fun create(generatorContext: IrGeneratorContext): IrInteractionBuilder {
      val interactionBuilderClass = generatorContext.findRequiredClassSymbol(INTERACTION_BUILDER_FQN)
      return IrInteractionBuilder(interactionBuilderClass)
    }
  }
}
