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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
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
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EqualStringsInSubexpressionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntArrayValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntegerEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.InterpolatedStringLiteralsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LargeStringCompleteDifferenceSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LargeStringDifferenceAtEndSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LargeStringDifferenceAtStartAndEndSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LargeStringDifferenceAtStartSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LargeStringDifferenceInMiddleSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LargeStringWithoutContextSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ListAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ListValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LogicalAndSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LogicalOrSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LongStringLiteralsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MapValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NestedVerifyInVerifyAllSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ObjectArrayValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.PropertyAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SetValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SimpleComparisonSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SingleLineToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringDiffIntOverflowSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringDifferenceInSubexpressionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringDifferenceSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringEqualsMethodCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringLiteralsWithLineBreaksAndNewlineEscapesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringLiteralsWithLineBreaksSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringLiteralsWithNewlineEscapesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringMethodCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringNullLeftSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringNullRightSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ThrowingToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.TypeHintValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VerifyAllPropertyAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VerifyEachPropertyAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VerifyPropertyAccessSpec
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.FailedStringComparisonRenderer
import spock.lang.PendingFeature
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
      |str.startsWith("xyz")
      ||   |
      ||   false
      |hello
      |
      """.trimMargin()
  }

  fun `string equals method call`() {
    expect
    failureMessage(StringEqualsMethodCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |str.equals("xyz")
      ||   |
      ||   false
      |hello
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
    // left is false, so && short-circuits: right is never evaluated and has no recorded value.
    expect
    failureMessage(LogicalAndSpec::class) ==
      """
      |Condition not satisfied:
      |
      |left && right
      ||    |
      ||    false
      |false
      |
      """.trimMargin()
  }

  fun `logical or`() {
    // left is false, so || must evaluate right too; both operands are recorded.
    expect
    failureMessage(LogicalOrSpec::class) ==
      """
      |Condition not satisfied:
      |
      |left || right
      ||    |  |
      |false|  false
      |     false
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

  fun `implicit target inside verify is not itself recorded`() {
    expect
    failureMessage(VerifyPropertyAccessSpec::class) ==
      """
      |Condition not satisfied:
      |
      |length == 3
      ||      |
      |5      false
      |
      """.trimMargin()
  }

  fun `implicit target inside verifyAll is not itself recorded`() {
    expect
    failureMessage(VerifyAllPropertyAccessSpec::class) ==
      """
      |Condition not satisfied:
      |
      |length == 3
      ||      |
      |5      false
      |
      """.trimMargin()
  }

  fun `implicit target inside nested verify-in-verifyAll is not itself recorded`() {
    expect
    failureMessage(NestedVerifyInVerifyAllSpec::class) ==
      """
      |Condition not satisfied:
      |
      |length == 3
      ||      |
      |5      false
      |
      """.trimMargin()
  }

  fun `verifyEach reports the same condition diagram per failing item`() {
    val message = failureMessage(VerifyEachPropertyAccessSpec::class)

    expect
    message.contains("Assertions failed for item[0] hello:")
    message.contains("Assertions failed for item[1] hi:")
    message.contains(
      """
      |Condition not satisfied:
      |
      |length == 3
      ||      |
      |5      false
      """.trimMargin()
    )
    message.contains(
      """
      |Condition not satisfied:
      |
      |length == 3
      ||      |
      |2      false
      """.trimMargin()
    )
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#shows differences between strings")
  fun `shows differences between strings`() {
    expect
    failureMessage(StringDifferenceSpec::class) ==
      """
      |Condition not satisfied:
      |
      |"the quick" == "the quirk"
      |            |
      |            false
      |            1 difference (88% similarity)
      |            the qui(c)k
      |            the qui(r)k
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#handles case where one side is null")
  fun `handles case where one side is null`() {
    expect
    failureMessage(StringNullRightSpec::class) ==
      """
      |Condition not satisfied:
      |
      |"foo" == null
      |      |
      |      false
      |
      """.trimMargin()
    failureMessage(StringNullLeftSpec::class) ==
      """
      |Condition not satisfied:
      |
      |null == "foo"
      |     |
      |     false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#shows differences between strings in subexpression")
  fun `shows differences between strings in subexpression`() {
    expect
    failureMessage(StringDifferenceInSubexpressionSpec::class) ==
      """
      |Condition not satisfied:
      |
      |("the quick" == "the quirk") == true
      |             |               |
      |             |               false
      |             false
      |             1 difference (88% similarity)
      |             the qui(c)k
      |             the qui(r)k
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#does not show differences if strings in subexpression are equal")
  fun `does not show differences if strings in subexpression are equal`() {
    expect
    failureMessage(EqualStringsInSubexpressionSpec::class) ==
      """
      |Condition not satisfied:
      |
      |("the quick" == "the quick") == false
      |             |               |
      |             true            false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#large String comparison does not cause OOM-Error, difference at start")
  fun `large String comparison does not cause OOM-Error, difference at start`() {
    val message = failureMessage(LargeStringDifferenceAtStartSpec::class)
    expect
    message.contains("1 difference (90% similarity) (comparing subset start: 0, end1: 11, end2: 11)")
    message.contains("(a)aaaaaaaaaa")
    message.contains("(b)aaaaaaaaaa")
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#large String comparison does not cause OOM-Error, difference in the middle")
  fun `large String comparison does not cause OOM-Error, difference in the middle`() {
    val message = failureMessage(LargeStringDifferenceInMiddleSpec::class)
    expect
    message.contains("1 difference (95% similarity) (comparing subset start: 12789, end1: 12811, end2: 12811)")
    message.contains("aaaaaaaaaaa(a)aaaaaaaaaa")
    message.contains("aaaaaaaaaaa(b)aaaaaaaaaa")
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#large String comparison does not cause OOM-Error, difference at end")
  fun `large String comparison does not cause OOM-Error, difference at end`() {
    val message = failureMessage(LargeStringDifferenceAtEndSpec::class)
    expect
    message.contains("1 difference (91% similarity) (comparing subset start: 25588, end1: 25600, end2: 25600)")
    message.contains("aaaaaaaaaaa(a)")
    message.contains("aaaaaaaaaaa(b)")
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#large String comparison does not cause OOM-Error, difference at start and  end")
  fun `large String comparison does not cause OOM-Error, difference at start and end`() {
    val message = failureMessage(LargeStringDifferenceAtStartAndEndSpec::class)
    expect
    message.contains("false")
    message.contains("Strings too large to calculate edit distance.")
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#large String comparison does not cause OOM-Error, complete difference")
  fun `large String comparison does not cause OOM-Error, complete difference`() {
    val message = failureMessage(LargeStringCompleteDifferenceSpec::class)
    expect
    message.contains("false")
    message.contains("Strings too large to calculate edit distance.")
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#large String comparison without room for context")
  fun `large String comparison without room for context`() {
    val stringLength = Math.sqrt(FailedStringComparisonRenderer.MAX_EDIT_DISTANCE_MEMORY.toDouble()).toInt()
    val message = failureMessage(LargeStringWithoutContextSpec::class)
    expect
    message.contains("false")
    message.contains("$stringLength differences (0% similarity) (comparing subset start: 0, end1: $stringLength, end2: $stringLength)")
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#String diff does not cause int overflow when shortening, causing OOM")
  fun `String diff does not cause int overflow when shortening, causing OOM`() {
    val message = failureMessage(StringDiffIntOverflowSpec::class)
    expect
    message.contains("false")
    message.contains("Strings too large to calculate edit distance.")
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#shows differences between string literals with line breaks")
  fun `shows differences between string literals with line breaks`() {
    expect
    failureMessage(StringLiteralsWithLineBreaksSpec::class) ==
      """
      |Condition not satisfied:
      |
      |${'"'}${'"'}${'"'}foo ${'"'}${'"'}${'"'} == ${'"'}${'"'}${'"'}bar ${'"'}${'"'}${'"'}
      |           |
      |           false
      |           4 differences (20% similarity)
      |           (foo)\n(-~)
      |           (bar)\n(\n)
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#shows differences between string literals with newline escapes")
  fun `shows differences between string literals with newline escapes`() {
    expect
    failureMessage(StringLiteralsWithNewlineEscapesSpec::class) ==
      listOf(
        "Condition not satisfied:",
        "",
        "\"foo  \" == \"bar  \"",
        "        |",
        "        false",
        "        3 differences (40% similarity)",
        "        (foo)  ",
        "        (bar)  ",
        ""
      ).joinToString("\n")
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#shows differences between string literals with line breaks and newline escapes")
  fun `shows differences between string literals with line breaks and newline escapes`() {
    expect
    failureMessage(StringLiteralsWithLineBreaksAndNewlineEscapesSpec::class) ==
      """
      |Condition not satisfied:
      |
      |"\\\nfoo" == "\\\nfoo       \n"
      |          |
      |          false
      |          8 differences (38% similarity)
      |          \\\nfoo(--------~)
      |          \\\nfoo(       \n)
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/886")
  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#shows differences between interpolated string literals with line breaks and newline escapes")
  fun `shows differences between interpolated string literals with line breaks and newline escapes`() {
    expect
    failureMessage(InterpolatedStringLiteralsSpec::class) ==
      """
      |Condition not satisfied:
      |
      |"${'$'}a\n" == "\\${'$'}b \n"
      |  |    |      |
      |  foo  |      bar
      |       false
      |       5 differences (16% similarity)
      |       (f~oo--)\n
      |       (\\bar )\n
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.StringComparisonRendering#shows differences between long string literals with line breaks and newline escapes")
  fun `shows differences between long string literals with line breaks and newline escapes`() {
    expect
    failureMessage(LongStringLiteralsSpec::class) ==
      """
      |Condition not satisfied:
      |
      |"Lorem ipsum\n\nLorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua." == "Lorem ipsum\nLorem ipsum  dolor sit amet,  consetetur sadipscing elitr, sed  diam nonumy eirmod tempor  invidunt ut labore et  dolore magna aliquyam erat, sed diam voluptua.\n"
      |                                                                                                                                                                             |
      |                                                                                                                                                                             false
      |                                                                                                                                                                             7 differences (95% similarity)
      |                                                                                                                                                                             Lorem ipsum\n(\n)Lorem ipsum (-)dolor sit amet, (-)consetetur sadipscing elitr, sed (-)diam nonumy eirmod tempor (-)invidunt ut labore et (-)dolore magna aliquyam erat, sed diam voluptua.(-~)
      |                                                                                                                                                                             Lorem ipsum\n(-~)Lorem ipsum ( )dolor sit amet, ( )consetetur sadipscing elitr, sed ( )diam nonumy eirmod tempor ( )invidunt ut labore et ( )dolore magna aliquyam erat, sed diam voluptua.(\n)
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
