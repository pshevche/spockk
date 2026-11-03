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
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import spock.lang.PendingFeature
import spock.lang.Specification

class ValidMockCreation : Specification() {

  var list1a: List<Any?>? = null
  val list2: List<Any?> = Mock(ArrayList::class.java)
  var list2a: List<Any?>? = null
  val list3 = Mock(List::class.java)

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/694")
  @MigratedFrom(
    "org.spockframework.smoke.mock.ValidMockCreation#field typed w/ mock untyped, separate assignment"
  )
  fun `field typed w- mock untyped, separate assignment`() {
    list1a = Mock()

    expect
    list1a is List<*>
  }

  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreation#field typed w/ mock typed")
  fun `field typed w- mock typed`() {
    expect
    list2 is ArrayList<*>
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.ValidMockCreation#field typed w/ mock typed, separate assignment"
  )
  fun `field typed w- mock typed, separate assignment`() {
    list2a = Mock(ArrayList::class.java)

    expect
    list2a is ArrayList<*>
  }

  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreation#field untyped w/ mock typed")
  fun `field untyped w- mock typed`() {
    expect
    list3 is List<*>
  }

  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreation#local typed w/ mock untyped")
  fun `local typed w- mock untyped`() {
    val list: List<Any?> = Mock()

    expect
    list is List<*>
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.ValidMockCreation#local typed with w/ mock untyped, separate assignment"
  )
  fun `local typed with w- mock untyped, separate assignment`() {
    val list: List<Any?>
    list = Mock()

    expect
    list is List<*>
  }

  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreation#local typed w/ mock typed")
  fun `local typed w- mock typed`() {
    val list: List<Any?> = Mock(ArrayList::class.java)

    expect
    list is ArrayList<*>
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.ValidMockCreation#local typed w/ mock typed, separate assignment"
  )
  fun `local typed w- mock typed, separate assignment`() {
    val list: List<Any?>
    list = Mock(ArrayList::class.java)

    expect
    list is ArrayList<*>
  }

  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreation#local untyped w/ mock typed")
  fun `local untyped w- mock typed`() {
    val list = Mock(List::class.java)

    expect
    list is List<*>
  }

  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreation#expr typed")
  fun `expr typed`() {
    expect
    Mock(List::class.java) is List<*>
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1037")
  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreation#creation in nested expr")
  fun `creation in nested expr`() {
    var list: Any? = null
    if (true) list = id(id(Mock(List::class.java)))

    expect
    list is List<*>
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.ValidMockCreation#creation in interaction result")
  fun `creation in interaction result`() {
    given
    val list: List<Any?> = Mock()
    list.get(any()) returns Mock(Map::class.java)

    expect
    list.get(42) is Map<*, *>
  }

  private fun id(arg: Any?): Any? = arg
}
