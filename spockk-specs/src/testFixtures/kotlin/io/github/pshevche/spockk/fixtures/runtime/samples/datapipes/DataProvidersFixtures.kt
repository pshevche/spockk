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

package io.github.pshevche.spockk.fixtures.runtime.samples.datapipes

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import spock.lang.Specification

class EmptyListProviderFixture : Specification() {
  fun `a feature`(x: Int) {
    expect
    true

    where
    variable(x).from(emptyList())
  }
}

class UnequalProvidersFixture1 : Specification() {
  fun `a feature`(x: Int, y: Int) {
    expect
    x == y

    where
    variable(x).from(1, 2)
    variable(y).from(1)
  }
}

class UnequalProvidersFixture2 : Specification() {
  fun `a feature`(x: Int, y: Int) {
    expect
    x == y

    where
    variable(x).from(1)
    variable(y).from(1, 2)
  }
}

class UnequalProvidersFixture3 : Specification() {
  fun `a feature`(x: Int, y: Int, z: Int) {
    expect
    x == y

    where
    variable(x).from(1)
    variable(y).from(1, 2)
    variable(z).from(1)
  }
}

class UnequalProvidersFixture4 : Specification() {
  fun `a feature`(x: Int, y: Int, z: Int) {
    expect
    x == y

    where
    variable(x).from(1, 2)
    variable(y).from(1)
    variable(z).from(1, 2)
  }
}

class ProviderWithoutValuesFixture : Specification() {
  fun `a feature`(x: Int, y: Int) {
    expect
    true

    where
    variable(x).from(listOf(1, 2, 3))
    variable(y).from(emptyList())
  }
}
