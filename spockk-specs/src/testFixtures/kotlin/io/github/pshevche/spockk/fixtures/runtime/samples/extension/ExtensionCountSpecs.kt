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

package io.github.pshevche.spockk.fixtures.runtime.samples.extension

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

@Stateful
@Stateless
class ExtensionCountASpec : Specification() {
  fun `a feature`() {
    expect
    assert(true)
  }
}

@Stateful
@Stateless
class ExtensionCountBSpec : Specification() {
  fun `b feature`() {
    expect
    assert(true)
  }
}

@Stateful
@Stateless
class ExtensionCountCSpec : Specification() {
  fun `c feature`() {
    expect
    assert(true)
  }
}

@Stateful
@Stateless
class ExtensionCountEverywhereSpec : Specification() {
  @field:Stateful
  @field:Stateless
  var a: Int = 0

  @field:Stateful
  @field:Stateless
  var b: Int = 0

  @Stateful
  @Stateless
  fun setupSpec() {}

  @Stateful
  @Stateless
  fun setup() {}

  @Stateful
  @Stateless
  fun `a feature`() {
    expect
    assert(true)
  }

  @Stateful
  @Stateless
  fun `b feature`() {
    expect
    assert(true)
  }
}
