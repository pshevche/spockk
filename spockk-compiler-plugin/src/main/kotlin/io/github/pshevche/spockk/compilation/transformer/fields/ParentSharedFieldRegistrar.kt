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

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.superClass

/**
 * Wires subclass feature method references to parent class shared field accessors.
 *
 * When a subclass extends a spec with shared fields, its feature methods may reference
 * parent shared fields. Since the [FieldReferenceReplacer] only has mappings for the current
 * class's own fields, this registrar walks the superclass hierarchy and maps:
 * 1. Parent DEFAULT getter/setter symbols → parent's generated getXxx()/setXxx() functions
 * 2. Subclass fake override getter/setter symbols → same generated functions (Kotlin IR may
 *    target fake override symbols rather than the parent's actual symbols)
 */
internal class ParentSharedFieldRegistrar(
  private val spec: IrClass,
  private val state: FieldRewriteState
) {

  fun register() {
    val parentAccessorMap = getParentAccessorsForCurrentInheritedFields()

    // Map fake override symbols in the current class to the same parent generated functions.
    // In Kotlin IR, CALLs in a subclass target fake override symbols rather than the parent's
    // actual getter/setter symbols.
    if (parentAccessorMap.isNotEmpty()) {
      for (property in spec.declarations.filterIsInstance<IrProperty>()) {
        if (!property.isFakeOverride) continue

        val fakeGetter = property.getter
        findReplacementFor(fakeGetter, parentAccessorMap)
          ?.also {
            state.onGetterReplaced(fakeGetter!!.symbol, it)
            state.onFunctionGenerated(fakeGetter.symbol)
          }

        val fakeSetter = property.setter
        findReplacementFor(fakeSetter, parentAccessorMap)
          ?.also {
            state.onSetterReplaced(fakeSetter!!.symbol, it)
            state.onFunctionGenerated(fakeSetter.symbol)
          }
      }
    }
  }

  private fun findReplacementFor(
    function: IrSimpleFunction?,
    parentAccessors: Map<IrFunctionSymbol, IrFunction>
  ): IrFunction? {
    return function?.overriddenSymbols
      ?.map { parentAccessors[it] }
      ?.firstOrNull()
  }

  private fun getParentAccessorsForCurrentInheritedFields(): Map<IrFunctionSymbol, IrFunction> {
    val parentAccessorMap = mutableMapOf<IrFunctionSymbol, IrFunction>()

    // Walk superclass hierarchy to find transformed shared fields and their generated accessors
    var superClass: IrClass? = spec.superClass
    while (superClass != null) {
      for (property in superClass.declarations.filterIsInstance<IrProperty>()) {
        val field = property.backingField ?: continue
        val fieldName = field.name.asString()
        if (!fieldName.startsWith($$"$spock_sharedField_")) continue

        val originalName = fieldName.removePrefix($$"$spock_sharedField_")
        val getterName = "get${originalName.replaceFirstChar { it.uppercaseChar() }}"
        val setterName = "set${originalName.replaceFirstChar { it.uppercaseChar() }}"

        val parentGetter = superClass.declarations.filterIsInstance<IrSimpleFunction>()
          .find { it.name.asString() == getterName }
        val parentSetter = superClass.declarations.filterIsInstance<IrSimpleFunction>()
          .find { it.name.asString() == setterName }

        val defaultGetter = property.getter
        val defaultSetter = property.setter

        if (parentGetter != null && defaultGetter != null) {
          state.onGetterReplaced(defaultGetter.symbol, parentGetter)
          parentAccessorMap[defaultGetter.symbol] = parentGetter
        }
        if (parentSetter != null && defaultSetter != null) {
          state.onSetterReplaced(defaultSetter.symbol, parentSetter)
          parentAccessorMap[defaultSetter.symbol] = parentSetter
        }
      }

      superClass = superClass.superClass
    }
    return parentAccessorMap
  }

}
