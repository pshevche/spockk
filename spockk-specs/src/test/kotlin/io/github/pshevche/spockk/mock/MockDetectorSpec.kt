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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.MockUtil
import spock.lang.PendingFeature
import spock.lang.Specification

class MockDetectorSpec : Specification() {

  private val detector = MockUtil()

  @MigratedFrom("org.spockframework.mock.MockDetectorSpec#detects interface based mocks")
  fun `detects interface based mocks`() {
    expect
    detector.isMock(Mock(List::class.java))
    !detector.isMock(emptyList<Any>())
  }

  @MigratedFrom("org.spockframework.mock.MockDetectorSpec#detects class based mocks")
  fun `detects class based mocks`() {
    expect
    detector.isMock(Mock(ArrayList::class.java))
    !detector.isMock(ArrayList<Any>())
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/658")
  @MigratedFrom("org.spockframework.mock.MockDetectorSpec#detects all natures of mock object")
  fun `detects all natures of mock object`() {
    expect
    detector.isMock(Mock(List::class.java))
    detector.isMock(Stub(List::class.java))
    detector.isMock(Spy(ArrayList::class.java))
    detector.isMock(GroovyMock(List::class.java))
    detector.isMock(GroovyStub(List::class.java))
    detector.isMock(GroovySpy(ArrayList::class.java))
  }

  @MigratedFrom("org.spockframework.mock.MockDetectorSpec#provides access to mock object information")
  fun `provides access to mock object information`() {
    given
    val list = Mock(List::class.java)
    val mock = detector.asMock(list)

    expect
    mock.name == "list"
    mock.type == List::class.java
    mock.instance == list
  }

  @MigratedFrom("org.spockframework.mock.MockDetectorSpec#complains if information about non-mock is requested")
  fun `complains if information about non-mock is requested`() {
    `when`
    detector.asMock(emptyList<Any>())

    then
    thrown(IllegalArgumentException::class.java)
  }
}
