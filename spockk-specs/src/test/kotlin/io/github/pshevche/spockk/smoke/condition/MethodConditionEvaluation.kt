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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

/**
 * Checks that arguments passed to a method-call condition (`foo(a, b)` as a whole condition) reach
 * the callee unchanged, including an array holding a single null or collection element.
 */
class MethodConditionEvaluation : Specification() {
  private var expected: Array<Any?> = emptyArray()

  @MigratedFrom("org.spockframework.smoke.condition.MethodConditionEvaluation#pass object array with one 'regular' element")
  fun `pass object array with one 'regular' element`() {
    expected = arrayOf(1)

    expect
    foo(expected)
  }

  @MigratedFrom("org.spockframework.smoke.condition.MethodConditionEvaluation#pass object array with one null element")
  fun `pass object array with one null element`() {
    expected = arrayOf(null)

    expect
    foo(expected)
  }

  @MigratedFrom("org.spockframework.smoke.condition.MethodConditionEvaluation#pass object array with one list element")
  fun `pass object array with one list element`() {
    expected = arrayOf(listOf(1, 2, 3))

    expect
    foo(expected)
  }

  private fun foo(actual: Array<Any?>): Boolean = actual.contentEquals(expected)

  private fun foo(@Suppress("UNUSED_PARAMETER") args: Collection<*>): Boolean = error("should never be called")
}
