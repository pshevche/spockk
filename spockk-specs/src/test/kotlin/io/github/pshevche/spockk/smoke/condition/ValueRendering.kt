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

package io.github.pshevche.spockk.smoke.condition

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.CharValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.DefaultToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EmptyStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EmptyToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EnumLiteralSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EnumVariableSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EnumWithToString
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EnumWithToStringLiteralSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ListValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MapValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MultiLineToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SingleLineToStringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StaticImportedEnumLiteralSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.StringValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ThrowingToStringValueSpec
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class ValueRendering : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#null value")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#char value")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#string value")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#empty string value")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#multi-line string value")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#list value")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#map value")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#single-line toString")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#multi-line toString")
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#empty toString")
  fun `empty toString`() {
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#exception-throwing toString")
  fun `exception-throwing toString`() {
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

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#variable with default to string is dump()ed")
  fun `variable with default to string is dump()ed`() {
    val expectedPattern = Regex(
      """^Condition not satisfied:\n\nx == null\n\| \|\n\| false\n""" +
        """<io\.github\.pshevche\.spockk\.fixtures\.runtime\.samples\.condition\.DefaultToString@[0-9a-f]+ a=4>\n$"""
    )
    expect
    expectedPattern.matches(failureMessage(DefaultToStringValueSpec::class))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/840")
  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#enum literal")
  fun `enum literal`() {
    expect
    failureMessage(EnumLiteralSpec::class) ==
      """
      |Condition not satisfied:
      |
      |Thread.State.NEW == null
      |       |         |
      |       |         false
      |       class java.lang.Thread${'$'}State
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/840")
  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#statically imported enum literal")
  fun `statically imported enum literal`() {
    expect
    failureMessage(StaticImportedEnumLiteralSpec::class) ==
      """
      |Condition not satisfied:
      |
      |NEW == null
      |    |
      |    false
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/840")
  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#enum literal with toString")
  fun `enum literal with toString`() {
    expect
    failureMessage(EnumWithToStringLiteralSpec::class) ==
      """
      |Condition not satisfied:
      |
      |EnumWithToString.VALUE == null
      ||                |     |
      ||                |     false
      ||                I'm a value
      |${EnumWithToString::class.java}
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ValueRendering#variable with enum value")
  fun `variable with enum value`() {
    expect
    failureMessage(EnumVariableSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == null
      || |
      || false
      |NEW
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
