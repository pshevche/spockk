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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers
import io.github.pshevche.spockk.compilation.ir.fqName
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression

internal enum class ImplicitAssertionHelperKind {
  VERIFY,
  VERIFY_ALL,
  VERIFY_EACH
}

/**
 * A statement that is a call to `verify`/`verifyAll`/`verifyEach` with a literal trailing lambda
 * argument - matches Spock's own restriction that only literal closures get implicit-condition
 * treatment (non-literal arguments, e.g. a lambda stored in a `val` first, are left as ordinary
 * calls).
 */
internal class ImplicitAssertionHelperCall(
  val call: IrCall,
  val kind: ImplicitAssertionHelperKind,
  val lambda: IrSimpleFunction
)

internal fun IrStatement.asImplicitAssertionHelperCall(): ImplicitAssertionHelperCall? {
  val call = this as? IrCall ?: return null
  val kind = when (call.fqName()) {
    IrIdentifiers.Spockk.VERIFY_FQN -> ImplicitAssertionHelperKind.VERIFY
    IrIdentifiers.Spockk.VERIFY_ALL_FQN -> ImplicitAssertionHelperKind.VERIFY_ALL
    IrIdentifiers.Spockk.VERIFY_EACH_FQN -> ImplicitAssertionHelperKind.VERIFY_EACH
    else -> return null
  }
  val lambda = (call.arguments.lastOrNull() as? IrFunctionExpression)?.function ?: return null
  return ImplicitAssertionHelperCall(call, kind, lambda)
}

internal fun List<IrStatement>.containsImplicitAssertionHelperCall(): Boolean =
  any { it.asImplicitAssertionHelperCall() != null }
