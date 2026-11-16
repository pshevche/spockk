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

import groovy.lang.GroovyShell
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryAllIterationsFailingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryChangeThreadDataDrivenFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryChangeThreadFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryConditionFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryCountFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryDataDrivenAllFailingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryDataDrivenAllPassingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryDataDrivenOneFailingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryDelayFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryExhaustedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryIgnoresPreviousFailuresFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryInheritedFromBaseFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryInstanceFieldFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryInterceptorChainFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryIoExceptionFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryIterationModeDataDrivenFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryIterationModeFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryOnClassFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryOnSubclassFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryOverridingSubclassFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryRemainingIterationsFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryReportedIterationsFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetrySetupFeatureCleanupModeDataDrivenFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetrySetupFeatureCleanupModeFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetrySharedFieldFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryStaticFieldFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryTracker
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryUnexpectedExceptionFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryUnhandledExceptionFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryUnhandledExceptionIterationModeFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryUnhandledExceptionSetupFeatureCleanupModeFixture
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EventConditions.event
import org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully
import org.junit.platform.testkit.engine.EventConditions.finishedWithFailure
import org.junit.platform.testkit.engine.EventConditions.test
import org.junit.platform.testkit.engine.Events
import org.opentest4j.MultipleFailuresError
import org.spockframework.builder.DelegatingScript
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.RunContext
import org.spockframework.runtime.SpockMultipleFailuresError
import org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD
import org.spockframework.util.IThrowableFunction
import spock.lang.Execution
import spock.lang.PendingFeature
import spock.lang.Specification
import java.io.IOException

@Execution(value = SAME_THREAD, reason = "tests use static fields")
class RetryFeatureExtensionSpec : Specification() {

  fun setup() {
    RetryTracker.reset()
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry fails after retries are exhausted")
  fun `@Retry fails after retries are exhausted`() {
    `when`
    val events = execute(selectClass(RetryExhaustedFixture::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(0).failed(1).skipped(0) }
    val failure = events.failures().single() as MultipleFailuresError
    failure.failures.size == 4
    failure.failures.allAre(ConditionNotSatisfiedError::class.java)
    RetryTracker.featureCounter.get() == 4
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry works for normal exceptions")
  fun `@Retry works for normal exceptions`() {
    `when`
    val events = execute(selectClass(RetryIoExceptionFixture::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(0).failed(1).skipped(0) }
    val failure = events.failures().single() as MultipleFailuresError
    failure.failures.size == 4
    failure.failures.allAre(IOException::class.java)
    RetryTracker.featureCounter.get() == 4
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry mode #mode executes setup and cleanup #expectedCount times (parallel: #parallel)"
  )
  fun `@Retry mode #mode executes setup and cleanup #expectedCount times (parallel - #parallel)`(
    mode: String,
    expectedCount: Int,
    parallel: Boolean
  ) {
    `when`
    val events = executeWithParallelism(fixtureFor(mode, unrolled = false), parallel)

    then
    events.assertStatistics { it.started(1).succeeded(0).failed(1).skipped(0) }
    val failure = events.failures().single() as MultipleFailuresError
    failure.failures.size == 4
    failure.failures.allAre(IOException::class.java)
    RetryTracker.setupCounter.get() == expectedCount
    RetryTracker.cleanupCounter.get() == expectedCount
    RetryTracker.featureCounter.get() == 4

    where
    variables(mode, expectedCount, parallel).from(
      listOf(
        "ITERATION",
        "SETUP_FEATURE_CLEANUP",
        "ITERATION",
        "SETUP_FEATURE_CLEANUP"
      ),
      listOf(1, 4, 1, 4),
      listOf(false, false, true, true)
    )
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry mode SETUP_FEATURE_CLEANUP is safe against offloading iteration execution to a different thread"
  )
  fun `@Retry mode SETUP_FEATURE_CLEANUP is safe against offloading iteration execution to a different thread`() {
    `when`
    val events = execute(selectClass(RetryChangeThreadFixture::class.java))

    then
    val failure = events.failures().single() as MultipleFailuresError
    failure.failures.size == 4
    failure.failures.allAre(ConditionNotSatisfiedError::class.java)
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry mode SETUP_FEATURE_CLEANUP is safe against offloading iteration execution to a different thread in data driven feature"
  )
  fun `@Retry mode SETUP_FEATURE_CLEANUP is safe against offloading iteration execution to a different thread in data driven feature`() {
    `when`
    val events = execute(selectClass(RetryChangeThreadDataDrivenFixture::class.java))

    then
    val failures = events.failures()
    val multipleFailures = failures.map { it as SpockMultipleFailuresError }
    val nested = multipleFailures.flatMap { it.failures }
    failures.size == 2
    failures.allAre(SpockMultipleFailuresError::class.java)
    multipleFailures.allHaveFourFailures()
    nested.allAre(ConditionNotSatisfiedError::class.java)
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry mode #mode executes setup and cleanup #expectedCount times for @Unroll'ed feature (parallel: #parallel)"
  )
  fun `@Retry mode #mode executes setup and cleanup #expectedCount times for @Unroll'ed feature (parallel - #parallel)`(
    mode: String,
    expectedCount: Int,
    parallel: Boolean
  ) {
    `when`
    val events = executeWithParallelism(fixtureFor(mode, unrolled = true), parallel)

    then
    events.assertStatistics { it.started(4).succeeded(1).failed(3).skipped(0) }
    val failure = events.failures().first() as MultipleFailuresError
    failure.failures.size == 4
    failure.failures.allAre(IOException::class.java)
    RetryTracker.setupCounter.get() == expectedCount
    RetryTracker.cleanupCounter.get() == expectedCount
    RetryTracker.featureCounter.get() == 12

    where
    variables(mode, expectedCount, parallel).from(
      listOf(
        "ITERATION",
        "SETUP_FEATURE_CLEANUP",
        "ITERATION",
        "SETUP_FEATURE_CLEANUP"
      ),
      listOf(3, 12, 3, 12),
      listOf(false, false, true, true)
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry count can be changed")
  fun `@Retry count can be changed`() {
    `when`
    val events = execute(selectClass(RetryCountFixture::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(0).failed(1).skipped(0) }
    RetryTracker.featureCounter.get() == 6
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry rethrows non handled exceptions")
  fun `@Retry rethrows non handled exceptions`() {
    `when`
    val events = execute(selectClass(RetryUnhandledExceptionFixture::class.java))

    then
    events.failures().single() is IllegalArgumentException
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry rethrows non handled exceptions for data driven features"
  )
  fun `@Retry rethrows non handled exceptions for data driven features`(mode: String) {
    `when`
    val events = execute(selectClass(unhandledExceptionFixtureFor(mode)))

    then
    val failures = events.failures()
    failures.isNotEmpty()
    failures.allAre(IllegalArgumentException::class.java)

    where
    variable(mode).from("ITERATION", "SETUP_FEATURE_CLEANUP")
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry works for data driven features")
  fun `@Retry works for data driven features`() {
    `when`
    val events = execute(selectClass(RetryDataDrivenAllFailingFixture::class.java))

    then
    events.assertStatistics { it.succeeded(1).failed(2).skipped(0) }
    RetryTracker.featureCounter.get() == 8
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry for @Unroll'ed data driven feature")
  fun `@Retry for @Unroll'ed data driven feature`() {
    `when`
    val events = execute(selectClass(RetryDataDrivenOneFailingFixture::class.java))

    then
    events.assertStatistics { it.succeeded(3).failed(1).skipped(0) }
    RetryTracker.featureCounter.get() == 4 + 2
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry doesn't affect data driven feature where all iterations pass"
  )
  fun `@Retry doesn't affect data driven feature where all iterations pass`() {
    `when`
    val events = execute(selectClass(RetryDataDrivenAllPassingFixture::class.java))

    then
    events.assertStatistics { it.succeeded(4).failed(0).skipped(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry doesn't affect @Unroll'ed data driven feature where all iterations pass"
  )
  fun `@Retry doesn't affect @Unroll'ed data driven feature where all iterations pass`() {
    `when`
    val events = execute(selectClass(RetryDataDrivenAllPassingFixture::class.java))

    then
    events.assertStatistics { it.succeeded(4).failed(0).skipped(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry mode SETUP_FEATURE_CLEANUP ignores previous failures if a retry succeeds"
  )
  fun `@Retry mode SETUP_FEATURE_CLEANUP ignores previous failures if a retry succeeds`() {
    `when`
    val events = execute(selectClass(RetryIgnoresPreviousFailuresFixture::class.java))

    then
    events.assertStatistics { it.succeeded(1).failed(0).skipped(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry can add delay between iteration executions")
  fun `@Retry can add delay between iteration executions`() {
    `when`
    val start = System.currentTimeMillis()
    val events = execute(selectClass(RetryDelayFixture::class.java))

    then
    System.currentTimeMillis() - start > 300
    events.assertStatistics { it.started(4).succeeded(3).failed(1).skipped(0) }
    RetryTracker.featureCounter.get() == 4 + 2
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/867")
  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry evaluates condition")
  fun `@Retry evaluates condition`() {
    `when`
    val events = execute(selectClass(RetryConditionFixture::class.java))

    then
    events.assertStatistics { it.started(4).succeeded(1).failed(3) }
    RetryTracker.featureCounter.get() == 4 + 1 + 1
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry does not evaluate condition if exception type is unexpected"
  )
  fun `@Retry does not evaluate condition if exception type is unexpected`() {
    `when`
    val events = execute(selectClass(RetryUnexpectedExceptionFixture::class.java))

    then
    events.assertStatistics { it.started(7).succeeded(1).failed(6) }
    RetryTracker.featureCounter.get() == (4 + 1) + (1 + 1) + (1 + 1)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/933")
  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry provides condition access to Specification instance shared fields"
  )
  fun `@Retry provides condition access to Specification instance shared fields`() {
    `when`
    val events = execute(selectClass(RetrySharedFieldFixture::class.java))

    then
    events.assertStatistics { it.started(4).succeeded(1).failed(3) }
    RetryTracker.featureCounter.get() == 1 + 4 + 1
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry provides condition access to Specification instance fields"
  )
  fun `@Retry provides condition access to Specification instance fields`() {
    `when`
    val events = execute(selectClass(RetryInstanceFieldFixture::class.java))

    then
    events.assertStatistics { it.started(4).succeeded(1).failed(3) }
    RetryTracker.featureCounter.get() == 1 + 4 + 1
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry provides condition access to static Specification fields"
  )
  fun `@Retry provides condition access to static Specification fields`() {
    `when`
    val events = execute(selectClass(RetryStaticFieldFixture::class.java))

    then
    events.assertStatistics { it.started(4).succeeded(1).failed(3) }
    RetryTracker.featureCounter.get() == 1 + 4 + 1
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry can be declared on a spec class")
  fun `@Retry can be declared on a spec class`() {
    `when`
    val events = execute(selectClass(RetryOnClassFixture::class.java))

    then
    events.assertStatistics { it.started(3).succeeded(1).failed(2) }
    RetryTracker.featureCounter.get() == 4 + 1 + 2
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry declared on a spec class is inherited")
  fun `@Retry declared on a spec class is inherited`() {
    `when`
    val events = execute(selectClass(RetryInheritedFromBaseFixture::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(0).failed(1) }
    RetryTracker.featureCounter.get() == 2
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry declared on a subclass is applied to all features"
  )
  fun `@Retry declared on a subclass is applied to all features`() {
    `when`
    val events = execute(selectClass(RetryOnSubclassFixture::class.java))

    then
    events.assertStatistics { it.started(2).succeeded(0).failed(2) }
    RetryTracker.featureCounter.get() == 2 + 2
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry declared on a spec class can be overridden"
  )
  fun `@Retry declared on a spec class can be overridden`() {
    `when`
    val events = execute(selectClass(RetryOverridingSubclassFixture::class.java))

    then
    events.assertStatistics { it.started(2).succeeded(0).failed(2) }
    RetryTracker.featureCounter.get() == 3 + 3
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry mode SETUP_FEATURE_CLEANUP runs remaining iterations after a failed one for @Unroll'ed features"
  )
  fun `@Retry mode SETUP_FEATURE_CLEANUP runs remaining iterations after a failed one for @Unroll'ed features`() {
    `when`
    val events = execute(selectClass(RetryRemainingIterationsFixture::class.java))

    then
    events.assertStatistics { it.succeeded(4).failed(1).skipped(0) }
    RetryTracker.iterationBuffer.toString() == "1222234"
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry mode SETUP_FEATURE_CLEANUP runs all iterations for @Unroll'ed features"
  )
  fun `@Retry mode SETUP_FEATURE_CLEANUP runs all iterations for @Unroll'ed features`() {
    `when`
    val events = execute(selectClass(RetryAllIterationsFailingFixture::class.java))

    then
    events.assertStatistics { it.succeeded(1).failed(3).skipped(0) }
    RetryTracker.iterationBuffer.toString() == "111122223333"
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry mode SETUP_FEATURE_CLEANUP correctly reports failed iterations for @Unroll'ed features"
  )
  fun `@Retry mode SETUP_FEATURE_CLEANUP correctly reports failed iterations for @Unroll'ed features`() {
    `when`
    val events = execute(selectClass(RetryReportedIterationsFixture::class.java))

    then
    events.assertStatistics { it.succeeded(2).failed(1).skipped(0) }
    events.finished().assertEventsMatchLoosely(
      event(test("iteration:0"), finishedSuccessfully()),
      event(test("iteration:1"), finishedWithFailure())
    )
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RetryFeatureExtensionSpec#@Retry interceptor chains to enclosed interceptors each time"
  )
  fun `@Retry interceptor chains to enclosed interceptors each time`() {
    `when`
    val events = execute(selectClass(RetryInterceptorChainFixture::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(0).failed(1).skipped(0) }
    val failure = events.failures().single() as MultipleFailuresError
    failure.failures.size == 4
    failure.failures.allAre(ConditionNotSatisfiedError::class.java)
    RetryTracker.extensionCounter.get() == 4
  }

  private fun fixtureFor(mode: String, unrolled: Boolean): Class<*> = when {
    mode == "ITERATION" && unrolled -> RetryIterationModeDataDrivenFixture::class.java
    mode == "ITERATION" -> RetryIterationModeFixture::class.java
    unrolled -> RetrySetupFeatureCleanupModeDataDrivenFixture::class.java
    else -> RetrySetupFeatureCleanupModeFixture::class.java
  }

  private fun unhandledExceptionFixtureFor(mode: String): Class<*> = when (mode) {
    "ITERATION" -> RetryUnhandledExceptionIterationModeFixture::class.java
    else -> RetryUnhandledExceptionSetupFeatureCleanupModeFixture::class.java
  }

  // Lambdas inside a condition crash the compiler plugin, so these keep them out of the conditions:
  // https://github.com/pshevche/spockk/issues/819
  private fun List<Throwable>.allAre(type: Class<*>) = all { type.isInstance(it) }

  private fun List<MultipleFailuresError>.allHaveFourFailures() = all { it.failures.size == 4 }

  private fun List<MultipleFailuresError>.allHaveFourIoFailures() =
    all { it.failures.size == 4 && it.failures.allAre(IOException::class.java) }

  private fun Events.failures(): List<Throwable> =
    failed().stream().map { it.getRequiredPayload(TestExecutionResult::class.java).throwable.get() }.toList()

  private fun executeWithParallelism(fixture: Class<*>, parallel: Boolean): Events {
    val compilerConfiguration = CompilerConfiguration().apply {
      scriptBaseClass = DelegatingScript::class.java.name
      disabledGlobalASTTransformations = setOf("org.spockframework.compiler.SpockTransform")
    }
    val script = GroovyShell(compilerConfiguration)
      .parse("runner { parallel { enabled $parallel; fixed(4) } }") as DelegatingScript
    return RunContext.withNewContext<Events, RuntimeException>(
      "parallel-$parallel",
      RunContext.get().spockUserHome,
      script,
      emptyList(),
      emptyList(),
      true,
      IThrowableFunction { execute(selectClass(fixture)) }
    )
  }
}
