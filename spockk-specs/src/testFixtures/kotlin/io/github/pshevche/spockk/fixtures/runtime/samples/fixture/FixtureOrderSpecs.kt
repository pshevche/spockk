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

package io.github.pshevche.spockk.fixtures.runtime.samples.fixture

import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

object FixtureOrderTracker {
  val log = mutableListOf<String>()
}

open class FixtureOrderBase : Specification() {
  open fun setupSpec() {
    FixtureOrderTracker.log.add("ss1")
  }

  open fun setup() {
    FixtureOrderTracker.log.add("s1")
  }

  open fun cleanup() {
    FixtureOrderTracker.log.add("c1")
  }

  open fun cleanupSpec() {
    FixtureOrderTracker.log.add("cs1")
  }
}

class FixtureOrderDerived : FixtureOrderBase() {
  override fun setupSpec() {
    FixtureOrderTracker.log.add("ss2")
  }

  override fun setup() {
    FixtureOrderTracker.log.add("s2")
  }

  override fun cleanup() {
    FixtureOrderTracker.log.add("c2")
  }

  override fun cleanupSpec() {
    FixtureOrderTracker.log.add("cs2")
  }

  fun `some feature method`() {
    expect
    true
  }
}
