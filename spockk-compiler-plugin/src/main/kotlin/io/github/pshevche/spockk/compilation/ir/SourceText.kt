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

package io.github.pshevche.spockk.compilation.ir

import io.github.pshevche.spockk.compilation.transformer.ir.SourceTextCache
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

/**
 * The 1-based source line/column of this expression's true start - shared by condition rendering
 * ([io.github.pshevche.spockk.compilation.transformer.ir.IrSpockRuntime]) and interaction building
 * (Spock's own `InteractionBuilder` constructor also takes a `(line, column, text)` triple), so both
 * features locate a statement's source position identically.
 */
internal fun IrExpression.sourceLineColumn(file: IrFile): Pair<Int, Int> {
  val startOffset = effectiveStartOffset()
  return (file.fileEntry.getLineNumber(startOffset) + 1) to (file.fileEntry.getColumnNumber(startOffset) + 1)
}

/**
 * The exact source text of this expression, starting [skipPrefixLength] characters after its true
 * start (used to skip a leading keyword like `assert` that isn't part of the condition itself). Null
 * if the source can't be read back (matches [io.github.pshevche.spockk.compilation.transformer.ir.IrSpockRuntime]'s
 * existing fallback behavior).
 */
internal fun IrExpression.sourceText(file: IrFile, sourceTextCache: SourceTextCache, skipPrefixLength: Int = 0): String? = try {
  sourceTextCache.get(file).substring(effectiveStartOffset() + skipPrefixLength, endOffset)
} catch (_: Exception) {
  null
}

// For a bare top-level call (e.g. `str.startsWith("xyz")`), Kotlin offsets the IrCall at the callee,
// not the receiver, so walk into receivers to find the expression's true start.
internal fun IrExpression.effectiveStartOffset(): Int = when (this) {
  is IrTypeOperatorCall -> argument.effectiveStartOffset()

  is IrCall ->
    symbol.owner.parameters
      .asSequence()
      .withIndex()
      .filter { (_, parameter) -> parameter.kind == IrParameterKind.DispatchReceiver || parameter.kind == IrParameterKind.ExtensionReceiver }
      .fold(startOffset) { minOffset, (index, _) -> minOf(minOffset, arguments[index]?.effectiveStartOffset() ?: minOffset) }

  else -> startOffset
}
