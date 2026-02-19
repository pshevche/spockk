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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FieldContext
import io.github.pshevche.spockk.compilation.ir.addMemberFunction
import io.github.pshevche.spockk.compilation.ir.irAnnotation
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import io.github.pshevche.spockk.compilation.ir.mutableStatements
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.name.Name

internal class FieldRewriter(
  override val context: IrGeneratorContext,
  private val spec: IrClass,
  private val fields: List<FieldContext>
) : SpockkIrRewriter {

  companion object {
    private const val FIELD_METADATA_FQN = "org.spockframework.runtime.model.FieldMetadata"
    private const val VOLATILE_FQN = "kotlin.jvm.Volatile"
    private const val SPECIFICATION_CONTEXT_FQN = "org.spockframework.runtime.SpecificationContext"
  }

  // Maps original field symbol → new getter (for IrGetField replacement)
  private val fieldGetters = mutableMapOf<IrFieldSymbol, IrSimpleFunction>()

  // Maps original field symbol → new setter (for IrSetField replacement)
  private val fieldSetters = mutableMapOf<IrFieldSymbol, IrSimpleFunction>()

  // Maps original property getter symbol → new getter (for IrCall replacement)
  private val getterReplacements = mutableMapOf<IrSimpleFunctionSymbol, IrSimpleFunction>()

  // Maps original property setter symbol → new setter (for IrCall replacement)
  private val setterReplacements = mutableMapOf<IrSimpleFunctionSymbol, IrSimpleFunction>()

  // Maps getter symbol → updated nullable return type (for in-place CALL type update)
  private val getterTypeUpdates = mutableMapOf<IrSimpleFunctionSymbol, IrType>()

  // Lazily created initializer methods
  private var instanceFieldsInitMethod: IrSimpleFunction? = null
  private var sharedFieldsInitMethod: IrSimpleFunction? = null

  // Symbols of generated functions to skip during reference replacement
  private val generatedFunctionSymbols = mutableSetOf<IrSimpleFunctionSymbol>()

  fun rewrite() {
    fields.forEach { rewriteField(it) }
    replaceFieldReferences()
  }

  private fun rewriteField(fieldCtx: FieldContext) {
    val backingField = fieldCtx.property.backingField ?: return

    // Add @FieldMetadata annotation to the backing field
    annotateField(backingField, fieldCtx)

    when {
      fieldCtx.isShared -> rewriteSharedField(backingField, fieldCtx)
      !fieldCtx.isLateinit && fieldCtx.isVal -> rewriteValField(backingField, fieldCtx)
      else -> rewriteVarField(backingField, fieldCtx)
    }
  }

  private fun annotateField(field: IrField, fieldCtx: FieldContext) {
    with(irBuilder(field.symbol)) {
      field.annotations += irAnnotation(
        FIELD_METADATA_FQN,
        irString(fieldCtx.name),
        irInt(fieldCtx.ordinal),
        irInt(fieldCtx.line),
        fieldCtx.hasInitializer.toIrConst(irBuiltIns.booleanType)
      )
    }
  }

  // Instance var field (including lateinit): just make private
  private fun rewriteVarField(field: IrField, fieldCtx: FieldContext) {
    makePrivate(field, fieldCtx.property)

    if (fieldCtx.hasInitializer && !fieldCtx.isLateinit) {
      moveInitializerToInstanceInit(field, fieldCtx)
      makeNullableWithNullDefault(field)
      // Track getter so CALL expressions in feature methods get their type updated
      fieldCtx.property.getter?.let { getter ->
        getterTypeUpdates[getter.symbol] = getter.returnType
      }
    }
  }

  // Instance val field: rename, make private var, generate getter, move init
  private fun rewriteValField(field: IrField, fieldCtx: FieldContext) {
    val originalFieldSymbol = field.symbol
    val originalGetter = fieldCtx.property.getter

    // Rename field and property
    val newName = InternalIdentifiers.getFinalFieldName(fieldCtx.name)
    field.name = newName
    fieldCtx.property.name = newName
    fieldCtx.property.getter?.name = Name.special("<get-${newName.asString()}>")

    // Make private var (remove final, change visibility)
    makePrivate(field, fieldCtx.property)
    makeMutable(field, fieldCtx.property)

    // Exclude the DEFAULT getter from reference replacement to preserve its direct GET_FIELD body.
    // The explicit getXxx() method (created below) is what feature methods use.
    originalGetter?.let { generatedFunctionSymbols += it.symbol }

    if (fieldCtx.hasInitializer) {
      // Create DEFAULT_PROPERTY_ACCESSOR setter before moving initializer so
      // moveInitializerToInstanceInit can use a setter CALL (matching expected IR)
      createValFieldSetter(fieldCtx.property, field, newName)
      moveInitializerToInstanceInit(field, fieldCtx)
      makeNullableWithNullDefault(field)
    }

    // Generate explicit getter: fun get<OriginalName>(): T? = $spock_finalField_xxx
    val getter = createValGetter(
      Name.identifier("get${fieldCtx.name.replaceFirstChar { it.uppercaseChar() }}"),
      fieldCtx.property,
      field
    )

    // Register for reference replacement
    originalGetter?.let { getterReplacements[it.symbol] = getter }
    fieldGetters[originalFieldSymbol] = getter
  }

  // Shared field: rename, make protected volatile, generate getter+setter, move init
  private fun rewriteSharedField(field: IrField, fieldCtx: FieldContext) {
    val originalFieldSymbol = field.symbol
    val originalGetter = fieldCtx.property.getter
    val originalSetter = fieldCtx.property.setter
    val isOverride = fieldCtx.property.overriddenSymbols.isNotEmpty()

    // Rename backing field (always)
    val newName = InternalIdentifiers.getSharedFieldName(fieldCtx.name)
    field.name = newName

    if (!isOverride) {
      // For non-override properties: also rename the property and its accessors
      fieldCtx.property.name = newName
      fieldCtx.property.getter?.name = Name.special("<get-${newName.asString()}>")
      fieldCtx.property.setter?.name = Name.special("<set-${newName.asString()}>")
    }

    // Make protected volatile mutable
    makeProtected(field, fieldCtx.property)
    makeMutable(field, fieldCtx.property)
    addVolatileAnnotation(field)

    if (isOverride) {
      // For override properties: update existing getter/setter bodies to route through
      // specificationContext.sharedInstance, preserving the interface-declared signatures.
      // Skip them in reference replacement (their bodies directly access the shared field).
      if (originalGetter != null) {
        updateSharedGetterBody(originalGetter, field)
        generatedFunctionSymbols += originalGetter.symbol
      }
      if (!fieldCtx.isVal && originalSetter != null) {
        updateSharedSetterBody(originalSetter, field)
        generatedFunctionSymbols += originalSetter.symbol
      }
    } else {
      // Preserve DEFAULT getter/setter bodies (GET_FIELD/SET_FIELD) by skipping them
      // during reference replacement. Feature method references are redirected to
      // the generated getXxx()/setXxx() methods below.
      originalGetter?.let { generatedFunctionSymbols += it.symbol }
      originalSetter?.let { generatedFunctionSymbols += it.symbol }

      // For shared val with initializer: create DEFAULT setter so init method can use
      // a setter CALL (matching the expected IR from `$spock_sharedField_x = value`).
      if (fieldCtx.isVal && fieldCtx.hasInitializer && !fieldCtx.isLateinit) {
        createValFieldSetter(fieldCtx.property, field, newName)
      }
    }

    if (fieldCtx.hasInitializer && !fieldCtx.isLateinit) {
      moveInitializerToSharedInit(field, fieldCtx)
      makeNullableWithNullDefault(field)
    }

    if (!isOverride) {
      // For non-override properties: generate new getter/setter functions
      val getter = createSharedGetter(
        Name.identifier("get${fieldCtx.name.replaceFirstChar { it.uppercaseChar() }}"),
        field
      )
      originalGetter?.let { getterReplacements[it.symbol] = getter }
      fieldGetters[originalFieldSymbol] = getter

      if (!fieldCtx.isVal) {
        val setter = createSharedSetter(
          Name.identifier("set${fieldCtx.name.replaceFirstChar { it.uppercaseChar() }}"),
          field
        )
        originalSetter?.let { setterReplacements[it.symbol] = setter }
        fieldSetters[originalFieldSymbol] = setter
      }
    }
  }

  // --- Visibility helpers ---

  private fun makePrivate(field: IrField, property: IrProperty) {
    field.visibility = DescriptorVisibilities.PRIVATE
    property.visibility = DescriptorVisibilities.PRIVATE
    property.getter?.visibility = DescriptorVisibilities.PRIVATE
    property.setter?.visibility = DescriptorVisibilities.PRIVATE
  }

  private fun makeProtected(field: IrField, property: IrProperty) {
    field.visibility = DescriptorVisibilities.PROTECTED
    property.visibility = DescriptorVisibilities.PROTECTED
    property.getter?.visibility = DescriptorVisibilities.PROTECTED
    property.setter?.visibility = DescriptorVisibilities.PROTECTED
  }

  // --- Mutability helpers ---

  private fun makeMutable(field: IrField, property: IrProperty) {
    field.isFinal = false
    property.isVar = true
  }

  // --- Initializer movement ---

  private fun moveInitializerToInstanceInit(field: IrField, fieldCtx: FieldContext) {
    val initExpr = field.initializer?.expression ?: return
    val initMethod = getOrCreateInstanceFieldsInit()
    val setter = fieldCtx.property.setter
    if (setter != null) {
      addSetterCallStatement(initMethod, setter, initExpr)
    } else {
      addFieldInitStatement(initMethod, field, initExpr)
    }
    field.initializer = null
  }

  private fun moveInitializerToSharedInit(field: IrField, fieldCtx: FieldContext) {
    val initExpr = field.initializer?.expression ?: return
    val initMethod = getOrCreateSharedFieldsInit()
    val setter = fieldCtx.property.setter
    if (setter != null) {
      addSetterCallStatement(initMethod, setter, initExpr)
    } else {
      addFieldInitStatement(initMethod, field, initExpr)
    }
    field.initializer = null
  }

  // Generates a CALL to the property setter (origin=EQ, dispatch receiver origin=IMPLICIT_ARGUMENT).
  // This matches the IR that Kotlin generates for `property = value` statements.
  private fun addSetterCallStatement(
    initMethod: IrSimpleFunction,
    setter: IrSimpleFunction,
    value: IrExpression
  ) {
    val dispatchReceiver = initMethod.parameters.first { it.name.asString() == "<this>" }
    val reboundValue = rebindDispatchReceiverReferences(value, dispatchReceiver)
    val call = with(irBuilder(initMethod.symbol)) {
      irCall(setter.symbol, irBuiltIns.unitType, origin = IrStatementOrigin.EQ).apply {
        arguments[0] = IrGetValueImpl(
          SYNTHETIC_OFFSET,
          SYNTHETIC_OFFSET,
          dispatchReceiver.type,
          dispatchReceiver.symbol,
          IrStatementOrigin.IMPLICIT_ARGUMENT
        )
        arguments[1] = reboundValue
      }
    }
    initMethod.mutableStatements()?.add(call)
  }

  private fun addFieldInitStatement(initMethod: IrSimpleFunction, field: IrField, value: IrExpression) {
    val dispatchReceiver = initMethod.parameters.first { it.name.asString() == "<this>" }
    // Rebind any 'this' references in the initializer to the init method's dispatch receiver
    val reboundValue = rebindDispatchReceiverReferences(value, dispatchReceiver)
    val setFieldStmt = with(irBuilder(initMethod.symbol)) {
      irSetField(
        receiver = irGet(dispatchReceiver),
        field = field,
        value = reboundValue
      )
    }
    initMethod.mutableStatements()?.add(setFieldStmt)
  }

  // Replaces IrGetValue references to any dispatch receiver '<this>' that is not the targetParam
  // with a reference to targetParam. This is needed when moving field initializers to a new method.
  private fun rebindDispatchReceiverReferences(
    expr: IrExpression,
    targetParam: IrValueParameter
  ): IrExpression {
    val rebinder = object : IrElementTransformerVoid() {
      override fun visitGetValue(expression: IrGetValue): IrExpression {
        val paramOwner = expression.symbol.owner
        if (paramOwner is IrValueParameter &&
          paramOwner.name.asString() == "<this>" &&
          paramOwner.symbol != targetParam.symbol
        ) {
          return irBuilder(targetParam.symbol).irGet(targetParam)
        }
        return super.visitGetValue(expression)
      }
    }
    return expr.transform(rebinder, null)
  }

  private fun makeNullableWithNullDefault(field: IrField) {
    val nullableType = field.type.makeNullable()
    field.type = nullableType

    val property = field.correspondingPropertySymbol?.owner

    // Update getter return type and the GET_FIELD expression type inside the getter body
    val getter = property?.getter
    if (getter != null) {
      getter.returnType = nullableType
      getter.body?.transform(object : IrElementTransformerVoid() {
        override fun visitGetField(expression: IrGetField): IrExpression {
          if (expression.symbol == field.symbol) {
            expression.type = nullableType
          }
          return super.visitGetField(expression)
        }
      }, null)
    }

    // Update setter parameter type and GET_VAR expressions referencing it in setter body
    val setter = property?.setter
    if (setter != null) {
      val valueParam = setter.parameters.firstOrNull { it.name.asString() != "<this>" }
      if (valueParam != null) {
        valueParam.type = nullableType
        setter.body?.transform(object : IrElementTransformerVoid() {
          override fun visitGetValue(expression: IrGetValue): IrExpression {
            if (expression.symbol == valueParam.symbol) {
              expression.type = nullableType
            }
            return super.visitGetValue(expression)
          }
        }, null)
      }
    }

    field.initializer = with(irBuilder(field.symbol)) { irExprBody(irNull()) }
  }

  private fun addVolatileAnnotation(field: IrField) {
    field.annotations += with(irBuilder(field.symbol)) {
      irAnnotation(VOLATILE_FQN)
    }
  }

  // --- Initializer method creation ---

  private fun getOrCreateInstanceFieldsInit(): IrSimpleFunction {
    return instanceFieldsInitMethod ?: createInitMethod(InternalIdentifiers.INITIALIZE_FIELDS_METHOD)
      .also { instanceFieldsInitMethod = it }
  }

  private fun getOrCreateSharedFieldsInit(): IrSimpleFunction {
    return sharedFieldsInitMethod ?: createInitMethod(InternalIdentifiers.INITIALIZE_SHARED_FIELDS_METHOD)
      .also { sharedFieldsInitMethod = it }
  }

  private fun createInitMethod(name: Name): IrSimpleFunction {
    val method = spec.addMemberFunction(name, irBuiltIns.unitType) as IrSimpleFunction
    method.visibility = DescriptorVisibilities.PRIVATE
    method.body = with(irBuilder(method.symbol)) {
      irBlockBody { }
    }
    generatedFunctionSymbols += method.symbol
    return method
  }

  // --- Getter/setter creation ---

  // Creates a DEFAULT_PROPERTY_ACCESSOR setter for a val field that was made mutable.
  // This allows $spock_initializeFields to call the setter (matching expected IR).
  // Note: removed from spec.declarations since DEFAULT_PROPERTY_ACCESSORs are only accessible
  // as children of their property, not as top-level class declarations.
  private fun createValFieldSetter(
    property: IrProperty,
    field: IrField,
    setterName: Name
  ): IrSimpleFunction {
    val setter = spec.addMemberFunction(
      Name.special("<set-${setterName.asString()}>"),
      irBuiltIns.unitType
    ) as IrSimpleFunction
    // Remove from top-level declarations - DEFAULT_PROPERTY_ACCESSORs live only in the property
    spec.declarations.remove(setter)
    setter.origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    setter.visibility = DescriptorVisibilities.PRIVATE
    setter.correspondingPropertySymbol = property.symbol
    property.setter = setter

    val valueParam = setter.addValueParameter(Name.special("<set-?>"), field.type)
    val thisParam = setter.parameters.first { it.name.asString() == "<this>" }
    setter.body = with(irBuilder(setter.symbol)) {
      irBlockBody {
        +irSetField(
          receiver = irGet(thisParam),
          field = field,
          value = irGet(valueParam)
        )
      }
    }
    generatedFunctionSymbols += setter.symbol
    return setter
  }

  // Creates an explicit getter (e.g. getAnswer()) that delegates to the DEFAULT property getter.
  // This matches the expected IR where `fun getAnswer(): Int? = $spock_finalField_answer`
  // accesses the property via its getter (origin=GET_PROPERTY), not GET_FIELD.
  private fun createValGetter(name: Name, property: IrProperty, field: IrField): IrSimpleFunction {
    val getter = spec.addMemberFunction(name, field.type) as IrSimpleFunction
    val thisParam = getter.parameters.first { it.name.asString() == "<this>" }
    val defaultGetter = property.getter
    getter.body = with(irBuilder(getter.symbol)) {
      irBlockBody {
        +irReturn(
          if (defaultGetter != null) {
            irCall(defaultGetter.symbol, field.type, origin = IrStatementOrigin.GET_PROPERTY).apply {
              // Use IMPLICIT_ARGUMENT origin to match the IR Kotlin generates for
              // implicit 'this' receivers in property access expressions
              arguments[0] = IrGetValueImpl(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                thisParam.type, thisParam.symbol,
                IrStatementOrigin.IMPLICIT_ARGUMENT
              )
            }
          } else {
            irGetField(irGet(thisParam), field)
          }
        )
      }
    }
    generatedFunctionSymbols += getter.symbol
    return getter
  }

  // Creates getXxx() that delegates to the DEFAULT getter on the shared instance:
  // `fun getXxx(): T? = (specificationContext.sharedInstance as Spec).$spock_sharedField_xxx`
  // This matches the expected IR where a CALL to the DEFAULT getter is dispatched on the cast instance.
  private fun createSharedGetter(name: Name, field: IrField): IrSimpleFunction {
    val property = field.correspondingPropertySymbol?.owner
      ?: error("Shared field ${field.name} has no backing property")
    val defaultGetter = property.getter
      ?: error("Shared field ${field.name} has no DEFAULT getter")
    val getter = spec.addMemberFunction(name, field.type) as IrSimpleFunction
    getter.body = with(irBuilder(getter.symbol)) {
      irBlockBody {
        +irReturn(
          irCall(defaultGetter.symbol, field.type, origin = IrStatementOrigin.GET_PROPERTY).apply {
            arguments[0] = irAs(irGetSharedInstance(getter), spec.defaultType)
          }
        )
      }
    }
    generatedFunctionSymbols += getter.symbol
    return getter
  }

  // Creates setXxx(value) that delegates to the DEFAULT setter on the shared instance:
  // `fun setXxx(value: T?) { (specificationContext.sharedInstance as Spec).$spock_sharedField_xxx = value }`
  private fun createSharedSetter(name: Name, field: IrField): IrSimpleFunction {
    val property = field.correspondingPropertySymbol?.owner
      ?: error("Shared field ${field.name} has no backing property")
    val defaultSetter = property.setter
      ?: error("Shared field ${field.name} has no DEFAULT setter")
    val setter = spec.addMemberFunction(name, irBuiltIns.unitType) as IrSimpleFunction
    val valueParam = setter.addValueParameter("value", field.type)
    setter.body = with(irBuilder(setter.symbol)) {
      irBlockBody {
        +irCall(defaultSetter.symbol, irBuiltIns.unitType, origin = IrStatementOrigin.EQ).apply {
          arguments[0] = irAs(irGetSharedInstance(setter), spec.defaultType)
          arguments[1] = irGet(valueParam)
        }
      }
    }
    generatedFunctionSymbols += setter.symbol
    return setter
  }

  // Update the body of an existing getter to route through specificationContext.sharedInstance
  // Used for override shared fields to preserve interface-declared signatures
  private fun updateSharedGetterBody(getter: IrSimpleFunction, field: IrField) {
    val property = field.correspondingPropertySymbol?.owner
    val defaultGetter = property?.getter
    getter.body = with(irBuilder(getter.symbol)) {
      irBlockBody {
        +irReturn(
          if (defaultGetter != null) {
            irCall(defaultGetter.symbol, getter.returnType, origin = IrStatementOrigin.GET_PROPERTY).apply {
              arguments[0] = irAs(irGetSharedInstance(getter), spec.defaultType)
            }
          } else {
            irGetField(irAs(irGetSharedInstance(getter), spec.defaultType), field, type = getter.returnType)
          }
        )
      }
    }
  }

  // Update the body of an existing setter to route through specificationContext.sharedInstance
  // Used for override shared fields to preserve interface-declared signatures
  private fun updateSharedSetterBody(setter: IrSimpleFunction, field: IrField) {
    val property = field.correspondingPropertySymbol?.owner
    val defaultSetter = property?.setter
    val valueParam = setter.parameters.first { it.name.asString() != "<this>" }
    setter.body = with(irBuilder(setter.symbol)) {
      irBlockBody {
        if (defaultSetter != null) {
          +irCall(defaultSetter.symbol, irBuiltIns.unitType, origin = IrStatementOrigin.EQ).apply {
            arguments[0] = irAs(irGetSharedInstance(setter), spec.defaultType)
            arguments[1] = irGet(valueParam)
          }
        } else {
          +irSetField(
            receiver = irAs(irGetSharedInstance(setter), spec.defaultType),
            field = field,
            value = irGet(valueParam)
          )
        }
      }
    }
  }

  // Build `this.specificationContext.sharedInstance` expression (returns Specification?).
  // Callers wrap with irAs(..., spec.defaultType) to cast to the spec class.
  private fun irGetSharedInstance(fn: IrFunction): IrExpression {
    val specContextGetter = findSpecificationContextGetter()
    val sharedInstanceGetter = findSharedInstanceGetter()

    return with(irBuilder(fn.symbol)) {
      val thisParam = fn.parameters.first { it.name.asString() == "<this>" }
      val specContextCall = irCall(specContextGetter.symbol, specContextGetter.returnType).apply {
        dispatchReceiver = irGet(thisParam)
      }
      irCall(sharedInstanceGetter.symbol, sharedInstanceGetter.returnType).apply {
        dispatchReceiver = specContextCall
      }
    }
  }

  private fun findSpecificationContextGetter(): IrSimpleFunction {
    // Look in the spec's superclass hierarchy for getSpecificationContext()
    var clazz: IrClass? = spec
    while (clazz != null) {
      val getter = clazz.declarations
        .filterIsInstance<IrSimpleFunction>()
        .find { it.name.asString() == "getSpecificationContext" }
      if (getter != null) return getter
      clazz = clazz.superTypes.firstOrNull()?.let {
        context.findRequiredClassSymbol(it.classFqn() ?: return@let null).owner
      }
    }
    // Fall back to SpecInternals
    val specInternalsClass = context.findRequiredClassSymbol("org.spockframework.runtime.SpecInternals")
    return specInternalsClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { it.name.asString() == "getSpecificationContext" }
  }

  private fun findSharedInstanceGetter(): IrSimpleFunction {
    val specContextClass = context.findRequiredClassSymbol(SPECIFICATION_CONTEXT_FQN)
    return specContextClass.owner.declarations
      .filterIsInstance<IrSimpleFunction>()
      .first { it.name.asString() == "getSharedInstance" }
  }

  // --- Reference replacement ---

  private fun replaceFieldReferences() {
    if (fieldGetters.isEmpty() && fieldSetters.isEmpty() &&
      getterReplacements.isEmpty() && setterReplacements.isEmpty() &&
      getterTypeUpdates.isEmpty()
    ) return

    val replacer = FieldReferenceReplacer()
    spec.declarations.forEach { decl ->
      if (decl is IrFunction || decl is IrProperty) {
        // Skip generated functions to avoid recursive replacement in their bodies
        if (decl is IrSimpleFunction && decl.symbol in generatedFunctionSymbols) return@forEach
        decl.accept(replacer, null)
      }
    }
  }

  private inner class FieldReferenceReplacer : IrElementTransformerVoidWithContext() {
    // Skip functions whose bodies must not be transformed (e.g. DEFAULT_PROPERTY_ACCESSORs
    // for renamed val fields, whose GET_FIELD body should remain as-is)
    override fun visitSimpleFunction(declaration: IrSimpleFunction) =
      if (declaration.symbol in generatedFunctionSymbols) declaration
      else super.visitSimpleFunction(declaration)

    override fun visitGetField(expression: IrGetField): IrExpression {
      val getter = fieldGetters[expression.symbol]
      if (getter != null) {
        return with(irBuilder(getter.symbol)) {
          irCall(getter.symbol, getter.returnType).apply {
            dispatchReceiver = expression.receiver
          }
        }
      }
      return super.visitGetField(expression)
    }

    override fun visitSetField(expression: IrSetField): IrExpression {
      val setter = fieldSetters[expression.symbol]
      if (setter != null) {
        return with(irBuilder(setter.symbol)) {
          irCall(setter.symbol, irBuiltIns.unitType).apply {
            dispatchReceiver = expression.receiver
            arguments[1] = expression.value
          }
        }
      }
      return super.visitSetField(expression)
    }

    override fun visitCall(expression: IrCall): IrExpression {
      // For var fields that became nullable: update the CALL type in-place
      val updatedType = getterTypeUpdates[expression.symbol]
      if (updatedType != null) {
        expression.type = updatedType
        return super.visitCall(expression)
      }

      val getterReplacement = getterReplacements[expression.symbol]
      if (getterReplacement != null) {
        return with(irBuilder(getterReplacement.symbol)) {
          irCall(getterReplacement.symbol, getterReplacement.returnType).apply {
            dispatchReceiver = expression.dispatchReceiver
          }
        }
      }

      val setterReplacement = setterReplacements[expression.symbol]
      if (setterReplacement != null) {
        return with(irBuilder(setterReplacement.symbol)) {
          irCall(setterReplacement.symbol, irBuiltIns.unitType).apply {
            dispatchReceiver = expression.dispatchReceiver
            arguments[1] = expression.arguments[1]
          }
        }
      }

      return super.visitCall(expression)
    }
  }
}

private fun org.jetbrains.kotlin.ir.types.IrType.classFqn(): String? =
  (this as? org.jetbrains.kotlin.ir.types.IrSimpleType)
    ?.classifier
    ?.let { it as? org.jetbrains.kotlin.ir.symbols.IrClassSymbol }
    ?.owner
    ?.fqNameWhenAvailable
    ?.asString()
