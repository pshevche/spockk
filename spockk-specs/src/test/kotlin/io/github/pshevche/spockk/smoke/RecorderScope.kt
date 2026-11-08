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

private class RecorderScopePerson(val name: String = "Fred", val age: Int = 42)

class RecorderScope : Specification() {

  @MigratedFrom("org.spockframework.smoke.RecorderScope#assert within closure")
  fun `assert within closure`() {
    given
    val sq = { x: Int ->
      assert(1 == 1)
      x * x
    }

    expect
    sq(2) == 4
  }

  @MigratedFrom("org.spockframework.smoke.RecorderScope#assert within external closure")
  fun `assert within external closure`() {
    given
    val sq = externalClosure()

    expect
    sq(2) == 4
  }

  @MigratedFrom("org.spockframework.smoke.RecorderScope#in with blocks")
  fun `in with blocks`() {
    given
    val person = RecorderScopePerson()

    expect
    person.age == 42
    verify(person) {
      name == "Fred"
      age == 42
    }
  }

  @MigratedFrom("org.spockframework.smoke.RecorderScope#method condition")
  fun `method condition`() {
    expect
    1 == 1
    verify(listOf(1, 2)) {
      size == 2
    }
  }

  @MigratedFrom("org.spockframework.smoke.RecorderScope#in verifyAll blocks")
  fun `in verifyAll blocks`() {
    given
    val p = RecorderScopePerson()

    expect
    verifyAll(p) {
      name == "Fred"
      age == 42
    }
  }

  private fun externalClosure(): (Int) -> Int = { x ->
    assert(1 == 1)
    x * x
  }
}
