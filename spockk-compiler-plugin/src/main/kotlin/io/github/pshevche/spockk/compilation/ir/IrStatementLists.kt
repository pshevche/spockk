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

package io.github.pshevche.spockk.compilation.ir

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.IrContainerExpression
import org.jetbrains.kotlin.ir.expressions.IrTry

/** The mutable statement lists directly nested one level inside a try/catch/finally or block. */
internal fun IrStatement.nestedStatementLists(): List<MutableList<IrStatement>> = when (this) {
  is IrTry -> buildList {
    (tryResult as? IrContainerExpression)?.let { add(it.statements) }
    catches.forEach { (it.result as? IrContainerExpression)?.let { result -> add(result.statements) } }
    (finallyExpression as? IrContainerExpression)?.let { add(it.statements) }
  }

  is IrContainerExpression -> listOf(statements)

  else -> emptyList()
}
