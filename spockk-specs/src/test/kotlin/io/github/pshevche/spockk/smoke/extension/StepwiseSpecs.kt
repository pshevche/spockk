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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseExecutionModeSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseFailingSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseIgnoreSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseResourceLockSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.assertj.core.api.Condition
import org.junit.platform.engine.FilterResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.junit.platform.launcher.PostDiscoveryFilter
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.Event
import org.junit.platform.testkit.engine.EventConditions.displayName
import org.junit.platform.testkit.engine.Events
import spock.lang.Specification

private fun countStepwiseSkipMessages(events: Events): Long =
  events
    .stream()
    .filter { it.payload.isPresent && it.payload.get() == "Skipped due to previous Error (by @Stepwise)" }
    .count()

private fun stepNames(count: Int): Array<Condition<Event>> =
  listOf("step1", "step2", "step3").subList(0, count).map { displayName(it) }.toTypedArray()

private fun executeIncludingOnly(specClass: Class<*>, displayName: String): Events =
  EngineTestKit.engine("spock")
    .selectors(selectClass(specClass))
    .filters(PostDiscoveryFilter { FilterResult.includedIf(it.displayName == displayName) })
    .execute()
    .testEvents()

class StepwiseSpecs : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.StepwiseSpecs#basic usage")
  fun `basic usage`() {
    `when`
    val events = execute(selectClass(StepwiseFailingSpec::class.java))
    val skipMessagesCount = countStepwiseSkipMessages(events)

    then
    events.assertStatistics { it.started(2).succeeded(1).failed(1).skipped(2) }
    skipMessagesCount == 2L
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.StepwiseSpecs#automatically runs excluded methods that lead up to an included method"
  )
  fun `automatically runs excluded methods that lead up to an included method`(step: String, succeeded: Int) {
    `when`
    val events = execute(selectMethod(StepwiseResourceLockSpec::class.java, step))

    then
    verifyAll {
      events.assertStatistics { it.started(succeeded.toLong()).succeeded(succeeded.toLong()).failed(0).skipped(0) }
      events.succeeded().assertEventsMatchExactly(*stepNames(succeeded))
    }

    where
    step    ; succeeded
    "step1" ; 1
    "step2" ; 2
    "step3" ; 3
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.StepwiseSpecs#automatically runs excluded methods that lead up to an included method with post discovery filter"
  )
  fun `automatically runs excluded methods that lead up to an included method with post discovery filter`(step: String, succeeded: Int) {
    `when`
    val events = executeIncludingOnly(StepwiseResourceLockSpec::class.java, step)

    then
    verifyAll {
      events.assertStatistics { it.started(succeeded.toLong()).succeeded(succeeded.toLong()).failed(0).skipped(0) }
      events.succeeded().assertEventsMatchExactly(*stepNames(succeeded))
    }

    where
    step    ; succeeded
    "step1" ; 1
    "step2" ; 2
    "step3" ; 3
  }

  @MigratedFrom("org.spockframework.smoke.extension.StepwiseSpecs#honors method-level @Ignore")
  fun `honors method-level @Ignore`() {
    `when`
    val events = execute(selectClass(StepwiseIgnoreSpec::class.java))

    then
    events.assertStatistics { it.succeeded(2).failed(0).skipped(1) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.StepwiseSpecs#sets childExecutionMode to SAME_THREAD")
  fun `sets childExecutionMode to SAME_THREAD`() {
    `when`
    val events = execute(selectClass(StepwiseExecutionModeSpec::class.java))

    then
    events.assertStatistics { it.succeeded(1).failed(0).skipped(0) }
  }
}
