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

package io.github.pshevche.spockk.compilation.transformer.interaction

import io.github.pshevche.spockk.compilation.transformer.ir.IrInteractionBuilder
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.expressions.IrExpression

/**
 * Accumulates one `new InteractionBuilder(...).setFixedCount(1).addEqualTarget(...)...build()`
 * expression, so callers describe an interaction step by step instead of threading the growing
 * expression through every call themselves.
 */
internal class InteractionChain(
  private val interactionBuilder: IrInteractionBuilder,
  private val builder: DeclarationIrBuilder,
  line: Int,
  column: Int,
  text: IrExpression
) {

  private var chain: IrExpression =
    interactionBuilder.irNew(builder, builder.irInt(line), builder.irInt(column), text)

  fun fixedCount(count: IrExpression) {
    chain = interactionBuilder.irSetFixedCount(builder, chain, count)
  }

  // Kotlin's `..` and `..<` both produce an inclusive IntRange (`..<` has already decremented its
  // `last`), so the range this reads its bounds from is never exclusive.
  fun rangeCount(minCount: IrExpression, maxCount: IrExpression) {
    chain = interactionBuilder.irSetRangeCount(builder, chain, minCount, maxCount, builder.irBoolean(true))
  }

  fun equalTarget(target: IrExpression) {
    chain = interactionBuilder.irAddEqualTarget(builder, chain, target)
  }

  fun equalMethodName(name: String) {
    chain = interactionBuilder.irAddEqualMethodName(builder, chain, builder.irString(name))
  }

  /**
   * Declares the argument list positional, which also initializes it: `InteractionBuilder` leaves
   * its constraint list null until this is called, so the first argument added without it throws.
   * Required even when no argument follows.
   */
  fun positionalArgList() {
    chain = interactionBuilder.irSetArgListKind(builder, chain)
  }

  fun equalArg(arg: IrExpression) {
    chain = interactionBuilder.irAddEqualArg(builder, chain, arg)
  }

  fun constantResponse(value: IrExpression) {
    chain = interactionBuilder.irAddConstantResponse(builder, chain, value)
  }

  fun codeResponse(closure: IrExpression) {
    chain = interactionBuilder.irAddCodeResponse(builder, chain, closure)
  }

  fun build(): IrExpression = interactionBuilder.irBuild(builder, chain)
}
