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
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class MethodExecutionOrder : Specification() {

  @Shared
  private val order = mutableListOf<Any>()

  fun cleanup() {
    order.add("c")
  }

  fun cleanupSpec() {
    assert(
      order == listOf("ss", "s", 3, "c", "s", 4, "c", "s", 5, "c", "s", 6, "c", "s", 7, "c", "s", 8, "c", "s", 9, "c", "s", 10, "c")
    )
  }

  @MigratedFrom("org.spockframework.smoke.MethodExecutionOrder#help, I need somebody")
  fun `help, I need somebody`() {
    order.add("h")
  }

  @MigratedFrom("org.spockframework.smoke.MethodExecutionOrder#first feature")
  fun `first feature`() {
    order.add(3)
    expect
    true
  }

  fun secondFeatureWithStandardName() {
    order.add(4)
    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.MethodExecutionOrder#just helping out")
  fun `just helping out`() {
    order.add("h")
  }

  @MigratedFrom("org.spockframework.smoke.MethodExecutionOrder#strangely named feature")
  fun `strangely named feature`() {
    order.add(5)
    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.MethodExecutionOrder#very very long name but still nothing but a feature")
  fun `very very long name but still nothing but a feature`() {
    order.add(6)
    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.MethodExecutionOrder#short one")
  fun `short one`() {
    order.add(7)
    var x = "blah"
    `when`
    x = x.reversed()

    then
    x == "halb"
  }

  fun setup() {
    order.add("s")
  }

  @MigratedFrom("org.spockframework.smoke.MethodExecutionOrder#helpless")
  fun helpless() {
    order.add("h")
  }

  fun setupSpec() {
    order.add("ss")
  }

  @MigratedFrom("org.spockframework.smoke.MethodExecutionOrder#parameterized feature")
  fun `parameterized feature`(x: Int) {
    order.add(x)
    expect
    true

    where
    variable(x).from(8, 9, 10)
  }
}
