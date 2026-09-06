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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.MOCK_CONTROLLER_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPECIFICATION_CONTEXT_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class IrSpecificationContext(
  rewriterContext: SpockkIrRewriterContext,
  private val spec: IrClass
) {
  private val specificationContextClass: IrClassSymbol =
    rewriterContext.findRequiredClassSymbol(SPECIFICATION_CONTEXT_FQN)
  private val mockControllerClass: IrClassSymbol =
    rewriterContext.findRequiredClassSymbol(MOCK_CONTROLLER_FQN)

  fun irGetCurrentBlock(builder: IrBuilder, specAccessor: IrValueParameter): IrCall {
    val specificationContextInstance = getSpecificationContextInstance(builder, specAccessor)
    val getCurrentBlock = specificationContextClass.functionByName("getCurrentBlock")
    return with(builder) {
      irCall(getCurrentBlock).apply {
        dispatchReceiver = specificationContextInstance
      }
    }
  }

  fun irSetCurrentBlock(builder: IrBuilder, specAccessor: IrValueParameter, failedBlockVar: IrVariable): IrExpression {
    val specificationContextInstance = getSpecificationContextInstance(builder, specAccessor)
    val setCurrentBlock = specificationContextClass.functionByName("setCurrentBlock")
    return with(builder) {
      irCall(setCurrentBlock).apply {
        dispatchReceiver = specificationContextInstance
        arguments[1] = irGet(failedBlockVar)
      }
    }
  }

  fun irSetThrownException(builder: IrBuilder, specAccessor: IrValueParameter, exceptionExpr: IrExpression): IrExpression {
    val specificationContextInstance = getSpecificationContextInstance(builder, specAccessor)
    val setThrownException = specificationContextClass.functionByName("setThrownException")
    return with(builder) {
      irCall(setThrownException).apply {
        dispatchReceiver = specificationContextInstance
        arguments[1] = exceptionExpr
      }
    }
  }

  // SpecificationContext.getMockController() declares its return type as the narrower
  // IMockController interface, which only declares handle(IMockInvocation) - the concrete
  // MockController class (already resolved above) is what actually declares
  // addInteraction/enterScope/leaveScope, so callers need the cast this performs.
  fun irGetMockController(builder: IrBuilder, specAccessor: IrValueParameter): IrExpression {
    val specificationContextInstance = getSpecificationContextInstance(builder, specAccessor)
    val getMockController = specificationContextClass.functionByName("getMockController")
    val call = with(builder) {
      irCall(getMockController).apply {
        dispatchReceiver = specificationContextInstance
      }
    }
    return builder.irAs(call, mockControllerClass.defaultType)
  }

  fun irGetSharedInstance(
    builder: IrBuilder,
    specAccessor: IrValueParameter
  ): IrExpression {
    val specificationContextInstance =
      getSpecificationContextInstance(
        builder,
        specAccessor,
        IrStatementOrigin.IMPLICIT_ARGUMENT,
        IrStatementOrigin.GET_PROPERTY
      )
    val getSharedInstance = specificationContextClass.functionByName("getSharedInstance")
    return with(builder) {
      irCall(getSharedInstance).apply {
        dispatchReceiver = specificationContextInstance
        origin = IrStatementOrigin.GET_PROPERTY
      }
    }
  }

  private fun getSpecificationContextInstance(
    builder: IrBuilder,
    specAccessor: IrValueParameter,
    dispatchReceiverOrigin: IrStatementOrigin? = null,
    callOrigin: IrStatementOrigin? = null
  ): IrExpression =
    with(builder) {
      val getSpecificationContextInstanceCall = irCall(spec.getSimpleFunction("getSpecificationContext")!!).apply {
        dispatchReceiver = irGet(specAccessor).apply {
          origin = dispatchReceiverOrigin
        }
        origin = callOrigin
      }
      return irAs(getSpecificationContextInstanceCall, specificationContextClass.defaultType)
    }
}
