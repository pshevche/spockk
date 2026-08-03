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
import io.github.pshevche.spockk.lang.verifyAll
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockMultipleFailuresError
import spock.lang.FailsWith
import spock.lang.Specification

private data class VerifyAllPc(val vendor: String, val clockRate: Int)

/**
 * Exercises `verifyAll` - like `verify`, but a failing condition is collected instead of stopping
 * the block, so every condition is checked and all failures are reported together.
 */
class VerifyAllSmokeTest : Specification() {

  fun `passes when all conditions hold`() {
    expect
    verifyAll {
      1 + 1 == 2
      2 + 2 == 4
    }
  }

  fun `passes with a target when all conditions hold`() {
    val pc = VerifyAllPc("Sunny", 2500)

    expect
    verifyAll(pc) {
      vendor == "Sunny"
      clockRate >= 2000
    }
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  fun `a single failing condition throws directly, not wrapped`() {
    expect
    verifyAll {
      1 + 1 == 2
      1 + 1 == 3
    }
  }

  @FailsWith(SpockMultipleFailuresError::class)
  fun `multiple failing conditions are collected and reported together`() {
    val pc = VerifyAllPc("Sunny", 2500)

    expect
    verifyAll(pc) {
      vendor == "Rainy"
      clockRate >= 9999
    }
  }

  fun `both failing conditions are checked, not just the first`() {
    var caught: SpockMultipleFailuresError? = null
    try {
      bothUnsatisfied()
    } catch (e: SpockMultipleFailuresError) {
      caught = e
    }

    expect
    caught != null
    caught!!.failures.size == 2
  }

  private fun bothUnsatisfied() {
    verifyAll {
      1 == 2
      3 == 4
    }
  }
}
