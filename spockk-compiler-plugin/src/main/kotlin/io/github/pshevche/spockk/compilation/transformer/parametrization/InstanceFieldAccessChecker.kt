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

package io.github.pshevche.spockk.compilation.transformer.parametrization

import io.github.pshevche.spockk.compilation.common.BaseSpockkIrElementTransformer
import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.fileOrNull

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class InstanceFieldAccessChecker(
  private val file: IrFile,
  private val whereBlockIr: IrElement
) : BaseSpockkIrElementTransformer() {
  fun check(expression: IrExpression) {
    expression.transform(this, null)
  }

  fun check(statements: Collection<IrStatement>) {
    statements.forEach { it.transform(this, null) }
  }

  override fun visitGetValue(expression: IrGetValue): IrExpression = super.visitGetValue(expression).also {
    if (expression.symbol.owner.name.asString() == "<this>") {
      throw CompilationException(
        "Only companion object members may be accessed from here",
        file,
        whereBlockIr
      )
    }
  }
}
