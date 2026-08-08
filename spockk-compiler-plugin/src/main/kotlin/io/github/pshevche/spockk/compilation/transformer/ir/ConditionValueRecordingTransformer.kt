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
 * Wraps every value-producing sub-expression of a condition in a value-recording call, mirroring
 * Spock's `org.spockframework.compiler.ConditionRewriter`. Children are recorded before their
 * enclosing expression (post-order), so recorded indices increase left-to-right and inside-out.
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
    // Like Spock, do not record implicit receivers: the spec instance (a feature's dispatch
    // receiver) or a nested verify/verifyAll/verifyEach lambda's implicit target (its extension
    // receiver, named `$this$verify` etc. - not `<this>`) is not a meaningful value in the rendered
    // condition, and recording it would shift the recorded indices.
    val owner = expression.symbol.owner
    if (owner is IrValueParameter &&
      (owner.kind == IrParameterKind.DispatchReceiver || owner.kind == IrParameterKind.ExtensionReceiver)
    ) {
      return expression
    }
    return record(expression)
  }

  override fun visitGetField(expression: IrGetField): IrExpression {
    expression.transformChildren(this, null)
    return record(expression)
  }

  override fun visitCall(expression: IrCall): IrExpression {
    val innerEquals = expression.innerEqualsOfNotOperator()
    if (innerEquals != null) {
      // Kotlin writes `a != b` as `(a == b).not()`. Record `a` and `b` as usual, but skip
      // recording the inner `==` call itself - only its negated result (the `.not()` call)
      // is the value that belongs to the whole `!=` expression.
      innerEquals.transformChildren(this, null)
      return record(expression)
    }

    if (expression.isPlainMethodCall()) {
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
      // Kotlin writes `a && b` as `if (a) b else false`: only the first branch holds real operands.
      IrStatementOrigin.ANDAND -> {
        val realOperands = expression.branches[0]
        realOperands.condition = realOperands.condition.transform(this, null)
        realOperands.result = realOperands.result.transform(this, null)
      }

      // Kotlin writes `a || b` as `if (a) true else b`: the real operands are split across both branches.
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
   * Kotlin writes `a != b` as `(a == b).not()`, tagging both calls [IrStatementOrigin.EXCLEQ].
   * User-written `!(a == b)` has the same shape but without that tag, so this only matches `!=`.
   */
  private fun IrCall.innerEqualsOfNotOperator(): IrCall? {
    if (origin != IrStatementOrigin.EXCLEQ) return null
    val innerEquals = dispatchReceiver as? IrCall ?: return null
    return innerEquals.takeIf { it.origin == IrStatementOrigin.EXCLEQ }
  }

  // True for `foo.bar(...)` or implicit-receiver `bar(...)`, not an operator, property getter, or `!x`.
  private fun IrCall.isPlainMethodCall(): Boolean =
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
