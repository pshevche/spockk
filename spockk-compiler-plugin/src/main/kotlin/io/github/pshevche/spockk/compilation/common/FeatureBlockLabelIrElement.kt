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

@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package io.github.pshevche.spockk.compilation.common

import io.github.pshevche.spockk.compilation.ir.requiredFqn
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

internal sealed class FeatureBlockLabelIrElement(
    val label: FeatureBlockLabel,
    open val description: String,
    open val file: IrFile,
    open val element: IrElement,
) {

    companion object {
        fun from(file: IrFile, element: IrGetObjectValue) = fromElement(element.requiredFqn(), "", file, element)

        fun from(file: IrFile, element: IrCall): FeatureBlockLabelIrElement? {
            val description = (element.arguments[0] as? IrConst)?.value
            return fromElement(element.requiredFqn(), description as? String ?: "", file, element)
        }

        private fun fromElement(
            fqn: String,
            description: String,
            file: IrFile,
            element: IrElement,
        ): FeatureBlockLabelIrElement? {
            return when (FeatureBlockLabel.from(fqn)) {
                FeatureBlockLabel.GIVEN -> Given(description, file, element)
                FeatureBlockLabel.WHEN -> When(description, file, element)
                FeatureBlockLabel.THEN -> Then(description, file, element)
                FeatureBlockLabel.EXPECT -> Expect(description, file, element)
                FeatureBlockLabel.AND -> And(description, file, element)
                else -> null
            }
        }
    }

    data class Given(override val description: String, override val file: IrFile, override val element: IrElement) :
        FeatureBlockLabelIrElement(FeatureBlockLabel.GIVEN, description, file, element)

    data class When(override val description: String, override val file: IrFile, override val element: IrElement) :
        FeatureBlockLabelIrElement(FeatureBlockLabel.WHEN, description, file, element)

    data class Then(override val description: String, override val file: IrFile, override val element: IrElement) :
        FeatureBlockLabelIrElement(FeatureBlockLabel.THEN, description, file, element)

    data class Expect(override val description: String, override val file: IrFile, override val element: IrElement) :
        FeatureBlockLabelIrElement(FeatureBlockLabel.EXPECT, description, file, element)

    data class And(override val description: String, override val file: IrFile, override val element: IrElement) :
        FeatureBlockLabelIrElement(FeatureBlockLabel.AND, description, file, element)

}
