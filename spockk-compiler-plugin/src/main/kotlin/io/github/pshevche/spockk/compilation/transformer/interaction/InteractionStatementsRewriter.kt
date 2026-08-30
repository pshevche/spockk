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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPOCK_SPREAD_WILDCARD_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPOCK_WILDCARD_FQN
import io.github.pshevche.spockk.compilation.ir.findFieldByName
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.findUniqueFunctionSymbol
import io.github.pshevche.spockk.compilation.ir.irImplicitNotNull
import io.github.pshevche.spockk.compilation.ir.irType
import io.github.pshevche.spockk.compilation.ir.irVal
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.ir.sourceLineColumn
import io.github.pshevche.spockk.compilation.ir.sourceText
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import io.github.pshevche.spockk.compilation.transformer.ir.getSpecificationContext
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Builds real `InteractionBuilder`/`MockController.addInteraction` IR from a parsed
 * [InteractionStatement] (or a [noMoreInteractions][io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.NO_MORE_INTERACTIONS_FQN]
 * call) - no matching/verification logic is reimplemented, mirroring how condition rendering reuses
 * `SpockRuntime`/`Condition` instead of reimplementing anything.
 */
internal class InteractionStatementsRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val feature: IrFunction
) : SpockkIrRewriter {

  private val builder = irBuilder(feature.symbol)
  private val wildcardClass = rewriterContext.findRequiredClassSymbol(SPOCK_WILDCARD_FQN)
  private val spreadWildcardClass = rewriterContext.findRequiredClassSymbol(SPOCK_SPREAD_WILDCARD_FQN)
  private val intProgressionClass = rewriterContext.findRequiredClassSymbol(INT_PROGRESSION_FQN)
  private val responseClosureFn = rewriterContext.findUniqueFunctionSymbol(RESPONSE_CLOSURE_CALLABLE_ID)
  private val captureClosureFn = rewriterContext.findUniqueFunctionSymbol(CAPTURE_CLOSURE_CALLABLE_ID)
  private val closureType = builder.irType(CLOSURE_FQN)

  fun rewrite(interaction: InteractionStatement): List<IrStatement> {
    val shape = interaction.methodCall.toMethodShape()
    val (extraStatements, interactionExpr) = buildInteractionExpr(
      positionSource = interaction.statement as IrExpression,
      cardinality = interaction.cardinality,
      shape = shape,
      response = interaction.response
    )
    return extraStatements + irAddInteractionCall(interactionExpr)
  }

  fun rewriteNoMoreInteractions(call: IrCall): List<IrStatement> {
    val mockExprs = (call.singleRegularArg() as? IrVararg)?.elements?.filterIsInstance<IrExpression>().orEmpty()
    return mockExprs.flatMap { mockExpr ->
      val shape = MethodShape(target = mockExpr, methodName = MethodNameKind.Wildcard, args = emptyList())
      val (extraStatements, interactionExpr) = buildInteractionExpr(
        positionSource = call,
        cardinality = InteractionCardinality.Fixed(builder.irInt(0)),
        shape = shape,
        response = null
      )
      extraStatements + irAddInteractionCall(interactionExpr)
    }
  }

  /**
   * Splits a block's statements into the real `addInteraction` statements built from every
   * interaction/[noMoreInteractions][io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spockk.NO_MORE_INTERACTIONS_FQN]
   * statement (in original order), and the remaining statements with those removed (also in
   * original order) - mirrors Spock's own `block.getAst().removeIf(interactions::contains)`.
   */
  fun extractAndRewrite(statements: List<IrStatement>): InteractionExtractionResult {
    val builtStatements = mutableListOf<IrStatement>()
    val remainingStatements = mutableListOf<IrStatement>()
    for (statement in statements) {
      val interaction = statement.asInteractionStatement()
      val noMoreInteractionsCall = statement.asNoMoreInteractionsCall()
      when {
        interaction != null -> builtStatements += rewrite(interaction)
        noMoreInteractionsCall != null -> builtStatements += rewriteNoMoreInteractions(noMoreInteractionsCall)
        else -> remainingStatements += statement
      }
    }
    return InteractionExtractionResult(builtStatements, remainingStatements)
  }

  private fun irAddInteractionCall(interactionExpr: IrExpression): IrCall {
    val specAccessor = feature.requiredThisParameter()
    val controller = feature.parentAsClass.getSpecificationContext(rewriterContext).irGetMockController(builder, specAccessor)
    return rewriterContext.mockController.irAddInteraction(builder, controller, interactionExpr)
  }

  private fun buildInteractionExpr(
    positionSource: IrExpression,
    cardinality: InteractionCardinality?,
    shape: MethodShape,
    response: InteractionResponse?
  ): Pair<List<IrStatement>, IrExpression> {
    val extraStatements = mutableListOf<IrStatement>()
    val (line, column) = positionSource.sourceLineColumn(feature.file)
    val text = positionSource.sourceText(feature.file, rewriterContext.sourceTextCache)
    val textExpr = text?.let { builder.irString(it) } ?: builder.irNull()

    var chain: IrExpression =
      rewriterContext.interactionBuilder.irNew(builder, builder.irInt(line), builder.irInt(column), textExpr)

    chain = when (cardinality) {
      null -> chain

      is InteractionCardinality.Fixed -> rewriterContext.interactionBuilder.irSetFixedCount(builder, chain, cardinality.count)

      is InteractionCardinality.Range -> {
        val (rangeVar, minExpr, maxExpr) = irRangeBounds(cardinality.range)
        extraStatements += rangeVar
        rewriterContext.interactionBuilder.irSetRangeCount(builder, chain, minExpr, maxExpr, builder.irBoolean(true))
      }
    }

    chain = rewriterContext.interactionBuilder.irAddEqualTarget(builder, chain, shape.target)
    chain = when (val methodName = shape.methodName) {
      is MethodNameKind.Literal -> rewriterContext.interactionBuilder.irAddEqualMethodName(builder, chain, builder.irString(methodName.name))

      // InteractionBuilder.addEqualMethodName special-cases name == Wildcard.INSTANCE.toString() ("_")
      // into a WildcardMethodNameConstraint - no need to reference the real Wildcard singleton here.
      MethodNameKind.Wildcard -> rewriterContext.interactionBuilder.irAddEqualMethodName(builder, chain, builder.irString("_"))
    }

    // argConstraints/argNames stay null - NullPointerException on the first addEqualArg/addCodeArg -
    // until this initializes them, even when shape.args is empty (anyMethod()/a bare no-arg call).
    chain = rewriterContext.interactionBuilder.irSetArgListKind(builder, chain)

    if (shape.methodName == MethodNameKind.Wildcard) {
      // anyMethod() promises to match "any method call ... regardless of ... arguments": an empty
      // arg-constraint list only matches a genuinely zero-arg invocation
      // (PositionalArgumentListConstraint.areConstraintsSatisfiedBy), so match any argument list via
      // Spock's own SpreadWildcard instead - the same sentinel Groovy's `_._(*_)` compiles to.
      chain = rewriterContext.interactionBuilder.irAddEqualArg(builder, chain, irSpreadWildcardInstance())
    } else {
      for (arg in shape.args) {
        chain = when {
          (arg as? IrCall)?.isAnyMarkerCall() == true ->
            rewriterContext.interactionBuilder.irAddEqualArg(builder, chain, irWildcardInstance())

          (arg as? IrCall)?.isCaptureMarkerCall() == true ->
            rewriterContext.interactionBuilder.irAddCodeArg(builder, chain, irCaptureClosureCall(arg))

          else -> rewriterContext.interactionBuilder.irAddEqualArg(builder, chain, arg)
        }
      }
    }

    chain = when (response) {
      null -> chain
      is InteractionResponse.Code -> rewriterContext.interactionBuilder.irAddCodeResponse(builder, chain, irResponseClosureCall(response.block))
      is InteractionResponse.Constant -> rewriterContext.interactionBuilder.irAddConstantResponse(builder, chain, response.value)
    }

    return extraStatements to rewriterContext.interactionBuilder.irBuild(builder, chain)
  }

  // `range` evaluates to an IntRange at runtime; declared once as a temp val so `.first`/`.last` each
  // read it without evaluating (and potentially side-effecting) the range expression twice. Kotlin's
  // `..`/`..<` for Int both always produce an inclusive IntRange (`..<`'s `last` is already
  // `endExclusive - 1`), so `setRangeCount`'s `inclusive` is always `true` here.
  private fun irRangeBounds(range: IrExpression): Triple<IrVariable, IrExpression, IrExpression> {
    val rangeVar = irVal(Name.identifier("\$spock_interaction_range"), range.type).apply {
      parent = feature
      initializer = range
    }
    // first/last are declared on IntProgression (IntRange's superclass) as properties, not plain
    // functions - functionByName (meant for Java-style methods) can't find their getters, so look
    // the properties up directly.
    val minExpr = builder.irCall(intProgressionClass.propertyGetter("first")).apply { dispatchReceiver = builder.irGet(rangeVar) }
    val maxExpr = builder.irCall(intProgressionClass.propertyGetter("last")).apply { dispatchReceiver = builder.irGet(rangeVar) }
    return Triple(rangeVar, minExpr, maxExpr)
  }

  private fun irWildcardInstance(): IrExpression {
    val instanceField = wildcardClass.findFieldByName("INSTANCE")
    val fieldAccess = builder.irGetField(null, instanceField).apply { superQualifierSymbol = wildcardClass }
    return builder.irImplicitNotNull(fieldAccess, builder.irType(SPOCK_WILDCARD_FQN))
  }

  private fun irSpreadWildcardInstance(): IrExpression {
    val instanceField = spreadWildcardClass.findFieldByName("INSTANCE")
    val fieldAccess = builder.irGetField(null, instanceField).apply { superQualifierSymbol = spreadWildcardClass }
    return builder.irImplicitNotNull(fieldAccess, builder.irType(SPOCK_SPREAD_WILDCARD_FQN))
  }

  // block's type is Function1<List<Any?>, R> (does/did's declared parameter type, R already
  // substituted for this call site) - its last type argument is R, responseClosure's own generic.
  private fun irResponseClosureCall(block: IrExpression): IrFunctionAccessExpression {
    val responseType = (block.type as IrSimpleType).arguments.last().typeOrFail
    return builder.irCall(responseClosureFn, closureType).apply {
      typeArguments[0] = responseType
      arguments[0] = block
    }
  }

  private fun irCaptureClosureCall(captureCall: IrCall): IrFunctionAccessExpression {
    val slotExpr = requireNotNull(captureCall.singleRegularArg()) { "capture(slot) is missing its slot argument" }
    val capturedType = (slotExpr.type as IrSimpleType).arguments.single().typeOrFail
    return builder.irCall(captureClosureFn, closureType).apply {
      typeArguments[0] = capturedType
      arguments[0] = slotExpr
    }
  }

  private data class MethodShape(val target: IrExpression, val methodName: MethodNameKind, val args: List<IrExpression>)

  private sealed class MethodNameKind {
    class Literal(val name: String) : MethodNameKind()

    data object Wildcard : MethodNameKind()
  }

  private fun IrCall.toMethodShape(): MethodShape = if (isAnyMethodMarkerCall()) {
    val target = requireNotNull(extensionReceiverArg()) { "anyMethod() is missing its receiver" }
    MethodShape(target, MethodNameKind.Wildcard, emptyList())
  } else {
    val target = requireNotNull(dispatchReceiverArg()) { "${symbol.owner.name} is missing its dispatch receiver" }
    MethodShape(target, MethodNameKind.Literal(symbol.owner.name.asString()), regularArgs())
  }

  private companion object {
    val INT_PROGRESSION_FQN = FqName("kotlin.ranges.IntProgression")
    val CLOSURE_FQN = FqName("groovy.lang.Closure")
    val LANG_PKG_FQN = FqName("io.github.pshevche.spockk.lang")
    val RESPONSE_CLOSURE_CALLABLE_ID = CallableId(LANG_PKG_FQN, Name.identifier("responseClosure"))
    val CAPTURE_CLOSURE_CALLABLE_ID = CallableId(LANG_PKG_FQN, Name.identifier("captureClosure"))
  }
}

private fun IrClassSymbol.propertyGetter(name: String): IrSimpleFunctionSymbol =
  owner.declarations.filterIsInstance<IrProperty>().first { it.name.asString() == name }.getter!!.symbol
