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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.verifyEach
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockAssertionError
import spock.lang.FailsWith
import spock.lang.PendingFeature
import spock.lang.Specification

/** Failing conditions inside `verify` (Spock's `with`), `verifyAll` and `verifyEach` blocks. */
class WithBlockFailingConditions : Specification() {

  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#basic usage")
  @FailsWith(ConditionNotSatisfiedError::class)
  fun `basic usage`() {
    val list = listOf(1, 2)

    expect
    verify(list) {
      size == 3
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#inlining")
  @FailsWith(ConditionNotSatisfiedError::class)
  fun inlining() {
    expect
    verify(listOf(1, 2)) {
      size == 3
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#nesting")
  @FailsWith(ConditionNotSatisfiedError::class)
  fun nesting() {
    val list = listOf(1, 2)

    expect
    verify(list) {
      size == 2
      get(0) == 1

      val map = mapOf("foo" to "bar")
      verify(map) {
        size == 2
      }
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#in then-block")
  @FailsWith(ConditionNotSatisfiedError::class)
  fun `in then-block`() {
    `when`
    val list = listOf(1, 2)

    then
    verify(list) {
      size == 3
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/820")
  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#in nested position")
  @FailsWith(ConditionNotSatisfiedError::class)
  fun `in nested position`() {
    `when`
    val list = listOf(1, 2)

    then
    repeat(1) {
      repeat(1) {
        verify(list) {
          size == 3
        }
      }
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/820")
  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#executed several times")
  @FailsWith(ConditionNotSatisfiedError::class)
  fun `executed several times`() {
    `when`
    val list = listOf(1, 2)

    then
    repeat(3) {
      verify(list) {
        size == 3
      }
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#in helper method")
  @FailsWith(ConditionNotSatisfiedError::class)
  fun `in helper method`() {
    val list = listOf(1, 2)

    expect
    helper(list)
  }

  @MigratedFrom(
    "org.spockframework.smoke.WithBlockFailingConditions#nested with expressions, the last one has a failing method condition"
  )
  @FailsWith(ConditionNotSatisfiedError::class)
  fun `nested with expressions, the last one has a failing method condition`() {
    val list = listOf(listOf(listOf("value")))

    expect
    verify(list) {
      verify(this[0]) {
        verify(this[0]) {
          verify(this[0]) {
            isEmpty()
          }
        }
      }
    }
  }

  @MigratedFrom(
    "org.spockframework.smoke.WithBlockFailingConditions#condition method #conditionMethod within condition method #conditionMethod"
  )
  fun `condition method #conditionMethod within condition method #conditionMethod`(
    conditionMethod: String,
    expectedException: Class<out Throwable>
  ) {
    `when`
    nestedFalse(conditionMethod)

    then
    thrown(expectedException)

    where
    conditionMethod ; expectedException
    "verify"        ; ConditionNotSatisfiedError::class.java
    "verifyAll"     ; ConditionNotSatisfiedError::class.java
    "verifyEach"    ; SpockAssertionError::class.java
  }

  @MigratedFrom(
    "org.spockframework.smoke.WithBlockFailingConditions#condition method #conditionMethod within condition method #conditionMethod with exception"
  )
  fun `condition method #conditionMethod within condition method #conditionMethod with exception`(
    conditionMethod: String,
    expectedException: Class<out Throwable>
  ) {
    `when`
    nestedTrueThenThrow(conditionMethod)

    then
    val exception = thrown(expectedException)
    (expectedException != Exception::class.java || exception.message == "foo") &&
      (expectedException == Exception::class.java || exception.message!!.contains("foo"))

    where
    conditionMethod ; expectedException
    "verify"        ; Exception::class.java
    "verifyAll"     ; Exception::class.java
    "verifyEach"    ; SpockAssertionError::class.java
  }

  @MigratedFrom(
    "org.spockframework.smoke.WithBlockFailingConditions#condition method #conditionMethod within condition method #conditionMethod with only exception"
  )
  fun `condition method #conditionMethod within condition method #conditionMethod with only exception`(
    conditionMethod: String,
    expectedException: Class<out Throwable>
  ) {
    `when`
    nestedThrow(conditionMethod)

    then
    val exception = thrown(expectedException)
    (expectedException != Exception::class.java || exception.message == "foo") &&
      (expectedException == Exception::class.java || exception.message!!.contains("foo"))

    where
    conditionMethod ; expectedException
    "verify"        ; Exception::class.java
    "verifyAll"     ; Exception::class.java
    "verifyEach"    ; SpockAssertionError::class.java
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#condition method #conditionMethod")
  fun `condition method #conditionMethod`(conditionMethod: String, expectedException: Class<out Throwable>) {
    `when`
    singleFalse(conditionMethod)

    then
    thrown(expectedException)

    where
    conditionMethod ; expectedException
    "verify"        ; ConditionNotSatisfiedError::class.java
    "verifyAll"     ; ConditionNotSatisfiedError::class.java
    "verifyEach"    ; SpockAssertionError::class.java
  }

  @MigratedFrom("org.spockframework.smoke.WithBlockFailingConditions#condition method #conditionMethod with exception")
  fun `condition method #conditionMethod with exception`(conditionMethod: String, expectedException: Class<out Throwable>) {
    `when`
    singleTrueThenThrow(conditionMethod)

    then
    val exception = thrown(expectedException)
    (expectedException != Exception::class.java || exception.message == "foo") &&
      (expectedException == Exception::class.java || exception.message!!.contains("foo"))

    where
    conditionMethod ; expectedException
    "verify"        ; Exception::class.java
    "verifyAll"     ; Exception::class.java
    "verifyEach"    ; SpockAssertionError::class.java
  }

  @MigratedFrom(
    "org.spockframework.smoke.WithBlockFailingConditions#condition method #conditionMethod with only exception"
  )
  fun `condition method #conditionMethod with only exception`(
    conditionMethod: String,
    expectedException: Class<out Throwable>
  ) {
    `when`
    singleThrow(conditionMethod)

    then
    val exception = thrown(expectedException)
    (expectedException != Exception::class.java || exception.message == "foo") &&
      (expectedException == Exception::class.java || exception.message!!.contains("foo"))

    where
    conditionMethod ; expectedException
    "verify"        ; Exception::class.java
    "verifyAll"     ; Exception::class.java
    "verifyEach"    ; SpockAssertionError::class.java
  }

  private fun helper(list: List<Int>) {
    verify(list) {
      size == 3
    }
  }

  private fun nestedFalse(method: String) {
    when (method) {
      "verify" -> nestedFalseVerify()
      "verifyAll" -> nestedFalseVerifyAll()
      "verifyEach" -> nestedFalseVerifyEach()
      else -> error(method)
    }
  }

  private fun nestedFalseVerify() {
    verify(listOf("")) {
      verify(listOf("")) {
        false
      }
    }
  }

  private fun nestedFalseVerifyAll() {
    verifyAll(listOf("")) {
      verifyAll(listOf("")) {
        false
      }
    }
  }

  private fun nestedFalseVerifyEach() {
    verifyEach(listOf("")) {
      verifyEach(listOf("")) {
        false
      }
    }
  }

  private fun nestedTrueThenThrow(method: String) {
    when (method) {
      "verify" -> nestedTrueThenThrowVerify()
      "verifyAll" -> nestedTrueThenThrowVerifyAll()
      "verifyEach" -> nestedTrueThenThrowVerifyEach()
      else -> error(method)
    }
  }

  private fun nestedTrueThenThrowVerify() {
    verify(listOf("")) {
      verify(listOf("")) {
        true
        throw Exception("foo")
      }
    }
  }

  private fun nestedTrueThenThrowVerifyAll() {
    verifyAll(listOf("")) {
      verifyAll(listOf("")) {
        true
        throw Exception("foo")
      }
    }
  }

  private fun nestedTrueThenThrowVerifyEach() {
    verifyEach(listOf("")) {
      verifyEach(listOf("")) {
        true
        throw Exception("foo")
      }
    }
  }

  private fun nestedThrow(method: String) {
    when (method) {
      "verify" -> nestedThrowVerify()
      "verifyAll" -> nestedThrowVerifyAll()
      "verifyEach" -> nestedThrowVerifyEach()
      else -> error(method)
    }
  }

  private fun nestedThrowVerify() {
    verify(listOf("")) {
      verify(listOf("")) {
        throw Exception("foo")
      }
    }
  }

  private fun nestedThrowVerifyAll() {
    verifyAll(listOf("")) {
      verifyAll(listOf("")) {
        throw Exception("foo")
      }
    }
  }

  private fun nestedThrowVerifyEach() {
    verifyEach(listOf("")) {
      verifyEach(listOf("")) {
        throw Exception("foo")
      }
    }
  }

  private fun singleFalse(method: String) {
    when (method) {
      "verify" -> singleFalseVerify()
      "verifyAll" -> singleFalseVerifyAll()
      "verifyEach" -> singleFalseVerifyEach()
      else -> error(method)
    }
  }

  private fun singleFalseVerify() {
    verify(listOf("")) {
      false
    }
  }

  private fun singleFalseVerifyAll() {
    verifyAll(listOf("")) {
      false
    }
  }

  private fun singleFalseVerifyEach() {
    verifyEach(listOf("")) {
      false
    }
  }

  private fun singleTrueThenThrow(method: String) {
    when (method) {
      "verify" -> singleTrueThenThrowVerify()
      "verifyAll" -> singleTrueThenThrowVerifyAll()
      "verifyEach" -> singleTrueThenThrowVerifyEach()
      else -> error(method)
    }
  }

  private fun singleTrueThenThrowVerify() {
    verify(listOf("")) {
      true
      throw Exception("foo")
    }
  }

  private fun singleTrueThenThrowVerifyAll() {
    verifyAll(listOf("")) {
      true
      throw Exception("foo")
    }
  }

  private fun singleTrueThenThrowVerifyEach() {
    verifyEach(listOf("")) {
      true
      throw Exception("foo")
    }
  }

  private fun singleThrow(method: String) {
    when (method) {
      "verify" -> singleThrowVerify()
      "verifyAll" -> singleThrowVerifyAll()
      "verifyEach" -> singleThrowVerifyEach()
      else -> error(method)
    }
  }

  private fun singleThrowVerify() {
    verify(listOf("")) {
      throw Exception("foo")
    }
  }

  private fun singleThrowVerifyAll() {
    verifyAll(listOf("")) {
      throw Exception("foo")
    }
  }

  private fun singleThrowVerifyEach() {
    verifyEach(listOf("")) {
      throw Exception("foo")
    }
  }
}
