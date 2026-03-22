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

package io.github.pshevche.spockk.compilation.shared

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

internal open class BaseSpockkIrElementVisitor : IrVisitorVoid() {
  private val classStack = ArrayDeque<IrClass>()
  private val functionStack = ArrayDeque<IrFunction>()

  protected val maybeCurrentIrClass: IrClass?
    get() = classStack.lastOrNull()

  protected val currentIrClass: IrClass
    get() = maybeCurrentIrClass!!

  protected val currentIrFunction: IrFunction
    get() = functionStack.last()

  override fun visitElement(element: IrElement) {
    element.acceptChildrenVoid(this)
  }

  final override fun visitClass(declaration: IrClass) {
    classStack.addLast(declaration)
    try {
      visitClassNew(declaration)
    } finally {
      classStack.removeLast()
    }
  }

  protected open fun visitClassNew(declaration: IrClass) {
    super.visitClass(declaration)
  }

  final override fun visitFunction(declaration: IrFunction) {
    functionStack.addLast(declaration)
    try {
      visitFunctionNew(declaration)
    } finally {
      functionStack.removeLast()
    }
  }

  protected open fun visitFunctionNew(declaration: IrFunction) {
    super.visitFunction(declaration)
  }

  final override fun visitProperty(declaration: IrProperty) {
    visitPropertyNew(declaration)
  }

  protected open fun visitPropertyNew(declaration: IrProperty) {
    super.visitProperty(declaration)
  }
}
