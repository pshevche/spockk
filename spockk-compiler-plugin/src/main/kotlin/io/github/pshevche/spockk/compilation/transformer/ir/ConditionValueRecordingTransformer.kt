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
      // A real method call gets two extra index slots with no recorded value - Spock's runtime
      // reserves the same two when it re-parses the condition text, for the method name and the
      // argument list, so skipping them here would shift every later index out of alignment.
      expression.transformReceiverArguments()
      recordCount++ // method name
      expression.transformNonReceiverArguments()
      recordCount++ // argument list
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
      // `a && b` desugars to `if (a) b else false`: only the first branch holds real operands.
      IrStatementOrigin.ANDAND -> {
        val realOperands = expression.branches[0]
        realOperands.condition = realOperands.condition.transform(this, null)
        realOperands.result = realOperands.result.transform(this, null)
      }

      // `a || b` desugars to `if (a) true else b`: the real operands are split across both branches.
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
   * True for a plain method call, `foo.bar(...)` or implicit-receiver `bar(...)` - as opposed to
   * an operator (`==`, `[]`, ...), a property getter, or `!x` (desugared to `x.not()`), none of
   * which get the extra index slots reserved below.
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
