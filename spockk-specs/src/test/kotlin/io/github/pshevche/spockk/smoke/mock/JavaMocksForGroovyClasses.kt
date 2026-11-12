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
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.PendingFeature
import spock.lang.Specification

class JavaMocksForGroovyClasses : Specification() {

  val mockMe: MockMe = Mock(MockMe::class.java)
  val accessorMock: MockMeAccessors = Mock(MockMeAccessors::class.java)

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#mock declared method")
  fun `mock declared method`() {
    `when`
    val result = mockMe.foo(42)

    then
    1 * mockMe.foo(42) returns "result"
    result == "result"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#mock declared property that is read with property syntax")
  fun `mock declared property that is read with property syntax`() {
    `when`
    val value = mockMe.bar

    then
    1 * mockMe.bar returns "value"
    value == "value"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#mock declared property that is read with method syntax")
  fun `mock declared property that is read with method syntax`() {
    `when`
    val value = accessorMock.getBar()

    then
    1 * accessorMock.getBar() returns "value"
    value == "value"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#mock declared property that is written with method syntax")
  fun `mock declared property that is written with method syntax`() {
    `when`
    accessorMock.setBar("value")

    then
    1 * accessorMock.setBar("value")
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#test setter on interface")
  fun `test setter on interface`() {
    given
    val sut: IGMock = Mock()

    `when`
    sut.setFoo("val")

    then
    1 * sut.setFoo(any())
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#test getter on interface")
  fun `test getter on interface`() {
    given
    val sut: IGMock = Mock()

    `when`
    sut.getFoo()

    then
    1 * sut.getFoo()
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#test setter on abstract")
  fun `test setter on abstract`() {
    given
    val sut: AGMock = Mock()

    `when`
    sut.setFoo("val")

    then
    1 * sut.setFoo(any())
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#test getter on abstract")
  fun `test getter on abstract`() {
    given
    val sut: AGMock = Mock()

    `when`
    sut.getFoo()

    then
    1 * sut.getFoo()
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#test setter on concrete")
  fun `test setter on concrete`() {
    given
    val sut: GMock = Mock()

    `when`
    sut.setFoo("val")

    then
    1 * sut.setFoo(any())
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#test getter on concrete")
  fun `test getter on concrete`() {
    given
    val sut: GMock = Mock()

    `when`
    sut.getFoo()

    then
    1 * sut.getFoo()
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMocksForGroovyClasses#Mock object boolean accessor via method")
  fun `Mock object boolean accessor via method`() {
    given
    val mockData: ExampleData = Mock()

    `when`
    val result = if (mockData.isCurrent()) "is enabled" else "is not enabled"

    then
    1 * mockData.isCurrent() returns true

    and
    result == "is enabled"
  }
}

open class MockMe {
  open fun foo(i: Int): String? = null

  open var bar: String? = null
}

open class MockMeAccessors {
  open fun getBar(): String? = null

  open fun setBar(value: String?) {}
}

interface IGMock {
  fun getFoo(): String?

  fun setFoo(value: String?)
}

abstract class AGMock : IGMock

open class GMock : AGMock() {
  private var prop: String? = null

  override fun getFoo(): String? = prop

  override fun setFoo(value: String?) {
    prop = value
  }
}

open class ExampleData {
  open fun isCurrent(): Boolean = false
}
