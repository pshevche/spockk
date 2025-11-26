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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.SpecContext
import org.jetbrains.kotlin.ir.declarations.IrClass

internal class SpecRewriter(private val irFactory: SpockkIrFactory) {
  fun rewrite(spec: IrClass, context: SpecContext) {
    annotateSpec(spec, context)
  }

  private fun annotateSpec(spec: IrClass, context: SpecContext) {
    spec.annotations += irFactory.specMetadataAnnotation(context.fileName, context.line)
  }
}
