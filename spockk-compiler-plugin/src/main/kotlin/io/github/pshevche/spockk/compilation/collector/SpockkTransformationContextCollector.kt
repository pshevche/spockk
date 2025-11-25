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

package io.github.pshevche.spockk.compilation.collector

import io.github.pshevche.spockk.compilation.common.BaseSpockkIrElementTransformer
import io.github.pshevche.spockk.compilation.common.MutableSpockkTransformationContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isClassWithFqName
import org.jetbrains.kotlin.ir.util.getAllSuperclasses
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.name.FqName

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class SpockkTransformationContextCollector(private val context: MutableSpockkTransformationContext) :
    BaseSpockkIrElementTransformer() {

    companion object {
        private val SPECIFICATION_FQN = FqName("spock.lang.Specification")
    }

    override fun visitClassNew(declaration: IrClass): IrStatement {
        if (isSpecification(declaration)) {
            context.addSpec(declaration)
        }

        return super.visitClassNew(declaration)
    }

    private fun isSpecification(declaration: IrClass): Boolean {
        return declaration.getAllSuperclasses().any { it.isClassWithFqName(SPECIFICATION_FQN) }
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration.isFakeOverride) {
            context.addPotentialFeature(currentIrClass, declaration)
        }

        return super.visitFunctionNew(declaration)
    }

    override fun visitBlockBody(body: IrBlockBody): IrBody {
        return body.transformPostfix {
            val blockCollector = createBlockCollector(currentFile)
            body.statements.forEach { blockCollector.consume(it) }
            val blocks = blockCollector.getBlockStatements()
            if (blocks.isNotEmpty()) {
                context.addFeature(currentIrClass, currentIrFunction, blocks)
            }
        }
    }

    private fun createBlockCollector(file: IrFile) =
        ValidatingFeatureBlockCollector(file, DefaultFeatureBlockCollector(file))
}
