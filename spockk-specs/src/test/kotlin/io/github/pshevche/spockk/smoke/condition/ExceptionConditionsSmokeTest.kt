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
import spock.lang.FailsWith
import spock.lang.Specification

/**
 * Pins the *current* behavior of Spock's `thrown()`/`notThrown()`/`noExceptionThrown()`
 * exception-condition helpers under Spockk, mirroring the scenarios covered by Spock's own
 * `ExceptionConditions` spec.
 *
 * Unlike real Spock, Spockk's `when` blocks are never wrapped in a try/catch that records the
 * thrown exception on `SpecificationContext` (see `FeatureRewriter`/`ConditionRewriter`, which
 * have no equivalent of Spock's `SpecRewriter.rewriteWhenBlockForExceptionCondition`). As a
 * result none of these helpers work as documented yet: `when`-block exceptions always propagate
 * raw instead of being caught, `thrown()` unconditionally throws `InvalidSpecException` (its
 * un-rewritten fallback body), and `notThrown()`/`noExceptionThrown()` silently no-op because
 * `getThrownException()` is never populated. These tests document that gap as a regression
 * baseline rather than the intended behavior.
 */
class ExceptionConditionsSmokeTest : Specification() {

  @FailsWith(IndexOutOfBoundsException::class)
  fun `thrown() does not catch the when-block exception`() {
    `when`
    "".substring(5)

    then
    thrown(IndexOutOfBoundsException::class.java)
  }

  @FailsWith(InvalidSpecException::class)
  fun `thrown() always throws even when the when-block does not throw`() {
    `when`
    val x = 1

    then
    thrown(RuntimeException::class.java)
  }

  fun `noExceptionThrown() passes, but only vacuously since it never observes the when-block`() {
    `when`
    val x = 1

    then
    noExceptionThrown()
  }

  @FailsWith(IllegalStateException::class)
  fun `noExceptionThrown() does not catch the exception it should reject`() {
    `when`
    throw IllegalStateException("boom")

    then
    noExceptionThrown()
  }

  @FailsWith(IllegalStateException::class)
  fun `notThrown() does not catch the exception it should reject`() {
    `when`
    throw IllegalStateException("boom")

    then
    notThrown(IllegalStateException::class.java)
  }

  fun `notThrown() passes, but only vacuously since it never observes the when-block`() {
    `when`
    val x = 1

    then
    notThrown(IllegalArgumentException::class.java)
  }
}
