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
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.RolledEmptyProviderFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.RollupClassFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.RollupMethodFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.RollupMethodOverUnrollClassFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.RollupSimpleFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.RollupWholeClassFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrollAndRollupOnFeatureFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrollAndRollupOnSpecFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.UnrolledEmptyProviderFixture
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.Events
import org.spockframework.runtime.ExtensionClassesLoader
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.RunContext
import org.spockframework.runtime.SpockExecutionException
import org.spockframework.runtime.extension.builtin.UnrollConfiguration
import org.spockframework.util.IThrowableFunction
import spock.lang.Specification

class UprolledFeatureMethods : Specification() {

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UprolledFeatureMethods#iterations of an uprolled feature do not count as separate tests"
  )
  fun `iterations of an uprolled feature do not count as separate tests`() {
    `when`
    val events = execute(selectClass(RollupSimpleFixture::class.java))

    then
    events.assertStatistics { it.started(1).aborted(0).succeeded(1).failed(0).skipped(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UprolledFeatureMethods#a feature with an empty data provider causes the same error regardless if it's unrolled or not"
  )
  fun `a feature with an empty data provider causes the same error regardless if it's unrolled or not`(
    fixture: Class<*>
  ) {
    `when`
    val failure = execute(selectClass(fixture)).failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()

    then
    failure is SpockExecutionException

    where
    variable(fixture).from(UnrolledEmptyProviderFixture::class.java, RolledEmptyProviderFixture::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.UprolledFeatureMethods#can roll up a whole class at once")
  fun `can roll up a whole class at once`() {
    `when`
    val events = execute(selectClass(RollupWholeClassFixture::class.java))

    then
    displayNames(events) == listOf("#actorName", "not data-driven", "#actorAge")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UprolledFeatureMethods#method-level uproll annotation wins over class-level unroll annotation"
  )
  fun `method-level uproll annotation wins over class-level unroll annotation`() {
    `when`
    val events = execute(selectClass(RollupMethodOverUnrollClassFixture::class.java))

    then
    displayNames(events) == listOf("method")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UprolledFeatureMethods#unroll and uproll cannot be combined on the same spec"
  )
  fun `unroll and uproll cannot be combined on the same spec`() {
    expect
    containerFailure(UnrollAndRollupOnSpecFixture::class.java) is InvalidSpecException
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UprolledFeatureMethods#unroll and uproll cannot be combined on the same feature"
  )
  fun `unroll and uproll cannot be combined on the same feature`() {
    expect
    containerFailure(UnrollAndRollupOnFeatureFixture::class.java) is InvalidSpecException
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UprolledFeatureMethods#method-level uproll annotation wins over configuration script"
  )
  fun `method-level uproll annotation wins over configuration script`() {
    `when`
    val events = withUnrollConfig(unrollByDefault = true) { execute(selectClass(RollupMethodFixture::class.java)) }

    then
    displayNames(events) == listOf("method")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.UprolledFeatureMethods#class-level uproll annotation wins over configuration script"
  )
  fun `class-level uproll annotation wins over configuration script`() {
    `when`
    val events = withUnrollConfig(unrollByDefault = true) { execute(selectClass(RollupClassFixture::class.java)) }

    then
    displayNames(events) == listOf("method")
  }

  private fun displayNames(events: Events): List<String> =
    events.started().list().map { it.testDescriptor.displayName }

  private fun containerFailure(fixture: Class<*>): Throwable? =
    EngineTestKit.engine("spock").selectors(selectClass(fixture)).execute().containerEvents().failed().list()
      .single().getRequiredPayload(TestExecutionResult::class.java).throwable.orElse(null)

  private fun <T> withUnrollConfig(
    unrollByDefault: Boolean? = null,
    defaultPattern: String? = null,
    action: () -> T
  ): T = RunContext.withNewContext<T, RuntimeException>(
    "UprolledFeatureMethods",
    RunContext.get().spockUserHome,
    null,
    ExtensionClassesLoader().loadExtensionClassesFromDefaultLocation(),
    (ExtensionClassesLoader().loadConfigClassesFromDefaultLocation() + UnrollConfiguration::class.java).distinct(),
    false,
    IThrowableFunction {
      val config = RunContext.get().getConfiguration(UnrollConfiguration::class.java)
      unrollByDefault?.let { config.unrollByDefault = it }
      defaultPattern?.let { config.defaultPattern = it }
      action()
    }
  )
}
