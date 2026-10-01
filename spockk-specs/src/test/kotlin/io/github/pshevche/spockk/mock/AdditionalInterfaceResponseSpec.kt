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

package io.github.pshevche.spockk.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.PendingFeature
import spock.lang.Specification
import java.util.function.IntSupplier

class AdditionalInterfaceResponseSpec : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.AdditionalInterfaceResponseSpec#Defining responses for additionalInterfaces for Groovy class"
  )
  fun `Defining responses for additionalInterfaces for Groovy class`() {
    given
    val a: A = Stub(mapOf<String, Any>("additionalInterfaces" to listOf(B::class.java)), A::class.java)

    `when`
    val methodFromAResult = a.methodFromA()
    val methodFromIfResult = (a as B).methodFromIf()

    then
    a.methodFromA() returns "MockedA"
    (a as B).methodFromIf() returns "Result"
    a is A
    methodFromAResult == "MockedA"
    a is B
    methodFromIfResult == "Result"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/658")
  @MigratedFrom(
    "org.spockframework.mock.AdditionalInterfaceResponseSpec#Defining responses for additionalInterfaces for Groovy class with GroovyStub"
  )
  fun `Defining responses for additionalInterfaces for Groovy class with GroovyStub`() {
    given
    val a: A = GroovyStub(mapOf<String, Any>("additionalInterfaces" to listOf(B::class.java)), A::class.java)

    `when`
    val methodFromAResult = a.methodFromA()
    val methodFromIfResult = (a as B).methodFromIf()

    then
    a.methodFromA() returns "MockedA"
    (a as B).methodFromIf() returns "Result"
    a is A
    methodFromAResult == "MockedA"
    a is B
    methodFromIfResult == "Result"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.AdditionalInterfaceResponseSpec#Defining responses for additionalInterfaces for Groovy interface"
  )
  fun `Defining responses for additionalInterfaces for Groovy interface`() {
    given
    val c: C = Stub(mapOf<String, Any>("additionalInterfaces" to listOf(B::class.java)), C::class.java)

    `when`
    val methodFromIfCResult = c.methodFromIfC()
    val methodFromIfResult = (c as B).methodFromIf()

    then
    c.methodFromIfC() returns "ResultC"
    (c as B).methodFromIf() returns "ResultB"
    c is C
    methodFromIfCResult == "ResultC"
    c is B
    methodFromIfResult == "ResultB"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/658")
  @MigratedFrom(
    "org.spockframework.mock.AdditionalInterfaceResponseSpec#Defining responses for additionalInterfaces for Groovy interface with GroovyStub"
  )
  fun `Defining responses for additionalInterfaces for Groovy interface with GroovyStub`() {
    given
    val c: C = GroovyStub(mapOf<String, Any>("additionalInterfaces" to listOf(B::class.java)), C::class.java)

    `when`
    val methodFromIfCResult = c.methodFromIfC()
    val methodFromIfResult = (c as B).methodFromIf()

    then
    c.methodFromIfC() returns "ResultC"
    (c as B).methodFromIf() returns "ResultB"
    c is C
    methodFromIfCResult == "ResultC"
    c is B
    methodFromIfResult == "ResultB"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/657")
  @MigratedFrom(
    "org.spockframework.mock.AdditionalInterfaceResponseSpec#Defining responses for additionalInterfaces for Java classes"
  )
  fun `Defining responses for additionalInterfaces for Java classes`() {
    given
    val a = Stub(mapOf<String, Any>("additionalInterfaces" to listOf(IntSupplier::class.java)), ArrayList::class.java)

    `when`
    val result = (a as IntSupplier).getAsInt()

    then
    (a as IntSupplier).getAsInt() returns 5
    a is ArrayList<*>
    a is IntSupplier
    result == 5
  }
}

open class A {
  open fun methodFromA(): String = "RealA"
}

interface B {
  fun methodFromIf(): String
}

interface C {
  fun methodFromIfC(): String
}
