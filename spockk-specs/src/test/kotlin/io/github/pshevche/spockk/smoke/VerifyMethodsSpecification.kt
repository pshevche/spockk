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
import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.Verify

private const val NOT_SATISFIED_X_POSITIVE = "Condition not satisfied:\n\nx > 0\n| |\n| false\n-2\n"

class VerifyMethodsSpecification : Specification() {

  @Verify
  private fun isPositive(x: Int) {
    listOf(true, false).any { it }
    x > 0
  }

  private object Assertions {
    @Verify
    fun isPositive(x: Int) {
      x > 0
    }

    @Verify
    fun isPositiveAndEven(x: Int) {
      x > 0
      x % 2 == 0
    }
  }

  @MigratedFrom("org.spockframework.smoke.VerifyMethodsSpecification#@Verify method succeeds")
  fun `@Verify method succeeds`() {
    expect
    isPositive(2)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @FailsWith(value = ConditionNotSatisfiedError::class, expectedMessage = NOT_SATISFIED_X_POSITIVE)
  @MigratedFrom("org.spockframework.smoke.VerifyMethodsSpecification#@Verify method fails")
  fun `@Verify method fails`() {
    expect
    isPositive(-2)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @FailsWith(value = ConditionNotSatisfiedError::class, expectedMessage = NOT_SATISFIED_X_POSITIVE)
  @MigratedFrom("org.spockframework.smoke.VerifyMethodsSpecification#@Verify works on methods from non-spec helper classes")
  fun `@Verify works on methods from non-spec helper classes`() {
    expect
    Assertions.isPositive(-2)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @FailsWith(value = ConditionNotSatisfiedError::class, expectedMessage = NOT_SATISFIED_X_POSITIVE)
  @MigratedFrom("org.spockframework.smoke.VerifyMethodsSpecification#first failing assertion fails the feature")
  fun `first failing assertion fails the feature`() {
    expect
    Assertions.isPositiveAndEven(-2)
  }
}
