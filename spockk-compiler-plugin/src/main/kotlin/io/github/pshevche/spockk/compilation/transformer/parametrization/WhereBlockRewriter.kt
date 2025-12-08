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

package io.github.pshevche.spockk.compilation.transformer.parametrization

import io.github.pshevche.spockk.compilation.common.FeatureBlockStatements
import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext
import io.github.pshevche.spockk.compilation.ir.addMemberFunction
import io.github.pshevche.spockk.compilation.ir.assignableParameters
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.ir.irArrayOf
import io.github.pshevche.spockk.compilation.ir.irListOf
import io.github.pshevche.spockk.compilation.ir.irStringArray
import io.github.pshevche.spockk.compilation.ir.irVar
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.IMPLICIT_COERCION_TO_UNIT
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.fileEntry
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class WhereBlockRewriter(
  override val context: IrGeneratorContext,
  private val spec: IrClass,
  private val feature: IrFunction,
  private val featureContext: SpockkTransformationContext.FeatureContext,
  private val whereBlock: FeatureBlockStatements
) : SpockkIrRewriter {

  companion object {
    private const val DATA_PROVIDER_METADATA_FQN =
      "org.spockframework.runtime.model.DataProviderMetadata"
    private const val DATA_PROCESSOR_METADATA_FQN =
      "org.spockframework.runtime.model.DataProcessorMetadata"
  }

  private val exceptionFactory =
    InvalidParametrizationExceptionFactory(spec.file, whereBlock.element.ir)
  private var dataProviderCount = 0
  private val dataProcessorVars = mutableListOf<IrVariable>()

  private lateinit var dataProcessorMethod: IrFunction

  fun rewrite() {
    dataProcessorMethod = initializeDataProcessorMethod()

    val stats = whereBlock.statements.listIterator()
    while (stats.hasNext()) {
      rewriteWhereStatements(stats)
    }

    finalizeDataProcessorMethod()
  }

  private fun rewriteWhereStatements(stats: ListIterator<IrStatement>) {
    // TODO: check for data-pipes syntax

    val potentialHeaderRow = getExpressionChain(stats)
    if (potentialHeaderRow.size > 1) {
      repeat(potentialHeaderRow.size) { stats.previous() }
      rewriteExpressionTableLikeParameterization(stats)
      return
    }

    throw exceptionFactory.unrecognizedParameterizationSyntaxException()
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
        throw exceptionFactory.invalidRowSizeException(rows.size + 1, row.size, rows.last().size)
      }

      rows.add(row)
    }

    if (rows.size == 1) {
      throw exceptionFactory.missingValuesRowsException()
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

  private fun isWildcardRef(header: IrStatement): Boolean {
    // TODO pshevche: support wildcards
    return false
  }

  private fun IrStatement.asDataTableVariable(): IrValueParameter {
    val paramSymbol =
      (this as? IrTypeOperatorCall)?.argument?.let { it as? IrGetValue }?.symbol
        as? IrValueParameterSymbol
    return feature.assignableParameters().find { it.symbol == paramSymbol }
      ?: throw exceptionFactory.invalidDataTableHeaderException()
  }

  private fun getPreviousDataTableVariables(nextDataVariableIndex: Int): List<String> =
    dataProcessorVars.subList(0, nextDataVariableIndex - 1).map { it.name.asString() }

  private fun rewriteSimpleParameterization(
    featureVariable: IrValueParameter,
    variableValues: List<IrExpression>
  ) {
    val nextDataVariableIndex = dataProcessorMethod.assignableParameters().size
    val dataProcessorParameter = createDataProcessorParameter(nextDataVariableIndex)
    val dataProcessorVar = createDataProcessorVariable(featureVariable)
    createDataProcessorStatement(
      dataProcessorVar,
      irBuilder(dataProcessorMethod.symbol).irGet(dataProcessorParameter)
    )
    createDataProviderMethod(featureVariable, variableValues, nextDataVariableIndex)
  }

  private fun createDataProcessorParameter(nextDataVariableIndex: Int): IrValueParameter =
    dataProcessorMethod.addValueParameter(
      Name.identifier("spock_p$nextDataVariableIndex"),
      irBuiltIns.anyType
    )

  private fun createDataProcessorVariable(featureVariable: IrValueParameter): IrVariable =
    irVar(featureVariable.name, featureVariable.type)
      .apply { parent = dataProcessorMethod }
      .also { dataProcessorVars.add(it) }

  private fun createDataProcessorStatement(
    dataProcessorVar: IrVariable,
    dataProcessorVarValue: IrExpression
  ) {
    with(irBuilder(dataProcessorMethod.symbol)) {
      dataProcessorMethod.mutableStatements()?.apply {
        add(dataProcessorVar)
        add(irSet(dataProcessorVar.symbol, irAs(dataProcessorVarValue, dataProcessorVar.type)))
      }
    }
  }

  private fun createDataProviderMethod(
    featureVariable: IrValueParameter,
    variableValues: List<IrExpression>,
    nextDataVariableIndex: Int
  ) {
    val previousVariables = getPreviousDataTableVariables(dataProcessorVars.size)
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

  fun createPreviousDataTableParameters(dataProvider: IrFunction, previousVariables: List<String>) {
    previousVariables.forEach {
      val parameterName = Name.identifier("spock_p_$it")
      dataProvider.addValueParameter(
        parameterName,
        irBuiltIns.listClass.typeWith(irBuiltIns.anyType)
      )
    }
  }

  private fun createDataProviderAnnotation(
    function: IrFunction,
    nextDataVariableIndex: Int,
    previousVariables: List<String>
  ): IrConstructorCall {
    val dataVariables =
      dataProcessorVars.subList(nextDataVariableIndex, dataProcessorVars.size).map {
        it.name.asString()
      }
    return with(irBuilder(function.symbol)) {
      irAnnotation(
        DATA_PROVIDER_METADATA_FQN,
        irInt(spec.fileEntry.getLineNumber(SYNTHETIC_OFFSET)),
        irStringArray(dataVariables),
        irStringArray(previousVariables)
      )
    }
  }

  private fun createDataProviderBody(
    function: IrFunction,
    featureVariable: IrValueParameter,
    variableValues: List<IrExpression>,
    previousVariables: List<String>
  ): IrBody =
    with(irBuilder(function.symbol)) {
      if (previousVariables.isEmpty()) {
        irBlockBody {
          +irReturn(
            irListOf(
              featureVariable.symbol.owner.type,
              unwrapImplicitCoercionToUnit(variableValues)
            )
          )
        }
      } else {
        // TODO: support references to previous data variables
        irBlockBody {
          +irReturn(
            irListOf(
              featureVariable.symbol.owner.type,
              unwrapImplicitCoercionToUnit(variableValues)
            )
          )
        }
      }
    }

  private fun unwrapImplicitCoercionToUnit(expressions: List<IrExpression>): List<IrExpression> =
    expressions.map { exp ->
      (exp as? IrTypeOperatorCall)?.let {
        if (it.operator == IMPLICIT_COERCION_TO_UNIT) it.argument else it
      } ?: exp
    }

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
