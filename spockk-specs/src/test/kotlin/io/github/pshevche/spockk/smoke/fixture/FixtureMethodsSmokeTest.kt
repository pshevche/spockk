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

package io.github.pshevche.spockk.smoke.fixture

import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification
import kotlin.test.assertEquals

class FixtureMethodsSmokeTest : Specification() {

  companion object {
    private val log = mutableListOf<String>()
  }

  fun setupSpec() {
    log.add("setupSpec")
  }

  fun setup() {
    log.add("setup")
  }

  fun cleanup() {
    log.add("cleanup")
  }

  fun cleanupSpec() {
    log.add("cleanupSpec")
  }

  fun `first feature`() {
    expect
    assertEquals(listOf("setupSpec", "setup"), log)

    cleanup
    log.add("feature1")
  }

  fun `second feature`() {
    expect
    assertEquals(listOf("setupSpec", "setup", "feature1", "cleanup", "setup"), log)

    cleanup
    log.add("feature2")
  }
}
