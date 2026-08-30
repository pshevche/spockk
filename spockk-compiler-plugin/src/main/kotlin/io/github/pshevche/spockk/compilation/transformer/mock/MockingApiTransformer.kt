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

@file:OptIn(InternalSymbolFinderAPI::class)

package io.github.pshevche.spockk.compilation.transformer.mock

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers
import io.github.pshevche.spockk.compilation.ir.findPropertyGetter
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.ir.nestedStatementLists
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.shared.BaseSpockkIrElementTransformer
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.interaction.InteractionStatementsRewriter
import io.github.pshevche.spockk.compilation.transformer.interaction.asInteractionStatement
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isSubtypeOf
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class MockingApiTransformer(
  override val rewriterContext: SpockkIrRewriterContext,
  private val spec: IrClass
) : BaseSpockkIrElementTransformer(),
  SpockkIrRewriter {

  private val specInternalsClass =
    rewriterContext.findRequiredClassSymbol(IrIdentifiers.Spock.SPEC_INTERNALS_FQN)
  private val kClassJavaPropGetter =
    rewriterContext.findPropertyGetter(IrIdentifiers.Kotlin.KCLASS_JAVA_CALLABLE_ID)

  // Interaction statements built from a Mock/Stub builder block's trailing lambda, spliced into the
  // declaring function right after the mock's own IrVariable, in a deliberate second pass (a
  // BaseSpockkIrElementTransformer visit can only replace the node it's at, not insert siblings).
  private val pendingInteractionSplices = mutableListOf<PendingInteractionSplice>()

  private class PendingInteractionSplice(val function: IrFunction, val variable: IrVariable, val statements: List<IrStatement>) {
    // A hoisted mock declaration (irTryHoistingVariables) has no initializer of its own anymore -
    // the real Mock()/Stub() assignment happens at a SET_VAR elsewhere. Splicing after the now-bare
    // declaration instead would run the interaction before the mock is actually assigned.
    fun matches(statement: IrStatement): Boolean =
      if (variable.initializer != null) statement === variable else statement is IrSetValue && statement.symbol == variable.symbol
  }

  companion object {

    private val MOCK_METHODS: Map<Name, Name> = setOf("Mock", "Stub", "Spy")
      .associate { Name.identifier(it) to Name.identifier(it + "Impl") }

    private val BUILDER_BLOCK_MOCK_METHODS = mapOf(
      IrIdentifiers.Spockk.MOCK_BUILDER_BLOCK_FQN to Name.identifier("MockImpl"),
      IrIdentifiers.Spockk.STUB_BUILDER_BLOCK_FQN to Name.identifier("StubImpl"),
      IrIdentifiers.Spockk.SPY_BUILDER_BLOCK_FQN to Name.identifier("SpyImpl")
    )
  }

  fun rewrite() {
    spec.declarations.forEach { declaration ->
      if (declaration is IrFunction || declaration is IrProperty) {
        declaration.accept(this, null)
      }
    }
    if (pendingInteractionSplices.isNotEmpty()) {
      spliceInteractionStatements()
    }
  }

  private fun spliceInteractionStatements() {
    pendingInteractionSplices.groupBy { it.function }.forEach { (function, splices) ->
      val statements = function.mutableStatements() ?: return@forEach
      val unconsumed = splices.toMutableList()
      spliceInto(statements, unconsumed)
      if (unconsumed.isNotEmpty()) {
        // A Mock/Stub builder block whose declaration isn't found in any statement list reachable
        // from its own function (nested inside an if/for/when-expression/etc.) - not silently
        // dropping the interactions it configured, since that would leave a stub quietly answering
        // with defaults instead of what the block actually declared.
        throw CompilationException(
          "Mock/Stub builder block interactions must be declared as a statement of a feature or fixture method body",
          spec.file,
          unconsumed.first().variable
        )
      }
    }
  }

  // Splices each pending mock's built interaction statements in right after its own declaration,
  // wherever that's actually found - not just this list's own top-level statements, but recursively
  // into every nested statement list reachable from it (a WhenBlockRewriter/InteractionScopeRewriter
  // try/catch around a paired then:'s exception condition, or CleanupBlockRewriter's try/finally
  // around the whole feature body when a cleanup: block is present, both nest the given: block's
  // own statements one level deeper before this splice pass ever runs).
  private fun spliceInto(statements: MutableList<IrStatement>, pending: MutableList<PendingInteractionSplice>) {
    if (pending.isEmpty()) return
    val rewritten = statements.flatMap { statement ->
      statement.nestedStatementLists().forEach { spliceInto(it, pending) }
      val splice = pending.firstOrNull { it.matches(statement) }
      if (splice != null) {
        pending.remove(splice)
        listOf(statement) + splice.statements
      } else {
        listOf(statement)
      }
    }
    statements.clear()
    statements.addAll(rewritten)
  }

  override fun visitVariable(declaration: IrVariable): IrStatement {
    // We are searching for
    // var name:Type = Mock(<any-args>)
    // which will match the Mock() call as initializer, and left-hand-side as variable.
    if (tryRewriteMockInitializer(declaration.initializer, declaration)) {
      return declaration // We already processed the initializer so do not continue with the children
    }
    return super.visitVariable(declaration)
  }

  // A hoisted mock declaration (irTryHoistingVariables splits `val name = Mock(...)` into a bare
  // `var name` plus a SET_VAR elsewhere, when e.g. a cleanup: block reads the mock) has no
  // initializer of its own to match here - the Mock()/Stub() call shows up as a SET_VAR's value
  // instead, so it needs the same detection.
  override fun visitSetValue(expression: IrSetValue): IrExpression {
    val variable = expression.symbol.owner as? IrVariable
    if (variable != null && tryRewriteMockInitializer(expression.value, variable)) {
      return expression
    }
    return super.visitSetValue(expression)
  }

  private fun tryRewriteMockInitializer(initExpr: IrExpression?, variable: IrVariable): Boolean {
    var init = initExpr
    if (init is IrTypeOperatorCall) {
      // This shall match/skip stuff like !! or implicit null checks after the Mock() initializer
      // var name = Mock(Runnable::class.java)!!
      init = init.argument
    }
    if (init !is IrCall) return false
    val builderBlockImplName = BUILDER_BLOCK_MOCK_METHODS[init.symbol.owner.fqNameWhenAvailable]
    if (builderBlockImplName != null) {
      rewriteBuilderBlockMockDeclaration(variable, init, builderBlockImplName)
    } else {
      processCall(init, variable)
    }
    return true
  }

  private fun rewriteBuilderBlockMockDeclaration(declaration: IrVariable, call: IrCall, mockImplMethodName: Name) {
    val blockArg = call.arguments.removeAt(call.arguments.lastIndex) as? IrFunctionExpression
      ?: throw CompilationException(
        "Mock/Stub builder block must be a literal lambda (`Mock(Type::class.java) { ... }`)",
        spec.file,
        call
      )
    // Unlike the inherited 1-arg Mock(Class)/Stub(Class) member, this 2-arg overload has no dispatch
    // receiver of its own - findMockImplMethod/rewriteMockCall assume `call.arguments` starts with
    // the spec instance, matching MockImpl's (Specification, name, Type, Class) signature.
    call.arguments.add(0, irBuilder(call.symbol).irGet(currentIrFunction.requiredThisParameter()))

    val mockImplMethod = findMockImplMethod(mockImplMethodName, mockImplArgCount(call), call) ?: return
    rewriteMockCall(call, declaration, mockImplMethod)

    val lambdaStatements = blockArg.function.mutableStatements() ?: return
    val lambdaBuilder = irBuilder(blockArg.function.symbol)
    val interactionRewriter = InteractionStatementsRewriter(rewriterContext, currentIrFunction)
    val lambdaReceiverParam = blockArg.function.parameters.first { it.kind == IrParameterKind.ExtensionReceiver }

    val builtStatements = lambdaStatements.flatMap { statement ->
      // The block's own statements reference the mock via their implicit T receiver, valid only
      // inside the lambda - rebind those references to the mock's own variable before moving the
      // statements out into the enclosing function, where that receiver no longer exists.
      val rebound = (statement as? IrExpression)?.rebindToVariable(lambdaReceiverParam, declaration, lambdaBuilder) ?: statement
      val interaction = rebound.asInteractionStatement(allowBareCall = true)
        ?: throw CompilationException(
          "Every statement in a Mock/Stub builder block must be an interaction statement (a call on the mock, " +
            "optionally wrapped in does/did/returns/returned)",
          spec.file,
          statement
        )
      interactionRewriter.rewrite(interaction)
    }
    if (builtStatements.isNotEmpty()) {
      pendingInteractionSplices += PendingInteractionSplice(currentIrFunction, declaration, builtStatements)
    }
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
        val mockImplMethod: IrSimpleFunction? =
          findMockImplMethod(mockMethodImplName, mockImplArgCount(call), call)
        if (mockImplMethod != null) {
          rewriteMockCall(call, variable, mockImplMethod)
        }
      }
    }
  }

  // MockImpl/StubImpl (SpecInternals) always take two more arguments than the user-facing call: the
  // inferred String name and Type, see SpecInternals.
  private fun mockImplArgCount(call: IrCall): Int = call.arguments.size + 2

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
    val ctx: IrTypeSystemContext = IrTypeSystemContextImpl(rewriterContext.irBuiltIns)
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

// Rebinds a Mock/Stub builder block's implicit-receiver references (valid only inside the block's
// own lambda) to the mock's IrVariable, so the statements still resolve once moved into the caller.
@OptIn(UnsafeDuringIrConstructionAPI::class)
private fun IrExpression.rebindToVariable(
  receiverParam: IrValueParameter,
  target: IrVariable,
  builder: DeclarationIrBuilder
): IrExpression {
  val rebinder = object : IrElementTransformerVoid() {
    override fun visitGetValue(expression: IrGetValue): IrExpression {
      if (expression.symbol == receiverParam.symbol) {
        return builder.irGet(target)
      }
      return super.visitGetValue(expression)
    }
  }
  return transform(rebinder, null)
}
