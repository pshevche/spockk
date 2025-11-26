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

package io.github.pshevche.spockk.fixtures.compilation

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.DumpIrTreeOptions
import org.jetbrains.kotlin.ir.util.dump

@OptIn(ExperimentalCompilerApi::class)
class IrDumpCapturingCompilerPlugin(private val irDumpBuilder: StringBuilder) : CompilerPluginRegistrar() {
  override val supportsK2: Boolean
    get() = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    IrGenerationExtension.registerExtension(SpockkIrGenerationExtension(irDumpBuilder))
  }

  internal class SpockkIrGenerationExtension(private val irDumpBuilder: StringBuilder) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
      val options = DumpIrTreeOptions(printFilePath = false)
      irDumpBuilder.append(moduleFragment.dump(options))
    }
  }
}
