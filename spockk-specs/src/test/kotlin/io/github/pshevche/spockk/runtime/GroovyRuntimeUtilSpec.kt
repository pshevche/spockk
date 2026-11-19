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

package io.github.pshevche.spockk.runtime

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Specification
import java.net.URL

class GroovyRuntimeUtilSpec : Specification() {

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilSpec#getterMethodToPropertyName")
  fun getterMethodToPropertyName(
    methodName: String,
    returnType: Class<*>,
    groovy3orLowerProperty: String?,
    groovy4orHigherProperty: String?
  ) {
    given
    val propertyName = if (GroovyRuntimeUtil.MAJOR_VERSION >= 4) groovy4orHigherProperty else groovy3orLowerProperty

    expect
    GroovyRuntimeUtil.getterMethodToPropertyName(methodName, emptyList<Class<*>>(), returnType) == propertyName

    where
    methodName        ; returnType                         ; groovy3orLowerProperty ; groovy4orHigherProperty
    "getLength"       ; Int::class.javaObjectType          ; "length"               ; "length"
    "getValid"        ; Boolean::class.javaPrimitiveType!! ; "valid"                ; "valid"
    "isEmpty"         ; Boolean::class.javaPrimitiveType!! ; "empty"                ; "empty"
    "isEmpty"         ; Boolean::class.javaObjectType      ; "empty"                ; null
    "getLengthStatic" ; Int::class.javaObjectType          ; "lengthStatic"         ; "lengthStatic"
    "isEmptyStatic"   ; Boolean::class.javaPrimitiveType!! ; "emptyStatic"          ; "emptyStatic"
    "getValidStatic"  ; Boolean::class.javaPrimitiveType!! ; "validStatic"          ; "validStatic"
    "getEmpty"        ; Boolean::class.javaPrimitiveType!! ; "empty"                ; "empty"
    "getEmpty"        ; Boolean::class.javaObjectType      ; "empty"                ; "empty"
    "getURL"          ; URL::class.java                    ; "URL"                  ; "URL"
    "getfoo"          ; String::class.java                 ; "foo"                  ; "foo"
    "isfoo"           ; Boolean::class.javaPrimitiveType!! ; "foo"                  ; "foo"
    "get"             ; Int::class.javaObjectType          ; null                   ; null
    "is"              ; Boolean::class.javaPrimitiveType!! ; null                   ; null
    "foo"             ; String::class.java                 ; null                   ; null
    "isfoo"           ; String::class.java                 ; null                   ; null
    "setFoo"          ; Void.TYPE                          ; null                   ; null
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilSpec#propertyToGetterMethodName")
  fun propertyToGetterMethodName() {
    expect
    GroovyRuntimeUtil.propertyToGetterMethodName("prop") == "getProp"
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilSpec#propertyToSetterMethodName")
  fun propertyToSetterMethodName() {
    expect
    GroovyRuntimeUtil.propertyToSetterMethodName("prop") == "setProp"
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilSpec#propertyToBooleanGetterMethodName")
  fun propertyToBooleanGetterMethodName() {
    expect
    GroovyRuntimeUtil.propertyToBooleanGetterMethodName("prop") == "isProp"
  }

  @MigratedFrom(
    "org.spockframework.runtime.GroovyRuntimeUtilSpec#coercing to primitive type has same effect as coercing to wrapper type (and doesn't blow up)"
  )
  fun `coercing to primitive type has same effect as coercing to wrapper type (and doesn't blow up)`() {
    expect
    GroovyRuntimeUtil.coerce<Any>("x", Char::class.javaPrimitiveType) == 'x'
    GroovyRuntimeUtil.coerce<Any>("x", Char::class.javaPrimitiveType) is Char

    GroovyRuntimeUtil.coerce<Any>(123, Byte::class.javaPrimitiveType) == 123.toByte()
    GroovyRuntimeUtil.coerce<Any>(123, Byte::class.javaPrimitiveType) is Byte
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilSpec#coerce with empty types throws IllegalArgumentException")
  fun `coerce with empty types throws IllegalArgumentException`() {
    `when`
    GroovyRuntimeUtil.coerce<Any?>("x")

    then
    thrown(IllegalArgumentException::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilSpec#instantiate closure")
  fun `instantiate closure`() {
    val owner = mapOf("x" to 1)
    val thisObject = ThisObject()

    expect
    GroovyRuntimeUtil.instantiateClosure(TestClosure::class.java, owner, thisObject).call(3) == 6
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilSpec#asArgumentList")
  fun asArgumentList() {
    `when`
    var l = GroovyRuntimeUtil.asArgumentList(null)

    then
    l.isEmpty()

    `when`
    l = GroovyRuntimeUtil.asArgumentList("A")

    then
    l.size == 1
    l == listOf("A")
  }

  @MigratedFrom("org.spockframework.runtime.GroovyRuntimeUtilSpec#GroovyRuntimeUtil.#method unwraps InvokerInvocationException")
  fun `GroovyRuntimeUtil․#method unwraps InvokerInvocationException`(method: String, call: () -> Any?) {
    `when`
    call()

    then
    thrown(TestObjThrowingEx.TestException::class.java)

    where
    method                 ; call
    "getProperty"          ; { GroovyRuntimeUtil.getProperty(TestObjThrowingEx(), "a") }
    "setProperty"          ; { GroovyRuntimeUtil.setProperty(TestObjThrowingEx(), "a", "b") }
    "invokeConstructor"    ; { GroovyRuntimeUtil.invokeConstructor(TestObjThrowingEx::class.java, "a") }
    "invokeMethod"         ; { GroovyRuntimeUtil.invokeMethod(TestObjThrowingEx(), "method") }
    "invokeMethodNullSafe" ; { GroovyRuntimeUtil.invokeMethodNullSafe(TestObjThrowingEx(), "method") }
    "asIterator"           ; { GroovyRuntimeUtil.asIterator(TestObjThrowingEx()) }
  }

  class ThisObject {
    @Suppress("unused")
    val y = 2
  }

  class TestClosure(owner: Any?, thisObject: Any?) : Closure<Any?>(owner, thisObject) {
    @Suppress("unused")
    fun doCall(z: Int): Int = (getOwner() as Map<*, *>)["x"] as Int + (getThisObject() as ThisObject).y + z
  }

  @Suppress("unused")
  class TestObjThrowingEx {
    class TestException : Exception()

    constructor()

    constructor(param: String) {
      throw TestException()
    }

    fun getA(): Any = throw TestException()

    fun setA(value: Any?): Unit = throw TestException()

    fun iterator(): Any = throw TestException()

    fun method(): Unit = throw TestException()
  }
}
