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

abstract class FixtureMethodInheritanceBase : Specification() {

  companion object {
    val log = mutableListOf<String>()
  }

  fun setupSpec() {
    log.add("parent:setupSpec")
  }

  fun setup() {
    log.add("parent:setup")
  }

  fun cleanup() {
    log.add("parent:cleanup")
  }

  fun cleanupSpec() {
    log.add("parent:cleanupSpec")
  }
}

class FixtureMethodInheritanceSpec : FixtureMethodInheritanceBase() {

  fun `verifies parent fixture methods run`() {
    expect
    assert(log.contains("parent:setupSpec"))
    assert(log.contains("parent:setup"))
  }
}
