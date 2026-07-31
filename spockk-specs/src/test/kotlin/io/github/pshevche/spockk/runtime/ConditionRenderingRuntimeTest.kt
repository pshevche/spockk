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
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ConditionRenderingSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.Events
import spock.lang.Specification
import kotlin.test.assertContains

class ConditionRenderingRuntimeTest : Specification() {

  fun `an unsatisfied condition renders the expression and its recorded values`() {
    `when`
    val events = execute(selectClass(ConditionRenderingSpec::class.java))

    then
    val message = failure(events).message ?: ""
    assertContains(message, "Condition not satisfied")
    assertContains(message, "x > y")
    assertContains(message, "5")
    assertContains(message, "10")
    assertContains(message, "false")
  }

  private fun failure(events: Events): Throwable =
    events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow()
}
