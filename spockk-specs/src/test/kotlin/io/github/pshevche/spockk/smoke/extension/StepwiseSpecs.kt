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
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.FilterResult
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.junit.platform.launcher.PostDiscoveryFilter
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.testkit.engine.Events
import spock.lang.Specification

private class RecordingListener : TestExecutionListener {
  val succeededNames = mutableListOf<String>()
  val notSucceeded = mutableListOf<TestIdentifier>()
  val skipped = mutableListOf<TestIdentifier>()

  override fun executionFinished(id: TestIdentifier, result: TestExecutionResult) {
    if (!id.isTest) return
    if (result.status == TestExecutionResult.Status.SUCCESSFUL) {
      succeededNames += id.displayName
    } else {
      notSucceeded += id
    }
  }

  override fun executionSkipped(id: TestIdentifier, reason: String) {
    skipped += id
  }
}

private fun countStepwiseSkipMessages(events: Events): Long =
  events
    .stream()
    .filter { it.payload.isPresent && it.payload.get() == "Skipped due to previous Error (by @Stepwise)" }
    .count()

private fun succeededDisplayNames(events: Events): List<String> =
  events.succeeded().stream().map { it.testDescriptor.displayName }.toList()

private fun runIncludingOnly(specClass: Class<*>, displayName: String): RecordingListener {
  val listener = RecordingListener()
  val request =
    LauncherDiscoveryRequestBuilder.request()
      .selectors(selectClass(specClass))
      .filters(PostDiscoveryFilter { FilterResult.includedIf(it.displayName == displayName) })
      .build()
  LauncherFactory.create().execute(request, listener)
  return listener
}

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
    events.assertStatistics { it.started(succeeded.toLong()).succeeded(succeeded.toLong()).failed(0).skipped(0) }
    succeededDisplayNames(events) ==
      listOf("step1", "step2", "step3").subList(0, succeeded)

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
    val listener = runIncludingOnly(StepwiseResourceLockSpec::class.java, step)

    then
    listener.succeededNames == listOf("step1", "step2", "step3").subList(0, succeeded)
    listener.notSucceeded.isEmpty()
    listener.skipped.isEmpty()

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
