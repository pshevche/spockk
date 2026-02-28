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

package io.github.pshevche.spockk.compilation.transformer.fields

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType

/**
 * Accumulates all state produced by field transformations. Internals are hidden behind
 * listener-style write methods and typed read methods, so strategies never access raw maps.
 */
internal class FieldRewriteState {
  private val fieldGetters = mutableMapOf<IrFieldSymbol, IrSimpleFunction>()
  private val fieldSetters = mutableMapOf<IrFieldSymbol, IrSimpleFunction>()
  private val getterReplacements = mutableMapOf<IrSimpleFunctionSymbol, IrSimpleFunction>()
  private val setterReplacements = mutableMapOf<IrSimpleFunctionSymbol, IrSimpleFunction>()
  private val getterTypeUpdates = mutableMapOf<IrSimpleFunctionSymbol, IrType>()
  private val generated = mutableSetOf<IrSimpleFunctionSymbol>()

  // Lazily-created initializer methods shared across all field strategies
  var instanceFieldsInitMethod: IrSimpleFunction? = null
  var sharedFieldsInitMethod: IrSimpleFunction? = null

  // -- write (listener) API --

  fun onFieldGetterCreated(fieldSymbol: IrFieldSymbol, getter: IrSimpleFunction) {
    fieldGetters[fieldSymbol] = getter
  }

  fun onFieldSetterCreated(fieldSymbol: IrFieldSymbol, setter: IrSimpleFunction) {
    fieldSetters[fieldSymbol] = setter
  }

  fun onGetterReplaced(originalSymbol: IrSimpleFunctionSymbol, replacement: IrSimpleFunction) {
    getterReplacements[originalSymbol] = replacement
  }

  fun onSetterReplaced(originalSymbol: IrSimpleFunctionSymbol, replacement: IrSimpleFunction) {
    setterReplacements[originalSymbol] = replacement
  }

  fun onGetterTypeUpdated(getterSymbol: IrSimpleFunctionSymbol, newType: IrType) {
    getterTypeUpdates[getterSymbol] = newType
  }

  fun onFunctionGenerated(symbol: IrSimpleFunctionSymbol) {
    generated += symbol
  }

  // -- read API for FieldReferenceReplacer / ParentSharedFieldRegistrar --

  fun fieldGetter(symbol: IrFieldSymbol): IrSimpleFunction? = fieldGetters[symbol]
  fun fieldSetter(symbol: IrFieldSymbol): IrSimpleFunction? = fieldSetters[symbol]
  fun getterReplacement(symbol: IrSimpleFunctionSymbol): IrSimpleFunction? = getterReplacements[symbol]
  fun setterReplacement(symbol: IrSimpleFunctionSymbol): IrSimpleFunction? = setterReplacements[symbol]
  fun getterTypeUpdate(symbol: IrSimpleFunctionSymbol): IrType? = getterTypeUpdates[symbol]
  fun isGenerated(symbol: IrSimpleFunctionSymbol): Boolean = symbol in generated

  fun isEmpty(): Boolean =
    fieldGetters.isEmpty() &&
      fieldSetters.isEmpty() &&
      getterReplacements.isEmpty() &&
      setterReplacements.isEmpty() &&
      getterTypeUpdates.isEmpty()
}
