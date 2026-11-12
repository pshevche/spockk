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

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Spy
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.mock.CannotCreateMockException
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.SpockException
import org.spockframework.runtime.extension.builtin.PreconditionContext
import org.spockframework.util.SpockDocLinks
import spock.lang.PendingFeature
import spock.lang.Requires
import spock.lang.Specification
import spock.mock.MockMakers

class JavaSpies : Specification() {

  @MigratedFrom(
    "org.spockframework.smoke.mock.JavaSpies#construct spied-on object using default constructor when no constructor args given (even if Objenesis is available on class path)"
  )
  fun `construct spied-on object using default constructor when no constructor args given (even if Objenesis is available on class path)`() {
    `when`
    Spy(NoDefaultConstructor::class.java)

    then
    thrown(CannotCreateMockException::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#construct spied-on object using provided constructor args")
  fun `construct spied-on object using provided constructor args`(
    ctorArgs: Any,
    arg1: Int,
    arg2: Int,
    arg3: Int,
    arg4: String?
  ) {
    given
    val spy: Constructable = Spy(mapOf("constructorArgs" to ctorArgs), Constructable::class.java)

    expect
    spy.arg1 == arg1
    spy.arg2 == arg2
    spy.arg3 == arg3
    spy.arg4 == arg4

    where
    ctorArgs                                                  ; arg1 ; arg2 ; arg3 ; arg4
    listOf(1)                                                 ; 1    ; 0    ; 0    ; null
    listOf(2, 3)                                              ; 0    ; 2    ; 3    ; null
    listOf("hi")                                              ; 0    ; 0    ; 0    ; "hi"
    mapOf("arg4" to "hi")                                     ; 0    ; 0    ; 0    ; "hi"
    mapOf("arg1" to 2, "arg3" to 5, "arg4" to "hi")           ; 2    ; 0    ; 5    ; "hi"
    listOf(mapOf("arg1" to 2, "arg3" to 5, "arg4" to "hi"))   ; 2    ; 0    ; 5    ; "hi"
    mapOf("arg2" to 1)                                        ; 0    ; 1    ; 0    ; null
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#call real methods by default")
  fun `call real methods by default`() {
    given
    val person: Person = Spy(mapOf("constructorArgs" to listOf("fred", 42)), Person::class.java)

    expect
    person.name == "fred"
    person.age == 42
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#call real equals method by default")
  fun `call real equals method by default`() {
    given
    val fred1: Person = Spy(mapOf("constructorArgs" to listOf("fred", 42)), Person::class.java)
    val fred2: Person = Spy(mapOf("constructorArgs" to listOf("fred", 21)), Person::class.java)
    val barney: Person = Spy(mapOf("constructorArgs" to listOf("barney", 33)), Person::class.java)

    expect
    fred1 == fred2
    fred1 != barney
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#call real hashCode method by default")
  fun `call real hashCode method by default`() {
    given
    val person: Person = Spy(mapOf("constructorArgs" to listOf("fred", 42)), Person::class.java)

    expect
    person.hashCode() == "fred".hashCode()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#call real toString method by default")
  fun `call real toString method by default`() {
    given
    val person: Person = Spy(mapOf("constructorArgs" to listOf("fred", 42)), Person::class.java)

    expect
    person.toString() == "Hi, I'm fred"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#can verify interactions with real methods")
  fun `can verify interactions with real methods`() {
    given
    val person: Person = Spy(mapOf("constructorArgs" to listOf("fred", 42)), Person::class.java)

    `when`
    val result = person.work()

    then
    1 * person.work()
    1 * person.getTask()
    1 * person.getWorkHours()
    result == "singing, singing"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#can spy on concrete instances")
  fun `can spy on concrete instances`() {
    given
    val person = Spy(Person())

    `when`
    val result = person.work()

    then
    1 * person.work()
    result == "singing, singing"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/994")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#using >>_ does not call original method and produces stubbed value")
  fun `using returns any does not call original method and produces stubbed value`() {
    given
    val person = Spy(Person())

    `when`
    val result = person.getFormattedAge()

    then
    1 * person.getFormattedAge()
    result == "42 years old"

    `when`
    val result2 = person.getFormattedAge()

    then
    1 * person.getFormattedAge() returns any()
    result2 == ""
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#spying on concrete instances can use partial mocking")
  fun `spying on concrete instances can use partial mocking`() {
    given
    val person = Spy(Person())

    `when`
    val result = person.work()

    then
    1 * person.work()
    1 * person.getTask() returns "work"
    result == "work, work"
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#can define interactions for spy on instances directly with closure")
  fun `can define interactions for spy on instances directly with closure`() {
    given
    val spy = Spy(NoDefaultConstructor(42)) {
      1 * getValue() returns 7
    }

    expect
    spy.getValue() == 7
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#can stub final classes")
  fun `can stub final classes`() {
    `when`
    val person: FinalPerson = Spy()
    person.getPhoneNumber() returns "6789"

    then
    person.getPhoneNumber() == "6789"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#can spy final methods as property with mockito")
  fun `can spy final methods as property with mockito`() {
    given
    val person: FinalPropertyPerson = Spy(mapOf("mockMaker" to MockMakers.mockito), FinalPropertyPerson::class.java)
    person.phoneNumber returns "6789"

    expect
    person.phoneNumber == "6789"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#can spy final methods with mockito")
  fun `can spy final methods with mockito`() {
    given
    val person: FinalMethodPerson = Spy(mapOf("mockMaker" to MockMakers.mockito), FinalMethodPerson::class.java)
    person.getPhoneNumber() returns "6789"

    expect
    person.getPhoneNumber() == "6789"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#cannot spy on final methods without specifying mockMaker")
  fun `cannot spy on final methods without specifying mockMaker`() {
    given
    val person: FinalMethodPerson = Spy()

    `when`
    person.getPhoneNumber() returns "6789"

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == FINAL_METHOD_MESSAGE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#cannot spy globally")
  fun `cannot spy globally`() {
    `when`
    Spy(mapOf("global" to true), Person::class.java)

    then
    val e = thrown(CannotCreateMockException::class.java)
    e.message!!.contains("global")
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#inferred type is ignored for instance mocks")
  fun `inferred type is ignored for instance mocks`() {
    `when`
    val person: Being = Person()
    val second: Being = Spy(person)

    then
    second is Person
    noExceptionThrown()
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#do not allow spying on other mocks")
  fun `do not allow spying on other mocks`() {
    given
    val src: ArrayList<Any> = Spy()

    `when`
    Spy(src)

    then
    thrown(SpockException::class.java)
  }

  @Requires(Java17Compatible::class, reason = "Only happens on 17+, without an explicit --add-opens")
  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#known issue copy fields")
  fun `known issue copy fields`() {
    `when`
    val second: List<Any> = Spy(ArrayList<Any>())

    then
    val e = thrown(CannotCreateMockException::class.java)
    e.message == "Cannot create mock for class java.util.ArrayList. Cannot copy fields.\n" +
      SpockDocLinks.SPY_ON_JAVA_17.link
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#no static type specified")
  fun `no static type specified`() {
    `when`
    Spy<Any>()

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == "Spy object type cannot be inferred automatically. Please specify a type explicitly (e.g. 'Spy(Person)')."
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaSpies#specified instance is null")
  fun `specified instance is null`() {
    `when`
    Spy(null as Any?)

    then
    val ex = thrown(SpockException::class.java)
    ex.message == "Spy instance may not be null"
  }

  open class Constructable() {
    var arg1: Int = 0
    var arg2: Int = 0
    var arg3: Int = 0
    var arg4: String? = null

    constructor(arg1: Int) : this() {
      this.arg1 = arg1
    }

    constructor(arg2: Int, arg3: Int) : this() {
      this.arg2 = arg2
      this.arg3 = arg3
    }

    constructor(arg4: String) : this() {
      this.arg4 = arg4
    }
  }

  class Java17Compatible(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
    @Suppress("unused")
    fun doCall(context: PreconditionContext<*>): Boolean = context.jvm.isJava17Compatible
  }

  interface Being

  open class Person(open var name: String, open var age: Int) : Being {
    constructor() : this("fred", 42)

    open fun work(): String = getTask().let { task -> List(getWorkHours()) { task } }.joinToString(", ")

    open fun getTask(): String = "singing"

    open fun getWorkHours(): Int = 2

    open fun getFormattedAge(): String = "$age years old"

    override fun toString(): String = "Hi, I'm $name"

    override fun equals(other: Any?): Boolean = other is Person && name == other.name

    override fun hashCode(): Int = name.hashCode()
  }

  class FinalPerson : Person() {
    fun getPhoneNumber(): String = "12345"
  }

  open class FinalMethodPerson : Person() {
    fun getPhoneNumber(): String = "12345"
  }

  open class FinalPropertyPerson : Person() {
    val phoneNumber: String get() = "12345"
  }

  open class NoDefaultConstructor(private val value: Int) {
    open fun getValue(): Int = value
  }

  private companion object {
    const val FINAL_METHOD_MESSAGE =
      "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. " +
        "Please use another mock maker supporting final methods."
  }
}
