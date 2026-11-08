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
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.PendingFeature
import spock.lang.Retry
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class PollingConditionsSpec : Specification() {

  private val conditions = PollingConditions()
  private var fieldConditions = PollingConditions()

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
    "spock.util.concurrent.PollingConditionsSpec#succeeds if all conditions are eventually satisfied with condition method '#conditionMethod' and field"
  )
  fun `succeeds if all conditions are eventually satisfied with condition method '#conditionMethod' and field`(
    conditionMethod: String
  ) {
    `when`
    fieldConditions = PollingConditions()
    val num = AtomicInteger(42)
    val str = AtomicReference<String?>(null)
    thread {
      Thread.sleep(500)
      str.set("hello")
    }
    poll(fieldConditions, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "hello")
    }

    then
    noExceptionThrown()

    where
    variable(conditionMethod).from(".eventually", ".call", "")
  }

  @Retry
  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#succeeds if all conditions are eventually satisfied with condition method '#conditionMethod' and local variable"
  )
  fun `succeeds if all conditions are eventually satisfied with condition method '#conditionMethod' and local variable`(
    conditionMethod: String
  ) {
    `when`
    val num = AtomicInteger(42)
    val str = AtomicReference<String?>(null)
    val polling = PollingConditions()
    thread {
      Thread.sleep(500)
      str.set("hello")
    }
    poll(polling, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "hello")
    }

    then
    noExceptionThrown()

    where
    variable(conditionMethod).from(".eventually", ".call", "")
  }

  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#fails if any condition isn't satisfied in time with condition method '#conditionMethod' and field"
  )
  fun `fails if any condition isn't satisfied in time with condition method '#conditionMethod' and field`(
    conditionMethod: String
  ) {
    `when`
    fieldConditions = PollingConditions().also { it.timeout = 0.1 }
    val num = AtomicInteger(42)
    val str = AtomicReference<String?>(null)
    poll(fieldConditions, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "bye")
    }

    then
    thrown(SpockTimeoutError::class.java)

    where
    variable(conditionMethod).from(".eventually", ".call", "")
  }

  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#fails if any condition isn't satisfied in time with condition method '#conditionMethod' and local variable"
  )
  fun `fails if any condition isn't satisfied in time with condition method '#conditionMethod' and local variable`(
    conditionMethod: String
  ) {
    `when`
    val num = AtomicInteger(42)
    val str = AtomicReference<String?>(null)
    val polling = PollingConditions().also { it.timeout = 0.1 }
    poll(polling, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "bye")
    }

    then
    thrown(SpockTimeoutError::class.java)

    where
    variable(conditionMethod).from(".eventually", ".call", "")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1071")
  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#reports failed condition of last failed attempt with condition method '#conditionMethod' and field"
  )
  fun `reports failed condition of last failed attempt with condition method '#conditionMethod' and field`(
    conditionMethod: String
  ) {
    `when`
    fieldConditions = PollingConditions().also { it.timeout = 0.1 }
    val num = AtomicInteger(42)
    val str = AtomicReference<String?>(null)
    poll(fieldConditions, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "bye")
    }

    then
    val e = thrown(SpockTimeoutError::class.java)
    val cause = e.cause as ConditionNotSatisfiedError
    cause.condition.text == "str.get() == \"bye\""

    where
    variable(conditionMethod).from(".eventually", ".call", "")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1071")
  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#reports failed condition of last failed attempt with condition method '#conditionMethod' and local variable"
  )
  fun `reports failed condition of last failed attempt with condition method '#conditionMethod' and local variable`(
    conditionMethod: String
  ) {
    `when`
    val num = AtomicInteger(42)
    val str = AtomicReference<String?>(null)
    val polling = PollingConditions().also { it.timeout = 0.1 }
    poll(polling, conditionMethod) {
      assert(num.get() == 42)
      assert(str.get() == "bye")
    }

    then
    val e = thrown(SpockTimeoutError::class.java)
    val cause = e.cause as ConditionNotSatisfiedError
    cause.condition.text == "str.get() == \"bye\""

    where
    variable(conditionMethod).from(".eventually", ".call", "")
  }

  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#can override timeout per invocation with condition method '#conditionMethod' and field"
  )
  fun `can override timeout per invocation with condition method '#conditionMethod' and field`(
    conditionMethod: String
  ) {
    `when`
    fieldConditions = PollingConditions()
    val num = AtomicInteger(0)
    thread {
      Thread.sleep(250)
      num.set(42)
    }
    pollWithTimeout(fieldConditions, conditionMethod, 0.0) {
      assert(num.get() == 42)
    }

    then
    thrown(SpockTimeoutError::class.java)

    where
    variable(conditionMethod).from(".within", ".call", "")
  }

  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#can override timeout per invocation with condition method '#conditionMethod' and local variable"
  )
  fun `can override timeout per invocation with condition method '#conditionMethod' and local variable`(
    conditionMethod: String
  ) {
    `when`
    val num = AtomicInteger(0)
    val polling = PollingConditions()
    thread {
      Thread.sleep(250)
      num.set(42)
    }
    pollWithTimeout(polling, conditionMethod, 0.0) {
      assert(num.get() == 42)
    }

    then
    thrown(SpockTimeoutError::class.java)

    where
    variable(conditionMethod).from(".within", ".call", "")
  }

  @Retry
  @MigratedFrom("spock.util.concurrent.PollingConditionsSpec#provides fine-grained control over polling rhythm")
  fun `provides fine-grained control over polling rhythm`() {
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

  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#correctly handles checks that take longer than given check interval"
  )
  fun `correctly handles checks that take longer than given check interval`() {
    given("polling condition with small timeout")
    val condition = PollingConditions().also { it.timeout = 0.05 }
    var secondAttempt = false

    `when`("first attempt already takes longer than the timeout")
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

    then("there will be no second one")
    thrown(SpockTimeoutError::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1071")
  @MigratedFrom("spock.util.concurrent.PollingConditionsSpec#correctly creates timeout error message")
  fun `correctly creates timeout error message`(onTimeoutClosure: String?, expectedMessageSuffix: String) {
    given
    val onTimeout = onTimeoutClosure?.let(::timeoutClosure)
    val polling = PollingConditions().also { it.timeout = 0.1 }
    polling.onTimeout(onTimeout)

    `when`
    polling.eventually(closure { 1 == 0 })

    then
    val e = thrown(SpockTimeoutError::class.java)
    timeoutMessage(expectedMessageSuffix).matches(e.message!!)

    where
    onTimeoutClosure        ; expectedMessageSuffix
    null                    ; ""
    "noArgClosure"          ; ": test"
    "throwableArgClosure"   ; ": ConditionNotSatisfiedError"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1071")
  @MigratedFrom(
    "spock.util.concurrent.PollingConditionsSpec#correctly creates timeout error message when onTimeout called multiple times"
  )
  fun `correctly creates timeout error message when onTimeout called multiple times`(
    onTimeoutClosure: String?,
    secondOnTimeoutClosure: String?,
    expectedMessageSuffix: String
  ) {
    given
    val onTimeout = onTimeoutClosure?.let(::timeoutClosure)
    val secondOnTimeout = secondOnTimeoutClosure?.let(::timeoutClosure)
    val polling = PollingConditions().also { it.timeout = 0.1 }
    polling.onTimeout(onTimeout)
    polling.onTimeout(secondOnTimeout)

    `when`
    polling.eventually(closure { 1 == 0 })

    then
    val e = thrown(SpockTimeoutError::class.java)
    timeoutMessage(expectedMessageSuffix).matches(e.message!!)

    where
    onTimeoutClosure      ; secondOnTimeoutClosure ; expectedMessageSuffix
    "noArgClosure"        ; null                   ; ""
    null                  ; "noArgClosure"         ; ": test"
    "throwableArgClosure" ; "noArgClosure"         ; ": test"
  }

  private fun poll(polling: PollingConditions, method: String, body: () -> Unit) {
    when (method) {
      ".eventually" -> polling.eventually(closure(body))
      ".call" -> polling.call(closure(body))
      "" -> polling(closure(body))
      else -> error("Unknown condition method: $method")
    }
  }

  private fun pollWithTimeout(polling: PollingConditions, method: String, timeout: Double, body: () -> Unit) {
    when (method) {
      ".within" -> polling.within(timeout, closure(body))
      ".call" -> polling.call(timeout, closure(body))
      "" -> polling(timeout, closure(body))
      else -> error("Unknown condition method: $method")
    }
  }

  private operator fun PollingConditions.invoke(body: Closure<*>) = call(body)

  private operator fun PollingConditions.invoke(timeout: Double, body: Closure<*>) = call(timeout, body)

  private fun timeoutMessage(suffix: String) =
    Regex("""Condition not satisfied after \d+(\.\d+)? seconds and \d+ attempts""" + Regex.escape(suffix))

  private fun timeoutClosure(name: String): Closure<String> = when (name) {
    "noArgClosure" -> noArgClosure
    "throwableArgClosure" -> throwableArgClosure
    else -> error("Unknown closure: $name")
  }

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
