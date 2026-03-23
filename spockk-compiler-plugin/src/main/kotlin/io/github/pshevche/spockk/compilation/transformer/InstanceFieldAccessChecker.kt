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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SHARED_ANNOTATION_FQN
import io.github.pshevche.spockk.compilation.shared.BaseSpockkIrElementVisitor
import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.ir.util.isStatic
import org.jetbrains.kotlin.ir.visitors.acceptVoid

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal class InstanceFieldAccessChecker(
  private val spec: IrClass,
  private val contextIr: IrElement
) : BaseSpockkIrElementVisitor() {
  fun check(expression: IrExpression) {
    expression.acceptVoid(this)
  }

  fun check(statements: Collection<IrStatement>) {
    statements.forEach { it.acceptVoid(this) }
  }

  override fun visitCall(expression: IrCall) {
    val owner = expression.symbol.owner
    val receiver = expression.dispatchReceiver

    if (receiver != null && receiver.isCurrentSpec()) {
      when {
        owner.isGetter || owner.isSetter -> {
          val backingField = owner.correspondingPropertySymbol?.owner?.backingField
          if (backingField?.hasAnnotation(SHARED_ANNOTATION_FQN) != true) {
            throwIllegalMemberAccessException()
          }
        }

        !owner.isStatic -> throwIllegalMemberAccessException()
      }
    }

    super.visitCall(expression)
  }

  private fun throwIllegalMemberAccessException(): Nothing = throw CompilationException(
    "Only companion object members and @Shared fields may be accessed from here",
    spec.file,
    contextIr
  )

  private fun IrExpression.isCurrentSpec(): Boolean =
    this is IrGetValue && symbol.owner.name.asString() == "<this>" &&
      symbol.owner.type.classOrNull?.owner == spec
}
