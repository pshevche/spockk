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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Kotlin.VOLATILE_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPECIFICATION_CONTEXT_FQN
import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPEC_INTERNALS_FQN
import io.github.pshevche.spockk.compilation.ir.addMemberFunction
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.ir.irGetThis
import io.github.pshevche.spockk.compilation.ir.makeMutable
import io.github.pshevche.spockk.compilation.ir.makeProtected
import io.github.pshevche.spockk.compilation.shared.SpockkTransformationContext.FieldContext
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.name.Name

/**
 * Handles @Shared fields:
 * - Renames backing field to $spock_sharedField_<name>
 * - Makes field protected volatile var
 * - For non-override properties: generates getXxx()/setXxx() methods that route through
 *   specificationContext.sharedInstance; excludes DEFAULT getter/setter from replacement
 * - For override properties: updates existing getter/setter bodies to route through
 *   specificationContext.sharedInstance directly (using GET_FIELD/SET_FIELD to avoid recursion)
 * - If initialized and not lateinit: moves initializer to $spock_initializeSharedFields(),
 *   makes field nullable with null default
 */
internal class SharedFieldStrategy(
  private val fieldCtx: FieldContext,
  state: FieldRewriteState,
  context: IrGeneratorContext,
  spec: IrClass
) : FieldStrategyBase(context, spec, state) {

  override fun rewrite(property: IrProperty) {
    val field = property.backingField ?: return
    val isOverride = property.overriddenSymbols.isNotEmpty()

    annotateField(field, fieldCtx)

    val newName = InternalIdentifiers.getSharedFieldName(fieldCtx.name)
    rename(field, property, newName, isOverride)
    updateModifiers(field, property)

    val originalFieldSymbol = field.symbol
    val originalGetter = property.getter
    val originalSetter = property.setter

    if (isOverride) {
      routeAccessorsThroughSpecContext(originalGetter, field, originalSetter)
    } else {
      // Preserve DEFAULT getter/setter bodies (GET_FIELD/SET_FIELD) by skipping them
      // during reference replacement. Feature method references are redirected to the
      // generated getXxx()/setXxx() methods below.
      // The DEFAULT getter/setter bodies must remain as simple GET_FIELD/SET_FIELD because:
      // - The $spock_initializeSharedFields method calls the setter directly on the
      //   shared instance, which needs direct field access (not sharedInstance routing)
      // - Subclass access to parent shared fields is handled by ParentSharedFieldRegistrar
      originalGetter?.let { state.onFunctionGenerated(it.symbol) }
      originalSetter?.let { state.onFunctionGenerated(it.symbol) }

      // For shared val with initializer: create DEFAULT setter so init method can use
      // a setter CALL (matching the expected IR from `$spock_sharedField_x = value`)
      if (fieldCtx.isVal && fieldCtx.hasInitializer && !fieldCtx.isLateinit) {
        createValFieldSetter(newName, property, field)
      }
    }

    if (fieldCtx.hasInitializer && !fieldCtx.isLateinit) {
      moveInitializerToSharedInit(field, property)
      makeFieldNullableWithNullDefault(field)
    }

    if (!isOverride) {
      createOrReplaceGetter(field, originalGetter, originalFieldSymbol)
      if (!fieldCtx.isVal) {
        createOrReplaceSetter(field, originalSetter, originalFieldSymbol)
      }
    }
  }

  // For override properties: update existing getter/setter bodies to route through
  // specificationContext.sharedInstance, preserving the interface-declared signatures.
  private fun routeAccessorsThroughSpecContext(
    originalGetter: IrSimpleFunction?,
    field: IrField,
    originalSetter: IrSimpleFunction?
  ) {
    if (originalGetter != null) {
      updateSharedGetterBody(originalGetter, field)
      state.onFunctionGenerated(originalGetter.symbol)
    }
    if (!fieldCtx.isVal && originalSetter != null) {
      updateSharedSetterBody(originalSetter, field)
      state.onFunctionGenerated(originalSetter.symbol)
    }
  }

  private fun createOrReplaceSetter(
    field: IrField,
    originalSetter: IrSimpleFunction?,
    originalFieldSymbol: IrFieldSymbol
  ) {
    val setter = createSharedSetter(
      Name.identifier("set${fieldCtx.name.replaceFirstChar { it.uppercaseChar() }}"),
      field
    )
    originalSetter?.let { state.onSetterReplaced(it.symbol, setter) }
    state.onFieldSetterCreated(originalFieldSymbol, setter)
  }

  private fun createOrReplaceGetter(
    field: IrField,
    originalGetter: IrSimpleFunction?,
    originalFieldSymbol: IrFieldSymbol
  ) {
    val getter = createSharedGetter(
      Name.identifier("get${fieldCtx.name.replaceFirstChar { it.uppercaseChar() }}"),
      field
    )
    originalGetter?.let { state.onGetterReplaced(it.symbol, getter) }
    state.onFieldGetterCreated(originalFieldSymbol, getter)
  }

  private fun rename(
    field: IrField,
    property: IrProperty,
    newName: Name,
    isOverride: Boolean
  ) {
    field.name = newName

    if (!isOverride) {
      // For non-override properties: also rename the property and its accessors
      property.name = newName
      property.getter?.name = Name.special("<get-${newName.asString()}>")
      property.setter?.name = Name.special("<set-${newName.asString()}>")
    }
  }

  // Make protected volatile mutable
  private fun updateModifiers(field: IrField, property: IrProperty) {
    property.makeProtected()
    field.makeMutable()
    property.makeMutable()
    addVolatileAnnotation(field)
  }

  private fun addVolatileAnnotation(field: IrField) {
    field.annotations += with(irBuilder(field.symbol)) { irAnnotation(VOLATILE_FQN) }
  }

  // Creates getXxx() that delegates to the DEFAULT getter on the shared instance via GET_PROPERTY.
  // The DEFAULT getter body (GET_FIELD) is not modified for non-override fields, so calling
  // it on the shared instance reads the field from the shared instance without recursion.
  private fun createSharedGetter(name: Name, field: IrField): IrFunction {
    val property = field.correspondingPropertySymbol?.owner
      ?: error("Shared field ${field.name} has no backing property")
    val defaultGetter = property.getter
      ?: error("Shared field ${field.name} has no DEFAULT getter")
    val getter = spec.addMemberFunction(name, field.type)
    getter.body = with(irBuilder(getter.symbol)) {
      irBlockBody {
        +irReturn(
          irCall(defaultGetter.symbol, field.type, origin = IrStatementOrigin.GET_PROPERTY).apply {
            arguments[0] = irAs(irGetSharedInstance(getter), spec.symbol.defaultType)
          }
        )
      }
    }
    state.onFunctionGenerated(getter.symbol)
    return getter
  }

  // Creates setXxx(value) that delegates to the DEFAULT setter on the shared instance via EQ.
  private fun createSharedSetter(name: Name, field: IrField): IrFunction {
    val property = field.correspondingPropertySymbol?.owner
      ?: error("Shared field ${field.name} has no backing property")
    val defaultSetter = property.setter
      ?: error("Shared field ${field.name} has no DEFAULT setter")
    val setter = spec.addMemberFunction(name, irBuiltIns.unitType)
    val valueParam = setter.addValueParameter("value", field.type)
    setter.body = with(irBuilder(setter.symbol)) {
      irBlockBody {
        +irCall(defaultSetter.symbol, irBuiltIns.unitType, origin = IrStatementOrigin.EQ).apply {
          arguments[0] = irAs(irGetSharedInstance(setter), spec.symbol.defaultType)
          arguments[1] = irGet(valueParam)
        }
      }
    }
    state.onFunctionGenerated(setter.symbol)
    return setter
  }

  // Update the body of an existing getter to route through specificationContext.sharedInstance.
  // Uses direct GET_FIELD (not CALL to DEFAULT getter) to avoid infinite recursion.
  private fun updateSharedGetterBody(getter: IrSimpleFunction, field: IrField) {
    getter.body = with(irBuilder(getter.symbol)) {
      irBlockBody {
        +irReturn(
          irGetField(irAs(irGetSharedInstance(getter), spec.symbol.defaultType), field, type = getter.returnType)
        )
      }
    }
  }

  // Update the body of an existing setter to route through specificationContext.sharedInstance.
  // Uses direct SET_FIELD (not CALL to DEFAULT setter) to avoid infinite recursion.
  private fun updateSharedSetterBody(setter: IrSimpleFunction, field: IrField) {
    val valueParam = setter.parameters.first { it.name.asString() != "<this>" }
    setter.body = with(irBuilder(setter.symbol)) {
      irBlockBody {
        +irSetField(
          receiver = irAs(irGetSharedInstance(setter), spec.symbol.defaultType),
          field = field,
          value = irGet(valueParam)
        )
      }
    }
  }

  private fun irGetSharedInstance(fn: IrFunction): IrExpression {
    val specContextGetter = findSpecificationContextGetter()
    val sharedInstanceGetter = findSharedInstanceGetter()
    val specContextClass = generatorContext.findRequiredClassSymbol(SPECIFICATION_CONTEXT_FQN)

    return with(irBuilder(fn.symbol)) {
      val thisParam = fn.parameters.first { it.name.asString() == "<this>" }
      val specContextCall =
        irCall(specContextGetter.symbol, specContextGetter.returnType, origin = IrStatementOrigin.GET_PROPERTY)
          .apply { dispatchReceiver = irGetThis(thisParam) }
      val castSpecContext = irAs(specContextCall, specContextClass.defaultType)
      irCall(sharedInstanceGetter.symbol, sharedInstanceGetter.returnType, origin = IrStatementOrigin.GET_PROPERTY)
        .apply { dispatchReceiver = castSpecContext }
    }
  }

  private fun findSpecificationContextGetter(): IrSimpleFunction {
    var clazz: IrClass? = spec
    while (clazz != null) {
      val getter = clazz.declarations
        .filterIsInstance<IrSimpleFunction>()
        .find { it.name.asString() == "getSpecificationContext" }
      if (getter != null) return getter
      clazz = clazz.superTypes.firstOrNull()?.let {
        val fqn = (it as? IrSimpleType)
          ?.classifier
          ?.let { c -> (c as? IrClassSymbol)?.owner?.fqNameWhenAvailable }
          ?: return@let null
        generatorContext.findRequiredClassSymbol(fqn).owner
      }
    }
    val specInternalsClass = generatorContext.findRequiredClassSymbol(SPEC_INTERNALS_FQN)
    return specInternalsClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { it.name.asString() == "getSpecificationContext" }
  }

  private fun findSharedInstanceGetter(): IrSimpleFunction {
    val specContextClass = generatorContext.findRequiredClassSymbol(SPECIFICATION_CONTEXT_FQN)
    return specContextClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { it.name.asString() == "getSharedInstance" }
  }
}
