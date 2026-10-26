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
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VerifyAllHelperMethodSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.VerifyAllMethodSpec
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockComparisonFailure
import org.spockframework.runtime.SpockMultipleFailuresError
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.VerifyAll

class VerifyAllMethodsSpecification : Specification() {

  @VerifyAll
  private fun isPositiveAndEven(x: Int) {
    x > 0
    x % 2 == 0
  }

  @MigratedFrom("org.spockframework.smoke.VerifyAllMethodsSpecification#@VerifyAll method succeeds")
  fun `@VerifyAll method succeeds`() {
    expect
    isPositiveAndEven(2)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.VerifyAllMethodsSpecification#@VerifyAll method fails")
  fun `@VerifyAll method fails`() {
    `when`
    val events = execute(selectClass(VerifyAllMethodSpec::class.java))

    then
    val failures =
      events.failed().list().map {
        it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
      }
    failures.size == 1
    val error = failures[0] as SpockMultipleFailuresError
    error.failures.size == 2
    (error.failures[0] as ConditionNotSatisfiedError).condition.text == "x > 0"
    val comparison = error.failures[1] as SpockComparisonFailure
    comparison.expected.stringRepresentation.trim() == "0"
    comparison.actual.stringRepresentation.trim() == "-1"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.VerifyAllMethodsSpecification#@VerifyAll works on methods from non-spec helper classes")
  fun `@VerifyAll works on methods from non-spec helper classes`() {
    `when`
    val events = execute(selectClass(VerifyAllHelperMethodSpec::class.java))

    then
    val failures =
      events.failed().list().map {
        it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
      }
    failures.size == 1
    val error = failures[0] as SpockMultipleFailuresError
    error.failures.size == 2
    (error.failures[0] as ConditionNotSatisfiedError).condition.text == "x > 0"
    val comparison = error.failures[1] as SpockComparisonFailure
    comparison.expected.stringRepresentation.trim() == "0"
    comparison.actual.stringRepresentation.trim() == "-1"
  }
}
