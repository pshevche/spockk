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

package io.github.pshevche.spockk.fixtures.runtime.samples.recorder

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.verify
import spock.lang.Specification

class NestedConditionsWithoutTopLevelConditionSpec : Specification() {

  fun foo() {
    expect
    // there must not be any top-level condition in this test
    verify(listOf(1, 2)) {
      size > 0
      forEach { assert(it > 0) }
    }
  }

  // not a feature (no block labels), so the failing asserts are never executed
  fun nestedAssert() {
    // there must not be any top-level condition in this test
    also {
      assert(false)
      also { assert(false) }
    }
  }
}
