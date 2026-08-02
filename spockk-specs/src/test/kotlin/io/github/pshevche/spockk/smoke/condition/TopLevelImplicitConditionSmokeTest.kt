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
import org.spockframework.runtime.ConditionFailedWithExceptionError
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.Specification

/**
 * Exercises top-level implicit conditions (a bare boolean expression statement in an `expect`
 * block) across the common Kotlin expression shapes, including both passing and expected-failure
 * cases annotated with @FailsWith.
 */
class TopLevelImplicitConditionSmokeTest : Specification() {

  fun `boolean literal condition`() {
    expect
    true
  }

  fun `equality condition`() {
    expect
    1 == 1
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  fun `unsatisfied equality comparison`() {
    expect
    1 == 2
  }

  fun `relational condition`() {
    expect
    2 > 1
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  fun `unsatisfied relational condition`() {
    expect
    2 > 3
  }

  fun `negation condition`() {
    expect
    !false
  }

  fun `logical and condition`() {
    val left = "spockk".isNotEmpty()
    val right = "kotlin".isNotEmpty()

    expect
    left && right
  }

  fun `logical or condition`() {
    val left = "spockk".isEmpty()
    val right = "kotlin".isNotEmpty()

    expect
    left || right
  }

  fun `arithmetic condition`() {
    expect
    1 + 1 == 2
  }

  fun `method call condition`() {
    expect
    "hello".startsWith("he")
  }

  fun `property access condition`() {
    expect
    "abc".length == 3
  }

  fun `string equality condition`() {
    expect
    "spockk" == "spockk"
  }

  @FailsWith(ConditionFailedWithExceptionError::class)
  fun `exception while evaluating an implicit condition`() {
    expect
    emptyList<Int>()[0] == 0
  }
}
