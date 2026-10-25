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
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import io.github.pshevche.spockk.smoke.mock.Greeter
import spock.lang.FailsWith
import spock.lang.PendingFeature
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
    val isInstance = Any()

    expect
    isInstance is Any
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#record does not clash with rewritten condition")
  fun `record does not clash with rewritten condition`() {
    val record = Any()

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#startRecordingValue does not clash with rewritten condition")
  fun `startRecordingValue does not clash with rewritten condition`() {
    val startRecordingValue = Any()

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#reset does not clash with rewritten condition")
  fun `reset does not clash with rewritten condition`() {
    val reset = Any()

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#despreadList does not clash with rewritten condition")
  fun `despreadList does not clash with rewritten condition`() {
    val despreadList = listOf<String>()

    expect
    listOf(*despreadList.toTypedArray()).isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#realizeNas does not clash with rewritten condition")
  fun `realizeNas does not clash with rewritten condition`() {
    val realizeNas = Any()

    expect
    realizeNas.toString().isNotEmpty()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#addInteraction does not clash with rewritten interaction")
  fun `addInteraction does not clash with rewritten interaction`() {
    val addInteraction = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.anyMethod()

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#setRangeCount does not clash with rewritten interaction")
  fun `setRangeCount does not clash with rewritten interaction`() {
    val setRangeCount = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.anyMethod()

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#setFixedCount does not clash with rewritten interaction")
  fun `setFixedCount does not clash with rewritten interaction`() {
    val setFixedCount = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.anyMethod()

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#addEqualTarget does not clash with rewritten interaction")
  fun `addEqualTarget does not clash with rewritten interaction`() {
    val addEqualTarget = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.getUsername()

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#addWildcardTarget does not clash with rewritten interaction")
  fun `addWildcardTarget does not clash with rewritten interaction`() {
    val addWildcardTarget = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.anyMethod()

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#addEqualMethodName does not clash with rewritten interaction")
  fun `addEqualMethodName does not clash with rewritten interaction`() {
    val addEqualMethodName = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.getUsername()

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#addEqualPropertyName does not clash with rewritten interaction")
  fun `addEqualPropertyName does not clash with rewritten interaction`() {
    val addEqualPropertyName = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.getUsername()

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#setArgListKind does not clash with rewritten interaction")
  fun `setArgListKind does not clash with rewritten interaction`() {
    val setArgListKind = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.greet(any())

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#addEqualArg does not clash with rewritten interaction")
  fun `addEqualArg does not clash with rewritten interaction`() {
    val addEqualArg = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.greet("Alice")

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#addCodeResponse does not clash with rewritten interaction")
  fun `addCodeResponse does not clash with rewritten interaction`() {
    val addCodeResponse = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.greet("Alice") does { "Hi" }

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#addConstantResponse does not clash with rewritten interaction")
  fun `addConstantResponse does not clash with rewritten interaction`() {
    val addConstantResponse = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.greet("Alice") returns "Hi"

    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/693")
  @MigratedFrom("org.spockframework.smoke.NameClashes#build does not clash with rewritten interaction")
  fun `build does not clash with rewritten interaction`() {
    val build = Any()
    val obj = Mock(Greeter::class.java)
    (0..Int.MAX_VALUE) * obj.anyMethod()

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#leaveScope does not clash with rewritten specification")
  fun `leaveScope does not clash with rewritten specification`() {
    val leaveScope = Any()

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#getMockController does not clash with rewritten specification")
  fun `getMockController does not clash with rewritten specification`() {
    val getMockController = Any()

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#getSpecificationContext does not clash with rewritten specification")
  fun `getSpecificationContext does not clash with rewritten specification`() {
    val getSpecificationContext = Any()

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#enterScope does not clash with rewritten specification")
  fun `enterScope does not clash with rewritten specification`() {
    val enterScope = Any()
    val obj = Mock(Greeter::class.java)

    `when`
    obj.toString()

    then
    (0..Int.MAX_VALUE) * obj.anyMethod()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#addSuppressed does not clash with rewritten specification")
  @FailsWith(Error::class)
  fun `addSuppressed does not clash with rewritten specification`() {
    val addSuppressed = Any()

    expect
    throw Error()

    cleanup
    throw Exception()
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#setThrownException does not clash with rewritten specification")
  fun `setThrownException does not clash with rewritten specification`() {
    val setThrownException = Any()

    `when`
    throw Exception()

    then
    thrown(Exception::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.NameClashes#validateCollectedErrors does not clash with rewritten specification")
  @FailsWith(Error::class)
  fun `validateCollectedErrors does not clash with rewritten specification`(validateCollectedErrors: Any?) {
    expect
    true
    throw Error()

    where
    variable(validateCollectedErrors).from(null)
  }
}
