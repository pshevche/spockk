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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.TooManyInvocationsError
import spock.lang.PendingFeature
import spock.lang.Specification

class OverlappingInteractions : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.OverlappingInteractions#invocations are spread over identical interactions")
  fun `invocations are spread over identical interactions`() {
    val list = Mock(MutableList::class.java) as MutableList<Int>

    `when`
    list.add(1)
    list.add(2)
    list.add(2)
    list.add(1)

    then
    1 * list.add(1)
    2 * list.add(2)
    1 * list.add(1)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom(
    "org.spockframework.smoke.mock.OverlappingInteractions#invocations are not spread over overlapping interactions in different scopes"
  )
  fun `invocations are not spread over overlapping interactions in different scopes`() {
    val list = Mock(MutableList::class.java) as MutableList<Int>

    1 * list.add(1)

    `when`
    list.add(1)

    then
    0 * list.add(1)
    thrown(TooManyInvocationsError::class.java)
  }
}
