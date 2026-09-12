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

import io.github.pshevche.spockk.compilation.ir.mutableStatements
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.ir.declarations.IrFunction

/**
 * Rewrites a plain (non-feature, non-fixture) member method of a Specification subclass that calls
 * verify/verifyAll/verifyEach as a direct statement. Bare booleans at the method's own top level are
 * *not* implicit conditions - only the interior of a matched call's lambda body gets that treatment,
 * via the same recursive [ConditionStatementsRewriter] core the feature-level rewriter uses.
 */
internal class HelperMethodRewriter(
  override val rewriterContext: SpockkIrRewriterContext
) : SpockkIrRewriter {

  fun rewrite(function: IrFunction) {
    val statements = function.mutableStatements() ?: return
    val builder = irBuilder(function.symbol)

    val valueRecorderVar = irValueRecorderDeclaration(builder, function)
    val errorCollectorVar = irStaticErrorCollectorDeclaration(builder, function)

    val rewritten = ConditionStatementsRewriter(rewriterContext).rewrite(
      statements = statements.toList(),
      enclosingFunction = function,
      builder = builder,
      valueRecorderVar = valueRecorderVar,
      errorCollectorVar = errorCollectorVar,
      treatAsConditionScope = false,
      allowInteractionStatements = true
    )

    statements.clear()
    statements.add(valueRecorderVar)
    statements.add(errorCollectorVar)
    statements.addAll(rewritten)
  }
}
