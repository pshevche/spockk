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
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

class InitializationOfUnsharedFields : Specification() {

  private var a: Any? = null
  private var b: Any? = 1
  private var c: Int = 0
  private var d: Int = 1

  fun setup() {
    assert(a == null)
    assert(b == 1)
    assert(c == 0)
    assert(d == 1)

    b = 2
  }

  @MigratedFrom("org.spockframework.smoke.InitializationOfUnsharedFields#feature 1")
  fun `feature 1`() {
    expect
    a == null
    b == 2
    c == 0
    d == 1

    cleanup
    a = 11111
    b = 11111
    c = 11111
    d = 11111
  }

  @MigratedFrom("org.spockframework.smoke.InitializationOfUnsharedFields#feature 2")
  fun `feature 2`() {
    expect
    a == null
    b == 2
    c == 0
    d == 1
  }
}
