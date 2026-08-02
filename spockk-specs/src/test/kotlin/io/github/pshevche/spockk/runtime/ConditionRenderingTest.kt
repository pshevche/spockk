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
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.CharValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ClassValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.CustomObjectDiffSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.DefaultToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EmptyListValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EmptyStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EmptyToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EnumValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EnumWithToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntArrayValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntegerEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ListAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ListValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LogicalAndSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LogicalOrSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MapValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ObjectArrayValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.PropertyAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SetValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SimpleComparisonSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SingleLineToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringMethodCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ThrowingToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.TypeHintValueSpec
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
      || false
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

  fun `char value`() {
    expect
    failureMessage(CharValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      |c false
      |
      """.trimMargin()
  }

  fun `empty string value`() {
    expect
    failureMessage(EmptyStringValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |""
      |
      """.trimMargin()
  }

  fun `multi-line string value`() {
    expect
    failureMessage(MultiLineStringValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |one
      |two
      |three
      |four
      |
      """.trimMargin()
  }

  fun `list value`() {
    expect
    failureMessage(ListValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |[1, 2, 3]
      |
      """.trimMargin()
  }

  fun `empty list value`() {
    expect
    failureMessage(EmptyListValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |[]
      |
      """.trimMargin()
  }

  fun `map value`() {
    expect
    failureMessage(MapValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |[a:1, b:2]
      |
      """.trimMargin()
  }

  fun `set value`() {
    expect
    failureMessage(SetValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |[1, 2, 3]
      |
      """.trimMargin()
  }

  fun `int array value`() {
    expect
    failureMessage(IntArrayValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |[1, 2]
      |
      """.trimMargin()
  }

  fun `object array value`() {
    expect
    failureMessage(ObjectArrayValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |[one, two]
      |
      """.trimMargin()
  }

  fun `single-line toString`() {
    expect
    failureMessage(SingleLineToStringValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |single line
      |
      """.trimMargin()
  }

  fun `multi-line toString`() {
    expect
    failureMessage(MultiLineToStringValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |mul
      |tiple
      |   lines
      |
      """.trimMargin()
  }

  fun `empty toString falls back to default object representation`() {
    expect
    failureMessage(EmptyToStringValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |${EmptyToStringValueSpec.capturedInstance.objectToString()}
      |
      """.trimMargin()
  }

  fun `throwing toString falls back to default object representation`() {
    expect
    failureMessage(ThrowingToStringValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |${ThrowingToStringValueSpec.capturedInstance.objectToString()} (renderer threw UnsupportedOperationException)
      |
      """.trimMargin()
  }

  fun `default toString is dumped`() {
    val expectedPattern = Regex(
      """^Condition not satisfied:\n\nx == null\n\| \|\n\| false\n""" +
        """<io\.github\.pshevche\.spockk\.fixtures\.runtime\.samples\.condition\.DefaultToString@[0-9a-f]+ a=4>\n$"""
    )
    expect
    expectedPattern.matches(failureMessage(DefaultToStringValueSpec::class))
  }

  fun `enum value`() {
    expect
    failureMessage(EnumValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |RED
      |
      """.trimMargin()
  }

  fun `enum value with toString`() {
    expect
    failureMessage(EnumWithToStringValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |I'm a value
      |
      """.trimMargin()
  }

  fun `class value`() {
    expect
    failureMessage(ClassValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |class java.lang.String
      |
      """.trimMargin()
  }

  fun `type hint for values with same representation but different types`() {
    expect
    failureMessage(TypeHintValueSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  1 (java.lang.String)
      || false
      |1 (java.lang.Integer)
      |
      """.trimMargin()
  }

  fun `custom objects without toString are dumped on both sides`() {
    val expectedPattern = Regex(
      """^Condition not satisfied:\n\nb1 == b2\n\|  \|  \|\n""" +
        """\|  \|  <io\.github\.pshevche\.spockk\.fixtures\.runtime\.samples\.condition\.Bean@[0-9a-f]+ integer=2 string=fun2>\n""" +
        """\|  false\n""" +
        """<io\.github\.pshevche\.spockk\.fixtures\.runtime\.samples\.condition\.Bean@[0-9a-f]+ integer=1 string=fun>\n$"""
    )
    expect
    expectedPattern.matches(failureMessage(CustomObjectDiffSpec::class))
  }

  private fun failureMessage(clazz: KClass<*>): String {
    val events = execute(selectClass(clazz.java))
    return events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow().message ?: ""
  }
}
