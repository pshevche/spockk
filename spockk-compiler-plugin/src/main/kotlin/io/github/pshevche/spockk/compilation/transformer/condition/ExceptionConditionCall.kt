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

package io.github.pshevche.spockk.compilation.transformer.condition

import io.github.pshevche.spockk.compilation.ir.asExceptionConditionCall
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall

/**
 * A top-level `thrown`/`notThrown`/`noExceptionThrown` call found in a `then` block's own
 * statement list - either a bare statement, or the initializer of a `val`/`var` declaration
 * (`val e = thrown(...)`). [call] is the unwrapped call (see
 * [io.github.pshevche.spockk.compilation.ir.asExceptionConditionCall]) usable for classifying and
 * rewriting; [BareStatement.statement] is the original (possibly coercion-wrapped) list entry,
 * needed to find and replace it in place.
 */
internal sealed class ExceptionConditionOccurrence(val call: IrCall) {
  internal class BareStatement(val statement: IrStatement, call: IrCall) : ExceptionConditionOccurrence(call)
  internal class VariableInitializer(val variable: IrVariable, call: IrCall) : ExceptionConditionOccurrence(call)
}

internal fun List<IrStatement>.findExceptionConditionOccurrences(): List<ExceptionConditionOccurrence> =
  mapNotNull { statement ->
    val bareCall = statement.asExceptionConditionCall()
    val initializerCall = (statement as? IrVariable)?.initializer?.asExceptionConditionCall()
    when {
      bareCall != null -> ExceptionConditionOccurrence.BareStatement(statement, bareCall)

      statement is IrVariable && initializerCall != null ->
        ExceptionConditionOccurrence.VariableInitializer(statement, initializerCall)

      else -> null
    }
  }

internal fun List<IrStatement>.hasExceptionCondition(): Boolean = findExceptionConditionOccurrences().isNotEmpty()
