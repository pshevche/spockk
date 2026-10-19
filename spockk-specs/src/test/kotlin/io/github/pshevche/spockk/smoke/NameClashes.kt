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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.anyMethod
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import io.github.pshevche.spockk.smoke.mock.Greeter
import spock.lang.FailsWith
import spock.lang.Specification

class NameClashes : Specification() {

  @MigratedFrom("org.spockframework.smoke.NameClashes#getAt data variable in multi assignment does not clash with getAt method in data processor method")
  fun `getAt data variable in multi assignment does not clash with getAt method in data processor method`(getAt: Int, foo: Int) {
    expect
    true

    where
    variables(getAt, foo).from(listOf(1), listOf(2))
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#isInstance does not clash with rewritten instanceof condition")
  fun `isInstance does not clash with rewritten instanceof condition`() {
    given
    val isInstance = Any()

    expect
    isInstance is Any
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#record does not clash with rewritten condition")
  fun `record does not clash with rewritten condition`() {
    given
    val record = Any()

    expect
    record != null
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#startRecordingValue does not clash with rewritten condition")
  fun `startRecordingValue does not clash with rewritten condition`() {
    given
    val startRecordingValue = Any()

    expect
    startRecordingValue != null
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#despreadList does not clash with rewritten condition")
  fun `despreadList does not clash with rewritten condition`() {
    given
    val despreadList = listOf<String>()

    expect
    listOf(*despreadList.toTypedArray()).isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#realizeNas does not clash with rewritten condition")
  fun `realizeNas does not clash with rewritten condition`() {
    given
    val realizeNas = Any()

    expect
    realizeNas.toString().isNotEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#reset does not clash with rewritten condition")
  fun `reset does not clash with rewritten condition`() {
    given
    val reset = Any()

    expect
    reset != null
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addInteraction does not clash with rewritten interaction")
  fun `addInteraction does not clash with rewritten interaction`() {
    given
    val addInteraction = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    (0..Int.MAX_VALUE) * obj.anyMethod()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#setRangeCount does not clash with rewritten interaction")
  fun `setRangeCount does not clash with rewritten interaction`() {
    given
    val setRangeCount = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    (0..Int.MAX_VALUE) * obj.anyMethod()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#setFixedCount does not clash with rewritten interaction")
  fun `setFixedCount does not clash with rewritten interaction`() {
    given
    val setFixedCount = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    1 * obj.anyMethod()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addEqualTarget does not clash with rewritten interaction")
  fun `addEqualTarget does not clash with rewritten interaction`() {
    given
    val addEqualTarget = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    1 * obj.getUsername()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addWildcardTarget does not clash with rewritten interaction")
  fun `addWildcardTarget does not clash with rewritten interaction`() {
    given
    val addWildcardTarget = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    (0..Int.MAX_VALUE) * obj.anyMethod()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addEqualMethodName does not clash with rewritten interaction")
  fun `addEqualMethodName does not clash with rewritten interaction`() {
    given
    val addEqualMethodName = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    1 * obj.getUsername()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addEqualPropertyName does not clash with rewritten interaction")
  fun `addEqualPropertyName does not clash with rewritten interaction`() {
    given
    val addEqualPropertyName = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    1 * obj.getUsername()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#setArgListKind does not clash with rewritten interaction")
  fun `setArgListKind does not clash with rewritten interaction`() {
    given
    val setArgListKind = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.greet("Alice")

    then
    1 * obj.greet(any())
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addEqualArg does not clash with rewritten interaction")
  fun `addEqualArg does not clash with rewritten interaction`() {
    given
    val addEqualArg = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.greet("Alice")

    then
    1 * obj.greet("Alice")
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addCodeResponse does not clash with rewritten interaction")
  fun `addCodeResponse does not clash with rewritten interaction`() {
    given
    val addCodeResponse = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.greet("Alice")

    then
    1 * obj.greet("Alice") does { "Hi" }
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addConstantResponse does not clash with rewritten interaction")
  fun `addConstantResponse does not clash with rewritten interaction`() {
    given
    val addConstantResponse = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.greet("Alice")

    then
    1 * obj.greet("Alice") returns "Hi"
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#build does not clash with rewritten interaction")
  fun `build does not clash with rewritten interaction`() {
    given
    val build = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    1 * obj.anyMethod()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#leaveScope does not clash with rewritten specification")
  fun `leaveScope does not clash with rewritten specification`() {
    given
    val leaveScope = Any()

    expect
    leaveScope != null
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#getMockController does not clash with rewritten specification")
  fun `getMockController does not clash with rewritten specification`() {
    given
    val getMockController = Any()

    expect
    getMockController != null
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#getSpecificationContext does not clash with rewritten specification")
  fun `getSpecificationContext does not clash with rewritten specification`() {
    given
    val getSpecificationContext = Any()

    expect
    getSpecificationContext != null
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#enterScope does not clash with rewritten specification")
  fun `enterScope does not clash with rewritten specification`() {
    given
    val enterScope = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.getUsername()

    then
    (0..Int.MAX_VALUE) * obj.anyMethod()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addSuppressed does not clash with rewritten specification")
  @FailsWith(Error::class)
  fun `addSuppressed does not clash with rewritten specification`() {
    given
    val addSuppressed = Any()

    expect
    throw Error()

    cleanup
    throw Exception()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#setThrownException does not clash with rewritten specification")
  fun `setThrownException does not clash with rewritten specification`() {
    given
    val setThrownException = Any()

    `when`
    throw Exception()

    then
    thrown(Exception::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#validateCollectedErrors does not clash with rewritten specification")
  @FailsWith(Error::class)
  fun `validateCollectedErrors does not clash with rewritten specification`(validateCollectedErrors: Int) {
    expect
    true
    throw Error()

    where
    variable(validateCollectedErrors).from(1)
  }
}
