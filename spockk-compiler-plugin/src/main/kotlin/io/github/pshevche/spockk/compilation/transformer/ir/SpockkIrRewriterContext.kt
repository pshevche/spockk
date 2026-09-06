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

import org.jetbrains.kotlin.ir.builders.IrGeneratorContext

/**
 * Wraps default IR generator context and provides abstractions for accessing IRs in Spock classes, such as SpockRuntime.
 */
internal class SpockkIrRewriterContext(
  private val defaultGenerator: IrGeneratorContext,
  val sourceTextCache: SourceTextCache = SourceTextCache(),
  val spockRuntime: IrSpockRuntime = IrSpockRuntime.create(defaultGenerator, sourceTextCache),
  val specInternals: IrSpecInternals = IrSpecInternals.create(defaultGenerator),
  val interactionBuilder: IrInteractionBuilder = IrInteractionBuilder.create(defaultGenerator),
  val mockController: IrMockController = IrMockController.create(defaultGenerator)
) : IrGeneratorContext by defaultGenerator
