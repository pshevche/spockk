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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import org.spockframework.mock.IMockObject
import org.spockframework.mock.ISpockMockObject
import spock.lang.Specification

@Suppress("FunctionOnlyReturningConstant", "FunctionNaming", "VariableNaming")
class MockDefaultResponses : Specification() {

  val imock: IMockable = Mock(IMockable::class.java)
  val cmock: Mockable = Mock(Mockable::class.java)

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockDefaultResponses#return values for unmatched invocations on interface-based mocks"
  )
  fun `return values for unmatched invocations on interface-based mocks`() {
    expect
    imock.getBoolean() == false
    imock.getByte() == 0.toByte()
    imock.getShort() == 0.toShort()
    imock.getInt() == 0
    imock.getLong() == 0L
    imock.getFloat() == 0f
    imock.getDouble() == 0.0
    imock.getObject() == null
    imock.getVoid() == Unit
    imock.getDynamic() == null
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockDefaultResponses#types of return values for unmatched invocations on interface-based mocks"
  )
  fun `types of return values for unmatched invocations on interface-based mocks`() {
    expect
    (imock.getBoolean() as Any).javaClass == java.lang.Boolean::class.java
    (imock.getByte() as Any).javaClass == java.lang.Byte::class.java
    (imock.getShort() as Any).javaClass == java.lang.Short::class.java
    (imock.getInt() as Any).javaClass == java.lang.Integer::class.java
    (imock.getLong() as Any).javaClass == java.lang.Long::class.java
    (imock.getFloat() as Any).javaClass == java.lang.Float::class.java
    (imock.getDouble() as Any).javaClass == java.lang.Double::class.java
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockDefaultResponses#return values for unmatched invocations on class-based mocks"
  )
  fun `return values for unmatched invocations on class-based mocks`() {
    expect
    cmock.getBoolean() == false
    cmock.getByte() == 0.toByte()
    cmock.getShort() == 0.toShort()
    cmock.getInt() == 0
    cmock.getLong() == 0L
    cmock.getFloat() == 0f
    cmock.getDouble() == 0.0
    cmock.getObject() == null
    cmock.getVoid() == Unit
    cmock.getDynamic() == null
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockDefaultResponses#types of return values for unmatched invocations on class-based mocks"
  )
  fun `types of return values for unmatched invocations on class-based mocks`() {
    expect
    (cmock.getBoolean() as Any).javaClass == java.lang.Boolean::class.java
    (cmock.getByte() as Any).javaClass == java.lang.Byte::class.java
    (cmock.getShort() as Any).javaClass == java.lang.Short::class.java
    (cmock.getInt() as Any).javaClass == java.lang.Integer::class.java
    (cmock.getLong() as Any).javaClass == java.lang.Long::class.java
    (cmock.getFloat() as Any).javaClass == java.lang.Float::class.java
    (cmock.getDouble() as Any).javaClass == java.lang.Double::class.java
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.MockDefaultResponses#default impl of ISpockMockObject.spock_mockInteractionValidator"
  )
  fun `default impl of ISpockMockObject_spock_mockInteractionValidator`() {
    given
    val obj =
      object : ISpockMockObject {
        override fun `$spock_get`(): IMockObject? = null
      }

    expect
    obj.`$spock_mockInteractionValidator`() == null
  }

  interface IMockable {
    fun getBoolean(): Boolean

    fun getByte(): Byte

    fun getShort(): Short

    fun getInt(): Int

    fun getLong(): Long

    fun getFloat(): Float

    fun getDouble(): Double

    fun getObject(): Any?

    fun getVoid()

    fun getDynamic(): Any?
  }

  open class Mockable {
    open fun getBoolean(): Boolean = true

    open fun getByte(): Byte = 42

    open fun getShort(): Short = 42

    open fun getInt(): Int = 42

    open fun getLong(): Long = 42

    open fun getFloat(): Float = 42f

    open fun getDouble(): Double = 42.0

    open fun getObject(): Any? = Any()

    open fun getVoid() {}

    open fun getDynamic(): Any? = 42
  }
}
