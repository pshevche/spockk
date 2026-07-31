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

package io.github.pshevche.spockk.runtime

import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ConditionExceptionSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.junit.platform.testkit.engine.Events
import org.spockframework.runtime.ConditionFailedWithExceptionError
import spock.lang.Specification
import kotlin.test.assertIs

class ConditionExceptionRuntimeTest : Specification() {

  fun `an exception in an implicit condition is reported as a ConditionFailedWithExceptionError`() {
    `when`
    val events = execute(selectMethod(ConditionExceptionSpec::class.java, "exception while evaluating an implicit condition"))

    then
    events.assertStatistics { it.started(1).failed(1) }
    assertIs<ConditionFailedWithExceptionError>(failure(events))
  }

  fun `an exception in an explicit condition is reported as a ConditionFailedWithExceptionError`() {
    `when`
    val events = execute(selectMethod(ConditionExceptionSpec::class.java, "exception while evaluating an explicit condition"))

    then
    events.assertStatistics { it.started(1).failed(1) }
    assertIs<ConditionFailedWithExceptionError>(failure(events))
  }

  private fun failure(events: Events): Throwable =
    events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow()
}
