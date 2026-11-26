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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.FailingConditionFalse
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.FailingConditionTrue
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.FailingConditionTrueTwice
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.FailingDataConditionFalse
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.FailingDataConditionTrue
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.FailingDataConditionTrueTwice
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InstanceFieldAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingConditionFalse
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingConditionTrue
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingConditionTrueTwice
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingDataConditionFalse
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingDataConditionPendingFeatureAfter
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingDataConditionPendingFeatureFirst
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingDataConditionTrue
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingDataConditionTrueTwice
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingParameterizedPendingFeatureAfter
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingParameterizedPendingFeatureFirst
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingPendingFeatureAfter
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PassingPendingFeatureFirst
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SharedFieldAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StaticFieldAccessSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.Events
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.PendingFeature
import spock.lang.Specification

class PendingFeatureIfExtensionSpec : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks failing feature as skipped if the condition passes and the test fails")
  fun `@PendingFeatureIf marks failing feature as skipped if the condition passes and the test fails`() {
    `when`
    val events = execute(selectClass(FailingConditionTrue::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(0).failed(0).aborted(1).skipped(0) }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks failing feature as skipped if the condition passes and the test fails even if applied twice")
  fun `@PendingFeatureIf marks failing feature as skipped if the condition passes and the test fails even if applied twice`() {
    `when`
    val events = execute(selectClass(FailingConditionTrueTwice::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(0).failed(0).aborted(1).skipped(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as failed if the conditional expression returns true")
  fun `@PendingFeatureIf marks passing feature as failed if the conditional expression returns true`() {
    `when`
    val events = execute(selectClass(PassingConditionTrue::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as failed if the conditional expression returns true even if applied twice")
  fun `@PendingFeatureIf marks passing feature as failed if the conditional expression returns true even if applied twice`() {
    `when`
    val events = execute(selectClass(PassingConditionTrueTwice::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as failed if the conditional expression returns true even if @PendingFeature is applied first")
  fun `@PendingFeatureIf marks passing feature as failed if the conditional expression returns true even if @PendingFeature is applied first`() {
    `when`
    val events = execute(selectClass(PassingPendingFeatureFirst::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing parameterized feature as failed if the conditional expression returns true even if @PendingFeature is applied first")
  fun `@PendingFeatureIf marks passing parameterized feature as failed if the conditional expression returns true even if @PendingFeature is applied first`() {
    `when`
    val events = execute(selectClass(PassingParameterizedPendingFeatureFirst::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as failed if the data variable accessing conditional expression returns true even if @PendingFeature is applied first")
  fun `@PendingFeatureIf marks passing feature as failed if the data variable accessing conditional expression returns true even if @PendingFeature is applied first`() {
    `when`
    val events = execute(selectClass(PassingDataConditionPendingFeatureFirst::class.java))

    then
    events.assertStatistics { it.failed(2) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as failed if the conditional expression returns true even if @PendingFeature is applied after it")
  fun `@PendingFeatureIf marks passing feature as failed if the conditional expression returns true even if @PendingFeature is applied after it`() {
    `when`
    val events = execute(selectClass(PassingPendingFeatureAfter::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeature but passes unexpectedly"
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing parameterized feature as failed if the conditional expression returns true even if @PendingFeature is applied after it")
  fun `@PendingFeatureIf marks passing parameterized feature as failed if the conditional expression returns true even if @PendingFeature is applied after it`() {
    `when`
    val events = execute(selectClass(PassingParameterizedPendingFeatureAfter::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeature but passes unexpectedly"
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as failed if the data variable accessing conditional expression returns true even if @PendingFeature is applied after it")
  fun `@PendingFeatureIf marks passing feature as failed if the data variable accessing conditional expression returns true even if @PendingFeature is applied after it`() {
    `when`
    val events = execute(selectClass(PassingDataConditionPendingFeatureAfter::class.java))

    then
    events.assertStatistics { it.failed(2) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks failing feature as failed if the conditional expression returns false")
  fun `@PendingFeatureIf marks failing feature as failed if the conditional expression returns false`() {
    `when`
    val events = execute(selectClass(FailingConditionFalse::class.java))

    then
    events.assertStatistics { it.failed(1) }
    conditionFailureOf(events).condition.values[0] == false
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as passed if the conditional expression returns false")
  fun `@PendingFeatureIf marks passing feature as passed if the conditional expression returns false`() {
    `when`
    val events = execute(selectClass(PassingConditionFalse::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1).failed(0).aborted(0).skipped(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks failing feature as skipped if the data variable accessing condition passes and the test fails")
  fun `@PendingFeatureIf marks failing feature as skipped if the data variable accessing condition passes and the test fails`() {
    `when`
    val events = execute(selectClass(FailingDataConditionTrue::class.java))

    then
    events.assertStatistics { it.started(2).succeeded(1).failed(0).aborted(1).skipped(0) }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks failing feature as skipped if the data variable accessing condition passes and the test fails even if applied twice")
  fun `@PendingFeatureIf marks failing feature as skipped if the data variable accessing condition passes and the test fails even if applied twice`() {
    `when`
    val events = execute(selectClass(FailingDataConditionTrueTwice::class.java))

    then
    events.assertStatistics { it.started(2).succeeded(1).failed(0).aborted(1).skipped(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as failed if the data variable accessing conditional expression returns true")
  fun `@PendingFeatureIf marks passing feature as failed if the data variable accessing conditional expression returns true`() {
    `when`
    val events = execute(selectClass(PassingDataConditionTrue::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as failed if the data variable accessing conditional expression returns true even if applied twice")
  fun `@PendingFeatureIf marks passing feature as failed if the data variable accessing conditional expression returns true even if applied twice`() {
    `when`
    val events = execute(selectClass(PassingDataConditionTrueTwice::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is AssertionError
    failureOf(events).message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks failing feature as failed if the data variable accessing conditional expression returns false")
  fun `@PendingFeatureIf marks failing feature as failed if the data variable accessing conditional expression returns false`() {
    `when`
    val events = execute(selectClass(FailingDataConditionFalse::class.java))

    then
    events.assertStatistics { it.failed(1) }
    failureOf(events) is SpockComparisonFailure
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf marks passing feature as passed if the data variable accessing conditional expression returns false")
  fun `@PendingFeatureIf marks passing feature as passed if the data variable accessing conditional expression returns false`() {
    `when`
    val events = execute(selectClass(PassingDataConditionFalse::class.java))

    then
    events.assertStatistics { it.started(2).succeeded(2).failed(0).aborted(0).skipped(0) }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/895")
  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf provides condition access to Specification instance shared fields")
  fun `@PendingFeatureIf provides condition access to Specification instance shared fields`() {
    `when`
    val events = execute(selectClass(SharedFieldAccessSpec::class.java))

    then
    events.assertStatistics { it.started(4).succeeded(3).failed(0).aborted(1).skipped(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf provides condition access to Specification instance fields")
  fun `@PendingFeatureIf provides condition access to Specification instance fields`() {
    `when`
    val events = execute(selectClass(InstanceFieldAccessSpec::class.java))

    then
    events.assertStatistics { it.started(4).succeeded(3).failed(0).aborted(1).skipped(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureIfExtensionSpec#@PendingFeatureIf provides condition access to static Specification fields")
  fun `@PendingFeatureIf provides condition access to static Specification fields`() {
    `when`
    val events = execute(selectClass(StaticFieldAccessSpec::class.java))

    then
    events.assertStatistics { it.started(2).succeeded(1).failed(0).aborted(1).skipped(0) }
  }

  private fun failureOf(events: Events): Throwable =
    events.failed().list().first().getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()

  private fun conditionFailureOf(events: Events): ConditionNotSatisfiedError =
    failureOf(events) as ConditionNotSatisfiedError
}
