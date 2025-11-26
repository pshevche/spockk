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

package io.github.pshevche.spockk.compilation

import com.google.auto.service.AutoService
import io.github.pshevche.spockk.compilation.collector.SpockkTransformationContextCollector
import io.github.pshevche.spockk.compilation.common.MutableSpockkTransformationContext
import io.github.pshevche.spockk.compilation.transformer.SpockkIrFactory
import io.github.pshevche.spockk.compilation.transformer.SpockkIrTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

@AutoService(CompilerPluginRegistrar::class)
@OptIn(ExperimentalCompilerApi::class)
class SpockkCompilerPlugin : CompilerPluginRegistrar() {
  override val supportsK2: Boolean
    get() = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    IrGenerationExtension.registerExtension(SpockkIrGenerationExtension())
  }

  private class SpockkIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
      val context = MutableSpockkTransformationContext()
      moduleFragment.transform(SpockkTransformationContextCollector(context), null)
      moduleFragment.transform(
        SpockkIrTransformer(SpockkIrFactory(pluginContext), context.finalized()),
        null
      )
    }
  }
}
