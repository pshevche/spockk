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
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.ErrorIfSomeUprolledAreErroringAndSomeSkippedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.ErrorIfSomeUprolledAreErroringAndSomeSuccessfulFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.FailingIfAllUprolledAreErroringFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.FailingIfAllUprolledAreFailingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.FailingIfSomeUprolledAreErroringAndSomeFailingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.FailingIfSomeUprolledAreFailingAndSomeSkippedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.FailingIfSomeUprolledAreFailingAndSomeSuccessfulFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.FailingIfThereIsOneUprolledIterationAndItIsFailingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SkippedIfAllUprolledAreSkippedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SkippedIfThereIsOneUprolledIterationAndItIsSkippedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.StepwiseIterationsTest
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfAllUnrolledAreErroringFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfAllUnrolledAreFailingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfAllUnrolledAreSkippedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfAllUnrolledAreSuccessfulFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfAllUprolledAreSuccessfulFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfSomeUnrolledAreErroringAndSomeFailingFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfSomeUnrolledAreErroringAndSomeSkippedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfSomeUnrolledAreErroringAndSomeSuccessfulFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfSomeUnrolledAreFailingAndSomeSkippedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfSomeUnrolledAreFailingAndSomeSuccessfulFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfSomeUnrolledAreSuccessfulAndSomeSkippedFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.parameterization.SuccessfulIfSomeUprolledAreSuccessfulAndSomeSkippedFixture
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.Events
import org.opentest4j.MultipleFailuresError
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.Specification

class ParameterizedFeatureNodeStatuses : Specification() {

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be skipped if all uprolled iterations are skipped")
  fun `should be skipped if all uprolled iterations are skipped`() {
    `when`
    val result = run(SkippedIfAllUprolledAreSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 1, testsSucceeded = 0, testsFailed = 0, totalStarted = 3, totalSkipped = 0, totalAborted = 1, totalSucceeded = 2, totalFailure = 0)
    result.failureTypes.isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be skipped if there is one uprolled iteration and it is skipped")
  fun `should be skipped if there is one uprolled iteration and it is skipped`() {
    `when`
    val result = run(SkippedIfThereIsOneUprolledIterationAndItIsSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 1, testsSucceeded = 0, testsFailed = 0, totalStarted = 3, totalSkipped = 0, totalAborted = 1, totalSucceeded = 2, totalFailure = 0)
    result.failureTypes.isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some uprolled iterations are successful and some skipped")
  fun `should be successful if some uprolled iterations are successful and some skipped`() {
    `when`
    val result = run(SuccessfulIfSomeUprolledAreSuccessfulAndSomeSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 1, testsFailed = 0, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 3, totalFailure = 0)
    result.failureTypes.isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if some uprolled iterations are failing and some skipped")
  fun `should be failing if some uprolled iterations are failing and some skipped`() {
    `when`
    val result = run(FailingIfSomeUprolledAreFailingAndSomeSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 0, testsFailed = 1, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 2, totalFailure = 1)
    result.failureTypes == listOf(SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if there is one uprolled iteration and it is failing")
  fun `should be failing if there is one uprolled iteration and it is failing`() {
    `when`
    val result = run(FailingIfThereIsOneUprolledIterationAndItIsFailingFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 0, testsFailed = 1, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 2, totalFailure = 1)
    result.failureTypes == listOf(SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be error if some uprolled iterations are erroring and some skipped")
  fun `should be error if some uprolled iterations are erroring and some skipped`() {
    `when`
    val result = run(ErrorIfSomeUprolledAreErroringAndSomeSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 0, testsFailed = 1, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 2, totalFailure = 1)
    result.failureTypes == listOf(Error::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all uprolled iterations are successful")
  fun `should be successful if all uprolled iterations are successful`() {
    `when`
    val result = run(SuccessfulIfAllUprolledAreSuccessfulFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 1, testsFailed = 0, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 3, totalFailure = 0)
    result.failureTypes.isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if some uprolled iterations are failing and some successful")
  fun `should be failing if some uprolled iterations are failing and some successful`() {
    `when`
    val result = run(FailingIfSomeUprolledAreFailingAndSomeSuccessfulFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 0, testsFailed = 1, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 2, totalFailure = 1)
    result.failureTypes == listOf(SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be error if some uprolled iterations are erroring and some successful")
  fun `should be error if some uprolled iterations are erroring and some successful`() {
    `when`
    val result = run(ErrorIfSomeUprolledAreErroringAndSomeSuccessfulFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 0, testsFailed = 1, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 2, totalFailure = 1)
    result.failureTypes == listOf(Error::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if all uprolled iterations are failing")
  fun `should be failing if all uprolled iterations are failing`() {
    `when`
    val result = run(FailingIfAllUprolledAreFailingFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 0, testsFailed = 1, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 2, totalFailure = 1)
    result.failureTypes == listOf(MultipleFailuresError::class.java)
    result.nestedFailureTypes == listOf(SpockComparisonFailure::class.java, SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if some uprolled iterations are erroring and some failing")
  fun `should be failing if some uprolled iterations are erroring and some failing`() {
    `when`
    val result = run(FailingIfSomeUprolledAreErroringAndSomeFailingFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 0, testsFailed = 1, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 2, totalFailure = 1)
    result.failureTypes == listOf(MultipleFailuresError::class.java)
    result.nestedFailureTypes == listOf(Error::class.java, SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if all uprolled iterations are erroring")
  fun `should be failing if all uprolled iterations are erroring`() {
    `when`
    val result = run(FailingIfAllUprolledAreErroringFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 0, containersStarted = 2, containersSkipped = 0, containersAborted = 0, containersSucceeded = 2, containersFailed = 0, testsStarted = 1, testsSkipped = 0, testsAborted = 0, testsSucceeded = 0, testsFailed = 1, totalStarted = 3, totalSkipped = 0, totalAborted = 0, totalSucceeded = 2, totalFailure = 1)
    result.failureTypes == listOf(MultipleFailuresError::class.java)
    result.nestedFailureTypes == listOf(Error::class.java, Error::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all unrolled iterations are skipped")
  fun `should be successful if all unrolled iterations are skipped`() {
    `when`
    val result = run(SuccessfulIfAllUnrolledAreSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 2, testsSucceeded = 1, testsFailed = 0, totalStarted = 5, totalSkipped = 0, totalAborted = 2, totalSucceeded = 3, totalFailure = 0)
    result.failureTypes.isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are successful and some skipped")
  fun `should be successful if some unrolled iterations are successful and some skipped`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreSuccessfulAndSomeSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 1, testsSucceeded = 2, testsFailed = 0, totalStarted = 5, totalSkipped = 0, totalAborted = 1, totalSucceeded = 4, totalFailure = 0)
    result.failureTypes.isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are failing and some skipped")
  fun `should be successful if some unrolled iterations are failing and some skipped`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreFailingAndSomeSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 1, testsSucceeded = 1, testsFailed = 1, totalStarted = 5, totalSkipped = 0, totalAborted = 1, totalSucceeded = 3, totalFailure = 1)
    result.failureTypes == listOf(SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are erroring and some skipped")
  fun `should be successful if some unrolled iterations are erroring and some skipped`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreErroringAndSomeSkippedFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 1, testsSucceeded = 1, testsFailed = 1, totalStarted = 5, totalSkipped = 0, totalAborted = 1, totalSucceeded = 3, totalFailure = 1)
    result.failureTypes == listOf(Error::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all unrolled iterations are successful")
  fun `should be successful if all unrolled iterations are successful`() {
    `when`
    val result = run(SuccessfulIfAllUnrolledAreSuccessfulFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 0, testsSucceeded = 3, testsFailed = 0, totalStarted = 5, totalSkipped = 0, totalAborted = 0, totalSucceeded = 5, totalFailure = 0)
    result.failureTypes.isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are failing and some successful")
  fun `should be successful if some unrolled iterations are failing and some successful`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreFailingAndSomeSuccessfulFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 0, testsSucceeded = 2, testsFailed = 1, totalStarted = 5, totalSkipped = 0, totalAborted = 0, totalSucceeded = 4, totalFailure = 1)
    result.failureTypes == listOf(SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are erroring and some successful")
  fun `should be successful if some unrolled iterations are erroring and some successful`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreErroringAndSomeSuccessfulFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 0, testsSucceeded = 2, testsFailed = 1, totalStarted = 5, totalSkipped = 0, totalAborted = 0, totalSucceeded = 4, totalFailure = 1)
    result.failureTypes == listOf(Error::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all unrolled iterations are failing")
  fun `should be successful if all unrolled iterations are failing`() {
    `when`
    val result = run(SuccessfulIfAllUnrolledAreFailingFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 0, testsSucceeded = 1, testsFailed = 2, totalStarted = 5, totalSkipped = 0, totalAborted = 0, totalSucceeded = 3, totalFailure = 2)
    result.failureTypes == listOf(SpockComparisonFailure::class.java, SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are erroring and some failing")
  fun `should be successful if some unrolled iterations are erroring and some failing`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreErroringAndSomeFailingFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 0, testsSucceeded = 1, testsFailed = 2, totalStarted = 5, totalSkipped = 0, totalAborted = 0, totalSucceeded = 3, totalFailure = 2)
    result.failureTypes == listOf(Error::class.java, SpockComparisonFailure::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all unrolled iterations are erroring")
  fun `should be successful if all unrolled iterations are erroring`() {
    `when`
    val result = run(SuccessfulIfAllUnrolledAreErroringFixture::class.java)

    then
    result.counts == NodeCounts(dynamicallyRegistered = 2, containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0, testsStarted = 3, testsSkipped = 0, testsAborted = 0, testsSucceeded = 1, testsFailed = 2, totalStarted = 5, totalSkipped = 0, totalAborted = 0, totalSucceeded = 3, totalFailure = 2)
    result.failureTypes == listOf(Error::class.java, Error::class.java)
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#exceptions in iteration interceptors should not affect other iterations"
  )
  fun `exceptions in iteration interceptors should not affect other iterations`() {
    `when`
    val result = run(StepwiseIterationsTest::class.java)

    then
    result.counts == NodeCounts(
      dynamicallyRegistered = 5,
      containersStarted = 3, containersSkipped = 0, containersAborted = 0, containersSucceeded = 3, containersFailed = 0,
      testsStarted = 6, testsSkipped = 0, testsAborted = 1, testsSucceeded = 5, testsFailed = 0,
      totalStarted = 8, totalSkipped = 0, totalAborted = 1, totalSucceeded = 7, totalFailure = 0
    )
  }

  private data class NodeCounts(
    val dynamicallyRegistered: Int,
    val containersStarted: Int,
    val containersSkipped: Int,
    val containersAborted: Int,
    val containersSucceeded: Int,
    val containersFailed: Int,
    val testsStarted: Int,
    val testsSkipped: Int,
    val testsAborted: Int,
    val testsSucceeded: Int,
    val testsFailed: Int,
    val totalStarted: Int,
    val totalSkipped: Int,
    val totalAborted: Int,
    val totalSucceeded: Int,
    val totalFailure: Int
  )

  private class RunResult(val counts: NodeCounts, val failureTypes: List<Class<out Throwable>>, val nestedFailureTypes: List<Class<out Throwable>>)

  private fun run(fixture: Class<*>): RunResult {
    val results = EngineTestKit.engine("spock").selectors(selectClass(fixture)).execute()
    val all = results.allEvents()
    val containers = results.containerEvents()
    val tests = results.testEvents()
    val failures = all.failed().list()
      .mapNotNull { it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElse(null) }
    val nested = failures.filterIsInstance<MultipleFailuresError>().flatMap { it.failures }
    return RunResult(
      NodeCounts(
        dynamicallyRegistered = all.dynamicallyRegistered().count().toInt(),
        containersStarted = containers.started().count().toInt(),
        containersSkipped = containers.skipped().count().toInt(),
        containersAborted = containers.aborted().count().toInt(),
        containersSucceeded = containers.succeeded().count().toInt(),
        containersFailed = containers.failed().count().toInt(),
        testsStarted = tests.started().count().toInt(),
        testsSkipped = tests.skipped().count().toInt(),
        testsAborted = tests.aborted().count().toInt(),
        testsSucceeded = tests.succeeded().count().toInt(),
        testsFailed = tests.failed().count().toInt(),
        totalStarted = all.started().count().toInt(),
        totalSkipped = all.skipped().count().toInt(),
        totalAborted = all.aborted().count().toInt(),
        totalSucceeded = all.succeeded().count().toInt(),
        totalFailure = all.failed().count().toInt()
      ),
      failures.map { it.javaClass },
      nested.map { it.javaClass }
    )
  }
}
