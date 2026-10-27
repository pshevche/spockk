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
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.BackslashEscapingSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.BinaryOperatorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.BooleanNegationSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.CastToBooleanSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ChainedExpressionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.CharValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ClassValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.CollectionPropertySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ConstantsOnlySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.CustomObjectDiffSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.DefaultToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EmptyListValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EmptyStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EmptyToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EnumValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EnumWithToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ExclusiveRangeSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ExplicitTargetCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ImplicitTargetCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.InclusiveRangeSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IndexAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntArrayValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntegerEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.JavaStaticFieldSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.JavaStaticMethodCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ListAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ListValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LogicalAndSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.LogicalOrSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MapValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NamedArgumentsCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NestedVerifyInVerifyAllSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NotOperatorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ObjectArrayValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.PrefixIncrementSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.PropertyAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameNameClassesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SetValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SimpleComparisonSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SimpleConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SingleLineToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SpreadArgumentSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SpreadIntoVarargsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StaticImportCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StaticImportEnumSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StaticImportFieldSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringEqualsMethodCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringMethodCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ThreeArgumentCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ThrowingToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.TopLevelMethodCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.TopLevelStaticCallSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.TypeHintValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.UnaryMinusSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.UnaryPlusSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VariableAsConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VariableExpressionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VerifyAllPropertyAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VerifyEachPropertyAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VerifyPropertyAccessSpec
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.PendingFeature
import spock.lang.ResourceLock
import spock.lang.Specification
import java.util.TimeZone
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

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#simple condition")
  fun `simple condition`() {
    expect
    failureMessage(SimpleConditionSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == 1
      || |
      |2 false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#multi-line condition")
  fun `multi-line condition`() {
    expect
    failureMessage(MultiLineConditionSpec::class) ==
      """
      |Condition not satisfied:
      |
      |1 + 2 == 4 - 2
      |  |   |    |
      |  3   |    2
      |      false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#MethodCallExpression with implicit target")
  fun `MethodCallExpression with implicit target`() {
    expect
    failureMessage(ImplicitTargetCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |one(a)
      ||   |
      ||   1
      |false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#MethodCallExpression with explicit target")
  fun `MethodCallExpression with explicit target`() {
    expect
    failureMessage(ExplicitTargetCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |a.get(b) == 5
      || |   |  |
      || 1   0  false
      |[1]
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#top-level MethodCallExpression")
  fun `top-level MethodCallExpression`() {
    expect
    failureMessage(TopLevelMethodCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |a.get(b)
      || |   |
      || |   0
      || false
      |[false]
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#MethodCallExpression with named arguments")
  fun `MethodCallExpression with named arguments`() {
    expect
    failureMessage(NamedArgumentsCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |person.eat(what = steak, where = tokyo)
      ||          |      |      |
      |p          steak  tokyo  false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#StaticMethodCallExpression")
  fun `StaticMethodCallExpression`() {
    expect
    failureMessage(StaticImportCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |min(a, b) == 5
      ||   |  |  |
      |1   1  2  false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#top-level StaticMethodCallExpression")
  fun `top-level StaticMethodCallExpression`() {
    expect
    failureMessage(TopLevelStaticCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |parseBoolean(a)
      ||            |
      |false        no
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#BinaryExpression")
  fun `BinaryExpression`() {
    expect
    failureMessage(BinaryOperatorSpec::class) ==
      """
      |Condition not satisfied:
      |
      |a * b == 5
      || | | |
      |0 0 1 false
      |
      """.trimMargin()
    failureMessage(IndexAccessSpec::class) ==
      """
      |Condition not satisfied:
      |
      |a[b]
      ||||
      |||0
      ||false
      |[false]
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#BooleanExpression")
  fun `BooleanExpression`() {
    expect
    failureMessage(VariableAsConditionSpec::class) ==
      """
      |Condition not satisfied:
      |
      |a
      ||
      |false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#RangeExpression")
  fun `RangeExpression`() {
    expect
    failureMessage(InclusiveRangeSpec::class) ==
      """
      |Condition not satisfied:
      |
      |(a..b) == 5..6
      | |  |  |
      | 1  2  false
      |
      """.trimMargin()
    failureMessage(ExclusiveRangeSpec::class) ==
      """
      |Condition not satisfied:
      |
      |(a..<b) == 5..6
      | |   |  |
      | 1   2  false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#ConstantExpression")
  fun `ConstantExpression`() {
    expect
    failureMessage(ConstantsOnlySpec::class) ==
      """
      |Condition not satisfied:
      |
      |"abc" == "abd"
      |      |
      |      false
      |      1 difference (66% similarity)
      |      ab(c)
      |      ab(d)
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#SpreadExpression")
  fun `SpreadExpression`() {
    expect
    failureMessage(SpreadArgumentSpec::class) ==
      """
      |Condition not satisfied:
      |
      |two(*a)
      ||    |
      ||    [1, 2]
      |false
      |
      """.trimMargin()
    failureMessage(SpreadIntoVarargsSpec::class) ==
      """
      |Condition not satisfied:
      |
      |intArrayOf(1, *a).isEmpty()
      ||              |  |
      |[1, 2, 3]      |  false
      |               [2, 3]
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#NotExpression")
  fun `NotExpression`() {
    expect
    failureMessage(NotOperatorSpec::class) ==
      """
      |Condition not satisfied:
      |
      |!a
      |||
      ||true
      |false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#CastExpression")
  fun `CastExpression`() {
    expect
    failureMessage(CastToBooleanSpec::class) ==
      """
      |Condition not satisfied:
      |
      |a as Boolean
      ||
      |false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#ArgumentListExpression")
  fun `ArgumentListExpression`() {
    expect
    failureMessage(ThreeArgumentCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |three(a, b, c)
      ||     |  |  |
      |false 1  2  3
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#statically imported field")
  fun `statically imported field`() {
    expect
    failureMessage(StaticImportFieldSpec::class) ==
      """
      |Condition not satisfied:
      |
      |MAX_VALUE == 0
      ||         |
      ||         false
      |2147483647
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#properly escape backslashes in rendering")
  fun `properly escape backslashes in rendering`() {
    expect
    failureMessage(BackslashEscapingSpec::class) ==
      """
      |Condition not satisfied:
      |
      |":\":\\" == null
      |         |
      |         false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#VariableExpression")
  fun `VariableExpression`() {
    expect
    failureMessage(VariableExpressionSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x
      ||
      |false
      |
      """.trimMargin()
  }

  @ResourceLock("TimeZone.default")
  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#ClassExpression with different classes with same name")
  fun `ClassExpression with different classes with same name`() {
    val defaultTimeZone = TimeZone.getDefault()
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    try {
      expect
      failureMessage(SameNameClassesSpec::class) ==
        """
      |Condition not satisfied:
      |
      |date.javaClass == java.sql.Date::class.java
      ||    |            |    |
      ||    |            |    false
      ||    |            class java.sql.Date
      ||    class java.util.Date
      |Thu Jan 01 00:00:00 UTC 1970
      |
        """.trimMargin()
    } finally {
      TimeZone.setDefault(defaultTimeZone)
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/848")
  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#MethodCallExpression invoking static method")
  fun `MethodCallExpression invoking static method`() {
    expect
    failureMessage(JavaStaticMethodCallSpec::class) ==
      """
      |Condition not satisfied:
      |
      |Math.max(a, b) == 5
      ||    |   |  |   |
      ||    2   1  2   false
      |class java.lang.Math
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/848")
  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#PropertyExpression")
  fun `PropertyExpression`() {
    expect
    failureMessage(CollectionPropertySpec::class) ==
      """
      |Condition not satisfied:
      |
      |a.size == 5
      || |    |
      || 1    false
      |[9]
      |
      """.trimMargin()
    failureMessage(JavaStaticFieldSpec::class) ==
      """
      |Condition not satisfied:
      |
      |Integer.MIN_VALUE == 5
      ||       |         |
      ||       |         false
      ||       -2147483648
      |class java.lang.Integer
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/847")
  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#PrefixExpression")
  fun `PrefixExpression`() {
    expect
    failureMessage(PrefixIncrementSpec::class) ==
      """
      |Condition not satisfied:
      |
      |++x == 5
      ||   |
      |1   false
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/847")
  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#UnaryMinusExpression")
  fun `UnaryMinusExpression`() {
    expect
    failureMessage(UnaryMinusSpec::class) ==
      """
      |Condition not satisfied:
      |
      |-a == 5
      ||| |
      ||1 false
      |-1
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/847")
  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#UnaryPlusExpression")
  fun `UnaryPlusExpression`() {
    expect
    failureMessage(UnaryPlusSpec::class) ==
      """
      |Condition not satisfied:
      |
      |+a == 5
      ||| |
      ||1 false
      |1
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/840")
  @MigratedFrom("org.spockframework.smoke.condition.ConditionRendering#statically imported enum value")
  fun `statically imported enum value`() {
    expect
    failureMessage(StaticImportEnumSpec::class) ==
      """
      |Condition not satisfied:
      |
      |BLOCKED == null
      |        |
      |        false
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
