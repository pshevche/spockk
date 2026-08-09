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

import io.github.pshevche.spockk.compilation.ir.IrIdentifiers.Spock.SPEC_INTERNALS_FQN
import io.github.pshevche.spockk.compilation.ir.findRequiredClassSymbol
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol

/**
 * Wraps `org.spockframework.runtime.SpecInternals`, a real Spock class shaded unmodified into
 * `spockk-core`, so `thrown(Type)` can reuse its `checkExceptionThrown` logic instead of
 * reimplementing exception-type matching and error selection.
 */
internal class IrSpecInternals private constructor(
  private val specInternalsClassSymbol: IrClassSymbol
) {

  fun irCheckExceptionThrown(
    builder: IrBuilder,
    specAccessor: IrValueParameter,
    exceptionTypeExpr: IrExpression
  ): IrCall {
    val checkExceptionThrown = specInternalsClassSymbol.functionByName("checkExceptionThrown")
    return with(builder) {
      irCall(checkExceptionThrown).apply {
        arguments[0] = irGet(specAccessor)
        arguments[1] = exceptionTypeExpr
      }
    }
  }

  companion object {
    fun create(generatorContext: IrGeneratorContext): IrSpecInternals {
      val specInternalsClass = generatorContext.findRequiredClassSymbol(SPEC_INTERNALS_FQN)
      return IrSpecInternals(specInternalsClass)
    }
  }
}
