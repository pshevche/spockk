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
import io.github.pshevche.spockk.compilation.common.referenceClass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.fir.backend.utils.defaultTypeWithoutArguments
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.toIrConst

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class SpockkIrFactory(private val pluginContext: IrPluginContext) {

    companion object {
        private const val SPEC_METADATA_FQN = "org.spockframework.runtime.model.SpecMetadata"
        private const val FEATURE_METADATA_FQN = "org.spockframework.runtime.model.FeatureMetadata"
        private const val BLOCK_METADATA_FQN = "org.spockframework.runtime.model.BlockMetadata"
        private const val BLOCK_KIND_FQN = "org.spockframework.runtime.model.BlockKind"
    }

    private val irBuiltIns = pluginContext.irBuiltIns

    fun specMetadataAnnotation(fileName: String, line: Int) = createConstructorCall(
        SPEC_METADATA_FQN,
        fileName.toIrConst(),
        line.toIrConst()
    )

    fun featureMetadataAnnotation(
        ordinal: Int,
        name: String,
        line: Int,
        parameterNames: List<String>,
        blocks: List<FeatureBlockStatements>,
    ): IrConstructorCall {
        return createConstructorCall(FEATURE_METADATA_FQN).apply {
            arguments[0] = ordinal.toIrConst()
            arguments[1] = name.toIrConst()
            arguments[2] = line.toIrConst()
            arguments[3] = parameterNames.toTypedArray().toIrConstantArray(this.symbol)
            arguments[4] = blocks.filter { it.label.blockKind != null }.toIrBlockMetadataArray(this.symbol)
//            arguments[4] = listOf<FeatureBlockStatements>().toIrBlockMetadataArray(this.symbol)
        }
    }

    private fun createConstructorCall(className: String, vararg args: IrExpression): IrConstructorCall {
        val classSymbol = pluginContext.referenceClass(className)
        val constructorSymbol = classSymbol.constructors.first()
        val classType = classSymbol.defaultType
        return IrConstructorCallImpl.fromSymbolOwner(classType, constructorSymbol).apply {
            args.withIndex().forEach {
                arguments[it.index] = it.value
            }
        }
    }

    private fun Any.toIrConst(): IrConst =
        this.toIrConst(pluginContext.referenceClass(this::class.qualifiedName!!).defaultTypeWithoutArguments)

    private fun Array<String>.toIrConstantArray(symbol: IrSymbol): IrVararg {
        return IrVarargImpl(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            irBuiltIns.arrayClass.typeWith(irBuiltIns.stringType),
            irBuiltIns.stringType,
            this.map { it.toIrConst() }
        )
    }

    private fun String.toEnumValue(className: String): IrGetEnumValue {
        val enumClassSymbol = pluginContext.referenceClass(className)
        val enumEntry = enumClassSymbol.owner.declarations
            .filterIsInstance<IrEnumEntry>()
            .first { it.name.asString() == this }
        return IrGetEnumValueImpl(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            enumClassSymbol.defaultType,
            enumEntry.symbol
        )
    }

    private fun List<FeatureBlockStatements>.toIrBlockMetadataArray(symbol: IrConstructorSymbol): IrVararg {
        val blockMetadataIrType = pluginContext.referenceClass(BLOCK_METADATA_FQN).defaultType
        return IrVarargImpl(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            irBuiltIns.arrayClass.typeWith(blockMetadataIrType),
            blockMetadataIrType,
            this.map { block ->
                createConstructorCall(BLOCK_METADATA_FQN).apply {
                    arguments[0] = block.label.blockKind!!.toEnumValue(BLOCK_KIND_FQN)
                    arguments[1] = arrayOf(block.description).toIrConstantArray(symbol)
                }
            }
        )
    }

}
