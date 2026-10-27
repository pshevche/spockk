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
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class DiffedObjectRendering : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#can handle null values")
  fun `can handle null values`() {
    val failure = comparisonFailure(IntegerEqualsNullSpec::class)

    expect
    failure.actual.stringRepresentation == "1\n"
    failure.expected.stringRepresentation == "null"
  }

  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#it should render a bean properly")
  fun `it should render a bean properly`() {
    val failure = comparisonFailure(MyBeanDiffSpec::class)

    expect
    failure.actual.stringRepresentation == "integer: 1\nstring: fun\n"
    failure.expected.stringRepresentation == "integer: 2\nstring: fun2\n"
  }

  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#it should render a bean sub-class properly")
  fun `it should render a bean sub-class properly`() {
    val failure = comparisonFailure(MySubBeanDiffSpec::class)

    expect
    failure.actual.stringRepresentation == "integer: 1\nstring: fun\n"
    failure.expected.stringRepresentation == "integer: 2\nstring: fun2\n"
  }

  @MigratedFrom(
    "org.spockframework.smoke.condition.DiffedObjectRendering#it should render a bean with an interface properly"
  )
  fun `it should render a bean with an interface properly`() {
    val failure = comparisonFailure(MyBean2DiffSpec::class)

    expect
    failure.actual.stringRepresentation == "long1: 1\nstring: fun\n"
    failure.expected.stringRepresentation == "long1: 2\nstring: fun2\n"
  }

  @MigratedFrom(
    "org.spockframework.smoke.condition.DiffedObjectRendering#it should render a bean sub-class with an interface properly"
  )
  fun `it should render a bean sub-class with an interface properly`() {
    val failure = comparisonFailure(MySubBean2DiffSpec::class)

    expect
    failure.actual.stringRepresentation == "long1: 1\nstring: fun\n"
    failure.expected.stringRepresentation == "long1: 2\nstring: fun2\n"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/843")
  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#render class properly")
  fun `render class properly`() {
    val failure = comparisonFailure(DifferentClassesDiffSpec::class)

    expect
    failure.actual.stringRepresentation == "class java.net.SocketTimeoutException [Bootstrap Class Loader]\n"
    failure.expected.stringRepresentation == "class java.lang.ClassNotFoundException [Bootstrap Class Loader]\n"
  }

  @MigratedFrom("org.spockframework.smoke.condition.DiffedObjectRendering#render class with class loader")
  fun `render class with class loader`() {
    val failure = comparisonFailure(SameNamedClassesDiffSpec::class)
    val actual = failure.actual.stringRepresentation
    val expected = failure.expected.stringRepresentation
    val loaderAddress = Regex("(?<=@)[0-9a-fA-F]+")
    val rendering = "class ${IsolatedClazz::class.java.name} [${IsolatingClassLoader::class.java.name}@X]\n"

    expect
    actual != expected
    actual.replaceFirst(loaderAddress, "X") == rendering
    expected.replaceFirst(loaderAddress, "X") == rendering
  }

  private fun comparisonFailure(clazz: KClass<*>): SpockComparisonFailure {
    val events = execute(selectClass(clazz.java))
    return events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow() as SpockComparisonFailure
  }
}
