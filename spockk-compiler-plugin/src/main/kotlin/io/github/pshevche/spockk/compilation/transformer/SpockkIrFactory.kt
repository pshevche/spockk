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

import io.github.pshevche.spockk.compilation.common.FeatureBlockStatements
import io.github.pshevche.spockk.compilation.ir.ContextAwareIrFactory
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class SpockkIrFactory(pluginContext: IrPluginContext) {

    companion object {
        private const val SPEC_METADATA_FQN = "org.spockframework.runtime.model.SpecMetadata"
        private const val FEATURE_METADATA_FQN = "org.spockframework.runtime.model.FeatureMetadata"
        private const val BLOCK_METADATA_FQN = "org.spockframework.runtime.model.BlockMetadata"
        private const val BLOCK_KIND_FQN = "org.spockframework.runtime.model.BlockKind"
    }

    private val irFactory: ContextAwareIrFactory = ContextAwareIrFactory(pluginContext)

    fun specMetadataAnnotation(fileName: String, line: Int) = irFactory.constructorCall(
        SPEC_METADATA_FQN,
        irFactory.const(fileName),
        irFactory.const(line)
    )

    fun featureMetadataAnnotation(
        ordinal: Int,
        name: String,
        line: Int,
        parameterNames: List<String>,
        blocks: List<FeatureBlockStatements>,
    ): IrConstructorCall = irFactory.constructorCall(
        FEATURE_METADATA_FQN,
        irFactory.const(ordinal),
        irFactory.const(name),
        irFactory.const(line),
        irFactory.stringArray(parameterNames),
        blockMetadataArray(blocks.filter { it.label.blockKind != null })
    )

    private fun blockMetadataArray(blocks: List<FeatureBlockStatements>): IrExpression {
        return irFactory.array(
            BLOCK_METADATA_FQN,
            blocks.map { block ->
                irFactory.constructorCall(
                    BLOCK_METADATA_FQN,
                    irFactory.enumValue(block.label.blockKind!!, BLOCK_KIND_FQN),
                    irFactory.stringArray(listOf(block.description))
                )
            }
        )
    }
}
