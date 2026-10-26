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
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.`when`
import spock.lang.PendingFeature
import spock.lang.Specification

class WithBlockPassingConditions : Specification() {

  @MigratedFrom("org.spockframework.smoke.WithBlockPassingConditions#basic usage")
  fun `basic usage`() {
    val list = listOf(1, 2)

    expect
    verify(list) {
      size == 2
      get(0) == 1
      get(1) == 2
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockPassingConditions#inlining")
  fun `inlining`() {
    expect
    verify(listOf(1, 2)) {
      size == 2
      get(0) == 1
      get(1) == 2
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockPassingConditions#nesting")
  fun `nesting`() {
    val list = listOf(1, 2)

    expect
    verify(list) {
      size == 2
      get(0) == 1

      val map = mapOf("foo" to "bar")
      verify(map) {
        size == 1
        get("foo") == "bar"
      }

      get(1) == 2
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockPassingConditions#in then-block")
  fun `in then-block`() {
    `when`
    val list = listOf(1, 2)

    then
    verify(list) {
      size == 2
      get(0) == 1
      get(1) == 2
    }
  }

  @MigratedFrom(
    "org.spockframework.smoke.WithBlockPassingConditions#nested with expressions, the last one has a method condition"
  )
  fun `nested with expressions, the last one has a method condition`() {
    val list = listOf(listOf(listOf("value")))

    expect
    verify(list) {
      verify(this[0]) {
        verify(this[0]) {
          verify(this[0]) {
            !isEmpty()
          }
        }
      }
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockPassingConditions#nested with expressions with conditions")
  fun `nested with expressions with conditions`() {
    val list = listOf(listOf(listOf("value")))

    expect
    verify(list) {
      verify(this[0]) {
        size > 0
        verify(this[0]) {
          !isEmpty()
          verify(this[0]) {
            this == "value"
          }
        }
      }
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/822")
  @MigratedFrom("org.spockframework.smoke.WithBlockPassingConditions#nested with expressions with interaction")
  fun `nested with expressions with interaction`() {
    val supplier = Mock(Supplier::class.java) {
      get() returns "value"
    }
    val list = listOf(listOf(listOf(supplier)))

    `when`
    supplier.get()

    then
    verify(list) {
      verify(this[0]) {
        verify(this[0]) {
          verify(this[0]) {
            1 * get()
          }
        }
      }
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockPassingConditions#in helper method")
  fun `in helper method`() {
    val list = listOf(1, 2)

    expect
    helper(list)
  }

  private fun helper(list: List<Int>) {
    verify(list) {
      size == 2
      get(0) == 1
      get(1) == 2
    }
  }

  interface Supplier<T> {
    fun get(): T
  }
}
