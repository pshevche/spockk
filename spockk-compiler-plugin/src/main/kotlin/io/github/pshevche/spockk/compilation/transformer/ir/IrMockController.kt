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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.MOCK_CONTROLLER_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

/**
 * Wraps `org.spockframework.mock.runtime.MockController`, a real Spock class shaded unmodified into
 * `spockk-core` - the same class that already backs every `Mock()`/`Stub()` call today.
 * `SpecificationContext.getMockController()` declares its return type as the narrower
 * `IMockController` interface, which only declares `handle(IMockInvocation)`;
 * `addInteraction`/`enterScope`/`leaveScope` are only on this concrete class, so
 * [IrSpecificationContext.irGetMockController] already casts to it before an [IrExpression] reaches
 * the methods below.
 */
internal class IrMockController private constructor(
  private val mockControllerClassSymbol: IrClassSymbol
) {

  fun irAddInteraction(builder: IrBuilder, controller: IrExpression, interaction: IrExpression): IrCall {
    val addInteraction = mockControllerClassSymbol.functionByName("addInteraction")
    return with(builder) {
      irCall(addInteraction).apply {
        dispatchReceiver = controller
        arguments[1] = interaction
      }
    }
  }

  fun irEnterScope(builder: IrBuilder, controller: IrExpression): IrCall {
    val enterScope = mockControllerClassSymbol.functionByName("enterScope")
    return with(builder) {
      irCall(enterScope).apply { dispatchReceiver = controller }
    }
  }

  fun irLeaveScope(builder: IrBuilder, controller: IrExpression): IrCall {
    val leaveScope = mockControllerClassSymbol.functionByName("leaveScope")
    return with(builder) {
      irCall(leaveScope).apply { dispatchReceiver = controller }
    }
  }

  companion object {
    fun create(generatorContext: IrGeneratorContext): IrMockController {
      val mockControllerClass = generatorContext.findRequiredClassSymbol(MOCK_CONTROLLER_FQN)
      return IrMockController(mockControllerClass)
    }
  }
}
