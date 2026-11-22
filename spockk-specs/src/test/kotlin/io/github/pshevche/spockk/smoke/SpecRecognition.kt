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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.recognition.AbstractIntermediarySpecRecognitionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.recognition.DirectSpecRecognitionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.recognition.FullyQualifiedIntermediarySpecRecognitionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.recognition.FullyQualifiedSpecRecognitionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.recognition.IntermediarySpecRecognitionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.recognition.SameUnitSpecRecognitionSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.Events
import org.spockframework.runtime.SpockAssertionError
import spock.lang.Specification

class SpecRecognition : Specification() {

  @MigratedFrom("org.spockframework.smoke.SpecRecognition#is properly recognized")
  fun `is properly recognized`(spec: Class<*>) {
    `when`
    val events = execute(selectClass(spec))
    val failure = failureOf(events)

    then
    events.assertStatistics { it.started(1).failed(1) }
    failure is SpockAssertionError

    where
    variable(spec).from(
      DirectSpecRecognitionSpec::class.java,
      FullyQualifiedSpecRecognitionSpec::class.java,
      IntermediarySpecRecognitionSpec::class.java,
      FullyQualifiedIntermediarySpecRecognitionSpec::class.java,
      AbstractIntermediarySpecRecognitionSpec::class.java
    )
  }

  @MigratedFrom(
    "org.spockframework.smoke.SpecRecognition#is properly recognized when inheriting from Specification base class through intermediary in same compilation unit"
  )
  fun `is properly recognized when inheriting from Specification base class through intermediary in same compilation unit`() {
    `when`
    val events = execute(selectClass(SameUnitSpecRecognitionSpec::class.java))
    val failure = failureOf(events)

    then
    events.assertStatistics { it.started(1).failed(1) }
    failure is SpockAssertionError
  }

  // Extracted in `when` because a lambda inside a condition crashes the compiler.
  private fun failureOf(events: Events): Throwable? =
    events.failed().list().singleOrNull()
      ?.getRequiredPayload(TestExecutionResult::class.java)?.throwable?.orElse(null)
}
