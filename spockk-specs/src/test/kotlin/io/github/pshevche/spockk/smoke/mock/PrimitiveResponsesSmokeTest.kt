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

package io.github.pshevche.spockk.smoke.mock

import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.did
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returned
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.mock.TooManyInvocationsError
import spock.lang.FailsWith
import spock.lang.Specification

interface Calculator {
  fun count(): Int

  fun total(): Long

  fun ratio(): Double

  fun enabled(): Boolean

  fun boxedCount(): Int?
}

/**
 * Covers interactions on methods whose return type is a numeric primitive. Kotlin's own `Int.times`
 * member wins overload resolution over Spockk's `Int.times(T)` extension for exactly those types, so
 * `N * mock.method()` reaches the plugin as a plain multiplication - every response form and the
 * cardinality itself silently did nothing before that shape was recognized. `Boolean` and a boxed
 * `Int?` are covered alongside as the contrast: no `Int.times` overload accepts them.
 */
class PrimitiveResponsesSmokeTest : Specification() {

  fun `returned produces the stubbed value for primitive return types`() {
    given
    val obj = Mock(Calculator::class.java)

    `when`
    val count = obj.count()
    val total = obj.total()
    val ratio = obj.ratio()
    val enabled = obj.enabled()
    val boxed = obj.boxedCount()

    then
    1 * obj.count() returned 42
    1 * obj.total() returned 42L
    1 * obj.ratio() returned 0.5
    1 * obj.enabled() returned true
    1 * obj.boxedCount() returned 42
    verifyAll {
      count == 42
      total == 42L
      ratio == 0.5
      enabled
      boxed == 42
    }
  }

  fun `returns produces the stubbed value for primitive return types`() {
    given
    val obj = Stub(Calculator::class.java) {
      count() returns 42
      total() returns 42L
      ratio() returns 0.5
      enabled() returns true
      boxedCount() returns 42
    }

    expect
    verifyAll {
      obj.count() == 42
      obj.total() == 42L
      obj.ratio() == 0.5
      obj.enabled()
      obj.boxedCount() == 42
    }
  }

  fun `did produces the computed value for primitive return types`() {
    given
    val obj = Mock(Calculator::class.java)

    `when`
    val count = obj.count()
    val total = obj.total()
    val ratio = obj.ratio()
    val enabled = obj.enabled()
    val boxed = obj.boxedCount()

    then
    1 * obj.count() did { 42 }
    1 * obj.total() did { 42L }
    1 * obj.ratio() did { 0.5 }
    1 * obj.enabled() did { true }
    1 * obj.boxedCount() did { 42 }
    verifyAll {
      count == 42
      total == 42L
      ratio == 0.5
      enabled
      boxed == 42
    }
  }

  fun `does produces the computed value for primitive return types`() {
    given
    val obj = Stub(Calculator::class.java) {
      count() does { 42 }
      total() does { 42L }
      ratio() does { 0.5 }
      enabled() does { true }
      boxedCount() does { 42 }
    }

    expect
    verifyAll {
      obj.count() == 42
      obj.total() == 42L
      obj.ratio() == 0.5
      obj.enabled()
      obj.boxedCount() == 42
    }
  }

  fun `a response computed from the invocation arguments reaches a primitive return type`() {
    given
    val obj = Stub(Calculator::class.java) {
      count() does { args -> args.size }
    }

    expect
    obj.count() == 0
  }

  /**
   * The cardinality itself, not just the response: an unsatisfied count on a primitive-returning
   * method has to fail rather than pass silently.
   */
  @FailsWith(TooFewInvocationsError::class)
  fun `too few invocations on a primitive-returning method fails`() {
    given
    val obj = Mock(Calculator::class.java)

    `when`
    obj.enabled()

    then
    1 * obj.count()
  }

  @FailsWith(TooManyInvocationsError::class)
  fun `too many invocations on a primitive-returning method fails`() {
    given
    val obj = Mock(Calculator::class.java)

    `when`
    obj.count()
    obj.count()

    then
    1 * obj.count()
  }

  fun `range cardinality accepts a count within bounds on a primitive-returning method`() {
    given
    val obj = Mock(Calculator::class.java)

    `when`
    obj.count()
    obj.count()

    then
    (1..3) * obj.count()
  }
}
