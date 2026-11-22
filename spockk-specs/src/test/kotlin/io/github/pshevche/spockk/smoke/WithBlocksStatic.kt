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
import spock.lang.Specification

private class WithBlocksStaticPerson(
  val name: String = "Fred",
  val age: Int = 42,
  val spouse: WithBlocksStaticPerson? = null
)

/** Spockk's `verify` corresponds to Spock's `with`. */
class WithBlocksStatic : Specification() {

  @MigratedFrom("org.spockframework.smoke.WithBlocksStatic#can statically compile against type of the target")
  fun `can statically compile against type of the target`() {
    given
    val fred = WithBlocksStaticPerson(spouse = WithBlocksStaticPerson(name = "Wilma"))

    expect
    verify(fred.spouse!!) {
      name == "Wilma"
      age == 42
    }
  }

  @MigratedFrom("org.spockframework.smoke.WithBlocksStatic#can statically compile with explicit class declaration")
  fun `can statically compile with explicit class declaration`() {
    given
    val fred: Any = WithBlocksStaticPerson(spouse = WithBlocksStaticPerson(name = "Wilma"))

    expect
    verify(fred as WithBlocksStaticPerson) {
      name == "Fred"
      age == 42
    }
  }
}
