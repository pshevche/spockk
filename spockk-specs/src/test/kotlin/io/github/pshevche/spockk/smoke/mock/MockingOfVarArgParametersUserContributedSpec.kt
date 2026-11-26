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
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.PendingFeature
import spock.lang.Specification

class MockingOfVarArgParametersUserContributedSpec : Specification() {
  private var methodWasCalled = false

  private val target: TargetClass = Mock(TargetClass::class.java)

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.MockingOfVarArgParametersUserContributedSpec#1-arg call on method supporting >= 1 params is matched with (_)")
  fun `1-arg call on method supporting greater or equal 1 params is matched with any`() {
    given
    target.oneOrGreater(any()) does { methodWasCalled = true }

    `when`
    target.oneOrGreater("one")

    then
    methodWasCalled
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.MockingOfVarArgParametersUserContributedSpec#1-arg call on method supporting >= 0 params is matched with (_)")
  fun `1-arg call on method supporting greater or equal 0 params is matched with any`() {
    given
    target.zeroOrGreater(any()) does { methodWasCalled = true }

    `when`
    target.zeroOrGreater("one")

    then
    methodWasCalled
  }

  interface TargetClass {
    fun oneOrGreater(first: String, vararg rest: String)

    fun zeroOrGreater(vararg all: String)
  }
}
