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

package io.github.pshevche.spockk.compilation.transformer.parametrization

import io.github.pshevche.spockk.compilation.common.FeatureBlock
import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.DATA_PROCESSOR_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.DATA_PROVIDER_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.addMemberFunction
import io.github.pshevche.spockk.compilation.ir.asFeatureVariable
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.ir.irArrayOf
import io.github.pshevche.spockk.compilation.ir.irListGet
import io.github.pshevche.spockk.compilation.ir.irListOf
import io.github.pshevche.spockk.compilation.ir.irStringArray
import io.github.pshevche.spockk.compilation.ir.irVar
import io.github.pshevche.spockk.compilation.ir.isFromCall
import io.github.pshevche.spockk.compilation.ir.isList
import io.github.pshevche.spockk.compilation.ir.isMultiVariableInitializer
import io.github.pshevche.spockk.compilation.ir.isSingleVariableInitializer
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.ir.rebindDispatchReceiverReferences
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.transformer.InstanceFieldAccessChecker
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.fir.lazy.Fir2IrLazyPropertyForPureField
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.IMPLICIT_COERCION_TO_UNIT
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fileEntry
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isConstantLike
import org.jetbrains.kotlin.name.Name

/**
 * Rewrites data provider statements like data tables and data pipes (to be implemented). The
 * implementation closely follows Spock's WhereBlockRewriter and tries to follow the established
 * implicit interface. There are some minor differences, however. In contrast to Groovy's AST nodes,
 * Kotlin's IR nodes sometimes can't be created in isolation. They need to be attached to a parent,
 * therefore, the rewriter sometimes does not cache the complete IR, but only the data required to
 * construct them.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class WhereBlockRewriter(
  override val context: IrGeneratorContext,
  private val spec: IrClass,
  private val feature: IrFunction,
  private val featureContext: SpockkTransformationContext.FeatureContext
) : SpockkIrRewriter {

  private val dataProcessorVars = mutableListOf<IrVariable>()
  private var dataProviderCount = 0
  private lateinit var dataProcessorMethod: IrFunction
  private lateinit var rewriteResources: SingleBlockRewriteResources

  private data class SingleBlockRewriteResources(
    val exceptionFactory: InvalidParametrizationExceptionFactory,
    val instanceFieldAccessChecker: InstanceFieldAccessChecker,
    val dataProcessorVars: MutableList<IrVariable>,
    val dataTableVars: MutableSet<IrVariable> = mutableSetOf()
  )

  fun rewrite(dataProviderBlock: FeatureBlock) {
    if (!::dataProcessorMethod.isInitialized) {
      dataProcessorMethod = initializeDataProcessorMethod()
    }
    rewriteResources = createRewriteResources(dataProviderBlock)
    val stats = dataProviderBlock.statements.listIterator()
    while (stats.hasNext()) {
      rewriteWhereStatements(stats)
    }
  }

  fun finalizeRewrite() {
    if (::dataProcessorMethod.isInitialized) {
      finalizeDataProcessorMethod()
    }
  }

  private fun createRewriteResources(block: FeatureBlock): SingleBlockRewriteResources =
    SingleBlockRewriteResources(
      InvalidParametrizationExceptionFactory(spec.file, block.element.ir),
      InstanceFieldAccessChecker(spec, block.element.ir),
      mutableListOf()
    )

  private fun rewriteWhereStatements(stats: ListIterator<IrStatement>) {
    val stat = stats.next()
    if (stat.isFromCall()) {
      stats.previous()
      rewriteDataPipeLikeParameterization(stats)
      return
    }

    stats.previous()
    val potentialHeaderRow = getExpressionChain(stats)
    if (potentialHeaderRow.size > 1) {
      repeat(potentialHeaderRow.size) { stats.previous() }
      rewriteExpressionTableLikeParameterization(stats)
      return
    }

    throw rewriteResources.exceptionFactory.unrecognizedParameterizationSyntaxException()
  }

  private fun getExpressionChain(stats: ListIterator<IrStatement>): List<IrStatement> {
    val result = mutableListOf<IrStatement>()
    var currentLine = -1

    while (stats.hasNext()) {
      val stat = stats.next()
      val statLine = spec.file.fileEntry.getLineNumber(stat.startOffset)
      currentLine = if (currentLine == -1) statLine else currentLine
      if (currentLine == statLine) {
        result.add(stat)
      } else {
        stats.previous()
        break
      }
    }

    return result.toList()
  }

  private fun rewriteDataPipeLikeParameterization(stats: ListIterator<IrStatement>) {
    val stat = stats.next() as IrCall
    val variablesCall =
      (stat.arguments.first()!! as? IrCall) ?: throw rewriteResources.exceptionFactory.invalidDataPipeTargetException()
    if (variablesCall.isSingleVariableInitializer()) {
      rewriteDataPipeForSingleVariable(variablesCall, stat)
    } else if (variablesCall.isMultiVariableInitializer()) {
      rewriteDataPipeForMultipleVariables(variablesCall, stat)
    } else {
      throw rewriteResources.exceptionFactory.invalidDataPipeSyntaxException()
    }
  }

  private fun rewriteDataPipeForSingleVariable(
    variablesCall: IrCall,
    stat: IrCall
  ) {
    val featureVariable = getFeatureVariablesDefinedInDataPipe(variablesCall).single()
    val values = stat.arguments.last()!!
    val referencedFeatureVariables =
      getReferencedPreviousFeatureVariables(
        getPreviousDataProcessorVariables(rewriteResources.dataProcessorVars.size + 1),
        values
      )
    if (referencedFeatureVariables.isEmpty()) {
      if (values.isConstantLike) {
        // don't create a data provider in this case and just set the value directly in the data processor
        rewriteSimpleDerivedParametrization(featureVariable, referencedFeatureVariables, values)
      } else {
        rewriteSimpleParameterization(featureVariable, listOf(values))
      }
    } else {
      if (values.type.isList() || values.type.isArray()) {
        throw rewriteResources.exceptionFactory.invalidPreviousVariableReferenceInDataPipeException()
      }
      rewriteSimpleDerivedParametrization(featureVariable, referencedFeatureVariables, values)
    }
  }

  private fun rewriteDataPipeForMultipleVariables(
    variablesCall: IrCall,
    stat: IrCall
  ) {
    val featureVariables = getFeatureVariablesDefinedInDataPipe(variablesCall)
    val values = stat.arguments.drop(1)
    val hasFeatureVariableReferences = values.any {
      getReferencedPreviousFeatureVariables(
        getPreviousDataProcessorVariables(rewriteResources.dataProcessorVars.size + 1),
        it!!
      ).isNotEmpty()
    }
    if (hasFeatureVariableReferences) {
      throw rewriteResources.exceptionFactory.invalidPreviousVariableReferenceInDataPipeException()
    }
    featureVariables.zip(values).forEach { (featureVariable, value) ->
      rewriteSimpleParameterization(featureVariable, listOf(value!!))
    }
  }

  private fun getFeatureVariablesDefinedInDataPipe(variablesCall: IrCall): List<IrValueParameter> =
    variablesCall.arguments.map {
      (it as? IrGetValue)?.asFeatureVariable()
        ?: throw rewriteResources.exceptionFactory.invalidDataPipeTargetException()
    }

  private fun rewriteExpressionTableLikeParameterization(stats: ListIterator<IrStatement>) {
    val rows = mutableListOf<List<IrStatement>>()

    while (stats.hasNext()) {
      val row = getExpressionChain(stats)

      // reached the end of the data table => rewind and keep processing
      if (row.size <= 1) {
        repeat(row.size) { stats.previous() }
        break
      }

      if (rows.isNotEmpty() && rows.last().size != row.size) {
        throw rewriteResources.exceptionFactory.invalidRowSizeException(rows.size + 1, row.size, rows.last().size)
      }

      rows.add(row)
    }

    if (rows.size == 1) {
      throw rewriteResources.exceptionFactory.missingValuesRowsException()
    }

    transposeRowsToColumns(rows).forEach { turnIntoSimpleParameterization(it) }
  }

  private fun transposeRowsToColumns(rows: List<List<IrStatement>>): List<List<IrStatement>> {
    if (rows.isEmpty()) {
      return emptyList()
    }

    return List(rows.first().size) { col -> rows.map { it[col] } }
  }

  private fun turnIntoSimpleParameterization(column: List<IrStatement>) {
    val potentialFeatureVariable = column.first()
    if (isWildcardRef(potentialFeatureVariable)) {
      // ignore the placeholder column
      return
    }

    val featureVariable = potentialFeatureVariable.asDataTableVariable()
    rewriteSimpleParameterization(featureVariable, column.drop(1).map { it as IrExpression })
  }

  private fun isWildcardRef(header: IrStatement): Boolean = (header as? IrExpression)
    ?.let { unwrapImplicitCoercionToUnit(it) }
    ?.let { it as? IrGetField }
    ?.let { it.symbol.owner.fqNameWhenAvailable == IrIdentifiers.Spock.WILDCARD_FQN }
    ?: false

  private fun IrStatement.asDataTableVariable(): IrValueParameter = (this as? IrTypeOperatorCall)
    ?.argument
    ?.let { it as? IrGetValue }
    ?.asFeatureVariable() ?: throw rewriteResources.exceptionFactory.invalidDataTableHeaderException()

  private fun IrGetValue.asFeatureVariable(): IrValueParameter =
    asFeatureVariable(feature) ?: throw rewriteResources.exceptionFactory.invalidFeatureVariableReferenceException()

  private fun getPreviousDataProcessorVariables(nextDataVariableIndex: Int): List<IrVariable> {
    if (rewriteResources.dataProcessorVars.isEmpty()) {
      return emptyList()
    }

    return rewriteResources.dataProcessorVars.subList(0, nextDataVariableIndex - 1)
  }

  private fun getPreviousDataTableVariables(nextDataVariableIndex: Int): List<IrVariable> =
    getPreviousDataProcessorVariables(nextDataVariableIndex)
      .filter { it in rewriteResources.dataTableVars }

  private fun getReferencedPreviousFeatureVariables(
    referenceableVariables: List<IrVariable>,
    expression: IrExpression
  ): Set<IrValueParameter> =
    PreviousFeatureVariablesAccessTracker(feature, referenceableVariables.map { it.name.asString() }.toSet())
      .check(expression)

  private fun rewriteSimpleParameterization(
    featureVariable: IrValueParameter,
    variableValues: List<IrExpression>
  ) {
    val nextDataVariableIndex = rewriteResources.dataProcessorVars.size
    val dataProcessorParameter = createDataProcessorParameter(nextDataVariableIndex)
    val dataProcessorVar = createDataProcessorVariable(featureVariable)
    rewriteResources.dataTableVars.add(dataProcessorVar)
    createDataProcessorStatement(
      dataProcessorVar,
      irBuilder(dataProcessorMethod.symbol).irGet(dataProcessorParameter)
    )
    createDataProviderMethod(featureVariable, variableValues, nextDataVariableIndex)
  }

  private fun rewriteSimpleDerivedParametrization(
    featureVariable: IrValueParameter,
    referencedFeatureVariables: Set<IrValueParameter>,
    value: IrExpression
  ) {
    val dataProcessorVar = createDataProcessorVariable(featureVariable)
    val featureVariableReplacement = referencedFeatureVariables.associateWith { fv ->
      val correspondingDataProcessorVar = rewriteResources.dataProcessorVars.first { it.name == fv.name }
      irBuilder(dataProcessorMethod.symbol).irGet(correspondingDataProcessorVar)
    }
    val dataProcessorVarValue = replaceFeatureVariableReferences(value, featureVariableReplacement)
    createDataProcessorStatement(dataProcessorVar, dataProcessorVarValue)
  }

  private fun replaceFeatureVariableReferences(
    expression: IrExpression,
    variableReplacement: Map<IrValueParameter, IrExpression>
  ): IrExpression = PreviousFeatureVariableReferenceReplacer(
    feature,
    variableReplacement
  ).replaceReferences(expression)

  private fun createDataProcessorParameter(nextDataVariableIndex: Int): IrValueParameter =
    dataProcessorMethod.addValueParameter(
      Name.identifier("spock_p$nextDataVariableIndex"),
      irBuiltIns.anyType
    )

  private fun createDataProcessorVariable(featureVariable: IrValueParameter): IrVariable =
    irVar(featureVariable.name, featureVariable.type)
      .apply { parent = dataProcessorMethod }
      .also {
        dataProcessorVars.add(it)
        rewriteResources.dataProcessorVars.add(it)
      }

  private fun createDataProcessorStatement(
    dataProcessorVar: IrVariable,
    dataProcessorVarValue: IrExpression
  ) {
    with(irBuilder(dataProcessorMethod.symbol)) {
      val dataProcessorStats = buildList<IrStatement> {
        add(dataProcessorVar)
        add(irSet(dataProcessorVar.symbol, irAs(dataProcessorVarValue, dataProcessorVar.type)))
      }.also {
        rewriteResources.instanceFieldAccessChecker.check(it)
      }
      dataProcessorMethod.mutableStatements()?.addAll(dataProcessorStats)
    }
  }

  private fun createDataProviderMethod(
    featureVariable: IrValueParameter,
    variableValues: List<IrExpression>,
    nextDataVariableIndex: Int
  ) {
    val previousVariables = getPreviousDataTableVariables(rewriteResources.dataProcessorVars.size)
    spec
      .addMemberFunction(
        InternalIdentifiers.getDataProviderName(featureContext, dataProviderCount++),
        irBuiltIns.anyType
      )
      .apply { createPreviousDataTableParameters(this, previousVariables) }
      .apply {
        annotations +=
          createDataProviderAnnotation(this, nextDataVariableIndex, previousVariables)
      }
      .apply {
        body = createDataProviderBody(this, featureVariable, variableValues, previousVariables)
      }
  }

  fun createPreviousDataTableParameters(dataProvider: IrFunction, previousVariables: List<IrVariable>) {
    previousVariables.forEach {
      val parameterName = Name.identifier("spock_p_${it.name.asString()}")
      dataProvider.addValueParameter(
        parameterName,
        irBuiltIns.listClass.typeWith(it.type)
      )
    }
  }

  private fun createDataProviderAnnotation(
    function: IrFunction,
    nextDataVariableIndex: Int,
    previousVariables: List<IrVariable>
  ): IrConstructorCall {
    val dataVariables =
      rewriteResources.dataProcessorVars.subList(nextDataVariableIndex, rewriteResources.dataProcessorVars.size).map {
        it.name.asString()
      }
    return with(irBuilder(function.symbol)) {
      irAnnotation(
        DATA_PROVIDER_METADATA_FQN,
        irInt(spec.fileEntry.getLineNumber(SYNTHETIC_OFFSET)),
        irStringArray(dataVariables),
        irStringArray(previousVariables.map { it.name.asString() })
      )
    }
  }

  private fun createDataProviderBody(
    dataProviderMethod: IrFunction,
    featureVariable: IrValueParameter,
    variableValues: List<IrExpression>,
    previousVariables: List<IrVariable>
  ): IrBody = with(irBuilder(dataProviderMethod.symbol)) {
    val dataProviderStatements =
      createDataProviderReturnStatement(this, dataProviderMethod, featureVariable, variableValues, previousVariables)
        .also { rewriteResources.instanceFieldAccessChecker.check(it) }
    irBlockBody {
      +irReturn(dataProviderStatements)
    }
  }

  private fun createDataProviderReturnStatement(
    builder: DeclarationIrBuilder,
    dataProviderMethod: IrFunction,
    featureVariable: IrValueParameter,
    variableValues: List<IrExpression>,
    previousVariables: List<IrVariable>
  ): IrExpression {
    val valuesWithReplacedFeatureVariableReferences = variableValues.withIndex().map { (idx, expr) ->
      val referencedFeatureVariables = getReferencedPreviousFeatureVariables(previousVariables, expr)
      if (referencedFeatureVariables.isEmpty()) {
        return@map expr
      }

      val variablesReplacement = referencedFeatureVariables.associateWith { fv ->
        val dataProviderVar =
          dataProviderMethod.parameters.find { it.name.asString() == "spock_p_${fv.name.asString()}" }!!
        builder.irListGet(builder.irGet(dataProviderVar), idx)
      }
      return@map replaceFeatureVariableReferences(expr, variablesReplacement)
    }

    val returnValues = postProcessDataProviderVariables(
      dataProviderMethod,
      valuesWithReplacedFeatureVariableReferences,
      builder
    )

    return returnValues
      .singleOrNull()
      ?.takeIf { it.type.isList() }
      ?: builder.irListOf(featureVariable.symbol.owner.type, returnValues)
  }

  private fun postProcessDataProviderVariables(
    dataProviderMethod: IrFunction,
    dataProviderValues: List<IrExpression>,
    builder: DeclarationIrBuilder
  ): List<IrExpression> = dataProviderValues
    .map { unwrapImplicitCoercionToUnit(it) }
    .map { replaceWildcardRef(it, builder) }
    .map { it.rebindDispatchReceiverReferences(dataProviderMethod.requiredThisParameter(), builder) }

  private fun rebindDispatchReceiverReferences(
    expressions: List<IrExpression>,
    dataProviderThisParam: IrValueParameter
  ): List<IrExpression> = expressions.map {
    it.rebindDispatchReceiverReferences(
      dataProviderThisParam,
      irBuilder(dataProviderThisParam.symbol)
    )
  }

  private fun replaceWildcardRef(dataProviderValue: IrExpression, builder: IrBuilder): IrExpression =
    (dataProviderValue as? IrGetValue)
      // the usage of spock's wildcard object is checked earlier
      ?.takeIf { it.symbol.owner.name.asString() == "_" }
      ?.let { irGetWildcardField(builder) }
      ?: dataProviderValue

  private fun irGetWildcardField(builder: IrBuilder): IrGetField {
    val specificationSymbol = builder.context.findRequiredClassSymbol(IrIdentifiers.Spock.SPECIFICATION_FQN)
    val wildcardField = specificationSymbol
      .owner
      .declarations
      .filterIsInstance<Fir2IrLazyPropertyForPureField>()
      .map { it.backingField!! }
      .single { it.symbol.owner.fqNameWhenAvailable == IrIdentifiers.Spock.WILDCARD_FQN }
    return builder.irGetField(receiver = null, field = wildcardField).apply {
      superQualifierSymbol = specificationSymbol
    }
  }

  private fun unwrapImplicitCoercionToUnit(exp: IrExpression): IrExpression = (exp as? IrTypeOperatorCall)?.let {
    if (it.operator == IMPLICIT_COERCION_TO_UNIT) it.argument else it
  } ?: exp

  private fun initializeDataProcessorMethod(): IrFunction =
    spec
      .addMemberFunction(
        InternalIdentifiers.getDataProcessorName(featureContext),
        irBuiltIns.anyType
      )
      .apply { initializeDataProcessorBody(this) }

  fun initializeDataProcessorBody(function: IrFunction) {
    function.body = irBuilder(function.symbol).irBlockBody {}
  }

  private fun finalizeDataProcessorMethod() {
    with(irBuilder(dataProcessorMethod.symbol)) {
      dataProcessorMethod.annotations += createDataProcessorAnnotation(this)
      dataProcessorMethod
        .mutableStatements()
        ?.add(irReturn(irArrayOf(irBuiltIns.anyType, dataProcessorVars.map { irGet(it) })))
    }
  }

  fun createDataProcessorAnnotation(builder: DeclarationIrBuilder): IrConstructorCall =
    builder.irAnnotation(
      DATA_PROCESSOR_METADATA_FQN,
      builder.irStringArray(dataProcessorVars.map { it.name.asString() })
    )
}
