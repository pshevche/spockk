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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.ERROR_COLLECTOR_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class IrErrorCollector private constructor(
  private val errorCollectorClassSymbol: IrClassSymbol,
  private val errorCollectorVar: IrVariable
) {

  fun irGet(builder: DeclarationIrBuilder): IrGetValue = builder.irGet(errorCollectorVar)

  fun irValidateCollectedErrors(builder: DeclarationIrBuilder): IrCall {
    val validateCollectedErrors = errorCollectorClassSymbol.functionByName("validateCollectedErrors")
    return builder.irCall(validateCollectedErrors).apply {
      dispatchReceiver = irGet(builder)
    }
  }

  companion object {
    fun create(generatorContext: IrGeneratorContext, errorCollectorVar: IrVariable): IrErrorCollector {
      val errorCollectorClassSymbol = generatorContext.findRequiredClassSymbol(ERROR_COLLECTOR_FQN)
      return IrErrorCollector(errorCollectorClassSymbol, errorCollectorVar)
    }
  }
}
