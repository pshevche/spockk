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
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.InvalidSpecException
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.mock.MockMakers

class JavaFinalMocks : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/984")
  @MigratedFrom("org.spockframework.smoke.mock.JavaFinalMocks#can mock final classes")
  fun `can mock final classes`() {
    given
    val person: FinalPerson = Mock(FinalPerson::class.java)

    `when`
    val result = person.phoneNumber

    then
    1 * person.phoneNumber returns "6789"
    result == "6789"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaFinalMocks#can mock final methods as property with mockito")
  fun `can mock final methods as property with mockito`() {
    given
    val person: FinalMethodPerson = Mock(mapOf("mockMaker" to MockMakers.mockito), FinalMethodPerson::class.java)

    `when`
    val result = person.phoneNumber

    then
    1 * person.phoneNumber returns "6789"
    result == "6789"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaFinalMocks#can mock final methods with mockito")
  fun `can mock final methods with mockito`() {
    given
    val person: FinalMethodPerson = Mock(mapOf("mockMaker" to MockMakers.mockito), FinalMethodPerson::class.java)
    person.phoneNumber returns "6789"

    `when`
    val result = person.phoneNumber

    then
    1 * person.phoneNumber returns "6789"
    result == "6789"
  }

  // Upstream also expects InvalidSpecException via @FailsWith; restore it when #657 closes (it hides @PendingFeature).
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaFinalMocks#cannot mock final methods with byteBuddy")
  fun `cannot mock final methods with byteBuddy`() {
    given
    val person: FinalMethodPerson = Mock(mapOf("mockMaker" to MockMakers.byteBuddy), FinalMethodPerson::class.java)

    `when`
    person.phoneNumber

    then
    1 * person.phoneNumber returns "6789"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaFinalMocks#cannot mock final is getter as property with byteBuddy")
  fun `cannot mock final is getter as property with byteBuddy`() {
    given
    val person: FinalMethodPerson = Mock(mapOf("mockMaker" to MockMakers.byteBuddy), FinalMethodPerson::class.java)

    `when`
    person.isFinalPerson

    then
    1 * person.isFinalPerson returns false
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.smoke.mock.JavaFinalMocks#cannot mock final methods with byteBuddy when one overload is non final no error message is produced"
  )
  fun `cannot mock final methods with byteBuddy when one overload is non final no error message is produced`() {
    given
    val person: FinalMethodPerson = Mock(mapOf("mockMaker" to MockMakers.byteBuddy), FinalMethodPerson::class.java)

    `when`
    val result = person.finalAndNonFinalOverload()

    then
    0 * person.finalAndNonFinalOverload() returns "B"
    result == "final"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom("org.spockframework.smoke.mock.JavaFinalMocks#non final method overload shall be mockable")
  fun `non final method overload shall be mockable`() {
    given
    val person: FinalMethodPerson = Mock(mapOf("mockMaker" to MockMakers.byteBuddy), FinalMethodPerson::class.java)

    `when`
    val result = person.finalAndNonFinalOverload("A")

    then
    person.finalAndNonFinalOverload("A") returns "B"
    result == "B"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.JavaFinalMocks#cannot mock final methods without specifying mockMaker")
  fun `cannot mock final methods without specifying mockMaker`() {
    given
    val person: FinalMethodPerson = Mock(FinalMethodPerson::class.java)

    `when`
    person.phoneNumber returns "6789"

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. " +
      "Please use another mock maker supporting final methods."
  }

  @MigratedFrom("org.spockframework.smoke.mock.JavaFinalMocks#no static type specified")
  fun `no static type specified`() {
    `when`
    Mock<Any>()

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == "Mock object type cannot be inferred automatically. Please specify a type explicitly (e.g. 'Mock(Person)')."
  }
}

class FinalPerson(val phoneNumber: String = "12345")

open class FinalMethodPerson {
  val phoneNumber: String = "12345"
  val isFinalPerson: Boolean = true

  fun finalAndNonFinalOverload(): String = "final"

  open fun finalAndNonFinalOverload(arg: String): String = arg
}
