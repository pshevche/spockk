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

package io.github.pshevche.spockk.fixtures.runtime.samples.ignore

import io.github.pshevche.spockk.lang.expect
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class IgnoredSpec : Specification() {

  fun foo() {
    expect
    assert(false)
  }

  fun bar() {
    expect
    assert(false)
  }
}

class IgnoredFeaturesSpec : Specification() {

  @Ignore
  fun ignored() {
    expect
    assert(false)
  }

  fun `not ignored`() {
    expect
    assert(true)
  }

  @Ignore
  fun `also ignored`() {
    expect
    assert(false)
  }
}

class IgnoredFixtureMethodSpec : Specification() {

  @Ignore
  fun setup() {}

  fun feature() {
    expect
    assert(true)
  }
}

open class IgnoredFeaturesBase : Specification() {

  @Ignore
  fun feature1() {
    expect
    assert(false)
  }

  fun feature2() {
    expect
    assert(true)
  }
}

class IgnoredFeaturesDerived : IgnoredFeaturesBase() {

  fun feature3() {
    expect
    assert(true)
  }

  @Ignore
  fun feature4() {
    expect
    assert(false)
  }
}

@Ignore
open class IgnoredBaseSpec : Specification() {

  fun feature1() {
    expect
    assert(true)
  }
}

class NotIgnoredDerivedSpec : IgnoredBaseSpec() {

  fun feature2() {
    expect
    assert(true)
  }
}

open class InheritableBaseSpec : Specification() {

  fun `base feature`() {
    expect
    assert(true)
  }
}

@Ignore(inherited = false)
open class NonInheritedIgnoreFoo : InheritableBaseSpec() {

  fun `foo feature`() {
    expect
    assert(true)
  }
}

open class NonInheritedIgnoreBar : NonInheritedIgnoreFoo() {

  fun `bar feature`() {
    expect
    assert(true)
  }
}

class NonInheritedIgnoreTest : NonInheritedIgnoreBar() {

  fun `test feature`() {
    expect
    assert(true)
  }
}

@Ignore(inherited = true)
open class InheritedIgnoreFoo : InheritableBaseSpec() {

  fun `foo feature`() {
    expect
    assert(true)
  }
}

open class InheritedIgnoreBar : InheritedIgnoreFoo() {

  fun `bar feature`() {
    expect
    assert(true)
  }
}

class InheritedIgnoreTest : InheritedIgnoreBar() {

  fun `test feature`() {
    expect
    assert(true)
  }
}
