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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.CannotCreateMockException
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.mock.MockMakers

class JavaStubs : Specification() {

  var person: JavaStubsPerson = Stub(JavaStubsPerson::class.java)

  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#default to empty response")
  fun `default to empty response`() {
    expect
    person.name == ""
    person.age == 0
    person.children == emptyList<String>()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#can be stubbed (using property syntax)")
  fun `can be stubbed (using property syntax)`() {
    given
    person.name returns "fred"

    expect
    person.name == "fred"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#can be stubbed (using method syntax)")
  fun `can be stubbed (using method syntax)`() {
    given
    person.name returns "fred"

    expect
    person.name == "fred"
  }

  @Issue("https://github.com/spockframework/spock/issues/1076")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#can stub property access")
  fun `can stub property access`() {
    given
    val classPerson: JavaStubsClassPerson = Stub(JavaStubsClassPerson::class.java)

    and
    classPerson.name returns "fred"
    classPerson.age returns 25

    expect
    classPerson.name == "fred"

    and
    classPerson.age == 25
  }

  @Issue("https://github.com/spockframework/spock/issues/1076")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#can stub property access for implicit getProperty() call")
  fun `can stub property access for implicit getProperty() call`() {
    given
    val classPerson: JavaStubsClassPerson = Stub(JavaStubsClassPerson::class.java)

    and
    classPerson.name returns "fred"
    classPerson.age returns 25

    expect
    classPerson.name == "fred"
    classPerson.age == 25
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/940")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#like to be stubbed at creation time")
  fun `like to be stubbed at creation time`() {
    given
    person = Stub(JavaStubsPerson::class.java) {
      name returns "fred"
    }

    expect
    person.name == "fred"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#cannot be mocked")
  fun `cannot be mocked`() {
    `when`
    1 * person.name returns "fred"

    then
    thrown(InvalidSpecException::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#can stand in for classes")
  fun `can stand in for classes`() {
    val classPerson: JavaStubsClassPerson = Stub(JavaStubsClassPerson::class.java) {
      name returns "barney"
      age returns 21
      children returns listOf("Bamm-Bamm")
    }

    expect
    classPerson.name == "barney"
    classPerson.age == 21
    classPerson.children == listOf("Bamm-Bamm")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#can stub final classes")
  fun `can stub final classes`() {
    `when`
    val finalPerson = Stub(JavaStubsFinalPerson::class.java)
    finalPerson.phoneNumber returns "6789"

    then
    finalPerson.phoneNumber == "6789"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#cannot stub globally")
  fun `cannot stub globally`() {
    `when`
    Stub(mapOf("global" to true), JavaStubsClassPerson::class.java)

    then
    val e = thrown(CannotCreateMockException::class.java)
    e.message!!.contains("global")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#cannot stub final methods without specifying mockMaker")
  fun `cannot stub final methods without specifying mockMaker`() {
    given
    val finalMethodPerson: JavaStubsFinalMethodPerson = Stub()

    `when`
    finalMethodPerson.phoneNumber returns "6789"

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == FINAL_PHONE_NUMBER_MESSAGE
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#can stub final methods with mockito")
  fun `can stub final methods with mockito`() {
    given
    val finalMethodPerson: JavaStubsFinalMethodPerson = Stub(mapOf("mockMaker" to MockMakers.mockito))
    finalMethodPerson.phoneNumber returns "6789"

    expect
    finalMethodPerson.phoneNumber == "6789"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#can stub final methods as property with mockito")
  fun `can stub final methods as property with mockito`() {
    given
    val finalMethodPerson: JavaStubsFinalMethodPerson = Stub(mapOf("mockMaker" to MockMakers.mockito))
    finalMethodPerson.phoneNumber returns "6789"

    expect
    finalMethodPerson.phoneNumber == "6789"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#cannot stub final methods with byteBuddy")
  fun `cannot stub final methods with byteBuddy`() {
    given
    val finalMethodPerson: JavaStubsFinalMethodPerson = Stub(mapOf("mockMaker" to MockMakers.byteBuddy))

    `when`
    finalMethodPerson.phoneNumber returns "6789"

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == FINAL_PHONE_NUMBER_MESSAGE

    and
    finalMethodPerson.phoneNumber == "12345"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.smoke.mock.JavaStubs#cannot stub final methods with byteBuddy without error message when one overload is non final"
  )
  fun `cannot stub final methods with byteBuddy without error message when one overload is non final`() {
    given
    val finalMethodPerson: JavaStubsFinalMethodPerson = Stub(mapOf("mockMaker" to MockMakers.byteBuddy))
    finalMethodPerson.finalAndNonFinalOverload() returns "B"

    expect
    finalMethodPerson.finalAndNonFinalOverload() == "final"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#non final method overload shall be stubable")
  fun `non final method overload shall be stubable`() {
    given
    val finalMethodPerson: JavaStubsFinalMethodPerson = Stub(mapOf("mockMaker" to MockMakers.byteBuddy))
    finalMethodPerson.finalAndNonFinalOverload("A") returns "B"

    expect
    finalMethodPerson.finalAndNonFinalOverload("A") == "B"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#cannot stub final method as property with byteBuddy")
  fun `cannot stub final method as property with byteBuddy`() {
    given
    val finalMethodPerson: JavaStubsFinalMethodPerson = Stub(mapOf("mockMaker" to MockMakers.byteBuddy))

    `when`
    finalMethodPerson.phoneNumber returns "6789"

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == FINAL_PHONE_NUMBER_MESSAGE

    and
    finalMethodPerson.phoneNumber == "12345"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaStubs#cannot stub final is getter as property with byteBuddy")
  fun `cannot stub final is getter as property with byteBuddy`() {
    given
    val finalMethodPerson: JavaStubsFinalMethodPerson = Stub(mapOf("mockMaker" to MockMakers.byteBuddy))

    `when`
    finalMethodPerson.isFinalPerson returns false

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message ==
      "The final method 'isFinalPerson' of 'finalMethodPerson' can't be mocked by the 'byte-buddy' mock maker. " +
      "Please use another mock maker supporting final methods."

    and
    finalMethodPerson.isFinalPerson
  }

  private companion object {
    const val FINAL_PHONE_NUMBER_MESSAGE =
      "The final method 'getPhoneNumber' of 'finalMethodPerson' can't be mocked by the 'byte-buddy' mock maker. " +
        "Please use another mock maker supporting final methods."
  }
}

interface JavaStubsPerson {
  val name: String
  val age: Int
  val children: List<String>
}

open class JavaStubsClassPerson : JavaStubsPerson {
  override val name: String = "default"
  override val age: Int = 0
  override val children: List<String> = emptyList()
}

class JavaStubsFinalPerson : JavaStubsClassPerson() {
  val phoneNumber: String = "12345"
}

open class JavaStubsFinalMethodPerson : JavaStubsClassPerson() {
  val phoneNumber: String = "12345"
  val isFinalPerson: Boolean = true

  fun finalAndNonFinalOverload(): String = "final"

  open fun finalAndNonFinalOverload(arg: String): String = arg
}
