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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatUntilFailureAbortedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatUntilFailureFirstFailureSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatUntilFailureMaxAttemptsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatUntilFailureSkipsOthersSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class RepeatUntilFailureExtensionSpec : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.RepeatUntilFailureExtensionSpec#repeats feature until the first failure")
  fun `repeats feature until the first failure`() {
    `when`
    val events = execute(selectClass(RepeatUntilFailureFirstFailureSpec::class.java))

    then
    events.assertStatistics { it.started(1 + 3).succeeded(1 + 2).failed(1) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RepeatUntilFailureExtensionSpec#repeats until maxAttempts is reached")
  fun `repeats until maxAttempts is reached`() {
    `when`
    val events = execute(selectClass(RepeatUntilFailureMaxAttemptsSpec::class.java))

    then
    events.assertStatistics { it.started(1 + 3).succeeded(1 + 3).failed(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RepeatUntilFailureExtensionSpec#skips other tests in the same class that do not have the same annotation too"
  )
  fun `skips other tests in the same class that do not have the same annotation too`() {
    `when`
    val events = execute(selectClass(RepeatUntilFailureSkipsOthersSpec::class.java))

    then
    events.assertStatistics { it.started(2 + 2).succeeded(2 + 2).failed(0).skipped(2) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RepeatUntilFailureExtensionSpec#ignores aborted tests")
  fun `ignores aborted tests`() {
    `when`
    val events = execute(selectClass(RepeatUntilFailureAbortedSpec::class.java))

    then
    events.assertStatistics { it.started(1 + 3).succeeded(1 + 2).failed(0).aborted(1) }
  }
}
