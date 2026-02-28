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

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FieldContext
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty

internal class FieldsRewriter(
  override val context: IrGeneratorContext,
  private val spec: IrClass,
  private val fields: Map<IrProperty, FieldContext>
) : SpockkIrRewriter {

  fun rewrite() {
    val state = FieldRewriteState()

    fields.forEach { (property, fieldCtx) ->
      SingleFieldRewriterStrategy.create(fieldCtx, context, spec, state).rewrite(property)
    }

    ParentSharedFieldRegistrar(spec, state).register()
    FieldReferenceReplacer(context, spec, state).replace()
  }
}
