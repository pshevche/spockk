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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.FailsWith
import spock.lang.Retry
import spock.lang.Specification
import spock.util.concurrent.BlockingVariables
import kotlin.concurrent.thread

class BlockingVariablesSpec : Specification() {

  @Retry
  @MigratedFrom("spock.util.concurrent.BlockingVariablesSpec#passing example 1 - variable is read after it is written")
  fun `passing example 1 - variable is read after it is written`() {
    val vars = BlockingVariables()

    `when`
    thread { vars.setProperty("foo", 42) }
    Thread.sleep(500)

    then
    vars.getProperty("foo") == 42
  }

  @Retry
  @MigratedFrom("spock.util.concurrent.BlockingVariablesSpec#passing example 2 - variable is read before it is written")
  fun `passing example 2 - variable is read before it is written`() {
    val vars = BlockingVariables()

    `when`
    thread {
      Thread.sleep(500)
      vars.setProperty("foo", 42)
    }

    then
    vars.getProperty("foo") == 42
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("spock.util.concurrent.BlockingVariablesSpec#failing example 1 - variable has wrong value")
  fun `failing example 1 - variable has wrong value`() {
    val vars = BlockingVariables()

    `when`
    thread { vars.setProperty("foo", 42) }

    then
    vars.getProperty("foo") == 21
  }

  @FailsWith(SpockTimeoutError::class)
  @MigratedFrom("spock.util.concurrent.BlockingVariablesSpec#failing example 2 - variable is read but not written")
  fun `failing example 2 - variable is read but not written`() {
    val vars = BlockingVariables()

    `when`
    thread { vars.setProperty("foo", 42) }

    then
    vars.getProperty("bar") == 42
  }

  @MigratedFrom("spock.util.concurrent.BlockingVariablesSpec#multiple variables")
  fun `multiple variables`() {
    val vars = BlockingVariables()

    `when`
    thread {
      vars.setProperty("baz", 3)
      vars.setProperty("bar", 2)
    }
    thread { vars.setProperty("foo", 1) }

    then
    vars.getProperty("foo") == 1
    vars.getProperty("bar") == 2
    vars.getProperty("baz") == 3
  }

  @MigratedFrom("spock.util.concurrent.BlockingVariablesSpec#variable may be null")
  fun `variable may be null`() {
    val vars = BlockingVariables()

    `when`
    thread { vars.setProperty("foo", null) }

    then
    vars.getProperty("foo") == null
  }

  @MigratedFrom("spock.util.concurrent.BlockingVariablesSpec#vars can be used in single thread")
  fun `vars can be used in single thread`() {
    val vars = BlockingVariables()

    `when`
    vars.setProperty("bar", 2)
    vars.setProperty("foo", 1)

    then
    vars.getProperty("foo") == 1
    vars.getProperty("bar") == 2
  }
}
