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

package io.github.pshevche.spockk.compilation.transformer.ir

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers
import io.github.pshevche.spockk.compilation.ir.fqName
import io.github.pshevche.spockk.compilation.ir.isAssertCall
import io.github.pshevche.spockk.compilation.ir.unwrapImplicitCoercionToUnit
import io.github.pshevche.spockk.compilation.shared.BaseSpockkIrElementTransformer
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

/**
 * Rewrites a condition expression into a value-recording expression tree, mirroring Spock's
 * `org.spockframework.compiler.ConditionRewriter`. Every value-producing sub-expression is
 * wrapped in `$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(n), <expr>)`.
 *
 * Kotlin IR collapses many distinct Groovy AST nodes onto a handful of expression types, so the
 * `visit*` methods below map to Spock's `visit*` methods as follows:
 *   - [visitConst]                -> visitConstantExpression
 *   - [visitGetValue]             -> visitVariableExpression
 *   - [visitGetField]             -> visitFieldExpression / visitAttributeExpression
 *   - [visitCall]                 -> visitMethodCallExpression / visitStaticMethodCallExpression /
 *                                    visitPropertyExpression / all operator expressions
 *                                    (binary, unary, not, prefix/postfix, spaceship, ...)
 *   - [visitConstructorCall]      -> visitConstructorCallExpression
 *   - [visitTypeOperator]         -> visitCastExpression (and `instanceof`)
 *   - [visitWhen]                 -> visitTernaryExpression / visitShortTernaryExpression / `&&` / `||`
 *   - [visitStringConcatenation]  -> visitGStringExpression
 *   - [visitVararg]               -> visitListExpression / visitArrayExpression
 *
 * Following Spock, children are converted before the enclosing node is recorded (post-order), so
 * that recorded indices increase left-to-right and inside-out.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class ConditionValueRecordingTransformer(
  private val builder: DeclarationIrBuilder,
  private val irValueRecorder: IrValueRecorder
) : BaseSpockkIrElementTransformer() {

  private var recordCount = 0

  fun transform(expr: IrExpression): IrExpression {
    // Explicit conditions arrive as `assert(<condition>)`. The assertion itself is performed by
    // SpockRuntime.verifyCondition, so drop the `assert` wrapper and record only the condition.
    val unwrapped = expr.unwrapImplicitCoercionToUnit()
    val condition = if (unwrapped.isAssertCall()) (unwrapped as IrCall).arguments.first()!! else unwrapped
    return condition.transform(this, null)
  }

  override fun visitConst(expression: IrConst): IrExpression = record(expression)

  override fun visitGetValue(expression: IrGetValue): IrExpression {
    // Like Spock, do not record implicit `this` receivers: the spec instance is not a meaningful
    // value in the rendered condition, and recording it would shift the recorded indices.
    val owner = expression.symbol.owner
    if (owner is IrValueParameter && owner.name.asString() == "<this>") {
      return expression
    }
    return record(expression)
  }

  override fun visitGetField(expression: IrGetField): IrExpression {
    expression.transformChildren(this, null)
    return record(expression)
  }

  override fun visitCall(expression: IrCall): IrExpression {
    val comparison = expression.desugaredNotEqualsComparisonOrNull()
    if (comparison != null) {
      // Kotlin desugars `a != b` into `(a == b).not()`, unlike Groovy, which keeps `!=` as a
      // single BinaryExpression. Record the comparison's own operands as usual, but skip
      // recording the intermediate `==` call itself - only its negated result (the `.not()`
      // call) corresponds to a value Groovy would record for the whole `!=` expression.
      comparison.transformChildren(this, null)
      return record(expression)
    }

    if (expression.representsGroovyMethodCallExpression()) {
      // Groovy's MethodCallExpression indexes two things Kotlin has no equivalent node for:
      // - its method name, converted (and thus indexed) as its own ConstantExpression child,
      //   right after the receiver and before the arguments;
      // - its whole argument list, wrapped in an ArgumentListExpression that reserves its own
      //   index (via `recordNa`, no value) right after the individual arguments and before the
      //   call itself.
      // Neither is ever independently displayed, but both still consume an index slot at
      // runtime, so reserve the same two slots here, in the same positions, to keep recorded
      // indices aligned with what Groovy's own ConditionRewriter would produce for the reparsed
      // text. Operator calls (`==`, `[]`, property getters, ...) don't go through this: Groovy
      // represents those as different node kinds that don't have a method name or argument list.
      expression.transformReceiverArguments()
      recordCount++
      expression.transformNonReceiverArguments()
      recordCount++
      return record(expression)
    }

    expression.transformChildren(this, null)
    return record(expression)
  }

  override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
    expression.transformChildren(this, null)
    return record(expression)
  }

  override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
    expression.transformChildren(this, null)
    // Only user-written casts and `instanceof` checks carry a recordable value; compiler-inserted
    // coercions (implicit casts, not-null assertions, unit coercions) are passed through untouched.
    return when (expression.operator) {
      IrTypeOperator.CAST,
      IrTypeOperator.SAFE_CAST,
      IrTypeOperator.INSTANCEOF,
      IrTypeOperator.NOT_INSTANCEOF -> record(expression)

      else -> expression
    }
  }

  override fun visitWhen(expression: IrWhen): IrExpression {
    when (expression.origin) {
      // Kotlin desugars `a && b` into `if (a) b else false` and `a || b` into `if (a) true else b`,
      // tagging the resulting IrWhen with ANDAND/OROR. Unlike Groovy, which keeps `&&`/`||` as a
      // single BinaryExpression with two real operands, this shape has two synthetic branches
      // whose `true`/`false` legs don't correspond to anything in the source text. Transform (and
      // record) only the real operands - the other branch's condition/result is left untouched -
      // so the recorded values match what Groovy's own ConditionRewriter would produce.
      IrStatementOrigin.ANDAND -> {
        val realOperands = expression.branches[0]
        realOperands.condition = realOperands.condition.transform(this, null)
        realOperands.result = realOperands.result.transform(this, null)
      }

      IrStatementOrigin.OROR -> {
        val left = expression.branches[0]
        val right = expression.branches[1]
        left.condition = left.condition.transform(this, null)
        right.result = right.result.transform(this, null)
      }

      else -> expression.transformChildren(this, null)
    }
    return record(expression)
  }

  override fun visitStringConcatenation(expression: IrStringConcatenation): IrExpression {
    expression.transformChildren(this, null)
    return record(expression)
  }

  override fun visitVararg(expression: IrVararg): IrExpression {
    expression.transformChildren(this, null)
    return record(expression)
  }

  private fun record(expression: IrExpression): IrExpression =
    irValueRecorder.irRecord(builder, recordCount++, expression)

  /**
   * Kotlin desugars `a != b` into `(a == b).not()`, tagging both calls with
   * [IrStatementOrigin.EXCLEQ]. A user-written `!(a == b)` produces a similarly shaped call
   * chain, but without that origin (the `.not()` call has no origin, and the inner call is
   * tagged [IrStatementOrigin.EQEQ] instead), so this only matches the compiler-synthesized
   * `!=` case.
   */
  private fun IrCall.desugaredNotEqualsComparisonOrNull(): IrCall? {
    if (origin != IrStatementOrigin.EXCLEQ) return null
    val comparison = dispatchReceiver as? IrCall ?: return null
    return comparison.takeIf { it.origin == IrStatementOrigin.EXCLEQ }
  }

  /**
   * A plain, source-level call (explicit or implicit receiver) that Groovy would parse as a
   * `MethodCallExpression` - as opposed to a property/operator call (`origin` tags those with e.g.
   * `GET_PROPERTY`, `GET_ARRAY_ELEMENT`, `EQEQ`, `PLUS`, ...), which Groovy represents with a
   * different node kind that has no separate method-name child. `!x`, desugared to `x.not()`, is
   * the one symbolic operator that isn't tagged with an origin of its own (unlike `.equals(...)`,
   * which is also `operator fun`-declared but genuinely is a Groovy-style method call when called
   * explicitly), so it's excluded by name instead.
   */
  private fun IrCall.representsGroovyMethodCallExpression(): Boolean =
    origin == null && fqName() != IrIdentifiers.Kotlin.BOOLEAN_NOT_FQN && symbol.owner.correspondingPropertySymbol == null

  private fun IrCall.transformReceiverArguments() {
    symbol.owner.parameters.forEachIndexed { index, parameter ->
      if (parameter.kind == IrParameterKind.DispatchReceiver || parameter.kind == IrParameterKind.ExtensionReceiver) {
        arguments[index] = arguments[index]?.transform(this@ConditionValueRecordingTransformer, null)
      }
    }
  }

  private fun IrCall.transformNonReceiverArguments() {
    symbol.owner.parameters.forEachIndexed { index, parameter ->
      if (parameter.kind != IrParameterKind.DispatchReceiver && parameter.kind != IrParameterKind.ExtensionReceiver) {
        arguments[index] = arguments[index]?.transform(this@ConditionValueRecordingTransformer, null)
      }
    }
  }
}
