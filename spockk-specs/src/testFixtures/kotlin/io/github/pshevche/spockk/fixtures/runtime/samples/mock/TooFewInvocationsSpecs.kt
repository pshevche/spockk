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

@file:Suppress("UNCHECKED_CAST")

package io.github.pshevche.spockk.fixtures.runtime.samples.mock

import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

interface TooFewInvocationsPerson {
  val name: String
  val age: Int

  fun wife(firstName: String, surname: String, age: Int, address: String): String
}

interface TooFewInvocationsWithDollar {
  fun compareTo(other: Any): Int

  fun `compareTo$`(other: Any): Int

  val foo: String

  @Suppress("ktlint:standard:property-naming")
  val `bar$`: String
}

class TooFewInvocationsDumpedPerson(val name: String, val age: Int)

class TooFewInvocationsOrderedBySimilaritySpec : Specification() {
  fun feature() {
    val list = Mock(MutableList::class.java) as MutableList<Any>
    val list2 = Mock(MutableList::class.java) as MutableList<Any>
    val map = Mock(MutableMap::class.java) as MutableMap<Any, Any>

    `when`
    list.removeAt(2)
    list2.add(2)
    list.add(1)
    map.size
    list.removeAt(2)

    then
    1 * list.add(2)
  }
}

class TooFewInvocationsNoUnmatchedSpec : Specification() {
  fun feature() {
    val list = Mock(MutableList::class.java) as MutableList<Any>

    `when`
    val x = 1

    then
    1 * list.add(any())
  }
}

class TooFewInvocationsAllUnsatisfiedSpec : Specification() {
  fun feature() {
    val list = Mock(MutableList::class.java) as MutableList<Any>

    `when`
    list.add(2)
    list.get(2)

    then
    1 * list.add(1)
    1 * list.get(1)
  }
}

class TooFewInvocationsUnverifiedInvocationsSpec : Specification() {
  fun feature() {
    val list = Mock(MutableList::class.java) as MutableList<Any>
    val list2 = Stub(MutableList::class.java) as MutableList<Any>

    `when`
    list.add(1)
    list2.add(2)

    then
    1 * list.add(2)
  }
}

class TooFewInvocationsOuterScopeSpec : Specification() {
  fun feature() {
    val list = Mock(MutableList::class.java) as MutableList<Any>

    list.add(1)

    `when`
    list.add(2)

    then
    2 * list.add(any())
  }
}

class TooFewInvocationsStringArgumentSpec : Specification() {
  fun feature() {
    val fred = Mock(TooFewInvocationsPerson::class.java)

    `when`
    fred.wife("Fred", "Flintstone", 30, "Quarry")

    then
    1 * fred.wife("Wilma", "Flintstone", 30, "Bedrock")
  }
}

class TooFewInvocationsDollarMethodNameSpec : Specification() {
  fun feature() {
    val mock = Mock(TooFewInvocationsWithDollar::class.java)

    `when`
    mock.compareTo("a")

    then
    1 * mock.`compareTo$`(any())
  }
}

class TooFewInvocationsDollarPropertyNameSpec : Specification() {
  fun feature() {
    val mock = Mock(TooFewInvocationsWithDollar::class.java)

    `when`
    mock.foo

    then
    1 * mock.`bar$`
  }
}

class TooFewInvocationsPropertyNameSpec : Specification() {
  fun feature() {
    val fred = Mock(TooFewInvocationsPerson::class.java)

    `when`
    fred.age

    then
    1 * fred.name
  }
}

class TooFewInvocationsTargetSpec : Specification() {
  fun feature() {
    val fred = Mock(TooFewInvocationsPerson::class.java)
    val barney = Mock(TooFewInvocationsPerson::class.java)

    `when`
    fred.name

    then
    1 * barney.name
  }
}

class TooFewInvocationsDumpRenderingSpec : Specification() {
  fun feature() {
    val list = Mock(MutableList::class.java) as MutableList<Any>
    val person = TooFewInvocationsDumpedPerson("Foo", 18)

    `when`
    list.add(person)

    then
    1 * list.add("bar")
  }
}

class TooFewInvocationsMultidimensionalArraySpec : Specification() {
  fun feature() {
    val list = Mock(MutableList::class.java) as MutableList<Any>
    val arr = arrayOf(intArrayOf(1, 2), intArrayOf(2, 5))

    `when`
    list.add(arr)

    then
    1 * list.add("bar")
  }
}
