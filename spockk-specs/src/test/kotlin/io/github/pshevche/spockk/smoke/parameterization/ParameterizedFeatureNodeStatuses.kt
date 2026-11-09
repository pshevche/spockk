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
import io.github.pshevche.spockk.lang.verifyAll
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
    verifyAll(result) {
      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 2
      totalFailureCount == 0
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be skipped if there is one uprolled iteration and it is skipped")
  fun `should be skipped if there is one uprolled iteration and it is skipped`() {
    `when`
    val result = run(SkippedIfThereIsOneUprolledIterationAndItIsSkippedFixture::class.java)

    then
    verifyAll(result) {
      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 2
      totalFailureCount == 0
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some uprolled iterations are successful and some skipped")
  fun `should be successful if some uprolled iterations are successful and some skipped`() {
    `when`
    val result = run(SuccessfulIfSomeUprolledAreSuccessfulAndSomeSkippedFixture::class.java)

    then
    verifyAll(result) {
      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 0

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 0
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if some uprolled iterations are failing and some skipped")
  fun `should be failing if some uprolled iterations are failing and some skipped`() {
    `when`
    val result = run(FailingIfSomeUprolledAreFailingAndSomeSkippedFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if there is one uprolled iteration and it is failing")
  fun `should be failing if there is one uprolled iteration and it is failing`() {
    `when`
    val result = run(FailingIfThereIsOneUprolledIterationAndItIsFailingFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be error if some uprolled iterations are erroring and some skipped")
  fun `should be error if some uprolled iterations are erroring and some skipped`() {
    `when`
    val result = run(ErrorIfSomeUprolledAreErroringAndSomeSkippedFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(Error::class.java)

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all uprolled iterations are successful")
  fun `should be successful if all uprolled iterations are successful`() {
    `when`
    val result = run(SuccessfulIfAllUprolledAreSuccessfulFixture::class.java)

    then
    verifyAll(result) {
      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 0

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 0
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if some uprolled iterations are failing and some successful")
  fun `should be failing if some uprolled iterations are failing and some successful`() {
    `when`
    val result = run(FailingIfSomeUprolledAreFailingAndSomeSuccessfulFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be error if some uprolled iterations are erroring and some successful")
  fun `should be error if some uprolled iterations are erroring and some successful`() {
    `when`
    val result = run(ErrorIfSomeUprolledAreErroringAndSomeSuccessfulFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(Error::class.java)

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if all uprolled iterations are failing")
  fun `should be failing if all uprolled iterations are failing`() {
    `when`
    val result = run(FailingIfAllUprolledAreFailingFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(MultipleFailuresError::class.java)
      nestedFailureTypes == listOf(SpockComparisonFailure::class.java, SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if some uprolled iterations are erroring and some failing")
  fun `should be failing if some uprolled iterations are erroring and some failing`() {
    `when`
    val result = run(FailingIfSomeUprolledAreErroringAndSomeFailingFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(MultipleFailuresError::class.java)
      nestedFailureTypes == listOf(Error::class.java, SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be failing if all uprolled iterations are erroring")
  fun `should be failing if all uprolled iterations are erroring`() {
    `when`
    val result = run(FailingIfAllUprolledAreErroringFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(MultipleFailuresError::class.java)
      nestedFailureTypes == listOf(Error::class.java, Error::class.java)

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all unrolled iterations are skipped")
  fun `should be successful if all unrolled iterations are skipped`() {
    `when`
    val result = run(SuccessfulIfAllUnrolledAreSkippedFixture::class.java)

    then
    verifyAll(result) {
      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 2
      testsSucceededCount == 1
      testsFailedCount == 0

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 2
      totalSucceededCount == 3
      totalFailureCount == 0
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are successful and some skipped")
  fun `should be successful if some unrolled iterations are successful and some skipped`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreSuccessfulAndSomeSkippedFixture::class.java)

    then
    verifyAll(result) {
      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 2
      testsFailedCount == 0

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 4
      totalFailureCount == 0
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are failing and some skipped")
  fun `should be successful if some unrolled iterations are failing and some skipped`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreFailingAndSomeSkippedFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 1
      testsFailedCount == 1

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 3
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are erroring and some skipped")
  fun `should be successful if some unrolled iterations are erroring and some skipped`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreErroringAndSomeSkippedFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(Error::class.java)

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 1
      testsFailedCount == 1

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 3
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all unrolled iterations are successful")
  fun `should be successful if all unrolled iterations are successful`() {
    `when`
    val result = run(SuccessfulIfAllUnrolledAreSuccessfulFixture::class.java)

    then
    verifyAll(result) {
      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 3
      testsFailedCount == 0

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 5
      totalFailureCount == 0
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are failing and some successful")
  fun `should be successful if some unrolled iterations are failing and some successful`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreFailingAndSomeSuccessfulFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 2
      testsFailedCount == 1

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 4
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are erroring and some successful")
  fun `should be successful if some unrolled iterations are erroring and some successful`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreErroringAndSomeSuccessfulFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(Error::class.java)

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 2
      testsFailedCount == 1

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 4
      totalFailureCount == 1
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all unrolled iterations are failing")
  fun `should be successful if all unrolled iterations are failing`() {
    `when`
    val result = run(SuccessfulIfAllUnrolledAreFailingFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(SpockComparisonFailure::class.java, SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 2

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 2
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if some unrolled iterations are erroring and some failing")
  fun `should be successful if some unrolled iterations are erroring and some failing`() {
    `when`
    val result = run(SuccessfulIfSomeUnrolledAreErroringAndSomeFailingFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(Error::class.java, SpockComparisonFailure::class.java)

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 2

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 2
    }
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#should be successful if all unrolled iterations are erroring")
  fun `should be successful if all unrolled iterations are erroring`() {
    `when`
    val result = run(SuccessfulIfAllUnrolledAreErroringFixture::class.java)

    then
    verifyAll(result) {
      failureTypes == listOf(Error::class.java, Error::class.java)

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 2

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 2
    }
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.ParameterizedFeatureNodeStatuses#exceptions in iteration interceptors should not affect other iterations"
  )
  fun `exceptions in iteration interceptors should not affect other iterations`() {
    `when`
    val result = run(StepwiseIterationsTest::class.java)

    then
    verifyAll(result) {
      dynamicallyRegisteredCount == 5

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 6
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 5
      testsFailedCount == 0

      totalStartedCount == 8
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 7
      totalFailureCount == 0
    }
  }

  private class RunResult(
    val dynamicallyRegisteredCount: Int,
    val containersStartedCount: Int,
    val containersSkippedCount: Int,
    val containersAbortedCount: Int,
    val containersSucceededCount: Int,
    val containersFailedCount: Int,
    val testsStartedCount: Int,
    val testsSkippedCount: Int,
    val testsAbortedCount: Int,
    val testsSucceededCount: Int,
    val testsFailedCount: Int,
    val totalStartedCount: Int,
    val totalSkippedCount: Int,
    val totalAbortedCount: Int,
    val totalSucceededCount: Int,
    val totalFailureCount: Int,
    val failures: List<Throwable>
  ) {
    val failureTypes: List<Class<out Throwable>> = failures.map { it.javaClass }
    val nestedFailureTypes: List<Class<out Throwable>> =
      failures.filterIsInstance<MultipleFailuresError>().flatMap { it.failures }.map { it.javaClass }
  }

  private fun run(fixture: Class<*>): RunResult {
    val results = EngineTestKit.engine("spock").selectors(selectClass(fixture)).execute()
    val all = results.allEvents()
    val containers = results.containerEvents()
    val tests = results.testEvents()
    val failures = all.failed().list()
      .mapNotNull { it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElse(null) }
    return RunResult(
      dynamicallyRegisteredCount = all.dynamicallyRegistered().count().toInt(),
      containersStartedCount = containers.started().count().toInt(),
      containersSkippedCount = containers.skipped().count().toInt(),
      containersAbortedCount = containers.aborted().count().toInt(),
      containersSucceededCount = containers.succeeded().count().toInt(),
      containersFailedCount = containers.failed().count().toInt(),
      testsStartedCount = tests.started().count().toInt(),
      testsSkippedCount = tests.skipped().count().toInt(),
      testsAbortedCount = tests.aborted().count().toInt(),
      testsSucceededCount = tests.succeeded().count().toInt(),
      testsFailedCount = tests.failed().count().toInt(),
      totalStartedCount = all.started().count().toInt(),
      totalSkippedCount = all.skipped().count().toInt(),
      totalAbortedCount = all.aborted().count().toInt(),
      totalSucceededCount = all.succeeded().count().toInt(),
      totalFailureCount = all.failed().count().toInt(),
      failures = failures
    )
  }
}
