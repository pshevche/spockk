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
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.anyMethod
import io.github.pshevche.spockk.lang.capture
import io.github.pshevche.spockk.lang.did
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.noMoreInteractions
import io.github.pshevche.spockk.lang.returned
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.slot
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.mock.TooManyInvocationsError
import spock.lang.FailsWith
import spock.lang.Specification

interface Greeter {
  fun setName(name: String)

  fun getUsername(): String
}

/**
 * Exercises the interaction-based testing preview end to end: `N * target.method(args)` cardinality,
 * `any()`/`anyMethod()` matchers, `does`/`did`/`returns`/`returned` responses, `capture`/`slot`
 * argument capture, `noMoreInteractions(...)` sugar, and the `Mock`/`Stub` trailing builder-block
 * syntax - see the design doc, `_docs/specs/2026-08-30-interaction-based-testing-design.md`. No
 * matching/verification logic is reimplemented here; every scenario below is really exercising
 * Spock's own `InteractionBuilder`/`MockController` runtime, reached through the compiler plugin's
 * rewrite.
 */
class InteractionsSmokeTest : Specification() {

  fun `satisfied cardinality passes`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")

    then
    1 * obj.setName("Alice")
  }

  @FailsWith(TooFewInvocationsError::class)
  fun `too few invocations fails`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    1 * obj.setName("Alice")
  }

  @FailsWith(TooManyInvocationsError::class)
  fun `too many invocations fails`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")
    obj.setName("Alice")

    then
    1 * obj.setName("Alice")
  }

  fun `range cardinality accepts any count within bounds`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")
    obj.setName("Alice")

    then
    (1..3) * obj.setName("Alice")
  }

  @FailsWith(TooManyInvocationsError::class)
  fun `range cardinality rejects a count above the upper bound`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")
    obj.setName("Alice")
    obj.setName("Alice")
    obj.setName("Alice")

    then
    (1..3) * obj.setName("Alice")
  }

  fun `any matches any value of the right type`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Bob")

    then
    1 * obj.setName(any())
  }

  fun `a literal-equality arg only matches that exact value`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")
    obj.setName("Bob")

    then
    1 * obj.setName("Alice")
    1 * obj.setName("Bob")
  }

  fun `returned produces the stubbed value`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    val result = obj.getUsername()

    then
    1 * obj.getUsername() returned "Alice"
    result == "Alice"
  }

  fun `did runs its side effect`() {
    given
    val obj = Mock(Greeter::class.java)
    var sideEffectRan = false

    `when`
    obj.setName("Alice")

    then
    1 * obj.setName("Alice") did { sideEffectRan = true }
    sideEffectRan
  }

  fun `capture records the actual invocation argument`() {
    given
    val slot = slot<String>()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")

    then
    1 * obj.setName(capture(slot))
    slot.captured == "Alice"
  }

  fun `anyMethod matches any method name`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")

    then
    1 * obj.anyMethod()
  }

  fun `noMoreInteractions passes when no undeclared call happened`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")

    then
    1 * obj.setName("Alice")
    noMoreInteractions(obj)
  }

  @FailsWith(TooManyInvocationsError::class)
  fun `noMoreInteractions fails when an undeclared call happened`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")
    obj.getUsername()

    then
    1 * obj.setName("Alice")
    noMoreInteractions(obj)
  }

  fun `Stub builder block interactions are active for the whole feature, not scope-limited`() {
    given
    val obj = Stub(Greeter::class.java) {
      getUsername() returns "Alice"
    }

    expect
    obj.getUsername() == "Alice"
    obj.getUsername() == "Alice"
  }

  fun `Mock builder block registers a real interaction usable across the whole feature`() {
    given
    val obj = Mock(Greeter::class.java) {
      setName("Alice") does { }
    }

    `when`
    obj.setName("Alice")

    then
    noExceptionThrown()
  }

  fun `capture inside a Stub builder block records the invocation argument`() {
    given
    val slot = slot<String>()
    val obj = Stub(Greeter::class.java) {
      setName(capture(slot)) does { }
    }

    `when`
    obj.setName("Alice")

    then
    slot.captured == "Alice"
  }

  fun `then block combines an interaction and a plain boolean condition`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    val result = obj.getUsername()

    then
    1 * obj.getUsername() returned "Alice"
    result == "Alice"
  }

  fun `interaction combined with thrown - exception is caught and the interaction is still verified`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")
    throw IllegalStateException("boom")

    then
    1 * obj.setName("Alice")
    thrown(IllegalStateException::class.java)
  }

  @FailsWith(TooFewInvocationsError::class)
  fun `interaction failure surfaces even when the when block also threw`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    throw IllegalStateException("boom")

    then
    1 * obj.setName("Alice")
    thrown(IllegalStateException::class.java)
  }

  fun `each when-then pair verifies its own interactions independently`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")

    then
    1 * obj.setName("Alice")

    `when`
    obj.setName("Bob")

    then
    1 * obj.setName("Bob")
  }
}
