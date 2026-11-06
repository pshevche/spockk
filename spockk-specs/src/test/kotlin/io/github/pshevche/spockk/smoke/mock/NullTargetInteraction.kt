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
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/issues/2339")
class NullTargetInteraction : Specification() {

  private val expectedMessage = "Interaction target must not be null, interactions can only be defined on mock objects"

  // A Java-returned platform type is null at runtime without Kotlin inserting a null check.
  private fun nullGreeter() = ThreadLocal<Greeter>().get()

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom("org.spockframework.smoke.mock.NullTargetInteraction#interaction on null target throws InvalidSpecException")
  fun `interaction on null target throws InvalidSpecException`() {
    given
    val nullObj = nullGreeter()

    `when`
    nullObj.getUsername() returns ""

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == expectedMessage
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/992")
  @MigratedFrom(
    "org.spockframework.smoke.mock.NullTargetInteraction#required interaction on null target throws InvalidSpecException"
  )
  fun `required interaction on null target throws InvalidSpecException`() {
    given
    val nullObj = nullGreeter()

    `when`
    1 * nullObj.getUsername()

    then
    val ex = thrown(InvalidSpecException::class.java)
    ex.message == expectedMessage
  }

  @MigratedFrom("org.spockframework.smoke.mock.NullTargetInteraction#interaction on real mock is still accepted")
  fun `interaction on real mock is still accepted`() {
    given
    val greeter = Mock(Greeter::class.java)

    `when`
    val name = greeter.getUsername()

    then
    notThrown(InvalidSpecException::class.java)
    1 * greeter.getUsername() returns "x"
    name == "x"
  }
}
