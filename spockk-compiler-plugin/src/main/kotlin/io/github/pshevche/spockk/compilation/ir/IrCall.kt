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

package io.github.pshevche.spockk.compilation.ir

import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

/**
 * Arguments by parameter kind. `IrCall.arguments` is one flat list covering receivers and value
 * arguments alike, positioned by the callee's own parameter list, so reading an argument means
 * finding its parameter first.
 */
internal fun IrCall.extensionReceiverArg(): IrExpression? = argumentForParameterKind(IrParameterKind.ExtensionReceiver)

internal fun IrCall.dispatchReceiverArg(): IrExpression? = argumentForParameterKind(IrParameterKind.DispatchReceiver)

internal fun IrCall.regularArgs(): List<IrExpression> =
  symbol.owner.parameters.withIndex()
    .filter { (_, parameter) -> parameter.kind == IrParameterKind.Regular }
    .mapNotNull { (index, _) -> arguments[index] }

internal fun IrCall.singleRegularArg(): IrExpression? = regularArgs().singleOrNull()

private fun IrCall.argumentForParameterKind(kind: IrParameterKind): IrExpression? {
  val index = symbol.owner.parameters.indexOfFirst { it.kind == kind }
  return if (index >= 0) arguments[index] else null
}
