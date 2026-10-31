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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.mock.TooFewInvocationsError
import spock.lang.FailsWith
import spock.lang.Issue
import spock.lang.Specification
import java.util.function.Consumer

class ArgumentMatching : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#match identical argument")
  fun `match identical argument`(arg: Any) {
    given
    val list = Mock(MutableList::class.java) as MutableList<Any?>

    `when`
    list.add(arg)

    then
    1 * list.add(arg)

    where
    variable(arg).from(1, "foo", Any())
  }

  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#match equal argument")
  fun `match equal argument`(arg1: Any?, arg2: Any?) {
    given
    val list = Mock(MutableList::class.java) as MutableList<Any?>

    `when`
    list.add(arg1)

    then
    1 * list.add(arg2)

    where
    variables(arg1, arg2).from(listOf(setOf(1, 2, 3), null), listOf(setOf(3, 2, 1), null))
  }

  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#match empty argument list")
  fun `match empty argument list`() {
    given
    val list = Mock(MutableList::class.java) as MutableList<Any?>

    `when`
    list.isEmpty()

    then
    1 * list.isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#allow list casting")
  @Issue("https://github.com/spockframework/spock/issues/1260")
  fun `allow list casting`() {
    given
    val consumer = Mock(Consumer::class.java) as Consumer<Any?>

    `when`
    consumer.accept(emptySet<Any>())

    then
    1 * consumer.accept(emptySet<Any>())
  }

  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#match named argument (empty args)")
  fun `match named argument (empty args)`() {
    given
    val na = Mock(NamedArgs::class.java)

    `when`
    na.m(emptyMap<Any, Any>())

    then
    1 * na.m(emptyMap<Any, Any>())
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.ArgumentMatching#match named argument (named args only, explicit map)"
  )
  fun `match named argument (named args only, explicit map)`() {
    given
    val na = Mock(NamedArgs::class.java)

    `when`
    na.m(mapOf("foo" to "bar"))

    then
    1 * na.m(mapOf("foo" to "bar"))
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.ArgumentMatching#match named argument (named args only, explicit map and positional arguments)"
  )
  fun `match named argument (named args only, explicit map and positional arguments)`() {
    given
    val na = Mock(NamedArgs::class.java)

    `when`
    na.m(mapOf("foo" to "bar"), "test")

    then
    1 * na.m(mapOf("foo" to "bar"), "test")
  }

  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#match empty (w/ varargs)")
  fun `match empty (with varargs)`() {
    given
    val varargs = Mock(Varargs2::class.java)

    `when`
    varargs.m()

    then
    1 * varargs.m()
  }

  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#match one (w/ varargs)")
  fun `match one (with varargs)`() {
    given
    val varargs = Mock(Varargs2::class.java)

    `when`
    varargs.m("one")

    then
    1 * varargs.m("one")
  }

  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#match multi (w/ varargs)")
  fun `match multi (with varargs)`() {
    given
    val varargs = Mock(Varargs2::class.java)

    `when`
    varargs.m("one", "two", "three", "four")

    then
    1 * varargs.m("one", "two", "three", "four")
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#expect fewer arguments than are provided")
  fun `expect fewer arguments than are provided`() {
    given
    val overloads = Mock(Overloads::class.java)

    `when`
    overloads.m("one")

    then
    1 * overloads.m()
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#expect more arguments than are provided")
  fun `expect more arguments than are provided`() {
    given
    val overloads = Mock(Overloads::class.java)

    `when`
    overloads.m()

    then
    1 * overloads.m("one")
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#expect fewer arguments than are provided (w/ varargs)")
  fun `expect fewer arguments than are provided (with varargs)`() {
    given
    val varargs = Mock(Varargs2::class.java)

    `when`
    varargs.m("one")

    then
    1 * varargs.m()
  }

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#expect more arguments than are provided (w/ varargs)")
  fun `expect more arguments than are provided (with varargs)`() {
    given
    val varargs = Mock(Varargs2::class.java)

    `when`
    varargs.m()

    then
    1 * varargs.m("one")
  }

  @Issue("https://github.com/spockframework/spock/issues/1322")
  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#expect non-matching Comparables")
  fun `expect non-matching Comparables`() {
    given
    val bar = Mock(ComparableExample::class.java)
    val baz = Mock(Comparable::class.java) as Comparable<Any?>
    val qux = Mock(Comparable::class.java) as Comparable<Any?>

    `when`
    bar.foo(baz)

    then
    1 * bar.foo(qux)
  }

  @Issue("https://github.com/spockframework/spock/issues/2352")
  @MigratedFrom("org.spockframework.smoke.mock.ArgumentMatching#expect non-matching generic Comparables to be unequal")
  fun `expect non-matching generic Comparables to be unequal`() {
    given
    val baz = Stub(GenericComparable::class.java)
    val qux = Stub(GenericComparable::class.java)

    `when`
    val result = baz != qux

    then
    result
  }

  interface Overloads {
    fun m()

    fun m(one: String)
  }

  interface ComparableExample {
    fun foo(arg: Comparable<*>)
  }

  abstract class GenericComparable : Comparable<GenericComparable>

  interface NamedArgs {
    fun m(args: Map<*, *>)

    fun m(args: Map<*, *>, a: String)
  }

  interface Varargs2 {
    fun m(vararg strings: String)
  }
}
