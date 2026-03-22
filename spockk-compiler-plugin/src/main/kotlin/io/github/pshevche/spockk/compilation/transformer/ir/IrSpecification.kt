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

package io.github.pshevche.spockk.compilation.transformer.ir

import org.jetbrains.kotlin.ir.declarations.IrClass

// contains extensions for IrClass with an assumption that "this" IrClass is a Spock specification

internal fun IrClass.getSpecificationContext(rewriterContext: SpockkIrRewriterContext) = IrSpecificationContext(
  rewriterContext,
  this
)
