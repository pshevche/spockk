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
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.MockUtil
import spock.lang.Specification
import spock.mock.DetachedMockFactory

class DetachedMockFactorySpec : Specification() {

  private val factory = DetachedMockFactory()

  @MigratedFrom("org.spockframework.mock.DetachedMockFactorySpec#Mock(class)")
  fun `Mock(class)`() {
    given
    val mock = factory.Mock(IMockMe::class.java)
    attach(mock)

    `when`
    mock.foo(2)

    then
    1 * mock.foo(2)
    mockName(mock) == "IMockMe"

    cleanup
    detach(mock)
  }

  @MigratedFrom("org.spockframework.mock.DetachedMockFactorySpec#Mock(options, class)")
  fun `Mock(options, class)`() {
    given
    val mock = factory.Mock(mapOf("name" to "customName"), IMockMe::class.java)
    attach(mock)

    `when`
    mock.foo(2)

    then
    1 * mock.foo(2)
    mockName(mock) == "customName"

    cleanup
    detach(mock)
  }

  @MigratedFrom("org.spockframework.mock.DetachedMockFactorySpec#Stub(class)")
  fun `Stub(class)`() {
    given
    val stub = factory.Stub(IMockMe::class.java)
    attach(stub)

    expect
    stub.foo(2) returns 4
    stub.foo(2) == 4
    stub.foo(1) == 0
    mockName(stub) == "IMockMe"

    cleanup
    detach(stub)
  }

  @MigratedFrom("org.spockframework.mock.DetachedMockFactorySpec#Stub(options, class)")
  fun `Stub(options, class)`() {
    given
    val stub = factory.Stub(mapOf("name" to "customName"), IMockMe::class.java)
    attach(stub)

    expect
    stub.foo(2) returns 4
    stub.foo(2) == 4
    stub.foo(1) == 0
    mockName(stub) == "customName"

    cleanup
    detach(stub)
  }

  @MigratedFrom("org.spockframework.mock.DetachedMockFactorySpec#Spy(class)")
  fun `Spy(class)`() {
    given
    val spy: IMockMe = factory.Spy(MockMe::class.java)
    attach(spy)

    `when`
    val result = spy.foo(2)

    then
    result == 1
    1 * spy.foo(2)

    cleanup
    detach(spy)
  }

  @MigratedFrom("org.spockframework.mock.DetachedMockFactorySpec#Spy(options, class)")
  fun `Spy(options, class)`() {
    given
    val spy: IMockMe = factory.Spy(mapOf("constructorArgs" to listOf(42)), MockMe::class.java)
    attach(spy)

    `when`
    val result = spy.foo(2)

    then
    result == 42
    1 * spy.foo(2)

    cleanup
    detach(spy)
  }

  @MigratedFrom("org.spockframework.mock.DetachedMockFactorySpec#Spy(obj)")
  fun `Spy(obj)`() {
    given
    val spy: IMockMe = factory.Spy(MockMe(42))
    attach(spy)

    `when`
    val result = spy.foo(2)

    then
    result == 42
    1 * spy.foo(2)

    cleanup
    detach(spy)
  }

  private fun mockName(mock: Any): String? = MockUtil().asMock(mock).name

  private fun attach(mock: Any) {
    MockUtil().attachMock(mock, this)
  }

  private fun detach(mock: Any?) {
    if (mock != null) {
      MockUtil().detachMock(mock)
    }
  }
}

interface IMockMe {
  fun foo(i: Int): Int
}

open class MockMe() : IMockMe {
  private var defaultAnswer: Int = 1

  constructor(defaultAnswer: Int) : this() {
    this.defaultAnswer = defaultAnswer
  }

  override fun foo(i: Int): Int = defaultAnswer
}
