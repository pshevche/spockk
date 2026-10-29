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
import io.github.pshevche.spockk.fixtures.runtime.samples.parallel.InheritedIsolatedSpecs
import io.github.pshevche.spockk.fixtures.runtime.samples.parallel.IsolatedExecutionSpecs
import io.github.pshevche.spockk.fixtures.runtime.samples.parallel.ParallelExecutionSpecs
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.Events
import org.opentest4j.TestAbortedException
import org.spockframework.mock.runtime.MockMakerConfiguration
import org.spockframework.runtime.RunContext
import org.spockframework.util.IThrowableFunction
import spock.config.RunnerConfiguration
import spock.lang.Isolated
import spock.lang.Specification
import java.nio.file.Files

@Isolated("Isolate from other tests to have full access to cores for the nested executions.")
class ParallelSpec : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#tests can execute in parallel")
  fun `tests can execute in parallel`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.TestsExecuteInParallel::class.java)

    then
    events.assertStatistics { it.succeeded(2).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#tests can get exclusive access to resources")
  fun `tests can get exclusive access to resources`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.ExclusiveResourceAccess::class.java)

    then
    events.assertStatistics { it.succeeded(2).failed(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.ParallelSpec#tests can get shared read and exclusive write access to resources"
  )
  fun `tests can get shared read and exclusive write access to resources`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.SharedReadExclusiveWriteAccess::class.java)

    then
    events.assertStatistics { it.succeeded(3).failed(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.ParallelSpec#a write lock on the specification causes same thread execution"
  )
  fun `a write lock on the specification causes same thread execution`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.WriteLockOnSpec::class.java)

    then
    events.assertStatistics { it.succeeded(2).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#@ResourceLock on specs is inherited")
  fun `@ResourceLock on specs is inherited`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.InheritedResourceLock::class.java)

    then
    events.assertStatistics { it.succeeded(2).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#@ResourceLock with only READ allows parallel execution")
  fun `@ResourceLock with only READ allows parallel execution`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.ReadOnlyResourceLock::class.java)

    then
    events.assertStatistics { it.succeeded(2).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#@ResourceLock on Spec can be combined with @ResourceLock")
  fun `@ResourceLock on Spec can be combined with @ResourceLock`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.SpecLockCombinedWithFeatureLock::class.java)

    then
    events.assertStatistics { it.succeeded(3).failed(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.ParallelSpec#@ResourceLock with only READ allows parallel execution of data-driven features"
  )
  fun `@ResourceLock with only READ allows parallel execution of data-driven features`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.ReadOnlyLockOnDataDrivenFeature::class.java)

    then
    events.assertStatistics { it.succeeded(4).failed(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.ParallelSpec#@ResourceLock with only WRITE forces same thread execution of data-driven features"
  )
  fun `@ResourceLock with only WRITE forces same thread execution of data-driven features`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.WriteLockOnDataDrivenFeature::class.java)

    then
    events.assertStatistics { it.succeeded(3).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#Execution can be set to same thread execution")
  fun `Execution can be set to same thread execution`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.SameThreadExecution::class.java)

    then
    events.assertStatistics { it.succeeded(2).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#@Execution on specs is inherited")
  fun `@Execution on specs is inherited`() {
    `when`
    val events = executeInParallel(ParallelExecutionSpecs.InheritedSameThreadExecution::class.java)

    then
    events.assertStatistics { it.succeeded(2).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#Execution can be isolated")
  fun `Execution can be isolated`() {
    `when`
    val events = executeInParallel(
      IsolatedExecutionSpecs.ASpec::class.java,
      IsolatedExecutionSpecs.IsolatedBSpec::class.java,
      IsolatedExecutionSpecs.CSpec::class.java
    )

    then
    events.assertStatistics { it.succeeded(6).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelSpec#@Isolated is inherited")
  fun `@Isolated is inherited`() {
    `when`
    val events = executeInParallel(
      InheritedIsolatedSpecs.ASpec::class.java,
      InheritedIsolatedSpecs.BSpec::class.java,
      InheritedIsolatedSpecs.CSpec::class.java
    )

    then
    events.assertStatistics { it.succeeded(6).failed(0) }
  }

  private fun executeInParallel(vararg specs: Class<*>): Events {
    if (Runtime.getRuntime().availableProcessors() < 2) {
      throw TestAbortedException("parallel execution needs at least 2 cores")
    }
    val selectors: List<DiscoverySelector> = specs.map { selectClass(it) }
    return RunContext.withNewContext<Events, RuntimeException>(
      "parallel",
      Files.createTempDirectory("spockk-parallel").toFile(),
      null,
      emptyList(),
      listOf(RunnerConfiguration::class.java, MockMakerConfiguration::class.java),
      false,
      IThrowableFunction { context ->
        val runner = context.getConfiguration(RunnerConfiguration::class.java)
        runner.optimizeRunOrder = false
        runner.parallel.enabled = true
        runner.parallel.fixed(4)
        EngineTestKit.engine("spock").selectors(*selectors.toTypedArray()).execute().testEvents().debug()
      }
    )
  }
}
