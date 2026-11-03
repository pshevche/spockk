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
import io.github.pshevche.spockk.lang.expect
import org.spockframework.mock.MockUtil
import spock.lang.PendingFeature
import spock.lang.Specification

class ValidMockCreationInDerivedClass : ValidMockCreationInDerivedClassBase() {

  var map: Map<*, *> = Mock(Map::class.java)

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1040")
  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreationInDerivedClass#all mocks have been created")
  fun `all mocks have been created`() {
    expect
    MockUtil().isMock(list)
    MockUtil().isMock(map)
  }
}

abstract class ValidMockCreationInDerivedClassBase : Specification() {
  var list: List<*> = Mock(List::class.java)
}
