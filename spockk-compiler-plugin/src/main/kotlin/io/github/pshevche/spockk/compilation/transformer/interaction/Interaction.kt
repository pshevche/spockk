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

package io.github.pshevche.spockk.compilation.transformer.interaction

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.IrExpression

/**
 * One fully parsed interaction, e.g. `2 * greeter.greet(any()) returned "hi"`. Everything
 * [InteractionStatementsRewriter] needs to build the real `InteractionBuilder` chain is resolved
 * here, so that rewriting never has to look at the original call shape again.
 *
 * [source] is the statement as written: it positions the generated interaction (line/column and
 * source text feed Spock's own "too few invocations" diagnostics) and identifies the entry to drop
 * from the block it was extracted out of.
 */
internal class Interaction(
  val source: IrStatement,
  val cardinality: InteractionCardinality?,
  val target: IrExpression,
  val method: InteractionMethod,
  val args: List<InteractionArg>,
  val response: InteractionResponse?
)

/** How many invocations are expected: `2 * ...` or `(1..3) * ...`. Absent means "any number". */
internal sealed interface InteractionCardinality {
  /** An exact count. */
  class Fixed(val count: IrExpression) : InteractionCardinality

  /** An `IntRange`-typed expression; the rewriter reads its `first`/`last` at runtime. */
  class Range(val range: IrExpression) : InteractionCardinality
}

/** Which method the interaction matches: one named method, or any of them (`anyMethod()`). */
internal sealed interface InteractionMethod {
  class Named(val name: String) : InteractionMethod

  data object Wildcard : InteractionMethod
}

/** How one argument is matched: against a value, or against anything (`any()`). */
internal sealed interface InteractionArg {
  class EqualTo(val value: IrExpression) : InteractionArg

  data object Wildcard : InteractionArg
}

/** What the mock does when the interaction matches. Absent means "return a default". */
internal sealed interface InteractionResponse {
  /** `does`/`did`: a lambda taking the real invocation arguments. */
  class Code(val block: IrExpression) : InteractionResponse

  /** `returns`/`returned`: a fixed value. */
  class Constant(val value: IrExpression) : InteractionResponse
}
