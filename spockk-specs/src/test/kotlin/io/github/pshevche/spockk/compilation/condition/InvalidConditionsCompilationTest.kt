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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import spock.lang.PendingFeature

@OptIn(ExperimentalCompilerApi::class)
class InvalidConditionsCompilationTest : BaseCompilationTest() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/873")
  @MigratedFrom("org.spockframework.smoke.condition.InvalidConditions#assignments are not allowed in expect-blocks")
  fun `assignments are not allowed in expect-blocks`(op: String) {
    `when`
    val result = transform(specWithFeatureBody(expectBlockWith(op)))

    then
    !result.isSuccess()
    result.compilation.messages.contains("assignment")

    where
    variable(op).from("=", "+=", "-=")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/873")
  @MigratedFrom("org.spockframework.smoke.condition.InvalidConditions#assignments are not allowed in then-blocks")
  fun `assignments are not allowed in then-blocks`(op: String) {
    `when`
    val result = transform(specWithFeatureBody(thenBlockWith(op)))

    then
    !result.isSuccess()
    result.compilation.messages.contains("assignment")

    where
    variable(op).from("=", "+=", "-=")
  }

  @MigratedFrom(
    "org.spockframework.smoke.condition.InvalidConditions#assignment arithmetic operators are not allowed in explicit conditions"
  )
  fun `assignment arithmetic operators are not allowed in explicit conditions`(op: String) {
    `when`
    val result = transform(specWithFeatureBody(explicitConditionWith(op)))

    then
    !result.isSuccess()
    result.compilation.messages.contains("Only expressions are allowed in this context")

    where
    variable(op).from("+=", "-=")
  }

  @MigratedFrom("org.spockframework.smoke.condition.InvalidConditions#assignments are not allowed in explicit conditions")
  fun `assignments are not allowed in explicit conditions`() {
    `when`
    val result = transform(specWithFeatureBody(explicitConditionWith("=")))

    then
    !result.isSuccess()
    result.compilation.messages.contains("No parameter with name 'x' found")
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
