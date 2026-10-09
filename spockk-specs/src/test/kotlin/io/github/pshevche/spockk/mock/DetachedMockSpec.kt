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
import io.github.pshevche.spockk.lang.returned
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.MockImplementation
import org.spockframework.mock.MockNature
import org.spockframework.mock.MockUtil
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class DetachedMockSpec : Specification() {

  @Shared
  private val mockUtil = MockUtil()

  @Shared
  private var listMock: List<Any?>? = null

  fun setupSpec() {
    @Suppress("UNCHECKED_CAST")
    listMock = mockUtil.createDetachedMock(
      "listMock",
      List::class.java,
      MockNature.MOCK,
      MockImplementation.JAVA,
      emptyMap(),
      javaClass.classLoader
    ) as List<Any?>
  }

  fun setup() {
    mockUtil.attachMock(listMock, this)
  }

  fun cleanup() {
    mockUtil.detachMock(listMock)
  }

  @MigratedFrom("org.spockframework.mock.DetachedMockSpec#Mock returns default answer")
  fun `Mock returns default answer`() {
    expect
    listMock!!.size == 0
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/670")
  @MigratedFrom("org.spockframework.mock.DetachedMockSpec#Configure and test mock")
  fun `Configure and test mock`() {
    `when`
    assert(listMock!!.size == 1)

    then
    1 * listMock!!.size returned 1
  }

  @MigratedFrom("org.spockframework.mock.DetachedMockSpec#Mock returns default answer after being configured and reattached")
  fun `Mock returns default answer after being configured and reattached`() {
    expect
    listMock!!.size == 0
  }
}
