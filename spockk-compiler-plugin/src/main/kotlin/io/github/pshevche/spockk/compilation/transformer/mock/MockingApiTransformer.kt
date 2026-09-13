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

@file:OptIn(InternalSymbolFinderAPI::class, UnsafeDuringIrConstructionAPI::class)

package io.github.pshevche.spockk.compilation.transformer.mock

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Kotlin.KCLASS_JAVA_CALLABLE_ID
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.MOCK_IMPL_NAME
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.MOCK_NAME
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPEC_INTERNALS_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPY_IMPL_NAME
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPY_NAME
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.STUB_IMPL_NAME
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.STUB_NAME
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.MOCK_BUILDER_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.SPY_BUILDER_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.STUB_BUILDER_BLOCK_FQN
import io.github.pshevche.spockk.compilation.ir.findPropertyGetter
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irKClassJavaLiteral
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.shared.BaseSpockkIrElementTransformer
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.interaction.InteractionStatementsRewriter
import io.github.pshevche.spockk.compilation.transformer.interaction.asInteraction
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrStatement
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
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isSubtypeOf
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name

/**
 * Redirects every `Mock`/`Stub`/`Spy` call to the matching static `SpecInternals` factory
 * (`MockImpl`/`StubImpl`/`SpyImpl`), passing the name and type Spock infers from the declaration
 * the mock is assigned to - what Spock's own Groovy AST transform does for the same call.
 *
 * The Spockk-only builder-block overloads (`Mock(Type::class.java) { ... }`) are redirected the
 * same way, and the interactions their trailing lambda declares are registered right after the
 * mock is assigned - see [MockBuilderBlockSplicer].
 */
internal class MockingApiTransformer(
  override val rewriterContext: SpockkIrRewriterContext,
  private val spec: IrClass
) : BaseSpockkIrElementTransformer(),
  SpockkIrRewriter {

  private val specInternalsClass = rewriterContext.findRequiredClassSymbol(SPEC_INTERNALS_FQN)
  private val kClassJavaPropGetter = rewriterContext.findPropertyGetter(KCLASS_JAVA_CALLABLE_ID)
  private val typeSystem = IrTypeSystemContextImpl(rewriterContext.irBuiltIns)
  private val builderBlockSplicer = MockBuilderBlockSplicer(spec)

  fun rewrite() {
    spec.declarations
      .filter { it is IrFunction || it is IrProperty }
      .forEach { it.accept(this, null) }
    builderBlockSplicer.splice()
  }

  // Matches `val name: Type = Mock(...)`: the call is the initializer, the mock's inferred name and
  // type come from the variable it is assigned to.
  override fun visitVariable(declaration: IrVariable): IrStatement {
    if (rewriteMockAssignment(declaration.initializer, declaration)) {
      // The initializer is rewritten, so its children must not be visited again.
      return declaration
    }
    return super.visitVariable(declaration)
  }

  // A hoisted declaration (see `irTryHoistingVariables`, e.g. when a cleanup block reads the mock)
  // keeps no initializer: the `Mock()` call shows up as a later assignment instead.
  override fun visitSetValue(expression: IrSetValue): IrExpression {
    val variable = expression.symbol.owner as? IrVariable
    if (variable != null && rewriteMockAssignment(expression.value, variable)) {
      return expression
    }
    return super.visitSetValue(expression)
  }

  // A mock created without being assigned to anything: no name or type can be inferred.
  override fun visitCall(expression: IrCall): IrExpression {
    rewriteInheritedMockCall(expression, mock = null)
    return super.visitCall(expression)
  }

  private fun rewriteMockAssignment(assignedValue: IrExpression?, mock: IrVariable): Boolean {
    // Skips a `!!` or implicit null check around the call, as in `val m = Mock(Runnable::class.java)!!`.
    val call = (assignedValue as? IrTypeOperatorCall)?.argument ?: assignedValue
    if (call !is IrCall) return false

    val builderBlockImpl = BUILDER_BLOCK_FACTORIES[call.symbol.owner.fqNameWhenAvailable]
    if (builderBlockImpl != null) {
      rewriteBuilderBlockMock(call, mock, builderBlockImpl)
    } else {
      rewriteInheritedMockCall(call, mock)
    }
    return true
  }

  /** `Mock`/`Stub`/`Spy` as inherited from `MockingApi`, with no builder block. */
  private fun rewriteInheritedMockCall(call: IrCall, mock: IrVariable?) {
    val factoryName = INHERITED_FACTORIES[call.symbol.owner.name] ?: return
    // Only calls to the spec's own inherited member, not a same-named function from elsewhere.
    if (call.symbol.owner.parent != spec) return
    val factory = findFactory(factoryName, call) ?: return
    redirectToFactory(call, mock, factory)
  }

  /**
   * `Mock(Type::class.java) { ... }`: a Spockk top-level function, so unlike the inherited member
   * it has no dispatch receiver of its own, and its trailing lambda has to be taken off the call
   * before it can be redirected to a factory that knows nothing about it.
   */
  private fun rewriteBuilderBlockMock(call: IrCall, mock: IrVariable, factoryName: Name) {
    val block = call.arguments.removeAt(call.arguments.lastIndex) as? IrFunctionExpression
      ?: throw CompilationException(
        "Mock/Stub builder block must be a literal lambda (`Mock(Type::class.java) { ... }`)",
        spec.file,
        call
      )
    // The factories all expect the spec instance first, matching MockImpl's own signature.
    call.arguments.add(0, irBuilder(call.symbol).irGet(currentIrFunction.requiredThisParameter()))

    val factory = findFactory(factoryName, call) ?: return
    redirectToFactory(call, mock, factory)

    val interactions = buildBlockInteractions(block, mock)
    if (interactions.isNotEmpty()) {
      builderBlockSplicer.record(currentIrFunction, mock, interactions)
    }
  }

  private fun buildBlockInteractions(block: IrFunctionExpression, mock: IrVariable): List<IrStatement> {
    val blockStatements = block.function.mutableStatements() ?: return emptyList()
    val blockBuilder = irBuilder(block.function.symbol)
    val blockReceiver = block.function.parameters.first { it.kind == IrParameterKind.ExtensionReceiver }
    val interactionRewriter = InteractionStatementsRewriter(rewriterContext, currentIrFunction)

    return blockStatements.flatMap { statement ->
      // Inside the block the mock is the lambda's receiver, which does not exist once the
      // statements move out into the enclosing method - rebind those references to the variable.
      val rebound = (statement as? IrExpression)?.rebindReceiverTo(blockReceiver, mock, blockBuilder) ?: statement
      val interaction = rebound.asInteraction(allowBareCall = true)
        ?: throw CompilationException(
          "Every statement in a Mock/Stub builder block must be an interaction statement (a call on the mock, " +
            "optionally wrapped in does/did/returns/returned)",
          spec.file,
          statement
        )
      interactionRewriter.rewrite(interaction)
    }
  }

  /** Points the call at a `SpecInternals` factory, inserting the arguments Spock infers. */
  private fun redirectToFactory(call: IrCall, mock: IrVariable?, factory: IrSimpleFunction) {
    with(irBuilder(call.symbol)) {
      // The factories are static, so what was an implicit dispatch receiver becomes argument zero.
      call.arguments[0] = irGet(currentIrFunction.requiredThisParameter())
      call.arguments.add(1, mock?.let { irString(it.name.asString()) } ?: irNull())
      call.arguments.add(2, inferredMockType(mock))
      call.symbol = factory.symbol
    }
  }

  private fun DeclarationIrBuilder.inferredMockType(mock: IrVariable?): IrExpression {
    val mockClass = mock?.type?.classOrNull ?: return irNull()
    return irKClassJavaLiteral(kClassJavaPropGetter.symbol, mockClass)
  }

  /**
   * The `SpecInternals` factory overload this call resolves to. The first three parameters (spec,
   * inferred name, inferred type) are fixed, so only the rest are matched against the call.
   *
   * A parameter typed as the candidate's own type parameter - `SpyImpl`'s "wrap this instance"
   * overloads - accepts anything, since there is no concrete type an argument could be checked
   * against. Candidates whose parameters are all concrete are tried first, so that open slot can
   * never shadow a more specific match, whatever order `SpecInternals` happens to declare them in.
   */
  private fun findFactory(factoryName: Name, call: IrCall): IrSimpleFunction? {
    // Two arguments more than the call has: the inferred name and type.
    val parameterCount = call.arguments.size + 2
    val candidates = specInternalsClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .filter { it.name == factoryName && it.parameters.size == parameterCount }

    return candidates.firstOrNull { it.accepts(call, parameterCount, allowOpenSlot = false) }
      ?: candidates.firstOrNull { it.accepts(call, parameterCount, allowOpenSlot = true) }
  }

  private fun IrSimpleFunction.accepts(call: IrCall, parameterCount: Int, allowOpenSlot: Boolean): Boolean =
    (INFERRED_PARAMETER_COUNT..<parameterCount).all { index ->
      val parameterType = parameters[index].type
      val argumentType = call.arguments[index - 2]?.type
      if (parameterType.classifierOrNull is IrTypeParameterSymbol) {
        allowOpenSlot
      } else {
        argumentType == null || argumentType.isSubtypeOf(parameterType, typeSystem)
      }
    }

  private companion object {
    /** Spec instance, inferred name and inferred type: the parameters no argument is matched to. */
    const val INFERRED_PARAMETER_COUNT = 3

    val INHERITED_FACTORIES = mapOf(
      MOCK_NAME to MOCK_IMPL_NAME,
      STUB_NAME to STUB_IMPL_NAME,
      SPY_NAME to SPY_IMPL_NAME
    )

    // The builder-block overloads are plain top-level functions, so they are matched by FQN rather
    // than by name and declaring class like the inherited members above.
    val BUILDER_BLOCK_FACTORIES = mapOf(
      MOCK_BUILDER_BLOCK_FQN to MOCK_IMPL_NAME,
      STUB_BUILDER_BLOCK_FQN to STUB_IMPL_NAME,
      SPY_BUILDER_BLOCK_FQN to SPY_IMPL_NAME
    )
  }
}

/** Replaces reads of [receiver] with reads of [target]. */
private fun IrExpression.rebindReceiverTo(
  receiver: IrValueParameter,
  target: IrVariable,
  builder: DeclarationIrBuilder
): IrExpression {
  val rebinder = object : IrElementTransformerVoid() {
    override fun visitGetValue(expression: IrGetValue): IrExpression {
      if (expression.symbol == receiver.symbol) return builder.irGet(target)
      return super.visitGetValue(expression)
    }
  }
  return transform(rebinder, null)
}
