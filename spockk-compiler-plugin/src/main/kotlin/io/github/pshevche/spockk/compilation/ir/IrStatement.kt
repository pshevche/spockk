/*
 * Copyright 2025 the original author or authors.
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

import io.github.pshevche.spockk.compilation.common.FeatureBlockLabelIrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable

internal fun IrStatement.asIrBlockLabel(file: IrFile): FeatureBlockLabelIrElement? =
  when (this) {
    is IrTypeOperatorCall -> (this.argument as? IrGetObjectValue)?.asIrBlockLabel(file)
    is IrCall -> asIrBlockLabel(file)
    else -> null
  }

private fun IrGetObjectValue.asIrBlockLabel(file: IrFile): FeatureBlockLabelIrElement? =
  FeatureBlockLabelIrElement.from(file, this)

internal fun IrGetObjectValue.requiredFqn() = symbol.owner.fqNameWhenAvailable!!.asString()

private fun IrCall.asIrBlockLabel(file: IrFile): FeatureBlockLabelIrElement? =
  FeatureBlockLabelIrElement.from(file, this)

internal fun IrCall.requiredFqn() = symbol.owner.fqNameWhenAvailable!!.asString()
