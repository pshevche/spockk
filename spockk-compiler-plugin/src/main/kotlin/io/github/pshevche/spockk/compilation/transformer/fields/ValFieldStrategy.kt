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
import io.github.pshevche.spockk.compilation.ir.addMemberFunction
import io.github.pshevche.spockk.compilation.ir.makeMutable
import io.github.pshevche.spockk.compilation.ir.makePrivate
import io.github.pshevche.spockk.compilation.transformer.InternalIdentifiers
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.Name

/**
 * Handles instance val fields (non-lateinit):
 * - Renames backing field and property to $spock_finalField_<name>
 * - Makes field and property private var (removes final, changes visibility)
 * - Excludes the DEFAULT getter from reference replacement
 * - If initialized: creates DEFAULT setter, moves initializer to $spock_initializeFields(),
 *   makes field nullable with null default
 * - Generates explicit getXxx() method that delegates to the DEFAULT getter via GET_PROPERTY
 */
internal class ValFieldStrategy(
  private val fieldCtx: FieldContext,
  state: FieldRewriteState,
  context: IrGeneratorContext,
  spec: IrClass
) : FieldStrategyBase(context, spec, state),
  SingleFieldRewriterStrategy {

  override fun transform(property: IrProperty) {
    val field = property.backingField ?: return
    val originalFieldSymbol = field.symbol
    val originalGetter = property.getter

    annotateField(field, fieldCtx)

    // Rename field and property to the Spock-internal name
    val newName = InternalIdentifiers.getFinalFieldName(fieldCtx.name)
    field.name = newName
    property.name = newName
    property.getter?.name = Name.special("<get-${newName.asString()}>")

    // Make private var (remove final, change visibility)
    field.makePrivate()
    property.makePrivate()
    field.makeMutable()
    property.makeMutable()

    // Exclude the DEFAULT getter from reference replacement to preserve its direct GET_FIELD body.
    // The explicit getXxx() method (created below) is what feature methods use.
    originalGetter?.let { state.onFunctionGenerated(it.symbol) }

    if (fieldCtx.hasInitializer) {
      // Create DEFAULT_PROPERTY_ACCESSOR setter before moving initializer so
      // moveInitializerToInstanceInit can use a setter CALL (matching expected IR)
      createValFieldSetter(property, field, newName)
      moveInitializerToInstanceInit(field, property)
      makeFieldNullableWithNullDefault(field)
    }

    // Generate explicit getter: fun get<OriginalName>(): T? = $spock_finalField_xxx
    val getter = createValGetter(
      Name.identifier("get${fieldCtx.name.replaceFirstChar { it.uppercaseChar() }}"),
      property,
      field
    )

    // Register for reference replacement
    originalGetter?.let { state.onGetterReplaced(it.symbol, getter) }
    state.onFieldGetterCreated(originalFieldSymbol, getter)
  }

  // Creates an explicit getter that delegates to the DEFAULT property getter via GET_PROPERTY origin.
  // This matches the expected IR: `fun getAnswer(): Int? = $spock_finalField_answer`
  private fun createValGetter(name: Name, property: IrProperty, field: IrField): IrSimpleFunction {
    val getter = spec.addMemberFunction(name, field.type) as IrSimpleFunction
    val thisParam = getter.parameters.first { it.name.asString() == "<this>" }
    val defaultGetter = property.getter
    getter.body = with(irBuilder(getter.symbol)) {
      irBlockBody {
        +irReturn(
          if (defaultGetter != null) {
            irCall(defaultGetter.symbol, field.type, origin = IrStatementOrigin.GET_PROPERTY).apply {
              arguments[0] = IrGetValueImpl(
                SYNTHETIC_OFFSET,
                SYNTHETIC_OFFSET,
                thisParam.type,
                thisParam.symbol,
                IrStatementOrigin.IMPLICIT_ARGUMENT
              )
            }
          } else {
            irGetField(irGet(thisParam), field)
          }
        )
      }
    }
    state.onFunctionGenerated(getter.symbol)
    return getter
  }
}
