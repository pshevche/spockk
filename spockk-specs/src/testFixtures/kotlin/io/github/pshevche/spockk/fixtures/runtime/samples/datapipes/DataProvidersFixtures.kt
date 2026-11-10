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

object CurrentUnequalProviders {
  private val holder = ThreadLocal<Triple<List<Int>, List<Int>, List<Int>>>()

  fun set(xs: List<Int>, ys: List<Int>, zs: List<Int>) = holder.set(Triple(xs, ys, zs))

  fun xs(): List<Int> = holder.get().first

  fun ys(): List<Int> = holder.get().second

  fun zs(): List<Int> = holder.get().third
}

class UnequalProvidersFixture : Specification() {
  fun `a feature`(x: Int, y: Int, z: Int) {
    expect
    x == y

    where
    variable(x).from(CurrentUnequalProviders.xs())
    variable(y).from(CurrentUnequalProviders.ys())
    variable(z).from(CurrentUnequalProviders.zs())
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
