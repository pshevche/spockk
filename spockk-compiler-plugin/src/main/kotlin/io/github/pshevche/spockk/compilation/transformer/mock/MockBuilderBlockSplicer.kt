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

@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package io.github.pshevche.spockk.compilation.transformer.mock

import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.ir.nestedStatementLists
import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.file

/**
 * Inserts the interactions a `Mock`/`Stub`/`Spy` builder block declares directly after the mock is
 * assigned, so they are registered before any code can call it.
 *
 * This runs as a second pass: a visitor can replace the declaration it is visiting but cannot add
 * statements next to it, so [MockingApiTransformer] records each mock as it rewrites it and splices
 * them all in once the spec has been visited.
 */
internal class MockBuilderBlockSplicer(private val spec: IrClass) {

  private val pending = mutableListOf<PendingSplice>()

  fun record(function: IrFunction, mock: IrVariable, interactions: List<IrStatement>) {
    pending += PendingSplice(function, mock, interactions)
  }

  fun splice() {
    pending.groupBy { it.function }.forEach { (function, splices) ->
      val statements = function.mutableStatements() ?: return@forEach
      val unspliced = splices.toMutableList()
      spliceInto(statements, unspliced)
      // A builder block whose mock is declared somewhere no statement list reaches (nested inside
      // an if/for/when expression) would otherwise leave a stub quietly answering with defaults
      // instead of what the block declared.
      if (unspliced.isNotEmpty()) {
        throw CompilationException(
          "Mock/Stub builder block interactions must be declared as a statement of a feature or fixture method body",
          spec.file,
          unspliced.first().mock
        )
      }
    }
  }

  /**
   * Rewrites [statements] with each pending mock's interactions inserted after it, recursing into
   * nested statement lists: by the time this runs, the declaration may sit one level deeper than
   * the method body, inside the try/catch a `when` block's exception condition produced or the
   * try/finally a `cleanup` block wrapped the whole feature in.
   */
  private fun spliceInto(statements: MutableList<IrStatement>, pending: MutableList<PendingSplice>) {
    if (pending.isEmpty()) return
    val spliced = statements.flatMap { statement ->
      statement.nestedStatementLists().forEach { spliceInto(it, pending) }
      val splice = pending.firstOrNull { it.assigns(statement) } ?: return@flatMap listOf(statement)
      pending.remove(splice)
      listOf(statement) + splice.interactions
    }
    statements.clear()
    statements.addAll(spliced)
  }

  private class PendingSplice(
    val function: IrFunction,
    val mock: IrVariable,
    val interactions: List<IrStatement>
  ) {
    /**
     * Whether [statement] is where the mock actually gets its value. A hoisted declaration (see
     * `irTryHoistingVariables`) has no initializer left, and its `Mock()` call happens at a later
     * assignment - splicing after the bare declaration instead would register the interactions
     * before the mock exists.
     */
    fun assigns(statement: IrStatement): Boolean = if (mock.initializer != null) {
      statement === mock
    } else {
      statement is IrSetValue && statement.symbol == mock.symbol
    }
  }
}
