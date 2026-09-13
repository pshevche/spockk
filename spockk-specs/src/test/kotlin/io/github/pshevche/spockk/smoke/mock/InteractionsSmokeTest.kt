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
import io.github.pshevche.spockk.lang.Spy
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.anyMethod
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.did
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.noMoreInteractions
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

interface Greeter {
  fun setName(name: String)

  fun getUsername(): String

  fun greet(name: String): String
}

open class GreeterImpl : Greeter {
  private var name = "default"

  override fun setName(name: String) {
    this.name = name
  }

  override fun getUsername(): String = name

  override fun greet(name: String): String = "Hi, $name"
}

data class User(val name: String, val age: Int)

/** Deliberately not a data class: identity, not structural equality. */
class Greeting(val text: String)

class Audit {
  val events = mutableListOf<String>()

  fun record(event: String) {
    events += event
  }
}

interface UserRepository {
  fun findUser(name: String): User

  fun findMissing(name: String): User?

  fun listUsers(): List<User>

  fun render(user: User): Greeting
}

/**
 * Exercises interaction-based testing end to end: `N * target.method(args)` cardinality,
 * `any()`/`anyMethod()` matchers, `does`/`did`/`returns`/`returned` responses (including reading the
 * invocation's actual arguments, returning custom types, and invoking other mocks or real
 * collaborators), `noMoreInteractions(...)` sugar, and the `Mock`/`Stub`/`Spy` trailing
 * builder-block syntax - see the design doc, `_docs/specs/2026-08-30-interaction-based-testing-design.md`.
 * No matching/verification logic is reimplemented here; every scenario below is really exercising
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

  fun `an explicitly parenthesized response still unwraps correctly`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    val result = obj.getUsername()

    then
    1 * (obj.getUsername() returned "Alice")
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

  fun `did receives the actual invocation arguments`() {
    given
    val obj = Mock(Greeter::class.java)
    var receivedName: Any? = null

    `when`
    obj.setName("Alice")

    then
    1 * obj.setName(any()) did { args -> receivedName = args[0] }
    receivedName == "Alice"
  }

  fun `does computes a response from the invocation arguments`() {
    given
    val obj = Stub(Greeter::class.java) {
      greet(any()) does { args -> "Hi, ${args[0]}" }
    }

    expect
    obj.greet("Alice") == "Hi, Alice"
    obj.greet("Bob") == "Hi, Bob"
  }

  fun `then block interaction computes a response from the invocation arguments`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    val result = obj.greet("Alice")

    then
    1 * obj.greet(any()) did { args -> "Hi, ${args[0]}" }
    result == "Hi, Alice"
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

  fun `a zero-argument noMoreInteractions is a no-op that does not disturb a later when-then pair`() {
    given
    val obj = Mock(Greeter::class.java)

    `when`
    obj.setName("Alice")

    then
    noMoreInteractions()

    `when`
    obj.setName("Bob")

    then
    1 * obj.setName("Bob")
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

  fun `then block interaction verifies calls on a plain Spy, delegating to the real method by default`() {
    given
    val spy = Spy(GreeterImpl::class.java)

    `when`
    spy.setName("Alice")
    val result = spy.getUsername()

    then
    1 * spy.setName("Alice")
    result == "Alice"
  }

  fun `Spy builder block stubs one method while others still delegate to the real implementation`() {
    given
    val spy = Spy(GreeterImpl()) {
      getUsername() returns "Overridden"
    }

    `when`
    spy.setName("Alice")

    then
    spy.getUsername() == "Overridden"
  }

  fun `Spy on an existing instance verifies calls, delegating to the real method by default`() {
    given
    val real = GreeterImpl()
    val spy = Spy(real)

    `when`
    spy.setName("Alice")
    val result = spy.getUsername()

    then
    1 * spy.setName("Alice")
    result == "Alice"
  }

  fun `Spy on an existing instance can still be stubbed via a then block`() {
    given
    val real = GreeterImpl()
    val spy = Spy(real)

    `when`
    val result = spy.getUsername()

    then
    1 * spy.getUsername() returned "Overridden"
    result == "Overridden"
  }

  fun `Mock builder block still works when the feature also has a cleanup block`() {
    given
    val obj = Mock(Greeter::class.java) {
      setName("Alice") does { }
    }

    `when`
    obj.setName("Alice")

    then
    noExceptionThrown()

    cleanup
    obj.getUsername()
  }

  fun `Mock builder block declared inside a when block whose then has an exception condition`() {
    given
    var sideEffectRan = false

    `when`
    // A Mock/Stub builder block declared here nests one level deeper than the top-level statement
    // list once WhenBlockRewriter wraps this when: block's statements in a try/catch (its paired
    // then: block below has an exception condition) - proves that nesting doesn't defeat the
    // interaction splice.
    val inner = Mock(Greeter::class.java) {
      setName("Alice") does { sideEffectRan = true }
    }
    inner.setName("Alice")
    throw IllegalStateException("boom")

    then
    sideEffectRan
    thrown(IllegalStateException::class.java)
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

  fun `returned produces a custom type instance`() {
    given
    val repo = Mock(UserRepository::class.java)

    `when`
    val result = repo.findUser("Alice")

    then
    1 * repo.findUser("Alice") returned User("Alice", 30)
    result == User("Alice", 30)
  }

  fun `returns produces custom type instances, including a generic and a null one`() {
    given
    val repo = Stub(UserRepository::class.java) {
      findUser(any()) returns User("Alice", 30)
      listUsers() returns listOf(User("Alice", 30), User("Bob", 40))
      findMissing(any()) returns null
    }

    expect
    verifyAll {
      repo.findUser("anyone") == User("Alice", 30)
      repo.listUsers() == listOf(User("Alice", 30), User("Bob", 40))
      repo.findMissing("nobody") == null
    }
  }

  fun `does computes a custom type instance from the invocation arguments`() {
    given
    val repo = Stub(UserRepository::class.java) {
      findUser(any()) does { args -> User(args[0] as String, 30) }
    }

    expect
    repo.findUser("Alice") == User("Alice", 30)
    repo.findUser("Bob") == User("Bob", 30)
  }

  fun `did produces a custom type instance`() {
    given
    val repo = Mock(UserRepository::class.java)

    `when`
    val result = repo.findUser("Alice")

    then
    1 * repo.findUser("Alice") did { User("Alice", 30) }
    result == User("Alice", 30)
  }

  /** The stubbed instance is handed through as-is, not copied or re-created. */
  fun `returned hands back the very instance it was given`() {
    given
    val repo = Mock(UserRepository::class.java)
    val greeting = Greeting("Hi, Alice")

    `when`
    val result = repo.render(User("Alice", 30))

    then
    1 * repo.render(any()) returned greeting
    result === greeting
  }

  fun `a custom type argument matches by equality`() {
    given
    val repo = Mock(UserRepository::class.java)

    `when`
    repo.render(User("Alice", 30))

    then
    1 * repo.render(User("Alice", 30))
  }

  fun `a did side effect invokes a method on another mock`() {
    given
    val greeter = Mock(Greeter::class.java)
    val repo = Mock(UserRepository::class.java)

    `when`
    greeter.setName("Alice")

    then
    1 * greeter.setName("Alice") did { repo.findUser("Alice") }
    1 * repo.findUser("Alice")
  }

  fun `a does side effect invokes a method on a real collaborator`() {
    given
    val audit = Audit()
    val greeter = Stub(Greeter::class.java) {
      setName(any()) does { args -> audit.record("setName(${args[0]})") }
    }

    `when`
    greeter.setName("Alice")
    greeter.setName("Bob")

    then
    audit.events == listOf("setName(Alice)", "setName(Bob)")
  }

  /** The re-entrant call is itself intercepted, so its own interaction supplies the response. */
  fun `a did side effect invokes another method on the same mock`() {
    given
    val greeter = Mock(Greeter::class.java)

    `when`
    val result = greeter.greet("Alice")

    then
    1 * greeter.greet("Alice") did { greeter.getUsername() }
    1 * greeter.getUsername() returned "Bob"
    result == "Bob"
  }

  /** `getUsername()` still reads "default", so the side effect replaced the real `setName`. */
  fun `a does side effect on a Spy replaces the real implementation`() {
    given
    val audit = Audit()
    val spy = Spy(GreeterImpl()) {
      setName(any()) does { args -> audit.record("setName(${args[0]})") }
    }

    `when`
    spy.setName("Alice")

    then
    audit.events == listOf("setName(Alice)")
    spy.getUsername() == "default"
  }
}
