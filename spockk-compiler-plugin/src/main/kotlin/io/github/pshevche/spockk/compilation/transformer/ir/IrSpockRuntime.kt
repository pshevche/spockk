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
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol

internal class IrSpockRuntime private constructor(
  private val spockRuntimeClassSymbol: IrClassSymbol
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

  companion object {
    fun create(generatorContext: IrGeneratorContext): IrSpockRuntime {
      val spockRuntimeClass = generatorContext.findRequiredClassSymbol(SPOCK_RUNTIME_FQN)
      return IrSpockRuntime(spockRuntimeClass)
    }
  }
}
