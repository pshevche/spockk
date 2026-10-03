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

package io.github.pshevche.spockk.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import spock.lang.PendingFeature
import spock.lang.Specification

class MockMapDelegateSpec : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/673")
  @MigratedFrom("org.spockframework.mock.MockMapDelegateSpec#Mock a Map delegate Issue #1145")
  fun `Mock a Map delegate Issue #1145`() {
    given
    val ctx: MapDelegate = Mock()

    expect
    ctx.size == 0
    ctx["foo"] == null
  }
}

open class MapDelegate(target: Map<String, Any?>) : Map<String, Any?> by target
