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

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType

/**
 * Accumulates all state produced by field transformations. Internals are hidden behind
 * listener-style write methods and typed read methods, so strategies never access raw maps.
 */
internal class FieldRewriteState {
  private val fieldGetters = mutableMapOf<IrFieldSymbol, IrFunction>()
  private val fieldSetters = mutableMapOf<IrFieldSymbol, IrFunction>()
  private val getterReplacements = mutableMapOf<IrFunctionSymbol, IrFunction>()
  private val setterReplacements = mutableMapOf<IrFunctionSymbol, IrFunction>()
  private val getterTypeUpdates = mutableMapOf<IrFunctionSymbol, IrType>()
  private val generated = mutableSetOf<IrFunctionSymbol>()

  // Lazily-created initializer methods shared across all field strategies
  var instanceFieldsInitMethod: IrFunction? = null
  var sharedFieldsInitMethod: IrFunction? = null

  // -- write (listener) API --

  fun onFieldGetterCreated(fieldSymbol: IrFieldSymbol, getter: IrFunction) {
    fieldGetters[fieldSymbol] = getter
  }

  fun onFieldSetterCreated(fieldSymbol: IrFieldSymbol, setter: IrFunction) {
    fieldSetters[fieldSymbol] = setter
  }

  fun onGetterReplaced(originalSymbol: IrFunctionSymbol, replacement: IrFunction) {
    getterReplacements[originalSymbol] = replacement
  }

  fun onSetterReplaced(originalSymbol: IrFunctionSymbol, replacement: IrFunction) {
    setterReplacements[originalSymbol] = replacement
  }

  fun onGetterTypeUpdated(getterSymbol: IrFunctionSymbol, newType: IrType) {
    getterTypeUpdates[getterSymbol] = newType
  }

  fun onFunctionGenerated(symbol: IrFunctionSymbol) {
    generated += symbol
  }

  // -- read API for FieldReferenceReplacer / ParentSharedFieldRegistrar --

  fun fieldGetter(symbol: IrFieldSymbol): IrFunction? = fieldGetters[symbol]
  fun fieldSetter(symbol: IrFieldSymbol): IrFunction? = fieldSetters[symbol]
  fun getterReplacement(symbol: IrFunctionSymbol): IrFunction? = getterReplacements[symbol]
  fun setterReplacement(symbol: IrFunctionSymbol): IrFunction? = setterReplacements[symbol]
  fun getterTypeUpdate(symbol: IrFunctionSymbol): IrType? = getterTypeUpdates[symbol]
  fun isGenerated(symbol: IrFunctionSymbol): Boolean = symbol in generated

  fun isEmpty(): Boolean =
    fieldGetters.isEmpty() &&
      fieldSetters.isEmpty() &&
      getterReplacements.isEmpty() &&
      setterReplacements.isEmpty() &&
      getterTypeUpdates.isEmpty()
}
