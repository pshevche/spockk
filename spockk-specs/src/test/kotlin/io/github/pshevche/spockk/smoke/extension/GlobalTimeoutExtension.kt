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

package io.github.pshevche.spockk.smoke.extension

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.ClassTimeoutOverrideSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.FeatureTimeoutOverrideSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingCleanupChildSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingCleanupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingCleanupSpecChildSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingCleanupSpecSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingFeatureChildSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingFeatureSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingSetupChildSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingSetupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingSetupSpecChildSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SleepingSetupSpecSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.SlowFixturesSpec
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineExecutionResults
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.builder.DelegatingScript
import org.spockframework.runtime.RunContext
import org.spockframework.runtime.SpockTimeoutError
import org.spockframework.runtime.extension.builtin.TimeoutConfiguration
import org.spockframework.util.IThrowableFunction
import spock.config.RunnerConfiguration
import spock.lang.Isolated
import spock.lang.Specification
import spock.lang.Timeout
import java.io.File
import java.time.Duration
import org.spockframework.runtime.extension.builtin.GlobalTimeoutExtension as SpockGlobalTimeoutExtension

@Timeout(60)
@Isolated("The timings are quite tight and it can get flaky on weak machines if run in parallel.")
class GlobalTimeoutExtension : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.GlobalTimeoutExtension#applies timeout to features")
  fun `applies timeout to features`() {
    given
    val config = globalTimeoutConfig()

    `when`
    val results = run(config, SleepingFeatureSpec::class.java)

    then
    results.timedOut()
  }

  @MigratedFrom("org.spockframework.smoke.extension.GlobalTimeoutExtension#supports inheritance for features")
  fun `supports inheritance for features`() {
    given
    val config = globalTimeoutConfig()

    `when`
    val results = run(config, SleepingFeatureChildSpec::class.java)

    then
    results.timedOut()
  }

  @MigratedFrom("org.spockframework.smoke.extension.GlobalTimeoutExtension#applies timeout to fixtures")
  fun `applies timeout to fixtures`(fixtureSpec: Class<*>) {
    given
    val config = globalTimeoutConfig(applyToFixtureMethods = true)

    `when`
    val results = run(config, fixtureSpec)

    then
    results.timedOut()

    where
    variable(fixtureSpec).from(
      SleepingSetupSpecSpec::class.java,
      SleepingSetupSpec::class.java,
      SleepingCleanupSpec::class.java,
      SleepingCleanupSpecSpec::class.java
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.GlobalTimeoutExtension#supports inheritance for fixtures")
  fun `supports inheritance for fixtures`(fixtureSpec: Class<*>) {
    given
    val config = globalTimeoutConfig(applyToFixtureMethods = true)

    `when`
    val results = run(config, fixtureSpec)

    then
    results.timedOut()

    where
    variable(fixtureSpec).from(
      SleepingSetupSpecChildSpec::class.java,
      SleepingSetupChildSpec::class.java,
      SleepingCleanupChildSpec::class.java,
      SleepingCleanupSpecChildSpec::class.java
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.GlobalTimeoutExtension#can be overwritten by feature annotation")
  fun `can be overwritten by feature annotation`() {
    given
    val config = globalTimeoutConfig()

    `when`
    val results = run(config, FeatureTimeoutOverrideSpec::class.java)

    then
    results.succeeded()
  }

  @MigratedFrom("org.spockframework.smoke.extension.GlobalTimeoutExtension#can be overwritten by class-level annotation")
  fun `can be overwritten by class-level annotation`() {
    given
    val config = globalTimeoutConfig()

    `when`
    val results = run(config, ClassTimeoutOverrideSpec::class.java)

    then
    results.succeeded()
  }

  @MigratedFrom("org.spockframework.smoke.extension.GlobalTimeoutExtension#can exclude fixture methods from global timeout")
  fun `can exclude fixture methods from global timeout`() {
    given
    val config = globalTimeoutConfig(applyToFixtureMethods = false, timeout = Duration.ofMillis(20))

    `when`
    val results = run(config, SlowFixturesSpec::class.java)

    then
    results.succeeded()
  }

  private fun globalTimeoutConfig(
    applyToFixtureMethods: Boolean = false,
    timeout: Duration = Duration.ofNanos(1)
  ): (TimeoutConfiguration) -> Unit = {
    it.globalTimeout = timeout
    it.printThreadDumpsOnInterruptAttempts = false
    it.applyGlobalTimeoutToFixtures = applyToFixtureMethods
  }

  // The specs classpath carries Gradle's Groovy 4 (via TestKit), which cannot compile a Spock config script.
  private fun run(configure: (TimeoutConfiguration) -> Unit, spec: Class<*>): EngineExecutionResults {
    val script = object : DelegatingScript() {
      override fun run(): Any? {
        configure(getProperty("timeout") as TimeoutConfiguration)
        return null
      }
    }
    return RunContext.withNewContext<EngineExecutionResults, RuntimeException>(
      "global-timeout",
      File("global-timeout"),
      script,
      listOf(SpockGlobalTimeoutExtension::class.java),
      listOf(RunnerConfiguration::class.java, TimeoutConfiguration::class.java),
      false,
      IThrowableFunction {
        EngineTestKit.engine("spock").selectors(selectClass(spec)).execute()
      }
    )
  }

  private fun EngineExecutionResults.timedOut(): Boolean =
    allEvents().failed().stream().anyMatch { event ->
      event.getRequiredPayload(org.junit.platform.engine.TestExecutionResult::class.java)
        .throwable.orElse(null) is SpockTimeoutError
    }

  private fun EngineExecutionResults.succeeded(): Boolean =
    allEvents().failed().count() == 0L && allEvents().aborted().count() == 0L
}
