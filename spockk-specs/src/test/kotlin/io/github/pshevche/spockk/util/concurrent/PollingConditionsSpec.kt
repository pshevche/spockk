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

package io.github.pshevche.spockk.util.concurrent

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Retry
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class PollingConditionsSpec : Specification() {

  private var fieldConditions = PollingConditions()
  private val conditions = PollingConditions()

  @MigratedFrom("spock.util.concurrent.PollingConditionsSpec#defaults")
  fun `defaults`() {
    expect
    conditions.timeout == 1.0
    conditions.initialDelay == 0.0
    conditions.delay == 0.1
    conditions.factor == 1.0
  }

  @Retry
  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#succeeds if all conditions are eventually satisfied with condition method '#conditionMethod' and field",
    "spock.util.concurrent.PollingConditionsSpec#succeeds if all conditions are eventually satisfied with condition method '#conditionMethod' and local variable"
  )
  fun `succeeds if all conditions are eventually satisfied`(conditionMethod: String, onField: Boolean) {
    given
    val num = AtomicInteger(0)
    val str = AtomicReference<String?>(null)
    val polling = pollingConditions(onField)
    num.set(42)
    thread {
      Thread.sleep(500)
      str.set("hello")
    }

    `when`
    poll(polling, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "hello")
    }

    then
    noExceptionThrown()

    where
    conditionMethod ; onField
    "eventually" ; true
    "eventually" ; false
    "call" ; true
    "call" ; false
  }

  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#fails if any condition isn't satisfied in time with condition method '#conditionMethod' and field",
    "spock.util.concurrent.PollingConditionsSpec#fails if any condition isn't satisfied in time with condition method '#conditionMethod' and local variable"
  )
  fun `fails if any condition isn't satisfied in time`(conditionMethod: String, onField: Boolean) {
    given
    val num = AtomicInteger(42)
    val str = AtomicReference<String?>(null)
    val polling = pollingConditions(onField, timeout = 0.1)

    `when`
    poll(polling, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "bye")
    }

    then
    thrown(SpockTimeoutError::class.java)

    where
    conditionMethod ; onField
    "eventually" ; true
    "eventually" ; false
    "call" ; true
    "call" ; false
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1071")
  @Issue("https://github.com/spockframework/spock/issues/413")
  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#reports failed condition of last failed attempt with condition method '#conditionMethod' and field",
    "spock.util.concurrent.PollingConditionsSpec#reports failed condition of last failed attempt with condition method '#conditionMethod' and local variable"
  )
  fun `reports failed condition of last failed attempt`(conditionMethod: String, onField: Boolean) {
    given
    val num = AtomicInteger(42)
    val str = AtomicReference<String?>(null)
    val polling = pollingConditions(onField, timeout = 0.1)

    `when`
    poll(polling, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "bye")
    }

    then
    val e = thrown(SpockTimeoutError::class.java)
    val cause = e.cause as ConditionNotSatisfiedError
    cause.condition.text == "str.get() == \"bye\""

    where
    conditionMethod ; onField
    "eventually" ; true
    "eventually" ; false
    "call" ; true
    "call" ; false
  }

  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#can override timeout per invocation with condition method '#conditionMethod' and field",
    "spock.util.concurrent.PollingConditionsSpec#can override timeout per invocation with condition method '#conditionMethod' and local variable"
  )
  fun `can override timeout per invocation`(conditionMethod: String, onField: Boolean) {
    given
    val num = AtomicInteger(0)
    val polling = pollingConditions(onField)
    thread {
      Thread.sleep(250)
      num.set(42)
    }

    `when`
    pollWithTimeout(polling, conditionMethod, 0.0) {
      assert(num.get() == 42)
    }

    then
    thrown(SpockTimeoutError::class.java)

    where
    conditionMethod ; onField
    "within" ; true
    "within" ; false
    "call" ; true
    "call" ; false
  }

  @Retry
  @MigratedFrom("spock.util.concurrent.PollingConditionsSpec#provides fine-grained control over polling rhythm")
  fun `provides fine-grained control over polling rhythm`() {
    given
    conditions.initialDelay = 0.01
    conditions.delay = 0.2
    conditions.factor = 2.0
    var count = 0
    val stats = mutableListOf(System.currentTimeMillis())

    `when`
    conditions.eventually(
      closure {
        stats += System.currentTimeMillis()
        if (++count < 3) assert(false)
      }
    )

    then
    count == 3
    val spans = (1..3).map { stats[it] - stats[it - 1] }
    spans[0] < spans[1]
    spans[1] < spans[2]
  }

  @MigratedFrom("spock.util.concurrent.PollingConditionsSpec#correctly handles checks that take longer than given check interval")
  fun `correctly handles checks that take longer than given check interval`() {
    given
    val condition = pollingConditions(false, timeout = 0.05)
    var secondAttempt = false

    `when`
    condition.eventually(
      closure {
        try {
          Thread.sleep(100)
          assert(secondAttempt)
        } finally {
          secondAttempt = true
        }
      }
    )

    then
    thrown(SpockTimeoutError::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1071")
  @MigratedFrom("spock.util.concurrent.PollingConditionsSpec#correctly creates timeout error message")
  fun `correctly creates timeout error message`(onTimeout: Closure<String>?, expectedMessageSuffix: String) {
    given
    val polling = pollingConditions(false, timeout = 0.1)
    polling.onTimeout(onTimeout)

    `when`
    polling.eventually(closure { assert(1 == 0) })

    then
    val e = thrown(SpockTimeoutError::class.java)
    timeoutMessage(expectedMessageSuffix).matches(e.message!!)

    where
    onTimeout ; expectedMessageSuffix
    null ; ""
    noArgClosure ; ": test"
    throwableArgClosure ; ": ConditionNotSatisfiedError"
  }

  @MigratedFrom("spock.util.concurrent.PollingConditionsSpec#correctly creates timeout error message when onTimeout called multiple times")
  fun `correctly creates timeout error message when onTimeout called multiple times`(
    onTimeout: Closure<String>?,
    secondOnTimeout: Closure<String>?,
    expectedMessageSuffix: String
  ) {
    given
    val polling = pollingConditions(false, timeout = 0.1)
    polling.onTimeout(onTimeout)
    polling.onTimeout(secondOnTimeout)

    `when`
    polling.eventually(closure { assert(1 == 0) })

    then
    val e = thrown(SpockTimeoutError::class.java)
    timeoutMessage(expectedMessageSuffix).matches(e.message!!)

    where
    onTimeout ; secondOnTimeout ; expectedMessageSuffix
    noArgClosure ; null ; ""
    null ; noArgClosure ; ": test"
    throwableArgClosure ; noArgClosure ; ": test"
  }

  private fun pollingConditions(onField: Boolean, timeout: Double = 1.0): PollingConditions {
    val polling = PollingConditions().also { it.timeout = timeout }
    if (!onField) return polling
    fieldConditions = polling
    return fieldConditions
  }

  private fun poll(polling: PollingConditions, method: String, body: () -> Unit) {
    when (method) {
      "eventually" -> polling.eventually(closure(body))
      "call" -> polling.call(closure(body))
      else -> error("Unknown condition method: $method")
    }
  }

  private fun pollWithTimeout(polling: PollingConditions, method: String, timeout: Double, body: () -> Unit) {
    when (method) {
      "within" -> polling.within(timeout, closure(body))
      "call" -> polling.call(timeout, closure(body))
      else -> error("Unknown condition method: $method")
    }
  }

  private fun timeoutMessage(suffix: String) =
    Regex("""Condition not satisfied after \d+(\.\d+)? seconds and \d+ attempts""" + Regex.escape(suffix))

  private fun closure(body: () -> Unit): Closure<Any?> = object : Closure<Any?>(this) {
    @Suppress("unused")
    fun doCall(): Any? {
      body()
      return null
    }
  }

  companion object {
    private val noArgClosure: Closure<String> = object : Closure<String>(PollingConditionsSpec::class.java) {
      @Suppress("unused")
      fun doCall(@Suppress("UNUSED_PARAMETER") it: Any?): String = "test"
    }

    private val throwableArgClosure: Closure<String> = object : Closure<String>(PollingConditionsSpec::class.java) {
      @Suppress("unused")
      fun doCall(err: Throwable): String = err.javaClass.simpleName
    }
  }
}
