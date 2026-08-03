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

package io.github.pshevche.spockk.smoke.condition

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.verifyEach
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockMultipleFailuresError
import spock.lang.FailsWith
import spock.lang.Specification

private data class HelperPc(val vendor: String, val clockRate: Int)

/**
 * Exercises verify/verifyAll/verifyEach factored out into a plain (non-feature) member method,
 * called from a `then`/`expect` block - failures are reported the same way as if the helper's body
 * were written inline.
 */
class VerifyInHelperMethodSmokeTest : Specification() {

  fun `verify inside a helper method passes when the condition holds`() {
    expect
    checkVendor(HelperPc("Sunny", 2500), "Sunny")
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  fun `verify inside a helper method fails the same way as an inline verify`() {
    expect
    checkVendor(HelperPc("Sunny", 2500), "Rainy")
  }

  @FailsWith(SpockMultipleFailuresError::class)
  fun `verifyAll inside a helper method still collects multiple failures`() {
    expect
    checkAllUnsatisfied(HelperPc("Sunny", 2500))
  }

  fun `verifyEach inside a helper method still checks every element`() {
    expect
    checkAllEven(listOf(2, 4, 6))
  }

  private fun checkVendor(pc: HelperPc, expected: String) {
    verify(pc) {
      vendor == expected
    }
  }

  private fun checkAllUnsatisfied(pc: HelperPc) {
    verifyAll(pc) {
      vendor == "Rainy"
      clockRate >= 9999
    }
  }

  private fun checkAllEven(values: List<Int>) {
    verifyEach(values) {
      this % 2 == 0
    }
  }
}
