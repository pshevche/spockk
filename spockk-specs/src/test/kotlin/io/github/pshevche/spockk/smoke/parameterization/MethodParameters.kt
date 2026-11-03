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
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.AdditionalArgumentsDisplayNameSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.AdditionalArgumentsRequiresSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.Bar
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.Foo
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.MoreParametersThanDataVariablesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnprovidedFeatureParametersSpec
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.Events
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class MethodParameters : Specification() {

  private fun unprovidedMessage(clazz: KClass<*>): String = failureOf(execute(selectClass(clazz.java)))

  private fun failureOf(events: Events): String =
    events.failed().list().first().getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow().message ?: ""

  @MigratedFrom("org.spockframework.smoke.parameterization.MethodParameters#typed parameters")
  fun `typed parameters`(x: Int, y: Int) {
    expect
    x == y

    where
    variable(x).from(1, 2)
    variable(y).from(1, 2)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.MethodParameters#typed primitive parameters")
  fun `typed primitive parameters`(x: Int, y: Int) {
    expect
    x == y

    where
    variable(x).from(1, 2)
    variable(y).from(1, 2)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.MethodParameters#untyped parameters")
  fun `untyped parameters`(x: Any?, y: Any?) {
    expect
    x == y

    where
    variable(x).from(1, 2)
    variable(y).from(1, 2)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.MethodParameters#partly typed parameters")
  fun `partly typed parameters`(x: Any?, y: Int) {
    expect
    x == y

    where
    variable(x).from(1, 2)
    variable(y).from(1, 2)
  }

  @Issue("https://github.com/spockframework/spock/issues/652")
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.MethodParameters#more parameters than data variables throw exception if not injected by some extension"
  )
  fun `more parameters than data variables throw exception if not injected by some extension`() {
    given
    val message = unprovidedMessage(MoreParametersThanDataVariablesSpec::class)

    expect
    message == "No argument was provided for parameters: 'z' in method: '${MoreParametersThanDataVariablesSpec::class.java.name}.foo'"
  }

  @Issue("https://github.com/spockframework/spock/issues/651")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1052")
  @MigratedFrom("org.spockframework.smoke.parameterization.MethodParameters#data values are injected by name, not order")
  fun `data values are injected by name, not order`(y: Int, x: Int) {
    expect
    2 * x == y

    where
    variable(x).from(1, 2)
    variable(y).from(2, 4)
  }

  @Issue("https://github.com/spockframework/spock/issues/652")
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.MethodParameters#method parameters that are eventually provided by extensions throw an exception at runtime if not set"
  )
  fun `method parameters that are eventually provided by extensions throw an exception at runtime if not set`() {
    given
    val message = unprovidedMessage(UnprovidedFeatureParametersSpec::class)

    expect
    message == "No argument was provided for parameters: 'x', 'y' in method: '${UnprovidedFeatureParametersSpec::class.java.name}.foo'"
  }

  @Foo
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.MethodParameters#method arguments in data driven feature methods can be provided by extensions"
  )
  fun `method arguments in data driven feature methods can be provided by extensions`(x: Int, y: Int, a: Any?) {
    expect
    x == 1
    y == 2
    a == "foo"

    where
    variable(x).from(1)
    variable(y).from(2)
  }

  @Bar
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.MethodParameters#method arguments in data driven feature methods can be provided by extensions by setting the whole arguments array"
  )
  fun `method arguments in data driven feature methods can be provided by extensions by setting the whole arguments array`(
    x: Int,
    y: Int,
    a: Any?
  ) {
    expect
    x == 1
    y == 2
    a == "bar"

    where
    variable(x).from(1)
    variable(y).from(2)
  }

  @Foo
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.MethodParameters#additional method arguments do not influence data values in iteration info"
  )
  fun `additional method arguments do not influence data values in iteration info`(x: Int, y: Int, a: Any?) {
    expect
    specificationContext.currentIteration.dataValues.toList() == listOf(1, 2)

    where
    variable(x).from(1)
    variable(y).from(2)
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.MethodParameters#data variables and iteration index are rendered properly with additional arguments"
  )
  fun `data variables and iteration index are rendered properly with additional arguments`() {
    given
    val events = execute(selectClass(AdditionalArgumentsDisplayNameSpec::class.java))
    val displayNames = events.succeeded().list().map { it.testDescriptor.displayName }

    expect
    events.failed().list().isEmpty()
    displayNames == listOf("a feature [x: 1, y: 3, #0]", "a feature [x: 2, y: 4, #1]", "a feature")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.MethodParameters#conditional annotations that check data values work properly with additional arguments"
  )
  fun `conditional annotations that check data values work properly with additional arguments`() {
    given
    val events = execute(selectClass(AdditionalArgumentsRequiresSpec::class.java))

    expect
    events.failed().list().isEmpty()
    events.succeeded().list().isNotEmpty()
  }
}
