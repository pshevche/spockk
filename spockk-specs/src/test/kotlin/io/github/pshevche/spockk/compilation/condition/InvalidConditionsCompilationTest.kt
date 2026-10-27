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

package io.github.pshevche.spockk.compilation.condition

import io.github.pshevche.spockk.compilation.BaseCompilationTest
import io.github.pshevche.spockk.compilation.TestDataFactory.specWithFeatureBody
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import spock.lang.PendingFeature

@OptIn(ExperimentalCompilerApi::class)
class InvalidConditionsCompilationTest : BaseCompilationTest() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/873")
  @MigratedFrom("org.spockframework.smoke.condition.InvalidConditions#assignments are not allowed in expect-blocks")
  fun `assignments are not allowed in expect-blocks`() {
    `when`
    val assign = transform(specWithFeatureBody(expectBlockWith("=")))
    val plusAssign = transform(specWithFeatureBody(expectBlockWith("+=")))
    val minusAssign = transform(specWithFeatureBody(expectBlockWith("-=")))

    then
    !assign.isSuccess()
    !plusAssign.isSuccess()
    !minusAssign.isSuccess()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/873")
  @MigratedFrom("org.spockframework.smoke.condition.InvalidConditions#assignments are not allowed in then-blocks")
  fun `assignments are not allowed in then-blocks`() {
    `when`
    val assign = transform(specWithFeatureBody(thenBlockWith("=")))
    val plusAssign = transform(specWithFeatureBody(thenBlockWith("+=")))
    val minusAssign = transform(specWithFeatureBody(thenBlockWith("-=")))

    then
    !assign.isSuccess()
    !plusAssign.isSuccess()
    !minusAssign.isSuccess()
  }

  @MigratedFrom(
    "org.spockframework.smoke.condition.InvalidConditions#assignment arithmetic operators are not allowed in explicit conditions"
  )
  fun `assignment arithmetic operators are not allowed in explicit conditions`() {
    `when`
    val plusAssign = transform(specWithFeatureBody(explicitConditionWith("+=")))
    val minusAssign = transform(specWithFeatureBody(explicitConditionWith("-=")))

    then
    !plusAssign.isSuccess()
    plusAssign.compilation.messages.contains("Only expressions are allowed in this context")
    !minusAssign.isSuccess()
    minusAssign.compilation.messages.contains("Only expressions are allowed in this context")
  }

  @MigratedFrom("org.spockframework.smoke.condition.InvalidConditions#assignments are not allowed in explicit conditions")
  fun `assignments are not allowed in explicit conditions`() {
    `when`
    val result = transform(specWithFeatureBody(explicitConditionWith("=")))

    then
    !result.isSuccess()
    result.compilation.messages.contains("No parameter with name 'x' found")
  }

  @MigratedFrom(
    "org.spockframework.smoke.condition.InvalidConditions#assignments are allowed if they are part of a variable declaration"
  )
  fun `assignments are allowed if they are part of a variable declaration`() {
    val x = 42

    expect
    x == 42
  }

  private fun expectBlockWith(op: String) =
    """
    var x = 0
    io.github.pshevche.spockk.lang.expect
    x $op 0
    """
      .trimIndent()

  private fun thenBlockWith(op: String) =
    """
    var x = 42
    io.github.pshevche.spockk.lang.`when`
    true

    io.github.pshevche.spockk.lang.then
    x $op 42
    """
      .trimIndent()

  private fun explicitConditionWith(op: String) =
    """
    var x = 42
    io.github.pshevche.spockk.lang.setup
    assert(x $op 42)
    """
      .trimIndent()
}
