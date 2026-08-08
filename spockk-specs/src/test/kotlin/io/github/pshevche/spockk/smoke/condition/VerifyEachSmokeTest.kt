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
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verifyEach
import io.github.pshevche.spockk.lang.`when`
import org.opentest4j.MultipleFailuresError
import org.spockframework.runtime.SpockAssertionError
import spock.lang.FailsWith
import spock.lang.Specification

private data class VerifyEachItem(val id: String)

/**
 * Exercises `verifyEach` - implicit conditions run once per element, with the element as the
 * implicit target, collecting failures across every element instead of stopping at the first one.
 */
class VerifyEachSmokeTest : Specification() {

  fun `passes when every element satisfies the block`() {
    expect
    verifyEach(listOf(2, 4, 6)) {
      this % 2 == 0
    }
  }

  @FailsWith(SpockAssertionError::class)
  fun `a single failing element throws directly, with item context in the message`() {
    expect
    verifyEach(listOf(2, 3, 4)) {
      this % 2 == 0
    }
  }

  fun `a single failing element's message identifies the item and index`() {
    given
    var caught: SpockAssertionError? = null

    `when`
    try {
      oneOddOutOfThree()
    } catch (e: SpockAssertionError) {
      caught = e
    }

    then
    caught != null
    caught!!.message!!.contains("item[1] 3")
  }

  fun `multiple failing elements are collected, later elements are still checked`() {
    given
    var caught: MultipleFailuresError? = null

    `when`
    try {
      twoOddOutOfFour()
    } catch (e: MultipleFailuresError) {
      caught = e
    }

    then
    caught != null
    caught!!.failures.size == 2
  }

  fun `the namer overload customizes how a failing item is identified`() {
    given
    var caught: SpockAssertionError? = null

    `when`
    try {
      bothItemsUnsatisfiedWithNamer()
    } catch (e: MultipleFailuresError) {
      caught = e.failures.first() as SpockAssertionError
    }

    then
    caught != null
    caught!!.message!!.contains("item[0] a")
  }

  private fun oneOddOutOfThree() {
    verifyEach(listOf(2, 3, 4)) {
      this % 2 == 0
    }
  }

  private fun twoOddOutOfFour() {
    verifyEach(listOf(1, 2, 3, 4)) {
      this % 2 == 0
    }
  }

  private fun bothItemsUnsatisfiedWithNamer() {
    verifyEach(listOf(VerifyEachItem("a"), VerifyEachItem("b")), { it.id }) {
      id == "nonexistent"
    }
  }
}
