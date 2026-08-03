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
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.Specification

private data class VerifyPc(val vendor: String, val clockRate: Int)

/**
 * Exercises `verify` - implicit conditions inside its lambda body, with the target as the implicit
 * receiver, failing fast on the first unsatisfied condition (matching Spock's `with`).
 */
class VerifySmokeTest : Specification() {

  fun `passes when all conditions inside the block hold`() {
    val pc = VerifyPc("Sunny", 2500)

    expect
    verify(pc) {
      vendor == "Sunny"
      clockRate >= 2000
    }
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  fun `fails on the first unsatisfied condition`() {
    val pc = VerifyPc("Sunny", 2500)

    expect
    verify(pc) {
      vendor == "Rainy"
      clockRate >= 2000
    }
  }

  fun `target members resolve without qualification`() {
    val pc = VerifyPc("Sunny", 2500)

    expect
    verify(pc) {
      vendor.startsWith("Sun")
    }
  }
}
