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

package io.github.pshevche.spockk.compilation.shared

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty

internal data class SpockkTransformationContext(private val specs: Map<IrClass, SpecContext>) {
  fun specContext(clazz: IrClass) = specs[clazz]

  fun featureContext(clazz: IrClass, feature: IrFunction) = specs[clazz]?.features[feature]

  internal data class SpecContext(
    val fileName: String,
    val line: Int,
    val features: Map<IrFunction, FeatureContext>,
    val fields: Map<IrProperty, FieldContext>,
    val fixtureMethods: Map<IrFunction, FixtureMethodKind>
  )

  internal data class FeatureContext(
    val specDepth: Int,
    val ordinal: Int,
    val name: String,
    val line: Int,
    val parameterNames: List<String>,
    val anonymousStatements: List<IrStatement>,
    val blocks: List<FeatureBlock>
  ) {
    val featureBlocks: List<FeatureBlock>
      get() = blocks.takeWhile { it.element.label !in SEPARATOR_LABELS }

    val cleanupBlocks: List<FeatureBlock>
      get() = blocks
        .dropWhile { it.element.label != FeatureBlockLabel.CLEANUP }
        .takeWhile { it.element.label != FeatureBlockLabel.WHERE }

    val dataProviderBlocks: List<FeatureBlock>
      get() = blocks.dropWhile { it.element.label != FeatureBlockLabel.WHERE }

    companion object {
      private val SEPARATOR_LABELS = setOf(FeatureBlockLabel.CLEANUP, FeatureBlockLabel.WHERE)
    }
  }

  internal data class FieldContext(
    val name: String,
    val ordinal: Int,
    val line: Int,
    val hasInitializer: Boolean,
    val isShared: Boolean,
    val isVal: Boolean,
    val isLateinit: Boolean
  )

  internal enum class FixtureMethodKind {
    SETUP,
    CLEANUP,
    SETUP_SPEC,
    CLEANUP_SPEC
  }
}
