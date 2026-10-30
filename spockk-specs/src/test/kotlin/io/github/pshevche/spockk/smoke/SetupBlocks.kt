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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.setup
import spock.lang.Specification

class SetupBlocks : Specification() {

  @MigratedFrom("org.spockframework.smoke.SetupBlocks#implicit")
  fun `implicit`() {
    val a = 1
  }

  @MigratedFrom("org.spockframework.smoke.SetupBlocks#blocks are executed")
  fun `blocks are executed`() {
    var a = 1
    setup
    a += 1
    and("blah")
    a += 2

    expect
    a == 4
  }

  @MigratedFrom("org.spockframework.smoke.SetupBlocks#given is alias for setup")
  fun `given is alias for setup`() {
    val a: Int
    given
    a = 1

    expect
    a == 1
  }
}
