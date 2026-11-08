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
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.DifferentClassesDiffSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IntegerEqualsNullSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IsolatedClazz
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.IsolatingClassLoader
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MyBean2DiffSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MyBeanDiffSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MySubBean2DiffSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.MySubBeanDiffSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SameNamedClassesDiffSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.Events
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.PendingFeature
import spock.lang.Specification

class DiffedObjectRendering : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#can handle null values")
  fun `can handle null values`() {
    `when`
    val events = execute(selectClass(IntegerEqualsNullSpec::class.java))

    then
    val failure = comparisonFailure(events)
    failure.actual.stringRepresentation == "1\n"
    failure.expected.stringRepresentation == "null"
  }

  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#it should render a bean properly")
  fun `it should render a bean properly`() {
    `when`
    val events = execute(selectClass(MyBeanDiffSpec::class.java))

    then
    val failure = comparisonFailure(events)
    failure.actual.stringRepresentation == "integer: 1\nstring: fun\n"
    failure.expected.stringRepresentation == "integer: 2\nstring: fun2\n"
  }

  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#it should render a bean sub-class properly")
  fun `it should render a bean sub-class properly`() {
    `when`
    val events = execute(selectClass(MySubBeanDiffSpec::class.java))

    then
    val failure = comparisonFailure(events)
    failure.actual.stringRepresentation == "integer: 1\nstring: fun\n"
    failure.expected.stringRepresentation == "integer: 2\nstring: fun2\n"
  }

  @MigratedFrom(
    "org.spockframework.smoke.condition.DiffedObjectRendering#it should render a bean with an interface properly"
  )
  fun `it should render a bean with an interface properly`() {
    `when`
    val events = execute(selectClass(MyBean2DiffSpec::class.java))

    then
    val failure = comparisonFailure(events)
    failure.actual.stringRepresentation == "long1: 1\nstring: fun\n"
    failure.expected.stringRepresentation == "long1: 2\nstring: fun2\n"
  }

  @MigratedFrom(
    "org.spockframework.smoke.condition.DiffedObjectRendering#it should render a bean sub-class with an interface properly"
  )
  fun `it should render a bean sub-class with an interface properly`() {
    `when`
    val events = execute(selectClass(MySubBean2DiffSpec::class.java))

    then
    val failure = comparisonFailure(events)
    failure.actual.stringRepresentation == "long1: 1\nstring: fun\n"
    failure.expected.stringRepresentation == "long1: 2\nstring: fun2\n"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/843")
  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#render class properly")
  fun `render class properly`() {
    `when`
    val events = execute(selectClass(DifferentClassesDiffSpec::class.java))

    then
    val failure = comparisonFailure(events)
    failure.actual.stringRepresentation == "class java.net.SocketTimeoutException [Bootstrap Class Loader]\n"
    failure.expected.stringRepresentation == "class java.lang.ClassNotFoundException [Bootstrap Class Loader]\n"
  }

  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#render class with class loader")
  fun `render class with class loader`() {
    `when`
    val events = execute(selectClass(SameNamedClassesDiffSpec::class.java))

    then
    val failure = comparisonFailure(events)
    val actual = failure.actual.stringRepresentation
    val expected = failure.expected.stringRepresentation
    val loaderAddress = Regex("(?<=@)[0-9a-fA-F]+")
    val rendering = "class ${IsolatedClazz::class.java.name} [${IsolatingClassLoader::class.java.name}@X]\n"
    actual != expected
    actual.replaceFirst(loaderAddress, "X") == rendering
    expected.replaceFirst(loaderAddress, "X") == rendering
  }

  private fun comparisonFailure(events: Events): SpockComparisonFailure =
    events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow() as SpockComparisonFailure
}
