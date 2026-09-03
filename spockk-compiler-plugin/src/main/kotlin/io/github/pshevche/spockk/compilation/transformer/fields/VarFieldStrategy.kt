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

import io.github.pshevche.spockk.compilation.ir.makePrivate
import io.github.pshevche.spockk.compilation.shared.SpockkTransformationContext.FieldContext
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty

/**
 * Handles instance var fields (including lateinit var):
 * - Makes field and property private
 * - If initialized and not lateinit: moves initializer to $spock_initializeFields(),
 *   makes field nullable with null default, updates getter return type in call sites
 */
internal class VarFieldStrategy(
  private val fieldCtx: FieldContext,
  state: FieldRewriteState,
  context: SpockkIrRewriterContext,
  spec: IrClass
) : FieldStrategyBase(context, spec, state) {

  override fun rewrite(property: IrProperty) {
    val field = property.backingField ?: return

    annotateField(field, property, fieldCtx)
    field.makePrivate()
    property.makePrivate()

    if (fieldCtx.hasInitializer && !fieldCtx.isLateinit) {
      moveFieldInitializerForInstanceField(field, property)
      makeFieldNullableWithNullDefault(field)
      // Track getter so CALL expressions in feature methods get their type updated
      property.getter?.let { state.onGetterTypeUpdated(it.symbol, it.returnType) }
    }
  }
}
