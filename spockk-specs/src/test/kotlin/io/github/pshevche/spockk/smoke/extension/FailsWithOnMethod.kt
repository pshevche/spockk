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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.FailsWithExpectedMessageSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.ConditionFailedWithExceptionError
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.FailsWith
import spock.lang.Specification

class FailsWithOnMethod : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.FailsWithOnMethod#@FailsWith can assert exception message")
  fun `@FailsWith can assert exception message`() {
    `when`
    val events = execute(selectClass(FailsWithExpectedMessageSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val e = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    e is SpockComparisonFailure
    val expected = """Condition not satisfied:

e.value == expectedMessage
| |        |
| |        My message
| Not my message
java.lang.RuntimeException: Not my message"""
    e.message!!.startsWith(expected)
  }

  @FailsWith(ConditionFailedWithExceptionError::class)
  @MigratedFrom("org.spockframework.smoke.extension.FailsWithOnMethod#can handle ConditionFailedWithExceptionError")
  fun `can handle ConditionFailedWithExceptionError`() {
    `when`
    val a = mapOf<String, String?>("b" to null)

    then
    a["b"]!!.length > 0
  }

  @FailsWith(NullPointerException::class)
  @MigratedFrom("org.spockframework.smoke.extension.FailsWithOnMethod#can handle cause of ConditionFailedWithExceptionError")
  fun `can handle cause of ConditionFailedWithExceptionError`() {
    `when`
    val a = mapOf<String, String?>("b" to null)

    then
    a["b"]!!.length > 0
  }
}
