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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.AssignedThrownNullSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.BareThrownNullSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.BareThrownWithoutTypeSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ExplicitAndInferredTypeSpec
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.mock.InteractionNotSatisfiedError
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.UnallowedExceptionThrownError
import org.spockframework.runtime.WrongExceptionThrownError
import spock.lang.FailsWith
import spock.lang.PendingFeature
import spock.lang.Specification
import java.io.IOException

/**
 * Exercises Spock's exception-condition helpers - `thrown()`, `notThrown()`, `noExceptionThrown()`
 * - mirroring the scenarios covered by Spock's own `ExceptionConditions` spec. A `when` block
 * paired with a `then` block containing one of these calls has its statements wrapped in a
 * try/catch that records the thrown exception on `SpecificationContext`
 * ([io.github.pshevche.spockk.compilation.transformer.WhenBlockRewriter]);
 * `thrown(Type::class.java)` is rewritten to call Spock's own already-shaded
 * `SpecInternals.checkExceptionThrown`
 * ([io.github.pshevche.spockk.compilation.transformer.condition.ExceptionConditionRewriter]).
 *
 * Deliberately out of scope (see the design doc, `_docs/specs/2026-08-09-exception-conditions-design.md`):
 * zero-arg `thrown()` used as a bare statement (no `val`/`var` to infer a type from), and nested
 * (non-top-level) exception-condition calls.
 */
class ExceptionConditions : Specification() {

  private class CustomError : Error()

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#basic usage")
  fun `catches the thrown exception`() {
    `when`
    "".substring(5)

    then
    thrown(IndexOutOfBoundsException::class.java)
  }

  fun `captures the thrown exception into a variable`() {
    `when`
    "".substring(5)

    then
    val e: IndexOutOfBoundsException = thrown(IndexOutOfBoundsException::class.java)
    e.message != null
  }

  fun `infers the exception type from a zero-arg thrown() assigned to a val`() {
    `when`
    "".substring(5)

    then
    val e: IndexOutOfBoundsException = thrown()
    e.message != null
  }

  @FailsWith(InvalidSpecException::class)
  fun `zero-arg thrown() as a bare statement is not rewritten and always throws`() {
    `when`
    "".substring(5)

    then
    thrown<IndexOutOfBoundsException>()
  }

  fun `a variable declared in when is still readable from then once the when block is try-wrapped`() {
    `when`
    val message = "boom"
    throw IllegalStateException(message)

    then
    val e = thrown(IllegalStateException::class.java)
    e.message == message
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#catch a RuntimeException")
  fun `catches a RuntimeException`() {
    `when`
    throw IllegalStateException("boom")

    then
    thrown(IllegalStateException::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#catch an Exception")
  fun `catches a checked Exception`() {
    `when`
    throw IOException("boom")

    then
    thrown(IOException::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#catch an Error")
  fun `catches an Error`() {
    `when`
    throw CustomError()

    then
    thrown(CustomError::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#catch base type")
  fun `catches the base Throwable type`() {
    `when`
    "".get(0)

    then
    val t: Throwable = thrown()
    t is IndexOutOfBoundsException
  }

  @FailsWith(WrongExceptionThrownError::class)
  fun `rejects the wrong exception type`() {
    `when`
    throw IllegalStateException("boom")

    then
    thrown(IllegalArgumentException::class.java)
  }

  @FailsWith(InvalidSpecException::class)
  fun `thrown(null) fails since the type cannot be inferred`() {
    `when`
    throw IllegalStateException("boom")

    then
    thrown<RuntimeException>(null)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#multiple exceptions")
  fun `each when-then pair catches its own exception independently`() {
    `when`
    throw IOException("first")

    then
    thrown(IOException::class.java)

    `when`
    throw IllegalStateException("second")

    then
    thrown(IllegalStateException::class.java)
  }

  fun `noExceptionThrown passes when nothing is thrown`() {
    `when`
    val x = 1

    then
    noExceptionThrown()
  }

  @FailsWith(UnallowedExceptionThrownError::class)
  fun `noExceptionThrown rejects any thrown exception`() {
    `when`
    throw IllegalStateException("boom")

    then
    noExceptionThrown()
  }

  fun `notThrown passes when nothing is thrown`() {
    `when`
    val x = 1

    then
    notThrown(IllegalStateException::class.java)
  }

  @FailsWith(UnallowedExceptionThrownError::class)
  fun `notThrown rejects a matching exception`() {
    `when`
    throw IllegalStateException("boom")

    then
    notThrown(IllegalStateException::class.java)
  }

  @FailsWith(IllegalStateException::class)
  fun `notThrown rethrows an exception of a different type`() {
    `when`
    throw IllegalStateException("boom")

    then
    notThrown(IllegalArgumentException::class.java)
  }

  @FailsWith(InvalidSpecException::class)
  fun `thrown() in an expect block is not rewritten and always throws`() {
    expect
    thrown(RuntimeException::class.java)
  }

  @FailsWith(IllegalStateException::class)
  fun `thrown() nested inside an if is not rewritten, so the raw exception propagates`() {
    `when`
    throw IllegalStateException("boom")

    then
    if (true) {
      thrown(IllegalStateException::class.java)
    }
  }

  @FailsWith(IllegalStateException::class)
  fun `thrown() used as a function argument is not rewritten, so the raw exception propagates`() {
    `when`
    throw IllegalStateException("boom")

    then
    println(thrown(IllegalStateException::class.java))
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#may occur after another condition")
  fun `may occur after another condition`() {
    `when`
    "".substring(5)

    then
    true
    thrown(IndexOutOfBoundsException::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#may occur in and'ed block")
  fun `may occur in and'ed block`() {
    `when`
    "".substring(5)

    then
    true

    and
    thrown(IndexOutOfBoundsException::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#when-block with var defs")
  fun `when-block with var defs`() {
    `when`
    val x: Char? = null
    val ch: Char? = "".get(0)

    then
    ch == null
    thrown(IndexOutOfBoundsException::class.java)
    ch == x
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#catch a Throwable")
  fun `catch a Throwable`() {
    `when`
    throw Throwable()

    then
    thrown(Throwable::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#exception in first block of when-group")
  fun `exception in first block of when-group`() {
    `when`
    throw Exception()

    and
    val x = 0

    then
    thrown(Exception::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#exception in second block of when-group")
  fun `exception in second block of when-group`() {
    `when`
    val x = 0

    and
    throw Exception()

    then
    thrown(Exception::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/851")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#thrown condition with explicit type null and inferred type")
  fun `thrown condition with explicit type null and inferred type`() {
    `when`
    throw Exception()

    then
    val e: Exception = thrown(null)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/853")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#thrown condition with undeterminable exception type")
  fun `thrown condition with undeterminable exception type (bare thrown)`() {
    `when`
    val events = execute(selectClass(BareThrownWithoutTypeSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is InvalidSpecException
    throwable.message == "Thrown exception type cannot be inferred automatically. Please specify a type explicitly (e.g. 'thrown(MyException)')."
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#thrown condition with undeterminable exception type")
  fun `thrown condition with undeterminable exception type (bare thrown null)`() {
    `when`
    val events = execute(selectClass(BareThrownNullSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is InvalidSpecException
    throwable.message == "Thrown exception type cannot be inferred automatically. Please specify a type explicitly (e.g. 'thrown(MyException)')."
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#thrown condition with undeterminable exception type")
  fun `thrown condition with undeterminable exception type (assigned thrown null)`() {
    `when`
    val events = execute(selectClass(AssignedThrownNullSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is InvalidSpecException
    throwable.message == "Thrown exception type cannot be inferred automatically. Please specify a type explicitly (e.g. 'thrown(MyException)')."
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#thrown condition with explicit and inferred type")
  fun `thrown condition with explicit and inferred type`() {
    `when`
    val events = execute(selectClass(ExplicitAndInferredTypeSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is WrongExceptionThrownError
    throwable.message == "Expected exception of type 'java.io.IOException', but got 'java.lang.Exception'"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/326")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#are able to observe a failing explicit condition")
  fun `are able to observe a failing explicit condition`() {
    `when`
    assert(1 * 2 == 3)

    then
    thrown(ConditionNotSatisfiedError::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @FailsWith(value = WrongExceptionThrownError::class, reason = "known limitation, not relevant in practice")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#are not able to observe a failing interaction")
  fun `are not able to observe a failing interaction`() {
    val list: MutableList<Any> = Mock()
    1 * list.remove(1)

    `when`
    list.add(1)

    then
    thrown(InteractionNotSatisfiedError::class.java)
  }

  private var ioException: IOException? = null

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/852")
  @FailsWith(WrongExceptionThrownError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#can use field assignment rather than declaration (not encouraged)")
  fun `can use field assignment rather than declaration (not encouraged)`() {
    `when`
    throw RuntimeException()

    then
    ioException = thrown()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/852")
  @FailsWith(WrongExceptionThrownError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#can use variable assignment rather than declaration (not encouraged)")
  fun `can use variable assignment rather than declaration (not encouraged)`() {
    var ioException: IOException?

    `when`
    throw RuntimeException()

    then
    ioException = thrown()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/852")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#can reuse exception variable declaration (not encouraged)")
  fun `can reuse exception variable declaration (not encouraged)`() {
    `when`
    throw RuntimeException()

    then
    var e: Exception = thrown()

    `when`
    throw IOException()

    then
    e = thrown()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#can have multi-assignments in when-block")
  fun `can have multi-assignments in when-block`() {
    `when`
    var (x, y) = Pair(1, 2)
    x = 3
    y = 4
    throw RuntimeException()
    x = 5
    y = 6

    then
    thrown(RuntimeException::class.java)
    x == 3
    y == 4
  }

  private fun foobar(): Int = throw IllegalStateException("foo")

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#thrown conditions don't destroy method reference when invocation is assigned to variable with the same name")
  fun `thrown conditions don't destroy method reference when invocation is assigned to variable with the same name`() {
    `when`
    val foobar = foobar()

    then
    thrown(IllegalStateException::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#thrown conditions don't destroy method reference when invocation is assigned to a multi-assignment with the same name")
  fun `thrown conditions don't destroy method reference when invocation is assigned to a multi-assignment with the same name`() {
    `when`
    val (foobar, b) = Pair(foobar(), 1)

    then
    thrown(Exception::class.java)
  }

  @FailsWith(WrongExceptionThrownError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionConditions#are able to observe a failing explicit condition (variation)")
  fun `are able to observe a failing explicit condition (variation)`() {
    `when`
    assert(1 * 2 == 3)

    then
    thrown(IOException::class.java)
  }
}
