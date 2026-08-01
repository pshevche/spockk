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

package io.github.pshevche.spockk.runtime

import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ArithmeticExpressionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.BooleanNegationSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ChainedExpressionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntegerEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ListAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LogicalAndSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LogicalOrSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.PropertyAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SimpleComparisonSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringMethodCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringValueSpec
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification
import kotlin.reflect.KClass

class ConditionRenderingTest : Specification() {

  fun `simple comparison`() {
    expect
    failureMessage(SimpleComparisonSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x > y
      || | |
      |5 | 10
      |  false
      |
      """.trimMargin()
  }

  fun `integer equality`() {
    expect
    failureMessage(IntegerEqualitySpec::class) ==
      """
      |Condition not satisfied:
      |
      |1 == 2
      |  |
      |  false
      |
      """.trimMargin()
  }

  fun `null value`() {
    expect
    failureMessage(NullValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x != null
      || |
      || true
      |null
      |
      """.trimMargin()
  }

  fun `string value`() {
    expect
    failureMessage(StringValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |hello
      |
      """.trimMargin()
  }

  fun `boolean negation`() {
    expect
    failureMessage(BooleanNegationSpec::class) ==
      """
      |Condition not satisfied:
      |
      |!flag
      |||
      ||true
      |false
      |
      """.trimMargin()
  }

  fun `string method call`() {
    expect
    failureMessage(StringMethodCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |startsWith("xyz")
      |
      """.trimMargin()
  }

  fun `property access`() {
    expect
    failureMessage(PropertyAccessSpec::class) ==
      """
      |Condition not satisfied:
      |
      |str.length == 3
      ||   |      |
      ||   5      false
      |hello
      |
      """.trimMargin()
  }

  fun `arithmetic expression`() {
    expect
    failureMessage(ArithmeticExpressionSpec::class) ==
      """
      |Condition not satisfied:
      |
      |a + b == 4
      || | | |
      |1 3 2 false
      |
      """.trimMargin()
  }

  fun `logical and`() {
    expect
    failureMessage(LogicalAndSpec::class) ==
      """
      |Condition not satisfied:
      |
      |left && right
      ||
      |false
      |
      """.trimMargin()
  }

  fun `logical or`() {
    expect
    failureMessage(LogicalOrSpec::class) ==
      """
      |Condition not satisfied:
      |
      |left || right
      ||
      |false
      |
      """.trimMargin()
  }

  fun `list access`() {
    expect
    failureMessage(ListAccessSpec::class) ==
      """
      |Condition not satisfied:
      |
      |list[0] == 10
      ||   |   |
      ||   1   false
      |[1, 2, 3]
      |
      """.trimMargin()
  }

  fun `chained expression`() {
    expect
    failureMessage(ChainedExpressionSpec::class) ==
      """
      |Condition not satisfied:
      |
      |list[0].length == 10
      ||   |   |      |
      ||   abc 3      false
      |[abc, def]
      |
      """.trimMargin()
  }

  fun `string equality`() {
    expect
    failureMessage(StringEqualitySpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  kotlin
      || false
      || 6 differences (0% similarity)
      || (sp)o(ckk-)
      || (k-)o(tlin)
      |spockk
      |
      """.trimMargin()
  }

  private fun failureMessage(clazz: KClass<*>): String {
    val events = execute(selectClass(clazz.java))
    return events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow().message ?: ""
  }
}
