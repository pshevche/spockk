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
import org.jetbrains.kotlin.ir.expressions.IrContainerExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrTry
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

  // Interaction statements built from a Mock/Stub builder block's trailing lambda, keyed by the
  // IrVariable the mock is declared into - spliced into that variable's enclosing function's
  // top-level statement list right after rewrite() finishes visiting every declaration (a
  // BaseSpockkIrElementTransformer visit can only replace the node it's currently at, not insert
  // siblings, so the splice is a deliberate second pass rather than happening inline).
  private val pendingInteractionSplices = mutableListOf<Pair<IrVariable, List<IrStatement>>>()

  companion object {

    private val MOCK_METHODS: Map<Name, Name> = setOf("Mock", "Stub", "Spy")
      .associate { Name.identifier(it) to Name.identifier(it + "Impl") }

    private val BUILDER_BLOCK_MOCK_METHODS = mapOf(
      IrIdentifiers.Spockk.MOCK_BUILDER_BLOCK_FQN to Name.identifier("MockImpl"),
      IrIdentifiers.Spockk.STUB_BUILDER_BLOCK_FQN to Name.identifier("StubImpl")
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
    val unconsumed = pendingInteractionSplices.toMutableList()
    spec.declarations.filterIsInstance<IrFunction>().forEach { function ->
      val statements = function.mutableStatements() ?: return@forEach
      spliceInto(statements, unconsumed)
    }
    if (unconsumed.isNotEmpty()) {
      // A Mock/Stub builder block whose declaration isn't found in any statement list reachable
      // from its own function (nested inside an if/for/when-expression/etc.) - not silently
      // dropping the interactions it configured, since that would leave a stub quietly answering
      // with defaults instead of what the block actually declared.
      throw CompilationException(
        "Mock/Stub builder block interactions must be declared as a statement of a feature or fixture method body",
        spec.file,
        unconsumed.first().first
      )
    }
  }

  // Splices each pending (declaration, builtStatements) pair in right after that declaration,
  // wherever it's actually found - not just this list's own top-level statements, but recursively
  // into every nested statement list reachable from it (a WhenBlockRewriter/InteractionScopeRewriter
  // try/catch around a paired then:'s exception condition, or CleanupBlockRewriter's try/finally
  // around the whole feature body when a cleanup: block is present, both nest the given: block's
  // own statements - and therefore a Mock/Stub declaration in it - one level deeper before this
  // splice pass ever runs).
  private fun spliceInto(statements: MutableList<IrStatement>, pending: MutableList<Pair<IrVariable, List<IrStatement>>>) {
    if (pending.isEmpty()) return
    val rewritten = statements.flatMap { statement ->
      nestedStatementLists(statement).forEach { spliceInto(it, pending) }
      val splice = pending.firstOrNull { it.first === statement }
      if (splice != null) {
        pending.remove(splice)
        listOf(statement) + splice.second
      } else {
        listOf(statement)
      }
    }
    statements.clear()
    statements.addAll(rewritten)
  }

  private fun nestedStatementLists(statement: IrStatement): List<MutableList<IrStatement>> = when (statement) {
    is IrTry -> buildList {
      (statement.tryResult as? IrContainerExpression)?.let { add(it.statements) }
      statement.catches.forEach { (it.result as? IrContainerExpression)?.let { result -> add(result.statements) } }
      (statement.finallyExpression as? IrContainerExpression)?.let { add(it.statements) }
    }

    is IrContainerExpression -> listOf(statement.statements)

    else -> emptyList()
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
      val builderBlockImplName = BUILDER_BLOCK_MOCK_METHODS[init.symbol.owner.fqNameWhenAvailable]
      if (builderBlockImplName != null) {
        rewriteBuilderBlockMockDeclaration(declaration, init, builderBlockImplName)
      } else {
        processCall(init, declaration)
      }
      return declaration // We already processed the initializer so do not continue with the
      // children
    }
    return super.visitVariable(declaration)
  }

  private fun rewriteBuilderBlockMockDeclaration(declaration: IrVariable, call: IrCall, mockImplMethodName: Name) {
    val blockArg = call.arguments.removeAt(call.arguments.lastIndex) as? IrFunctionExpression
      ?: throw CompilationException(
        "Mock/Stub builder block must be a literal lambda (`Mock(Type::class.java) { ... }`)",
        spec.file,
        call
      )
    // Unlike the inherited 1-arg Mock(Class)/Stub(Class) member (whose own dispatch receiver already
    // supplies this slot), this 2-arg overload is a plain top-level function with no receiver of its
    // own - findMockImplMethod/rewriteMockCall both assume `call.arguments` already starts with the
    // spec instance, matching MockImpl's own (Specification, name, Type, Class) signature.
    call.arguments.add(0, irBuilder(call.symbol).irGet(currentIrFunction.requiredThisParameter()))

    val implArgCount = call.arguments.size + 2
    val mockImplMethod = findMockImplMethod(mockImplMethodName, implArgCount, call) ?: return
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
      pendingInteractionSplices += declaration to builtStatements
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

// A Mock/Stub builder block's own statements reference the mock via [receiverParam] (the block
// lambda's own extension receiver parameter - named "$this$Stub"/"$this$Mock" by convention, *not*
// "<this>", which only applies to an ordinary member function's dispatch receiver), valid only
// inside that lambda. Rebinds every such reference to `target` instead, mirroring
// io.github.pshevche.spockk.compilation.ir.rebindDispatchReceiverReferences (kept separate since
// that helper matches by the "<this>" name, and its target is typed as an IrValueParameter, not the
// IrVariable a mock is declared into).
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
