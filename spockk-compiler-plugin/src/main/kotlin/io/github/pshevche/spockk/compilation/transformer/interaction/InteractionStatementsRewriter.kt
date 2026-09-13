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

@file:OptIn(UnsafeDuringIrConstructionAPI::class, InternalSymbolFinderAPI::class)

package io.github.pshevche.spockk.compilation.transformer.interaction

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Groovy.CLOSURE_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Kotlin.INT_PROGRESSION_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPOCK_SPREAD_WILDCARD_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPOCK_WILDCARD_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.WILDCARD_METHOD_NAME
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.RESPONSE_CLOSURE_CALLABLE_ID
import io.github.pshevche.spockk.compilation.ir.findPropertyGetter
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.findUniqueFunctionSymbol
import io.github.pshevche.spockk.compilation.ir.irJavaSingletonInstance
import io.github.pshevche.spockk.compilation.ir.irType
import io.github.pshevche.spockk.compilation.ir.irVal
import io.github.pshevche.spockk.compilation.ir.singleRegularArg
import io.github.pshevche.spockk.compilation.ir.sourceLineColumn
import io.github.pshevche.spockk.compilation.ir.sourceText
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers.INTERACTION_RANGE_VAR
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.util.file

/**
 * Builds the `mockController.addInteraction(new InteractionBuilder(...)...build())` statements a
 * parsed [Interaction] compiles to.
 *
 * No matching or verification logic is implemented here: the generated code drives Spock's own
 * `InteractionBuilder` and `MockController`, shaded unmodified into `spockk-core`, the same way
 * condition rendering reuses Spock's own `SpockRuntime`.
 */
internal class InteractionStatementsRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val feature: IrFunction
) : SpockkIrRewriter {

  private val builder = irBuilder(feature.symbol)
  private val mockController = FeatureMockController(rewriterContext, feature)
  private val wildcardClass = rewriterContext.findRequiredClassSymbol(SPOCK_WILDCARD_FQN)
  private val spreadWildcardClass = rewriterContext.findRequiredClassSymbol(SPOCK_SPREAD_WILDCARD_FQN)
  private val intProgressionClass = rewriterContext.findRequiredClassSymbol(INT_PROGRESSION_FQN)
  private val responseClosureFn = rewriterContext.findUniqueFunctionSymbol(RESPONSE_CLOSURE_CALLABLE_ID)
  private val closureType = builder.irType(CLOSURE_FQN)

  /**
   * Moves a `then` block's interactions out of it: they become the [InteractionScope] registered
   * ahead of the paired `when` block, leaving the block's other statements in source order,
   * mirroring Spock's own `SpecRewriter.moveInteractions`.
   */
  fun extractScope(statements: List<IrStatement>): InteractionExtractionResult {
    val registrations = mutableListOf<IrStatement>()
    val remainingStatements = mutableListOf<IrStatement>()
    statements.forEach { statement ->
      val interaction = statement.asInteraction()
      val noMoreInteractionsCall = statement.asNoMoreInteractionsCall()
      when {
        interaction != null -> registrations += rewrite(interaction)
        noMoreInteractionsCall != null -> registrations += rewriteNoMoreInteractions(noMoreInteractionsCall)
        else -> remainingStatements += statement
      }
    }
    return InteractionExtractionResult(InteractionScope(mockController, registrations), remainingStatements)
  }

  fun rewrite(interaction: Interaction): List<IrStatement> = buildList {
    val chain = interactionChain(interaction.source)

    when (val cardinality = interaction.cardinality) {
      null -> Unit

      is InteractionCardinality.Fixed -> chain.fixedCount(cardinality.count)

      is InteractionCardinality.Range -> {
        val range = hoistRange(cardinality.range)
        add(range.declaration)
        chain.rangeCount(range.first, range.last)
      }
    }

    chain.equalTarget(interaction.target)
    chain.equalMethodName(interaction.method.matchedName())
    chain.positionalArgList()
    interaction.argMatchers().forEach { chain.equalArg(it) }

    when (val response = interaction.response) {
      null -> Unit
      is InteractionResponse.Code -> chain.codeResponse(irResponseClosure(response.block))
      is InteractionResponse.Constant -> chain.constantResponse(response.value)
    }

    add(mockController.irAddInteraction(chain.build()))
  }

  /** `noMoreInteractions(a, b)`: every listed mock expects zero calls to any of its methods. */
  fun rewriteNoMoreInteractions(call: IrCall): List<IrStatement> {
    val mocks = (call.singleRegularArg() as? IrVararg)?.elements?.filterIsInstance<IrExpression>().orEmpty()
    return mocks.flatMap { mock ->
      rewrite(
        Interaction(
          source = call,
          cardinality = InteractionCardinality.Fixed(builder.irInt(0)),
          target = mock,
          method = InteractionMethod.Wildcard,
          args = emptyList(),
          response = null
        )
      )
    }
  }

  private fun interactionChain(source: IrStatement): InteractionChain {
    val positionSource = source as IrExpression
    val (line, column) = positionSource.sourceLineColumn(feature.file)
    val text = positionSource.sourceText(feature.file, rewriterContext.sourceTextCache)
    return InteractionChain(
      interactionBuilder = rewriterContext.interactionBuilder,
      builder = builder,
      line = line,
      column = column,
      text = text?.let { builder.irString(it) } ?: builder.irNull()
    )
  }

  private fun InteractionMethod.matchedName(): String = when (this) {
    is InteractionMethod.Named -> name
    InteractionMethod.Wildcard -> WILDCARD_METHOD_NAME
  }

  private fun Interaction.argMatchers(): List<IrExpression> = when (method) {
    // An empty constraint list only matches a genuinely zero-argument call
    // (PositionalArgumentListConstraint), so matching any argument list takes Spock's own
    // SpreadWildcard sentinel - what Groovy's `_._(*_)` compiles to.
    InteractionMethod.Wildcard -> listOf(builder.irJavaSingletonInstance(spreadWildcardClass))

    is InteractionMethod.Named -> args.map { arg ->
      when (arg) {
        InteractionArg.Wildcard -> builder.irJavaSingletonInstance(wildcardClass)
        is InteractionArg.EqualTo -> arg.value
      }
    }
  }

  /**
   * Declares the range as a temp `val` so reading its `first` and `last` evaluates the range
   * expression once rather than twice.
   */
  private fun hoistRange(range: IrExpression): HoistedRange {
    val declaration = irVal(INTERACTION_RANGE_VAR, range.type).apply {
      parent = feature
      initializer = range
    }
    return HoistedRange(
      declaration = declaration,
      first = irReadRangeBound(declaration, "first"),
      last = irReadRangeBound(declaration, "last")
    )
  }

  private fun irReadRangeBound(rangeVar: IrVariable, bound: String): IrExpression =
    builder.irCall(intProgressionClass.findPropertyGetter(bound)).apply {
      dispatchReceiver = builder.irGet(rangeVar)
    }

  // A does/did block is typed Function1<List<Any?>, R>; responseClosure wraps it in the
  // groovy.lang.Closure an InteractionBuilder code response expects, with R as its type argument.
  private fun irResponseClosure(block: IrExpression): IrExpression {
    val responseType = (block.type as IrSimpleType).arguments.last().typeOrFail
    return builder.irCall(responseClosureFn, closureType).apply {
      typeArguments[0] = responseType
      arguments[0] = block
    }
  }

  private class HoistedRange(val declaration: IrVariable, val first: IrExpression, val last: IrExpression)
}

/**
 * The outcome of moving a `then` block's interactions out of it: the [scope] they are registered
 * in, and the block with those entries removed.
 */
internal class InteractionExtractionResult(
  val scope: InteractionScope,
  val remainingStatements: List<IrStatement>
)
