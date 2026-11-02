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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.verifyAll
import org.spockframework.mock.MockUtil
import spock.lang.Specification
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Queue
import java.util.SortedMap
import java.util.SortedSet

class StubDefaultResponses : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns false for boolean")
  fun `returns false for boolean`() {
    given
    val stub = Stub(Stubbable::class.java)

    expect
    !stub.getBoolean()
    !stub.getBooleanWrapper()!!
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns zero for character")
  fun `returns zero for character`() {
    given
    val stub = Stub(Stubbable::class.java)

    expect
    stub.getChar() == 0.toChar()
    stub.getCharWrapper() == 0.toChar()
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns zero for primitive number")
  fun `returns zero for primitive number`() {
    given
    val stub = Stub(Stubbable::class.java)

    expect
    stub.getByte() == 0.toByte()
    stub.getShort() == 0.toShort()
    stub.getInt() == 0
    stub.getLong() == 0L
    stub.getFloat() == 0f
    stub.getDouble() == 0.0
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns zero for primitive number wrapper")
  fun `returns zero for primitive number wrapper`() {
    given
    val stub = Stub(Stubbable::class.java)

    expect
    // `!!` avoids a compiler plugin crash on Byte?/Short? equality: https://github.com/pshevche/spockk/issues/1031
    stub.getByteWrapper()!! == 0.toByte()
    stub.getShortWrapper()!! == 0.toShort()
    stub.getIntWrapper() == 0
    stub.getLongWrapper() == 0L
    stub.getFloatWrapper() == 0f
    stub.getDoubleWrapper() == 0.0
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns zero for big number")
  fun `returns zero for big number`() {
    given
    val stub = Stub(Stubbable::class.java)

    expect
    stub.getBigInteger() == BigInteger.ZERO
    stub.getBigDecimal() == BigDecimal.ZERO
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns empty char sequence")
  fun `returns empty char sequence`() {
    given
    val stub = Stub(Stubbable::class.java)

    expect
    stub.getCharSequence().toString() == ""
    stub.getString() == ""
    stub.getStringBuilder().isEmpty()
    stub.getStringBuffer().isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns empty array")
  fun `returns empty array`() {
    given
    val stub = Stub(Stubbable::class.java)

    expect
    stub.getPrimitiveArray().isEmpty()
    stub.getInterfaceArray().isEmpty()
    stub.getClassArray().isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns empty collection")
  fun `returns empty collection`() {
    given
    val stub = Stub(Stubbable::class.java)

    expect
    stub.getIterable().toList().isEmpty()
    stub.getCollection().isEmpty()
    stub.getQueue().isEmpty()
    stub.getList().isEmpty()
    stub.getSet().isEmpty()
    stub.getMap().isEmpty()
    stub.getSortedSet().isEmpty()
    stub.getSortedMap().isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns dummy for unknown interface")
  fun `returns dummy for unknown interface`() {
    given
    val stub = Stub(Stubbable::class.java)
    val person = stub.getUnknownInterface()

    expect
    verifyAll {
      MockUtil().isMock(person)
      person.name == ""
      person.age == 0
      person.children == emptyList<String>()
    }
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns newly constructed object for unknown class with default constructor")
  fun `returns newly constructed object for unknown class with default constructor`() {
    given
    val stub = Stub(Stubbable::class.java)
    val person = stub.getUnknownClassWithDefaultCtor()

    expect
    verifyAll {
      !MockUtil().isMock(person)
      person.name == "default"
      person.age == 0
      person.children == null
    }
  }

  @MigratedFrom("org.spockframework.smoke.mock.StubDefaultResponses#returns dummy for unknown class without default constructor")
  fun `returns dummy for unknown class without default constructor`() {
    given
    val stub = Stub(Stubbable::class.java)
    val person = stub.getUnknownClassWithoutDefaultCtor()

    expect
    verifyAll {
      MockUtil().isMock(person)
      person.name == ""
      person.age == 0
      person.children == emptyList<String>()
    }
  }

  interface Stubbable {
    fun getByte(): Byte

    fun getShort(): Short

    fun getInt(): Int

    fun getLong(): Long

    fun getFloat(): Float

    fun getDouble(): Double

    fun getBoolean(): Boolean

    fun getChar(): Char

    fun getByteWrapper(): Byte?

    fun getShortWrapper(): Short?

    fun getIntWrapper(): Int?

    fun getLongWrapper(): Long?

    fun getFloatWrapper(): Float?

    fun getDoubleWrapper(): Double?

    fun getBooleanWrapper(): Boolean?

    fun getCharWrapper(): Char?

    fun getBigInteger(): BigInteger

    fun getBigDecimal(): BigDecimal

    fun getCharSequence(): CharSequence

    fun getString(): String

    fun getStringBuilder(): StringBuilder

    fun getStringBuffer(): StringBuffer

    fun getPrimitiveArray(): IntArray

    fun getInterfaceArray(): Array<IPerson>

    fun getClassArray(): Array<Person>

    fun getIterable(): Iterable<Any>

    fun getCollection(): Collection<Any>

    fun getQueue(): Queue<Any>

    fun getList(): List<Any>

    fun getSet(): Set<Any>

    fun getMap(): Map<Any, Any>

    fun getSortedSet(): SortedSet<Any>

    fun getSortedMap(): SortedMap<Any, Any>

    fun getUnknownInterface(): IPerson

    fun getUnknownClassWithDefaultCtor(): Person

    fun getUnknownClassWithoutDefaultCtor(): ImmutablePerson
  }

  interface IPerson {
    val name: String
    val age: Int
    val children: List<String>?
  }

  open class Person(
    override var name: String = "default",
    override var age: Int = 0,
    override var children: List<String>? = null
  ) : IPerson

  open class ImmutablePerson(name: String, age: Int, children: List<String>?) : Person(name, age, children)
}
