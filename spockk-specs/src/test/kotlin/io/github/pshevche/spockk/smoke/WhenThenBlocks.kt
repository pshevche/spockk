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
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

class WhenThenBlocks : Specification() {

  @MigratedFrom("org.spockframework.smoke.WhenThenBlocks#basic usage")
  fun `basic usage`() {
    var x = 0

    `when`
    x = 1

    then
    x == 1
  }

  @MigratedFrom("org.spockframework.smoke.WhenThenBlocks#named blocks")
  fun `named blocks`() {
    var x = 0

    `when`("x is assigned 1")
    x = 1

    then("x is 1")
    x == 1
  }

  @MigratedFrom("org.spockframework.smoke.WhenThenBlocks#and-ed blocks")
  fun `and-ed blocks`() {
    var x = 0
    var y = 0

    `when`
    x = 1

    and
    y = 2

    then
    x == 1

    and("y is 2")
    y == 2
  }

  @MigratedFrom("org.spockframework.smoke.WhenThenBlocks#multiple when-thens")
  fun `multiple when-thens`() {
    var x = 0

    `when`
    x = 1

    then
    x == 1

    `when`
    x = 2

    then
    x == 2
  }

  @MigratedFrom("org.spockframework.smoke.WhenThenBlocks#full house")
  fun `full house`() {
    var x = 0
    var y = 0

    `when`("blah")
    x = 1

    and("blah")
    y = 2

    then("blah")
    x == 1

    and("blah")
    y == 2

    `when`
    x = 42

    then
    x == 42

    and
    y == 2
  }
}
