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

package io.github.pshevche.spockk.smoke.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.returns
import spock.lang.PendingFeature
import spock.lang.Specification

class GlobalInteractions : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/670")
  @MigratedFrom("org.spockframework.smoke.mock.GlobalInteractions#optional interaction")
  fun `optional interaction`() {
    val m = Mock(List::class.java) {
      size returns 1
    }

    expect
    m.size == 1
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/670")
  @MigratedFrom("org.spockframework.smoke.mock.GlobalInteractions#optional interaction defined in helper method")
  fun `optional interaction defined in helper method`() {
    val m = optional()

    expect
    m.size == 1
  }

  private fun optional(): List<*> {
    val m = Mock(List::class.java) {
      size returns 1
    }
    return m
  }
}
