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
import spock.lang.IgnoreRest
import spock.lang.Specification

open class IgnoreRestBaseSpec1 : Specification() {
  @IgnoreRest
  fun `feature1`() {
    expect
    assert(true)
  }

  fun `feature2`() {
    expect
    assert(true)
  }
}

class IgnoreRestDerivedSpec1 : IgnoreRestBaseSpec1() {
  fun `feature3`() {
    expect
    assert(true)
  }

  fun `feature4`() {
    expect
    assert(false)
  }
}

open class IgnoreRestBaseSpec2 : Specification() {
  fun `feature1`() {
    expect
    assert(false)
  }

  fun `feature2`() {
    expect
    assert(true)
  }
}

class IgnoreRestDerivedSpec2 : IgnoreRestBaseSpec2() {
  fun `feature3`() {
    expect
    assert(true)
  }

  @IgnoreRest
  fun `feature4`() {
    expect
    assert(true)
  }
}

open class IgnoreRestBaseSpec3 : Specification() {
  @IgnoreRest
  fun `feature1`() {
    expect
    assert(true)
  }

  fun `feature2`() {
    expect
    assert(true)
  }
}

class IgnoreRestDerivedSpec3 : IgnoreRestBaseSpec3() {
  fun `feature3`() {
    expect
    assert(true)
  }

  @IgnoreRest
  fun `feature4`() {
    expect
    assert(true)
  }
}
