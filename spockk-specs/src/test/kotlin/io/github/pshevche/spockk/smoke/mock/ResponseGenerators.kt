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
import io.github.pshevche.spockk.lang.did
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returned
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.Issue
import spock.lang.Specification
import java.io.IOException
import java.util.concurrent.Callable

class ResponseGenerators : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.ResponseGenerators#return simple response")
  fun `return simple response`() {
    given
    val list = Mock(MutableList::class.java) as MutableList<Int>

    `when`
    val x0 = list.get(0)
    val x1 = list.get(0)

    then
    (0..Int.MAX_VALUE) * list.get(0) returned 42
    x0 == 42
    x1 == 42
  }

  @MigratedFrom("org.spockframework.smoke.mock.ResponseGenerators#return code response")
  fun `return code response`() {
    given
    val list = Mock(MutableList::class.java) as MutableList<Int>

    `when`
    val x0 = list.get(0)
    val x1 = list.get(0)

    then
    (0..Int.MAX_VALUE) * list.get(0) did { 42 }
    x0 == 42
    x1 == 42
  }

  @MigratedFrom("org.spockframework.smoke.mock.ResponseGenerators#access args with 'it' variable")
  fun `access args with it variable`() {
    given
    val list = Mock(MutableList::class.java) as MutableList<Int>
    var received: List<Any?> = emptyList()

    `when`
    list.add(3, 5)

    then
    1 * list.add(any(), any()) did { received = it }
    received[0] == 3
    received[1] == 5
  }

  @MigratedFrom("org.spockframework.smoke.mock.ResponseGenerators#access args with named variable")
  fun `access args with named variable`() {
    given
    val list = Mock(MutableList::class.java) as MutableList<Int>
    var argCount = -1

    `when`
    list.add(3, 5)

    then
    1 * list.add(any(), any()) did { args -> argCount = args.size }
    argCount == 2
  }

  @MigratedFrom("org.spockframework.smoke.mock.ResponseGenerators#access args with destructuring")
  fun `access args with destructuring`() {
    given
    val list = Mock(MutableList::class.java) as MutableList<Int>
    var from: Any? = null
    var to: Any? = null

    `when`
    list.add(3, 5)

    then
    1 * list.add(any(), any()) did { (f, t) ->
      from = f
      to = t
    }
    from == 3
    to == 5
  }

  @MigratedFrom("org.spockframework.smoke.mock.ResponseGenerators#access single arg without destructuring")
  fun `access single arg without destructuring`() {
    given
    val list = Mock(MutableList::class.java) as MutableList<Int>
    var received: List<Any?> = emptyList()

    `when`
    list.get(3)

    then
    1 * list.get(any()) did {
      received = it
      0
    }
    received == listOf(3)
  }

  @Issue("https://github.com/spockframework/spock/issues/288")
  @MigratedFrom("org.spockframework.smoke.mock.ResponseGenerators#exceptions thrown from code response generators aren't wrapped")
  fun `exceptions thrown from code response generators aren't wrapped`(exception: Throwable) {
    given
    val callable = Mock(Callable::class.java) {
      call() did { throw exception }
    }

    `when`
    val caught = KotlinCaller().call(callable)

    then
    caught === exception

    where
    variable(exception).from(RuntimeException(), IOException())
  }

  class KotlinCaller {
    fun call(callable: Callable<*>): Throwable? = try {
      callable.call()
      null
    } catch (t: Throwable) {
      t
    }
  }
}
