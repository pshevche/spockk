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

package io.github.pshevche.spockk.compilation.transformer.fixture

import io.github.pshevche.spockk.compilation.ir.makePrivate
import io.github.pshevche.spockk.compilation.shared.SpockkTransformationContext.FixtureMethodKind
import io.github.pshevche.spockk.compilation.transformer.InstanceFieldAccessChecker
import io.github.pshevche.spockk.compilation.transformer.SpockkIrRewriter
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody

internal class FixtureMethodRewriter(
  override val generatorContext: IrGeneratorContext,
  private val spec: IrClass,
  private val fixtureMethods: Map<IrFunction, FixtureMethodKind>
) : SpockkIrRewriter {

  fun rewrite() {
    fixtureMethods.forEach { (function, kind) ->
      function.makePrivate()
      if (kind == FixtureMethodKind.SETUP_SPEC || kind == FixtureMethodKind.CLEANUP_SPEC) {
        val body = function.body as IrBlockBody
        InstanceFieldAccessChecker(spec, function).check(body.statements)
      }
    }
  }
}
