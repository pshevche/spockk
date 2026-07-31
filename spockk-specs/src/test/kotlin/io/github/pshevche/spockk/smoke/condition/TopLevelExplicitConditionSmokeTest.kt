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
import spock.lang.Specification

/**
 * Exercises top-level explicit conditions (`assert(...)`) across the common Kotlin expression
 * shapes. Every condition here is satisfied, so the spec passes when the conditions are correctly
 * rewritten and evaluated.
 */
class TopLevelExplicitConditionSmokeTest : Specification() {

  fun `boolean literal condition`() {
    expect
    assert(true)
  }

  fun `equality condition`() {
    expect
    assert(1 == 1)
  }

  fun `relational condition`() {
    expect
    assert(2 > 1)
  }

  fun `negation condition`() {
    expect
    assert(!false)
  }

  fun `logical and condition`() {
    val left = "spockk".isNotEmpty()
    val right = "kotlin".isNotEmpty()

    expect
    assert(left && right)
  }

  fun `logical or condition`() {
    val left = "spockk".isEmpty()
    val right = "kotlin".isNotEmpty()

    expect
    assert(left || right)
  }

  fun `arithmetic condition`() {
    expect
    assert(1 + 1 == 2)
  }

  fun `method call condition`() {
    expect
    assert("hello".startsWith("he"))
  }

  fun `property access condition`() {
    expect
    assert("abc".length == 3)
  }

  fun `string equality condition`() {
    expect
    assert("spockk" == "spockk")
  }
}
