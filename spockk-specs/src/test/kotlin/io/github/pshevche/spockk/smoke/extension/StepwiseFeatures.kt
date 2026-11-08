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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RollupIterationCounter
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseBasicFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseExecutionModeFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseNotDataDrivenFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseRollupFixture
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Specification

class StepwiseFeatures : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.StepwiseFeatures#basic usage")
  fun `basic usage`() {
    `when`
    val events = execute(selectClass(StepwiseBasicFixture::class.java))
    val skipMessages = events.skipped().list()
      .count { it.getPayload(String::class.java).orElse(null) == "skipping subsequent iterations after failure" }

    then
    events.assertStatistics { it.started(3 + 1).succeeded(2 + 1).failed(1).skipped(2) }
    skipMessages == 2
  }

  @MigratedFrom("org.spockframework.smoke.extension.StepwiseFeatures#basic usage rolled-up")
  fun `basic usage rolled-up`() {
    RollupIterationCounter.count.set(0)

    `when`
    val events = execute(selectClass(StepwiseRollupFixture::class.java))

    then
    RollupIterationCounter.count.get() == 3
    events.assertStatistics { it.started(1).succeeded(0).failed(1).skipped(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.StepwiseFeatures#annotated feature must be data-driven")
  fun `annotated feature must be data-driven`() {
    `when`
    val failures = EngineTestKit.engine("spock")
      .selectors(selectClass(StepwiseNotDataDrivenFixture::class.java))
      .execute()
      .containerEvents()
      .failed()
      .list()
      .map { it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElse(null) }

    then
    failures.single() is InvalidSpecException
  }

  @MigratedFrom("org.spockframework.smoke.extension.StepwiseFeatures#feature annotation sets executionMode to SAME_THREAD")
  fun `feature annotation sets executionMode to SAME_THREAD`() {
    `when`
    val events = execute(selectClass(StepwiseExecutionModeFixture::class.java))

    then
    events.assertStatistics { it.started(2 + 1).succeeded(2 + 1).failed(0).skipped(0) }
  }
}
