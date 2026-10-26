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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verifyEach
import io.github.pshevche.spockk.lang.`when`
import org.opentest4j.MultipleFailuresError
import org.spockframework.runtime.SpockAssertionError
import spock.lang.PendingFeature
import spock.lang.Specification

/**
 * Exercises `verifyEach` - implicit conditions run once per element, with the element as the
 * implicit target, collecting failures across every element instead of stopping at the first one.
 */
class VerifyEachBlocks : Specification() {

  @MigratedFrom("org.spockframework.smoke.VerifyEachBlocks#verifyEach succeeds")
  fun `verifyEach succeeds`() {
    given
    val range = (1..10).toList()

    expect
    verifyEach(range) {
      this > 0
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/812")
  @MigratedFrom("org.spockframework.smoke.VerifyEachBlocks#verifyEach handles a single failed element verification")
  fun `verifyEach handles a single failed element verification`() {
    given
    val range = (1..10).toList()

    `when`
    val e = catchFailure { allBelowTen(range) }

    then
    e is SpockAssertionError
    e.message == SINGLE_FAILURE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/812")
  @MigratedFrom("org.spockframework.smoke.VerifyEachBlocks#verifyEach handles multiple failed element verifications")
  fun `verifyEach handles multiple failed element verifications`() {
    given
    val range = (1..10).toList()

    `when`
    val e = catchFailure { allEven(range) }

    then
    e is MultipleFailuresError
    e.message == MULTIPLE_FAILURES
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/812")
  @MigratedFrom("org.spockframework.smoke.VerifyEachBlocks#rendering of the item can be customized with the namer")
  fun `rendering of the item can be customized with the namer`() {
    given
    val range = (1..10).toList()

    `when`
    val e = catchFailure { allBelowTenNamed(range) }

    then
    e is SpockAssertionError
    e.message == NAMED_FAILURE
  }

  @MigratedFrom("org.spockframework.smoke.VerifyEachBlocks#verifyEach fails handles exception")
  fun `verifyEach fails handles exception`() {
    given
    val range = (1..10).toList()

    `when`
    val e = catchFailure { throwAtFive(range) }

    then
    e is SpockAssertionError
    e.message == "Assertions failed for item[4] 5:\njava.lang.RuntimeException: x == 5"
  }

  @MigratedFrom("org.spockframework.smoke.VerifyEachBlocks#verifyEach fails handles exceptions")
  fun `verifyEach fails handles exceptions`() {
    given
    val range = (1..10).toList()

    `when`
    val e = catchFailure { throwAtFiveAndSix(range) }

    then
    e is MultipleFailuresError
    e.message ==
      "Multiple Failures (2 failures)\n" +
      "\torg.spockframework.runtime.SpockAssertionError: Assertions failed for item[4] 5:\n" +
      "java.lang.RuntimeException: x == 5\n" +
      "\torg.spockframework.runtime.SpockAssertionError: Assertions failed for item[5] 6:\n" +
      "java.lang.RuntimeException: x == 6"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @MigratedFrom("org.spockframework.smoke.VerifyEachBlocks#verifyEach fails handles nested exceptions")
  fun `verifyEach fails handles nested exceptions`() {
    given
    val range = (1..10).toList()

    `when`
    val e = catchFailure { verifyEach(range) { checks(this) } }

    then
    e is MultipleFailuresError
    e.message == NESTED_FAILURES
  }

  private fun allBelowTen(range: List<Int>) {
    verifyEach(range) {
      this < 10
    }
  }

  private fun allEven(range: List<Int>) {
    verifyEach(range) {
      this % 2 == 0
    }
  }

  private fun allBelowTenNamed(range: List<Int>) {
    verifyEach(range, { "int($it)" }) {
      this < 10
    }
  }

  private fun throwAtFive(range: List<Int>) {
    verifyEach(range) {
      if (this == 5) throw RuntimeException("x == $this").withoutStackTrace()
    }
  }

  private fun throwAtFiveAndSix(range: List<Int>) {
    verifyEach(range) {
      if (this in 5..6) throw RuntimeException("x == $this").withoutStackTrace()
    }
  }

  private fun checks(x: Int) {
    doCheck(x, ::nestedException)
    doCheck(x, ::nestedAssertion)
  }

  private fun doCheck(x: Int, consumer: (Int) -> Unit) = consumer(x)

  private fun nestedException(x: Int) {
    if (x == 5) throw RuntimeException("x == 5").withoutStackTrace()
  }

  private fun nestedAssertion(x: Int) {
    assert(x != 7)
  }

  private fun catchFailure(action: () -> Unit): Throwable {
    try {
      action()
    } catch (e: Throwable) {
      return e
    }
    throw AssertionError("expected a failure but none was thrown")
  }

  private fun <T : Throwable> T.withoutStackTrace(): T = apply { stackTrace = emptyArray() }

  private companion object {
    val SINGLE_FAILURE =
      """
      |Assertions failed for item[9] 10:
      |Condition not satisfied:
      |
      |this < 10
      ||    |
      |10   false
      |
      """.trimMargin()
    val MULTIPLE_FAILURES = buildString {
      append("Multiple Failures (5 failures)")
      for (i in listOf(1, 3, 5, 7, 9)) {
        append("\n\torg.spockframework.runtime.SpockAssertionError: Assertions failed for item[${i - 1}] $i:\n")
        append("Condition not satisfied:\n\nthis % 2 == 0\n|    |   |\n$i    1   false\n")
      }
    }
    val NAMED_FAILURE =
      """
      |Assertions failed for item[9] int(10):
      |Condition not satisfied:
      |
      |this < 10
      ||    |
      |10   false
      |
      """.trimMargin()
    val NESTED_FAILURES =
      "Multiple Failures (2 failures)\n" +
        "\torg.spockframework.runtime.SpockAssertionError: Assertions failed for item[4] 5:\n" +
        "java.lang.RuntimeException: x == 5\n" +
        "\torg.spockframework.runtime.SpockAssertionError: Assertions failed for item[6] 7:\n" +
        "Condition not satisfied:\n\nx != 7\n| |\n7 false\n"
  }
}
