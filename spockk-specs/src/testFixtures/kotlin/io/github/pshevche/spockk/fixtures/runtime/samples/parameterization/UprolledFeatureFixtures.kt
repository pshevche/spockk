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

package io.github.pshevche.spockk.fixtures.runtime.samples.parameterization

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import spock.lang.Rollup
import spock.lang.Specification
import spock.lang.Unroll

class UprollActor {
  val details: Map<String, Any> = mapOf("name" to "fred", "age" to 30)
}

class RollupSimpleFixture : Specification() {
  @Rollup
  fun foo(x: Int) {
    expect
    true

    where
    variable(x).from(1, 2, 3)
  }
}

class UnrolledEmptyProviderFixture : Specification() {
  fun foo(x: Int) {
    expect
    true

    where
    variable(x).from(emptyList<Int>())
  }
}

class RolledEmptyProviderFixture : Specification() {
  @Rollup
  fun foo(x: Int) {
    expect
    true

    where
    variable(x).from(emptyList<Int>())
  }
}

// Upstream names these features "#actor.details.name" and "#actor.details.age"; a dot is not allowed in a
// JVM method name, see https://github.com/pshevche/spockk/issues/1103.
@Rollup
class RollupWholeClassFixture : Specification() {
  fun `#actorName`(actor: UprollActor) {
    expect
    true

    where
    variable(actor).from(listOf(UprollActor()))
  }

  fun `not data-driven`() {
    expect
    true
  }

  fun `#actorAge`(actor: UprollActor) {
    expect
    true

    where
    variable(actor).from(listOf(UprollActor()))
  }
}

@Unroll("#actor.details.name")
class RollupMethodOverUnrollClassFixture : Specification() {
  @Rollup
  fun method(actor: UprollActor) {
    expect
    true

    where
    variable(actor).from(listOf(UprollActor()))
  }
}

@Unroll("#actor.details.name")
@Rollup
class UnrollAndRollupOnSpecFixture : Specification() {
  fun method(actor: UprollActor) {
    expect
    true

    where
    variable(actor).from(listOf(UprollActor()))
  }
}

class UnrollAndRollupOnFeatureFixture : Specification() {
  @Unroll("#actor.details.name")
  @Rollup
  fun method(actor: UprollActor) {
    expect
    true

    where
    variable(actor).from(listOf(UprollActor()))
  }
}

class RollupMethodFixture : Specification() {
  @Rollup
  fun method(actor: UprollActor) {
    expect
    true

    where
    variable(actor).from(listOf(UprollActor()))
  }
}

@Rollup
class RollupClassFixture : Specification() {
  fun method(actor: UprollActor) {
    expect
    true

    where
    variable(actor).from(listOf(UprollActor()))
  }
}
