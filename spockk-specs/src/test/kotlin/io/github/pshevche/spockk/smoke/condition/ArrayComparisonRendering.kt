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
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.DefaultToStringArrayValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntArray2dEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntArray3dEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntArrayValueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ObjectArray2dEqualitySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ObjectArrayValueSpec
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification
import kotlin.reflect.KClass

class ArrayComparisonRendering : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.ArrayComparisonRendering#primitive array value")
  fun `primitive array value`() {
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

  @MigratedFrom("org.spockframework.smoke.condition.ArrayComparisonRendering#object array value")
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

  @MigratedFrom("org.spockframework.smoke.condition.ArrayComparisonRendering#arrays of variables with default to string is dump()ed")
  fun `arrays of variables with default to string is dump()ed`() {
    val element = """<io\.github\.pshevche\.spockk\.fixtures\.runtime\.samples\.condition\.DefaultToString@[0-9a-f]+ a=4>"""
    val expectedPattern = Regex(
      "Condition not satisfied:\n\nx == null\n\\| \\|\n\\| false\n\\[$element, $element]\n"
    )
    expect
    expectedPattern.matches(failureMessage(DefaultToStringArrayValueSpec::class))
  }

  @MigratedFrom("org.spockframework.smoke.condition.ArrayComparisonRendering#primitive two-dimensional array value")
  fun `primitive two-dimensional array value`() {
    expect
    failureMessage(IntArray2dEqualitySpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  [[1, 2], [1, 5]]
      || false
      |[[1, 2], [2, 5]]
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ArrayComparisonRendering#object two-dimensional array value")
  fun `object two-dimensional array value`() {
    expect
    failureMessage(ObjectArray2dEqualitySpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  [[one, two], [one, five]]
      || false
      |[[one, two], [two, five]]
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ArrayComparisonRendering#multidimensional array value with higher cardinality")
  fun `multidimensional array value with higher cardinality`() {
    expect
    failureMessage(IntArray3dEqualitySpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  [[[1, 2], [1, 5]], [[1, 2], [1, 5]]]
      || false
      |[[[1, 2], [2, 5]], [[1, 2], [2, 5]]]
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
