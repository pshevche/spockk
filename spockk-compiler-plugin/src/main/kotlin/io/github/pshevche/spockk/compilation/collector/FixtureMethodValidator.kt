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

package io.github.pshevche.spockk.compilation.collector

import io.github.pshevche.spockk.compilation.collector.FixtureMethodIdentifiers.fixtureMethodKind
import io.github.pshevche.spockk.compilation.ir.asIrBlockLabel
import io.github.pshevche.spockk.compilation.ir.assignableParameters
import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class FixtureMethodValidator(
  private val file: IrFile,
  private val function: IrFunction,
  private val body: IrBlockBody
) {

  fun validate() {
    validateNoParameters()
    validateNoBlockLabels()
    validateNoSuperFixtureMethodCalls()
  }

  private fun validateNoParameters() {
    if (function.assignableParameters().isNotEmpty()) {
      throw CompilationException(
        "Fixture method '${function.name.asString()}' must not have any parameters, " +
          "but found '${function.assignableParameters().joinToString { it.name.asString() }}'",
        file,
        function
      )
    }
  }

  private fun validateNoBlockLabels() {
    body.statements.forEach { statement ->
      statement.asIrBlockLabel(file)?.let { blockLabel ->
        throw CompilationException(
          "Fixture method '${function.name.asString()}' must not contain block labels, " +
            "but found '${blockLabel.label.displayName}'",
          file,
          statement
        )
      }
    }
  }

  private fun validateNoSuperFixtureMethodCalls() {
    body.acceptVoid(object : IrVisitorVoid() {
      override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
      }

      override fun visitCall(expression: IrCall) {
        val calledMethodName = expression.symbol.owner.name.asString()
        if (expression.superQualifierSymbol != null &&
          fixtureMethodKind(calledMethodName) != null
        ) {
          throw CompilationException(
            "Fixture method '${function.name.asString()}' must not call " +
              "'super.$calledMethodName()'",
            file,
            expression
          )
        }
        super.visitCall(expression)
      }
    })
  }
}
