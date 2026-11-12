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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.returned
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.PendingFeature
import spock.lang.Specification

@Suppress("UNCHECKED_CAST")
private val listType = MutableList::class.java as Class<MutableList<Any?>>

@Suppress("UNCHECKED_CAST")
private val arrayListType = ArrayList::class.java as Class<ArrayList<Any?>>

class JavaMockBasics : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can stub interface")
  fun `can stub interface`() {
    val list = Mock(listType)
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can stub class")
  fun `can stub class`() {
    val list = Mock(arrayListType)
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can stub call at creation time")
  fun `can stub call at creation time`() {
    val list = Mock(listType) {
      get(42) returns "foo"
    }

    expect
    list.get(42) == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can stub call beforehand")
  fun `can stub call beforehand`() {
    val list = Mock(listType)
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can stub call in then-block")
  fun `can stub call in then-block`() {
    val list = Mock(listType)

    `when`
    val result = list.get(42)

    then
    list.get(42) returned "foo"
    result == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can mock interface")
  fun `can mock interface`() {
    val list = Mock(listType)

    `when`
    list.add(1)

    then
    1 * list.add(1)
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can mock class")
  fun `can mock class`() {
    val list = Mock(arrayListType)

    `when`
    list.add(1)

    then
    1 * list.add(1)
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can stub and mock call at creation time")
  fun `can stub and mock call at creation time`() {
    val list = Mock(listType) {
      1 * get(42) returned "foo"
    }

    expect
    list.get(42) == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can stub and mock call beforehand")
  fun `can stub and mock call beforehand`() {
    val list = Mock(listType)
    1 * list.get(42) returned "foo"

    expect
    list.get(42) == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can mock call in then-block")
  fun `can mock call in then-block`() {
    val list = Mock(listType)

    `when`
    list.get(42)

    then
    1 * list.get(42)
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#can stub and mock call in then-block")
  fun `can stub and mock call in then-block`() {
    val list = Mock(listType)

    `when`
    val result = list.get(42)

    then
    1 * list.get(42) returned "foo"
    result == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/994")
  @MigratedFrom("org.spockframework.smoke.mock.JavaMockBasics#using >>_ returns stubbed value")
  fun `using returns any returns stubbed value`() {
    val list = Mock(listType)

    `when`
    val result = list.get(42)
    val resultDefault = list.get(0)

    then
    1 * list.get(42) returned any()
    1 * list.get(0)
    result != null
    resultDefault == null
  }
}
