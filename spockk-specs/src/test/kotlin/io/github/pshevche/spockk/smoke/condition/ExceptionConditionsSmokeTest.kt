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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.UnallowedExceptionThrownError
import org.spockframework.runtime.WrongExceptionThrownError
import spock.lang.FailsWith
import spock.lang.Specification
import java.io.IOException

/**
 * Exercises Spock's exception-condition helpers - `thrown()`, `notThrown()`, `noExceptionThrown()`
 * - mirroring the scenarios covered by Spock's own `ExceptionConditions` spec. A `when` block
 * paired with a `then` block containing one of these calls has its statements wrapped in a
 * try/catch that records the thrown exception on `SpecificationContext`
 * ([io.github.pshevche.spockk.compilation.transformer.condition.WhenBlockRewriter]);
 * `thrown(Type::class.java)` is rewritten to call Spock's own already-shaded
 * `SpecInternals.checkExceptionThrown`
 * ([io.github.pshevche.spockk.compilation.transformer.condition.ExceptionConditionRewriter]).
 *
 * Deliberately out of scope (see the design doc, `_docs/specs/2026-08-09-exception-conditions-design.md`):
 * zero-arg `thrown()` used as a bare statement (no `val`/`var` to infer a type from), and nested
 * (non-top-level) exception-condition calls.
 */
class ExceptionConditionsSmokeTest : Specification() {

  private class CustomError : Error()

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

  fun `catches a RuntimeException`() {
    `when`
    throw IllegalStateException("boom")

    then
    thrown(IllegalStateException::class.java)
  }

  fun `catches a checked Exception`() {
    `when`
    throw IOException("boom")

    then
    thrown(IOException::class.java)
  }

  fun `catches an Error`() {
    `when`
    throw CustomError()

    then
    thrown(CustomError::class.java)
  }

  fun `catches the base Throwable type`() {
    `when`
    throw IllegalStateException("boom")

    then
    val t: Throwable = thrown(Throwable::class.java)
    t is IllegalStateException
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
}
