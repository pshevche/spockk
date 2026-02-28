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

package io.github.pshevche.spockk.compilation.transformer.fields

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FieldContext
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.FIELD_METADATA_FQN
import io.github.pshevche.spockk.compilation.ir.addMemberFunction
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.ir.irGetThis
import io.github.pshevche.spockk.compilation.ir.makeNullableWithNullDefault
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.ir.rebindDispatchReceiverReferences
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.name.Name

internal abstract class FieldStrategyBase(
  override val context: IrGeneratorContext,
  protected val spec: IrClass,
  protected val state: FieldRewriteState
) : SingleFieldRewriterStrategy {

  // --- Annotation ---

  protected fun annotateField(field: IrField, fieldCtx: FieldContext) {
    with(irBuilder(field.symbol)) {
      field.annotations += irAnnotation(
        FIELD_METADATA_FQN,
        irString(fieldCtx.name),
        irInt(fieldCtx.ordinal),
        irInt(fieldCtx.line),
        irBoolean(fieldCtx.hasInitializer)
      )
    }
  }

  // --- Init method management ---

  protected fun getOrCreateInstanceFieldsInit(): IrFunction =
    state.instanceFieldsInitMethod ?: createInitMethod(InternalIdentifiers.INITIALIZE_FIELDS_METHOD)
      .also { state.instanceFieldsInitMethod = it }

  protected fun getOrCreateSharedFieldsInit(): IrFunction =
    state.sharedFieldsInitMethod ?: createInitMethod(InternalIdentifiers.INITIALIZE_SHARED_FIELDS_METHOD)
      .also { state.sharedFieldsInitMethod = it }

  private fun createInitMethod(name: Name): IrSimpleFunction {
    val method = spec.addMemberFunction(name, irBuiltIns.unitType) as IrSimpleFunction
    method.visibility = DescriptorVisibilities.PRIVATE
    method.body = with(irBuilder(method.symbol)) { irBlockBody { } }
    state.onFunctionGenerated(method.symbol)
    return method
  }

  // --- Initializer movement ---

  protected fun moveFieldInitializerForInstanceField(field: IrField, property: IrProperty) {
    moveFieldInitializerToInitializeFieldMethod(field, property, getOrCreateInstanceFieldsInit())
  }

  protected fun moveInitializerToSharedInit(field: IrField, property: IrProperty) {
    moveFieldInitializerToInitializeFieldMethod(field, property, getOrCreateSharedFieldsInit())
  }

  private fun moveFieldInitializerToInitializeFieldMethod(
    field: IrField,
    property: IrProperty,
    initializeFieldsMethod: IrFunction
  ) {
    val initExpr = field.initializer?.expression ?: return
    val dispatchReceiver = initializeFieldsMethod.requiredThisParameter()
    val reboundValue = initExpr.rebindDispatchReceiverReferences(dispatchReceiver, irBuilder(dispatchReceiver.symbol))
    val initializeFieldsStat = with(irBuilder(initializeFieldsMethod.symbol)) {
      val setter = property.setter
      if (setter == null) {
        irSetField(receiver = irGet(dispatchReceiver), field = field, value = reboundValue)
      } else {
        // Generates a CALL to the property setter (origin=EQ, dispatch receiver origin=IMPLICIT_ARGUMENT).
        // This matches the IR that Kotlin generates for `property = value` statements.
        irCall(setter.symbol, irBuiltIns.unitType).apply {
          origin = IrStatementOrigin.EQ
          arguments[0] = irGetThis(dispatchReceiver)
          arguments[1] = reboundValue
        }
      }
    }
    initializeFieldsMethod.mutableStatements()?.add(initializeFieldsStat)
    field.initializer = null
  }

  // --- Field type helpers ---

  protected fun makeFieldNullableWithNullDefault(field: IrField) =
    field.makeNullableWithNullDefault(irBuilder(field.symbol))

  // --- Setter creation ---

  // Creates a DEFAULT_PROPERTY_ACCESSOR setter for a val field that was made mutable.
  // This allows $spock_initializeFields to call the setter (matching expected IR).
  // Removed from spec.declarations since DEFAULT_PROPERTY_ACCESSORs are only accessible
  // as children of their property, not as top-level class declarations.
  protected fun createValFieldSetter(
    setterName: Name,
    property: IrProperty,
    field: IrField
  ): IrSimpleFunction {
    val setter = spec.addMemberFunction(
      Name.special("<set-${setterName.asString()}>"),
      irBuiltIns.unitType
    ) as IrSimpleFunction
    spec.declarations.remove(setter)
    setter.origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    setter.visibility = property.visibility
    setter.correspondingPropertySymbol = property.symbol
    property.setter = setter

    val valueParam = setter.addValueParameter(Name.special("<set-?>"), field.type)
    val thisParam = setter.parameters.first { it.name.asString() == "<this>" }
    setter.body = with(irBuilder(setter.symbol)) {
      irBlockBody {
        +irSetField(receiver = irGet(thisParam), field = field, value = irGet(valueParam))
      }
    }
    state.onFunctionGenerated(setter.symbol)
    return setter
  }
}
