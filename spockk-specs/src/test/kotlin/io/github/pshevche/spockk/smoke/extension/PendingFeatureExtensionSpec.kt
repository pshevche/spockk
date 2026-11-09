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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureAborted
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureAbortedAndFailingIterations
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureAbortedAndPassingIterations
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureAllIterationsAborted
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureAllIterationsFail
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureAllIterationsPass
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureFailing
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureOneIterationFails
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeaturePassing
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureThrowing
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureUnhandledException
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureUnhandledInIterations
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureUnhandledInSomeIterations
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureUnhandledWithAbortedIterations
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureUnhandledWithPassingIterations
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingFeatureWithReason
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.junit.platform.testkit.engine.Events
import org.opentest4j.MultipleFailuresError
import spock.lang.Specification

private const val NOT_YET = "Feature not yet implemented correctly."
private const val PASSES = "Feature is marked with @PendingFeature but passes unexpectedly"

private fun Events.throwables(): List<Throwable> =
  stream().map { it.getPayload(TestExecutionResult::class.java).get().throwable.get() }.toList()

private fun Events.abortedReasons(): List<String?> = aborted().throwables().map { it.message }

private fun Events.abortedCauseMessages(): List<String?> =
  (aborted().throwables().single().cause as MultipleFailuresError).failures.map { it.message }

private fun Events.failureMessages(): List<String?> = failed().throwables().map { it.message }

private fun Events.failureClasses(): List<Class<out Throwable>> =
  failed().throwables().map { it.javaClass }

private fun Events.multipleFailureClasses(): List<Class<out Throwable>> =
  (failed().throwables().single() as MultipleFailuresError).failures.map { it.javaClass }

class PendingFeatureExtensionSpec : Specification() {
  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks failing feature as skipped")
  fun `@PendingFeature marks failing feature as skipped`() {
    `when`
    val events = execute(selectClass(PendingFeatureFailing::class.java))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf(NOT_YET)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature includes reason in exception message")
  fun `@PendingFeature includes reason in exception message`() {
    `when`
    val events = execute(selectClass(PendingFeatureWithReason::class.java))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf("$NOT_YET Reason: 42")
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks feature that fails with exception as skipped")
  fun `@PendingFeature marks feature that fails with exception as skipped`() {
    `when`
    val events = execute(selectClass(PendingFeatureThrowing::class.java))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf(NOT_YET)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature rethrows non handled exceptions")
  fun `@PendingFeature rethrows non handled exceptions`() {
    `when`
    val events = execute(selectClass(PendingFeatureUnhandledException::class.java))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureClasses() == listOf(IllegalArgumentException::class.java)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks passing feature as failed")
  fun `@PendingFeature marks passing feature as failed`() {
    `when`
    val events = execute(selectClass(PendingFeaturePassing::class.java))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureMessages() == listOf(PASSES)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature ignores aborted feature")
  fun `@PendingFeature ignores aborted feature`() {
    `when`
    val events = execute(selectClass(PendingFeatureAborted::class.java))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf("ignored because I can")
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks uprolled data driven feature where every iteration fails as skipped")
  fun `@PendingFeature marks uprolled data driven feature where every iteration fails as skipped`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAllIterationsFail::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf(NOT_YET)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks unrolled data driven feature iterations that fail as skipped")
  fun `@PendingFeature marks unrolled data driven feature iterations that fail as skipped`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAllIterationsFail::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 1L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 3L
      abortedReasons() == listOf(NOT_YET, NOT_YET, NOT_YET)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks uprolled data driven feature where one iteration fails others are successful as skipped")
  fun `@PendingFeature marks uprolled data driven feature where one iteration fails others are successful as skipped`() {
    `when`
    val events = execute(selectMethod(PendingFeatureOneIterationFails::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf(NOT_YET)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks unrolled data driven feature where one iteration fails others are successful as skipped")
  fun `@PendingFeature marks unrolled data driven feature where one iteration fails others are successful as skipped`() {
    `when`
    val events = execute(selectMethod(PendingFeatureOneIterationFails::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 3L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf(NOT_YET)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks uprolled data driven feature where all iterations pass as failed")
  fun `@PendingFeature marks uprolled data driven feature where all iterations pass as failed`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAllIterationsPass::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureMessages() == listOf(PASSES)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks unrolled data driven feature where all iterations pass as failed")
  fun `@PendingFeature marks unrolled data driven feature where all iterations pass as failed`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAllIterationsPass::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 3L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureMessages() == listOf(PASSES)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature rethrows non handled exceptions in uprolled data driven feature")
  fun `@PendingFeature rethrows non handled exceptions in uprolled data driven feature`() {
    `when`
    val events = execute(selectMethod(PendingFeatureUnhandledInIterations::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureClasses() == listOf(MultipleFailuresError::class.java)
      multipleFailureClasses() == listOf(IllegalArgumentException::class.java, IllegalArgumentException::class.java, IllegalArgumentException::class.java)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature")
  fun `@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature`() {
    `when`
    val events = execute(selectMethod(PendingFeatureUnhandledInIterations::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 1L
      failed().count() == 3L
      skipped().count() == 0L
      aborted().count() == 0L
      failureClasses() == listOf(IllegalArgumentException::class.java, IllegalArgumentException::class.java, IllegalArgumentException::class.java)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are failing")
  fun `@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are failing`() {
    `when`
    val events = execute(selectMethod(PendingFeatureUnhandledInSomeIterations::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureClasses() == listOf(IllegalArgumentException::class.java)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are failing")
  fun `@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are failing`() {
    `when`
    val events = execute(selectMethod(PendingFeatureUnhandledInSomeIterations::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 1L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 2L
      abortedReasons() == listOf(NOT_YET, NOT_YET)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are passing")
  fun `@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are passing`() {
    `when`
    val events = execute(selectMethod(PendingFeatureUnhandledWithPassingIterations::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureClasses() == listOf(MultipleFailuresError::class.java)
      multipleFailureClasses() == listOf(IllegalArgumentException::class.java, IllegalArgumentException::class.java)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are passing")
  fun `@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are passing`() {
    `when`
    val events = execute(selectMethod(PendingFeatureUnhandledWithPassingIterations::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 2L
      failed().count() == 2L
      skipped().count() == 0L
      aborted().count() == 0L
      failureClasses() == listOf(IllegalArgumentException::class.java, IllegalArgumentException::class.java)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature ignores uprolled data driven feature where all iterations are aborted")
  fun `@PendingFeature ignores uprolled data driven feature where all iterations are aborted`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAllIterationsAborted::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf("All iterations were aborted")
      abortedCauseMessages() == listOf("ignored because I can", "ignored because I can", "ignored because I can")
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature ignores unrolled data driven feature where all iterations are aborted")
  fun `@PendingFeature ignores unrolled data driven feature where all iterations are aborted`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAllIterationsAborted::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 1L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 3L
      abortedReasons() == listOf("ignored because I can", "ignored because I can", "ignored because I can")
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks uprolled data driven feature where some iterations are aborted and some pass as failed")
  fun `@PendingFeature marks uprolled data driven feature where some iterations are aborted and some pass as failed`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAbortedAndPassingIterations::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureMessages() == listOf(PASSES)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks unrolled data driven feature where some iterations are aborted and some pass as failed")
  fun `@PendingFeature marks unrolled data driven feature where some iterations are aborted and some pass as failed`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAbortedAndPassingIterations::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 2L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf("ignored because I can")
      failureMessages() == listOf(PASSES)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature marks uprolled data driven feature where some iterations are aborted and some fail as skipped")
  fun `@PendingFeature marks uprolled data driven feature where some iterations are aborted and some fail as skipped`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAbortedAndFailingIterations::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 1L
      abortedReasons() == listOf(NOT_YET)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature does not mark unrolled data driven feature where some iterations are aborted and some fail as skipped")
  fun `@PendingFeature does not mark unrolled data driven feature where some iterations are aborted and some fail as skipped`() {
    `when`
    val events = execute(selectMethod(PendingFeatureAbortedAndFailingIterations::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 1L
      failed().count() == 0L
      skipped().count() == 0L
      aborted().count() == 3L
      abortedReasons() == listOf("ignored because I can", NOT_YET, "ignored because I can")
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are aborted")
  fun `@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are aborted`() {
    `when`
    val events = execute(selectMethod(PendingFeatureUnhandledWithAbortedIterations::class.java, "rolled"))

    then
    verifyAll(events) {
      started().count() == 1L
      succeeded().count() == 0L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 0L
      failureClasses() == listOf(IllegalArgumentException::class.java)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.PendingFeatureExtensionSpec#@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are aborted")
  fun `@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are aborted`() {
    `when`
    val events = execute(selectMethod(PendingFeatureUnhandledWithAbortedIterations::class.java, "unrolled"))

    then
    verifyAll(events) {
      started().count() == 4L
      succeeded().count() == 1L
      failed().count() == 1L
      skipped().count() == 0L
      aborted().count() == 2L
      abortedReasons() == listOf("ignored because I can", "ignored because I can")
      failureClasses() == listOf(IllegalArgumentException::class.java)
    }
  }
}
