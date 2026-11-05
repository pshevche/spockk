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

package io.github.pshevche.spockk.smoke.parameterization

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.ParameterizationFixtures
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Issue
import spock.lang.Specification

class Parameterizations : Specification() {

  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#multi-parameterization")
  fun `multi-parameterization`(a: Int, b: Int) {
    expect
    a == b

    where
    variables(a, b).from(listOf(1, 2, 3), listOf(1, 2, 3))
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#multi-derived-parameterization")
  fun `multi-derived-parameterization`(aAndB: List<Int>, a: Int, b: Int) {
    expect
    a == b

    where
    variable(aAndB).from(listOf(listOf(1, 1), listOf(2, 2), listOf(3, 3)))
    variable(a).from(aAndB[0])
    variable(b).from(aAndB[1])
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#derived parameterization")
  fun `derived parameterization`(a: String, b: String) {
    expect
    a == b.uppercase()

    where
    variable(a).from("A", "B", "C")
    variable(b).from(a.lowercase())
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#dependent derived parameterizations")
  fun `dependent derived parameterizations`(a: Int, b: Int, c: Int, d: Int) {
    expect
    d == a

    where
    variable(a).from(1, 2, 3)
    variable(b).from(a)
    variable(c).from(b * 2)
    variable(d).from(c / 2)
  }

  @Issue("https://github.com/spockframework/spock/issues/271")
  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#can use data variables named p0, p1, etc.")
  fun `can use data variables named p0, p1, etc`(p0: Int, p1: Int) {
    expect
    p1 == p0 * 2

    where
    variables(p0, p1).from(listOf(1, 2, 3), listOf(2, 4, 6))
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#can use data variables named like special @Unroll variables")
  fun `can use data variables named like special @Unroll variables`(featureName: Int, iterationIndex: Int) {
    expect
    iterationIndex == featureName * 2

    where
    variable(featureName).from(1, 2, 3)
    variable(iterationIndex).from(2, 4, 6)
  }

  @Issue("https://github.com/spockframework/spock/issues/396")
  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#can call closures contained in data variables with method syntax")
  fun `can call closures contained in data variables with method syntax`(
    a: () -> Int,
    b: (Int, Int) -> Int
  ) {
    expect
    a() == 1
    b(1, 2) == 3
    b(0, 0) - 0 == 0
    "123".length == 3

    where
    variable(a).from({ 1 })
    variable(b).from({ i: Int, j: Int -> i + j })
  }

  @Issue("https://github.com/spockframework/spock/issues/880")
  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#variables in helper methods do not interfere with omitted data variables")
  fun `variables in helper methods do not interfere with omitted data variables`(a: String, b: String, result: Int) {
    expect
    a.compareTo(b).coerceIn(-1, 1) == result

    where
    variables(a, b, result).from(listOf("abcdef12"), listOf("abcdef12"), listOf(0))
  }

  private fun someOtherMethod(): DoubleArray {
    val result = DoubleArray(0)
    return result
  }

  @Issue("https://github.com/spockframework/spock/issues/880")
  @MigratedFrom("org.spockframework.smoke.parameterization.Parameterizations#variables in helper methods do not interfere with typed data variables")
  fun `variables in helper methods do not interfere with typed data variables`(x: Int) {
    expect
    1 == x

    where
    variable(x).from(1)
  }

  private fun unused(): Int {
    val x = "4"
    return x.length + 2
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.Parameterizations#data provider throwing exception should not produce additional NullPointerException"
  )
  fun `data provider throwing exception should not produce additional NullPointerException`() {
    `when`
    val events = execute(selectClass(ParameterizationFixtures.ThrowingProviderFixture::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is RuntimeException
    throwable.message == "foo"
  }
}
