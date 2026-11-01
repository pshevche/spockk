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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.PendingFeature
import spock.lang.Specification

class JavaStubBasics : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubBasics#can stub interface")
  fun `can stub interface`() {
    given
    val list = Mock(MutableList::class.java as Class<MutableList<String>>)
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubBasics#can stub class")
  fun `can stub class`() {
    given
    val list = Mock(ArrayList::class.java as Class<ArrayList<String>>)
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaStubBasics#can stub call at creation time")
  fun `can stub call at creation time`() {
    given
    val list = Mock(MutableList::class.java as Class<MutableList<String>>) {
      get(42) returns "foo"
    }

    expect
    list.get(42) == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubBasics#can stub call beforehand")
  fun `can stub call beforehand`() {
    given
    val list = Mock(MutableList::class.java as Class<MutableList<String>>)
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaStubBasics#can stub call in then-block")
  fun `can stub call in then-block`() {
    given
    val list = Mock(MutableList::class.java as Class<MutableList<String>>)

    `when`
    val result = list.get(42)

    then
    list.get(42) returns "foo"
    result == "foo"
  }
}
