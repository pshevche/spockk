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
import io.github.pshevche.spockk.lang.anyMethod
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.Issue
import spock.lang.Specification

class WildcardUsages : Specification() {

  private companion object {
    @Suppress("UNCHECKED_CAST")
    val LIST_TYPE = MutableList::class.java as Class<MutableList<Any?>>
  }

  @MigratedFrom("org.spockframework.smoke.mock.WildcardUsages#match any argument")
  fun `match any argument`() {
    val list = Mock(MutableList::class.java)

    `when`
    list.subList(1, 1)
    list.subList(2, 1)
    list.subList(3, 2)

    then
    2 * list.subList(any(), 1)
  }

  @MigratedFrom("org.spockframework.smoke.mock.WildcardUsages#match any method name and argument list")
  fun `match any method name and argument list`() {
    val list = Mock(MutableList::class.java)

    `when`
    list.clear()
    list.get(1)
    list.subList(3, 5)

    then
    3 * list.anyMethod()
  }

  @Issue("https://github.com/spockframework/spock/issues/460")
  @MigratedFrom(
    "org.spockframework.smoke.mock.WildcardUsages#wildcard method name doesn't match Object's equals(), hashCode(), toString(), and finalize() methods"
  )
  fun `wildcard method name doesn't match Object's equals(), hashCode(), toString(), and finalize() methods`() {
    val list = Mock(ArrayList::class.java)

    `when`
    list.equals(Any())
    list.hashCode()
    list.toString()

    then
    0 * list.anyMethod()
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.WildcardUsages#wildcard method name matches overloaded equals(), hashCode(), toString(), and finalize() methods"
  )
  fun `wildcard method name matches overloaded equals(), hashCode(), toString(), and finalize() methods`() {
    val overloaded = Mock(Overloaded::class.java)

    `when`
    overloaded.equals("me")
    overloaded.hashCode(emptyList<Any?>())
    overloaded.toString(true)
    overloaded.finalize(true)

    then
    4 * overloaded.anyMethod()
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.WildcardUsages#usage in interaction doesn't interfere with usage in where-block"
  )
  fun `usage in interaction doesn't interfere with usage in where-block`(ignored: Int, type: Class<MutableList<Any?>>) {
    val list = Mock(type)

    `when`
    list.add(1)

    then
    1 * list.add(any())

    where
    variables(ignored, type).from(listOf(123), listOf(LIST_TYPE))
  }
}

interface Overloaded {
  fun equals(str: String): Boolean

  fun hashCode(list: List<*>): Int

  fun toString(verbose: Boolean): String

  fun finalize(now: Boolean)
}
