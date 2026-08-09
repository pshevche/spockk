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

import io.github.pshevche.spockk.compilation.ir.isThrownCall
import io.github.pshevche.spockk.compilation.ir.requiredThisParameter
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import io.github.pshevche.spockk.compilation.transformer.ir.SpockkIrRewriterContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.util.file

/**
 * Detects and rewrites top-level `thrown`/`notThrown`/`noExceptionThrown` calls in a `then`
 * block's own statement list. Deliberately separate from [ConditionStatementsRewriter], which is
 * shared/recursive machinery for ordinary conditions and `verify`/`verifyAll`/`verifyEach` -
 * exception conditions are then-block-only and never recurse into nested lambdas.
 *
 * `thrown(Type::class.java)` is rewritten to call Spock's own `SpecInternals.checkExceptionThrown`
 * (already shaded into `spockk-core`), reusing its exception-matching/error-selection logic
 * unchanged - including its handling of `thrown(null)` and non-`Throwable` types. `notThrown`/
 * `noExceptionThrown` are left untouched: their inherited bodies on `Specification` already read
 * the exception [WhenBlockRewriter] records correctly, with no rewrite needed. A zero-arg
 * `thrown()` is detected (for when-block-wrapping and validation purposes) but not rewritten - see
 * the design doc's deferred scope.
 */
internal class ExceptionConditionRewriter(
  override val rewriterContext: SpockkIrRewriterContext,
  private val feature: IrFunction
) : SpockkIrRewriter {

  private val builder = irBuilder(feature.symbol)

  fun rewrite(statements: List<IrStatement>): List<IrStatement> {
    val occurrences = statements.findExceptionConditionOccurrences()
    if (occurrences.size > 1) {
      throw InvalidExceptionConditionExceptionFactory(feature.file, occurrences[1].call)
        .multipleExceptionConditionsException()
    }

    val occurrence = occurrences.singleOrNull() ?: return statements
    if (!occurrence.call.isThrownCall()) return statements

    // Zero-arg `thrown()`: not rewritten, falls through to its real (unconditionally throwing)
    // fallback body - see design doc's deferred scope.
    val exceptionTypeArg = occurrence.call.arguments.getOrNull(1) ?: return statements

    val rewrittenCall = rewriterContext.specInternals.irCheckExceptionThrown(
      builder,
      feature.requiredThisParameter(),
      exceptionTypeArg
    )

    return when (occurrence) {
      is ExceptionConditionOccurrence.BareStatement -> {
        // Discarded value: nullability doesn't matter, cast to the original call's own type.
        val castCall = builder.irAs(rewrittenCall, occurrence.call.type)
        // thrown()'s non-Unit result, used as a bare statement, was coercion-wrapped by the
        // Kotlin frontend before this rewrite ever saw it - preserve that shape so the rewritten
        // statement looks exactly as ordinary Kotlin source producing the same call would.
        val replacement = if (occurrence.statement.isCoercedToUnit()) builder.irCoerceToUnit(castCall) else castCall
        statements.map { if (it === occurrence.statement) replacement else it }
      }

      is ExceptionConditionOccurrence.VariableInitializer -> {
        // Cast to the variable's declared type (not the unwrapped call's flexibly-nullable type),
        // preserving whatever non-null assertion the original initializer had.
        occurrence.variable.initializer = builder.irAs(rewrittenCall, occurrence.variable.type)
        statements
      }
    }
  }
}

private fun IrStatement.isCoercedToUnit(): Boolean =
  this is IrTypeOperatorCall && operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT

private fun DeclarationIrBuilder.irCoerceToUnit(value: IrExpression): IrExpression =
  IrTypeOperatorCallImpl(
    startOffset,
    endOffset,
    context.irBuiltIns.unitType,
    IrTypeOperator.IMPLICIT_COERCION_TO_UNIT,
    context.irBuiltIns.unitType,
    value
  )
