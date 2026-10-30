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
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.DifferentRepresentationsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.EqualsMethodNoTypeHintsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NestedEqualityEqualValuesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NestedEqualityTypeHintsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullValuesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameLiteralRepresentationsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameRenderedAndLiteralRepresentationsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameRepresentationsDifferentAnonymousTypesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameRepresentationsDifferentArrayTypesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameRepresentationsSameTypesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.TypeHintValueSpec
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class EqualityComparisonRendering : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#values with different representations")
  fun `values with different representations`() {
    expect
    failureMessage(DifferentRepresentationsSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      |1 |  2
      |  false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#values with same representations and same types")
  fun `values with same representations and same types`() {
    expect
    failureMessage(SameRepresentationsSameTypesSpec::class).replace(Regex("(?<=@)[0-9a-f]+"), "X") ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  Fred (io.github.pshevche.spockk.fixtures.runtime.samples.condition.EqualityPerson@X)
      || false
      |Fred (io.github.pshevche.spockk.fixtures.runtime.samples.condition.EqualityPerson@X)
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#values with same representations and different types")
  fun `values with same representations and different types`() {
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

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#values with same representations and different array types")
  fun `values with same representations and different array types`() {
    expect
    failureMessage(SameRepresentationsDifferentArrayTypesSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  [1] (java.lang.String[])
      || false
      |[1] (int[])
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#values with same representations and different anonymous types")
  fun `values with same representations and different anonymous types`() {
    expect
    failureMessage(SameRepresentationsDifferentAnonymousTypesSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  foo (io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameRepresentationsDifferentAnonymousTypesSpec${'$'}${'$'}spock_feature_0_0${'$'}y${'$'}1)
      || false
      |foo (io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameRepresentationsDifferentAnonymousTypesSpec${'$'}${'$'}spock_feature_0_0${'$'}x${'$'}1)
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#values with same rendered and literal representations")
  fun `values with same rendered and literal representations`() {
    expect
    failureMessage(SameRenderedAndLiteralRepresentationsSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == 123
      || |  |
      || |  123 (java.lang.Integer)
      || false
      |123 (java.lang.String)
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/845")
  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#values with same literal representations")
  fun `values with same literal representations`() {
    expect
    failureMessage(SameLiteralRepresentationsSpec::class) ==
      """
      |Condition not satisfied:
      |
      |arrayListOf(0, 1) == linkedSetOf(0, 1)
      ||                 |  |
      ||                 |  [0, 1] (java.util.LinkedHashSet)
      ||                 false
      |[0, 1] (java.util.ArrayList)
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#null values")
  fun `null values`() {
    expect
    failureMessage(NullValuesSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x == y
      || |  |
      || |  null (java.lang.String)
      || false
      |null (void)
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/849")
  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#type hints are also added for nested equality comparisons")
  fun `type hints are also added for nested equality comparisons`() {
    expect
    failureMessage(NestedEqualityTypeHintsSpec::class) ==
      """
      |Condition not satisfied:
      |
      |((x == y) as Any) is String
      |  | |  |  |       |  |
      |  | |  |  false   |  class java.lang.String
      |  | |  |          false
      |  | |  1 (java.lang.String)
      |  | false (java.lang.Boolean)
      |  1 (java.lang.Integer)
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/849")
  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#type hints are not added for nested equality comparisons if values are equal")
  fun `type hints are not added for nested equality comparisons if values are equal`() {
    expect
    failureMessage(NestedEqualityEqualValuesSpec::class) ==
      """
      |Condition not satisfied:
      |
      |((x == y) as Any) is String
      |  | |  |  |       |  |
      |  [1]| |  true    |  class java.lang.String
      |    |  [1]        false
      |    true (java.lang.Boolean)
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#type hints are not added when equals method is used (but only when equality operator is used)")
  fun `type hints are not added when equals method is used (but only when equality operator is used)`() {
    expect
    failureMessage(EqualsMethodNoTypeHintsSpec::class) ==
      """
      |Condition not satisfied:
      |
      |x.equals(y)
      || |      |
      |1 false  1
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
