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
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.verifyAll
import spock.lang.Specification

private open class WithBlocksPerson(
  val name: String = "Fred",
  val age: Int = 42,
  val spouse: WithBlocksPerson? = null
) {
  fun check() {
    assert(true)
  }
}

private open class WithBlocksEmployee(name: String = "Fred", val employer: String = "MarsTravelUnited") : WithBlocksPerson(name)

private class WithBlocksManager(name: String) : WithBlocksEmployee(name)

/** Spockk's `verify` corresponds to Spock's `with`. */
class WithBlocks : Specification() {

  @MigratedFrom("org.spockframework.smoke.WithBlocks#don't turn nested with expressions into condition")
  fun `don't turn nested with expressions into condition`() {
    given
    val list = listOf(listOf(listOf("end")))

    expect
    verify(list) {
      verify(this[0]) {
        verify(this[0]) {
          verify(this[0]) {
            this == "end"
          }
        }
      }
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlocks#can refer to properties of target object with property syntax")
  fun `can refer to properties of target object with property syntax`() {
    given
    val person = WithBlocksPerson()

    expect
    verify(person) {
      name == "Fred"
      age == 42
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlocks#can state the expected target type")
  fun `can state the expected target type`() {
    given
    val fred = WithBlocksPerson(spouse = WithBlocksManager("Wilma"))

    expect
    verify(fred.spouse as WithBlocksEmployee) {
      name == "Wilma"
      employer == "MarsTravelUnited"
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlocks#method condition is invoked on closure but not on the spec")
  fun `method condition is invoked on closure but not on the spec`() {
    given
    val map = mapOf("value1" to 1, "value2" to 2)

    expect
    verify(map) {
      size == 2
      containsKey("value2")
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlocks#nested method conditions are invoked on closure but not on the spec")
  fun `nested method conditions are invoked on closure but not on the spec`() {
    given
    val map = mapOf("value1" to 1, "value2" to 2)

    expect
    verify(map) {
      containsKey("value2")

      val list = listOf(1, 2, 3)
      verify(list) {
        contains(2)
      }
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlocks#spec has methods with the same signature as the with target object")
  fun `spec has methods with the same signature as the with target object`() {
    given
    val list = listOf(1, 2, 3)

    expect
    size() == 42 // WithBlocks::size
    contains(4) // WithBlocks::contains
    verify(list) {
      size == 3 // list::size
      contains(3) // list::contains
      this@WithBlocks.contains(4) // WithBlocks::contains
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlocks#with works with void methods")
  fun `with works with void methods`() {
    given
    val person = WithBlocksPerson()

    expect
    person.check()
    checkCondition()
    verify(person) {
      check()
      checkCondition()
      verifyAll {
        check()
        checkCondition()
      }
    }
  }

  private fun size() = 42

  private fun contains(value: Any?) = value == 4

  private fun checkCondition() {
    assert(true)
  }
}
