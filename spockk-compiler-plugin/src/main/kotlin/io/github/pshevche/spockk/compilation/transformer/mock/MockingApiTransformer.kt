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

@file:OptIn(InternalSymbolFinderAPI::class)

package io.github.pshevche.spockk.compilation.transformer.mock

import io.github.pshevche.spockk.compilation.common.BaseSpockkIrElementTransformer
import io.github.pshevche.spockk.compilation.common.SpockkConstants
import io.github.pshevche.spockk.compilation.ir.findPropertyGetter
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.isSubtypeOf
import org.jetbrains.kotlin.name.Name
import java.util.stream.Collectors

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class MockingApiTransformer(
  override val context: IrGeneratorContext,
  private val spec: IrClass
) : BaseSpockkIrElementTransformer(),
  SpockkIrRewriter {

  private val specInternalsClass =
    context.findRequiredClassSymbol(SpockkConstants.SPEC_INTERNALS_CLASS_ID)
  private val kClassJavaPropGetter =
    context.findPropertyGetter(SpockkConstants.KCLASS_JAVA_PROPERTY_ID)

  companion object {

    private val MOCK_METHODS: Map<Name, Name> =
      setOf("Mock", "Stub", "Spy")
        .stream()
        .collect(
          Collectors.toMap({ f -> Name.identifier(f) }, { f -> Name.identifier(f + "Impl") })
        )
  }

  fun rewriteMockingApi() {
    spec.declarations.forEach { declaration ->
      if (declaration is IrFunction || declaration is IrProperty) {
        declaration.accept(this, null)
      }
    }
  }

  override fun visitVariable(declaration: IrVariable): IrStatement {
    // We are searching for
    // var name:Type = Mock(<any-args>)
    // which will match the Mock() call as initializer, and left-hand-side as variable.
    var init = declaration.initializer
    if (init is IrTypeOperatorCall) {
      // This shall match/skip stuff like !! or implicit null checks after the Mock() initializer
      // var name = Mock(Runnable::class.java)!!
      init = init.argument
    }
    if (init is IrCall) {
      processCall(init, declaration)
      return declaration // We already processed the initializer so do not continue with the
      // children
    }
    return super.visitVariable(declaration)
  }

  override fun visitCall(expression: IrCall): IrExpression {
    processCall(expression, null)
    return super.visitCall(expression)
  }

  private fun processCall(call: IrCall, variable: IrVariable?) {
    val owner = call.symbol.owner
    val methodName = owner.name
    val mockMethodImplName = MOCK_METHODS[methodName]
    if (mockMethodImplName != null) {
      val parent = owner.parent
      if (parent == spec) {
        val implArgCount =
          call.arguments.size +
            2 // We need two more arguments for String inferredName, Type inferredType, see
        // SpecInternals Spock class
        val mockImplMethod: IrSimpleFunction? =
          findMockImplMethod(mockMethodImplName, implArgCount, call)
        if (mockImplMethod != null) {
          rewriteMockCall(call, variable, mockImplMethod)
        }
      }
    }
  }

  private fun rewriteMockCall(
    expression: IrCall,
    variable: IrVariable?,
    mockImplMethod: IrSimpleFunction
  ) {
    irBuilder(expression.symbol).let {
      // inferredName argument
      expression.arguments.add(1, mockName(variable, it))
      // inferredType argument
      expression.arguments.add(2, inferMockType(variable, it))
      expression.symbol = mockImplMethod.symbol
    }
  }

  private fun inferMockType(variable: IrVariable?, builder: DeclarationIrBuilder): IrExpression {
    val classSym = variable?.type?.classOrNull
    if (variable == null || classSym == null) {
      return builder.irNull()
    }

    val call = builder.irCall(kClassJavaPropGetter.symbol, variable.type)
    call.arguments.clear()
    call.arguments.add(builder.kClassReference(classSym.defaultType))

    return call
  }

  private fun mockName(variable: IrVariable?, builder: DeclarationIrBuilder): IrConst {
    val mockName: String?
    if (variable != null) {
      mockName = variable.name.toString()
    } else {
      mockName = null
    }
    val inferredName = mockName?.let { builder.irString(it) } ?: builder.irNull()
    return inferredName
  }

  private fun findMockImplMethod(
    mockMethodImplName: Name,
    implArgCount: Int,
    call: IrCall
  ): IrSimpleFunction? {
    val ctx: IrTypeSystemContext = IrTypeSystemContextImpl(context.irBuiltIns)
    val mockImplMethod: IrSimpleFunction? =
      specInternalsClass.owner.findDeclaration { m: IrSimpleFunction ->
        if (m.name == mockMethodImplName && m.parameters.size == implArgCount) {
          // We ignore the first three arguments: Spec, inferredName and inferredType
          for (i in 3..<implArgCount) {
            val callArg =
              call.arguments[
                i - 2
              ] // We only skip two inferredName and inferredType, because the Spec
            // is passed as first argument.

            val callType = callArg?.type
            val methodParam = m.parameters[i]
            val paramType = methodParam.type
            if (callType != null && !callType.isSubtypeOf(paramType, ctx)) {
              return@findDeclaration false
            }
          }
          return@findDeclaration true
        }
        false
      }
    return mockImplMethod
  }
}
