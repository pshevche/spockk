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
import io.github.pshevche.spockk.lang.Spy
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.PendingFeature
import spock.lang.Specification
import java.util.regex.Pattern

open class SpyFoo(s: String?, verifier: SpyFoo?) {
  constructor(p: Pattern?, verifier: SpyFoo?) : this(null as String?, verifier) {
    verifier?.verify(Pattern::class.java)
  }

  private val verified = verifier?.verify(String::class.java)

  open fun verify(clazz: Any?) {}
}

class JavaSpyBasics : Specification() {

  private fun createArrayListDouble(): ArrayList<Any?> {
    val list = Spy(ArrayList::class.java) as ArrayList<Any?>
    repeat(42) { list.add(null) }
    list.add("foo")
    return list
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can stub interface")
  fun `can stub interface`() {
    given
    val list: MutableList<Any?> = createArrayListDouble()
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can stub class")
  fun `can stub class`() {
    given
    val list = createArrayListDouble()
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can stub call beforehand")
  fun `can stub call beforehand`() {
    given
    val list: MutableList<Any?> = createArrayListDouble()
    list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can stub call in then-block")
  fun `can stub call in then-block`() {
    given
    val list: MutableList<Any?> = createArrayListDouble()

    `when`
    val result = list.get(42)

    then
    list.get(42) returns "foo"
    result == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can mock interface")
  fun `can mock interface`() {
    given
    val list: MutableList<Any?> = createArrayListDouble()

    `when`
    list.add(1)

    then
    1 * list.add(1)
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can mock class")
  fun `can mock class`() {
    given
    val list = createArrayListDouble()

    `when`
    list.add(1)

    then
    1 * list.add(1)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can mock call beforehand")
  fun `can mock call beforehand`() {
    val list: MutableList<Any?> = createArrayListDouble()
    1 * list.get(42)

    `when`
    list.get(42)

    then
    noExceptionThrown()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can stub and mock call beforehand")
  fun `can stub and mock call beforehand`() {
    given
    val list: MutableList<Any?> = createArrayListDouble()
    1 * list.get(42) returns "foo"

    expect
    list.get(42) == "foo"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can mock call in then-block")
  fun `can mock call in then-block`() {
    given
    val list: MutableList<Any?> = createArrayListDouble()

    `when`
    list.get(42)

    then
    1 * list.get(42)
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can stub and mock call in then-block")
  fun `can stub and mock call in then-block`() {
    given
    val list: MutableList<Any?> = createArrayListDouble()

    `when`
    val result = list.get(42)

    then
    1 * list.get(42) returns "foo"
    result == "foo"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/994")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#using >>_ returns stubbed value")
  fun `using returns any returns stubbed value`() {
    given
    val list: MutableList<Any?> = createArrayListDouble()

    `when`
    val result = list.get(42)
    val resultDefault = list.get(0)

    then
    1 * list.get(42) returns any()
    1 * list.get(0)
    result != null
    resultDefault == null
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpyBasics#can disambiguate constructors with null arguments")
  fun `can disambiguate constructors with null arguments`() {
    given
    val verifier: SpyFoo = Mock()

    `when`
    Spy(mapOf("constructorArgs" to listOf(null as String?, verifier)), SpyFoo::class.java)

    then
    1 * verifier.verify(String::class.java)

    `when`
    Spy(mapOf("constructorArgs" to listOf(null as Pattern?, verifier)), SpyFoo::class.java)

    then
    1 * verifier.verify(Pattern::class.java)
  }
}
