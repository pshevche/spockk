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
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.CurrentUnequalProviders
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.EmptyListProviderFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.ProviderWithoutValuesFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.datapipes.UnequalProvidersFixture
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.SpockExecutionException
import spock.lang.Specification
import kotlin.reflect.KClass

class DataProviders : Specification() {

  companion object {
    fun failureOf(fixture: KClass<*>): Throwable =
      execute(selectClass(fixture.java)).failed().list().single()
        .getRequiredPayload(TestExecutionResult::class.java)
        .throwable.orElseThrow()

    fun failureOf(xs: List<Int>, ys: List<Int>, zs: List<Int>): Throwable {
      CurrentUnequalProviders.set(xs, ys, zs)
      return failureOf(UnequalProvidersFixture::class)
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#literal")
  fun literal(x: Int) {
    expect
    x == 1

    where
    variable(x).from(1)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#empty list")
  fun `empty list`() {
    `when`
    val failure = failureOf(EmptyListProviderFixture::class)

    then
    failure is SpockExecutionException
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#single-element list")
  fun `single-element list`(x: Int) {
    expect
    x == 1

    where
    variable(x).from(listOf(1))
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#multi-element list")
  fun `multi-element list`(x: Int) {
    expect
    listOf(1, 2, 3).contains(x)

    where
    variable(x).from(listOf(3, 2, 1))
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#data providers with same number of values")
  fun `data providers with same number of values`(x: Int, y: Int) {
    expect
    x == y

    where
    variable(x).from(listOf(1, 2, 3))
    variable(y).from(listOf(1, 2, 3))
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#data providers with different number of values")
  fun `data providers with different number of values`(xs: List<Int>, ys: List<Int>, zs: List<Int>) {
    `when`
    val failure = failureOf(xs, ys, zs)

    then
    failure is SpockExecutionException

    where
    variables(xs, ys, zs).from(
      listOf(listOf(1, 2), listOf(1), listOf(1), listOf(1, 2)),
      listOf(listOf(1), listOf(1, 2), listOf(1, 2), listOf(1)),
      listOf(listOf(1, 2), listOf(1, 2), listOf(1), listOf(1, 2))
    )
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#data providers one of which has no values")
  fun `data providers one of which has no values`() {
    `when`
    val failure = failureOf(ProviderWithoutValuesFixture::class)

    then
    failure is SpockExecutionException
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#computed")
  fun computed(a: Int, b: Int) {
    expect
    a == b

    where
    variable(a).from(
      run {
        val x = 0
        val y = 1
        listOf(x, y)
      }
    )
    variable(b).from(listOf(0, 1))
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#estimate iterations correctly")
  fun `estimate iterations correctly`(a: Int, b: Int) {
    expect
    specificationContext.currentIteration.estimatedNumIterations == 3

    where
    variable(a).from(listOf(1, 2, 3))
    variable(b).from((1..3).toList())
  }
}
