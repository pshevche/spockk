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

package io.github.pshevche.spockk.fixtures.runtime.samples.condition

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.verifyEach
import spock.lang.Specification
import java.lang.Boolean.parseBoolean
import java.lang.Integer.MAX_VALUE
import java.lang.Math.min
import java.lang.Thread.State.BLOCKED
import java.sql.Date

class SimpleComparisonSpec : Specification() {
  fun `test`() {
    val x = 5
    val y = 10
    expect
    x > y
  }
}

class IntegerEqualitySpec : Specification() {
  fun `test`() {
    expect
    1 == 2
  }
}

class NullValueSpec : Specification() {
  fun `test`() {
    val x: String? = null
    expect
    x != null
  }
}

class StringValueSpec : Specification() {
  fun `test`() {
    val x = "hello"
    expect
    x == null
  }
}

class BooleanNegationSpec : Specification() {
  fun `test`() {
    val flag = true
    expect
    !flag
  }
}

class StringMethodCallSpec : Specification() {
  fun `test`() {
    val str = "hello"
    expect
    str.startsWith("xyz")
  }
}

class PropertyAccessSpec : Specification() {
  fun `test`() {
    val str = "hello"
    expect
    str.length == 3
  }
}

class ArithmeticExpressionSpec : Specification() {
  fun `test`() {
    val a = 1
    val b = 2
    expect
    a + b == 4
  }
}

class LogicalAndSpec : Specification() {
  fun `test`() {
    val left = "spockk".isEmpty()
    val right = "kotlin".isNotEmpty()
    expect
    left && right
  }
}

class LogicalOrSpec : Specification() {
  fun `test`() {
    val left = "spockk".isEmpty()
    val right = "kotlin".isEmpty()
    expect
    left || right
  }
}

class ListAccessSpec : Specification() {
  fun `test`() {
    val list = listOf(1, 2, 3)
    expect
    list[0] == 10
  }
}

class ChainedExpressionSpec : Specification() {
  fun `test`() {
    val list = listOf("abc", "def")
    expect
    list[0].length == 10
  }
}

class StringEqualitySpec : Specification() {
  fun `test`() {
    val x = "spockk"
    val y = "kotlin"
    expect
    x == y
  }
}

class CharValueSpec : Specification() {
  fun `test`() {
    val x: Char? = 'c'
    expect
    x == null
  }
}

class EmptyStringValueSpec : Specification() {
  fun `test`() {
    val x = ""
    expect
    x == null
  }
}

class MultiLineStringValueSpec : Specification() {
  fun `test`() {
    val x = "one\ntwo\rthree\r\nfour"
    expect
    x == null
  }
}

class ListValueSpec : Specification() {
  fun `test`() {
    val x = listOf(1, 2, 3)
    expect
    x == null
  }
}

class EmptyListValueSpec : Specification() {
  fun `test`() {
    val x = emptyList<Int>()
    expect
    x == null
  }
}

class MapValueSpec : Specification() {
  fun `test`() {
    val x = linkedMapOf("a" to 1, "b" to 2)
    expect
    x == null
  }
}

class SetValueSpec : Specification() {
  fun `test`() {
    val x = linkedSetOf(1, 2, 3)
    expect
    x == null
  }
}

class IntArrayValueSpec : Specification() {
  fun `test`() {
    val x = intArrayOf(1, 2)
    expect
    x == null
  }
}

class ObjectArrayValueSpec : Specification() {
  fun `test`() {
    val x = arrayOf("one", "two")
    expect
    x == null
  }
}

class SingleLineToString {
  override fun toString() = "single line"
}

class SingleLineToStringValueSpec : Specification() {
  fun `test`() {
    val x = SingleLineToString()
    expect
    x == null
  }
}

class MultiLineToString {
  override fun toString() = "mul\ntiple\n   lines"
}

class MultiLineToStringValueSpec : Specification() {
  fun `test`() {
    val x = MultiLineToString()
    expect
    x == null
  }
}

class EmptyToString {
  fun objectToString(): String = super.toString()
  override fun toString() = ""
}

class EmptyToStringValueSpec : Specification() {
  fun `test`() {
    val x = EmptyToString()
    capturedInstance = x
    expect
    x == null
  }

  companion object {
    lateinit var capturedInstance: EmptyToString
  }
}

class ThrowingToString {
  fun objectToString(): String = super.toString()
  override fun toString(): String = throw UnsupportedOperationException()
}

class ThrowingToStringValueSpec : Specification() {
  fun `test`() {
    val x = ThrowingToString()
    capturedInstance = x
    expect
    x == null
  }

  companion object {
    lateinit var capturedInstance: ThrowingToString
  }
}

class DefaultToString {
  val a = 4
}

class DefaultToStringValueSpec : Specification() {
  fun `test`() {
    val x = DefaultToString()
    expect
    x == null
  }
}

enum class ColorValue {
  RED,
  GREEN
}

class EnumValueSpec : Specification() {
  fun `test`() {
    val x = ColorValue.RED
    expect
    x == null
  }
}

enum class EnumWithToString {
  VALUE;

  override fun toString() = "I'm a value"
}

class EnumWithToStringValueSpec : Specification() {
  fun `test`() {
    val x = EnumWithToString.VALUE
    expect
    x == null
  }
}

class ClassValueSpec : Specification() {
  fun `test`() {
    val x = String::class.java
    expect
    x == null
  }
}

class TypeHintValueSpec : Specification() {
  fun `test`() {
    val x: Any = 1
    val y: Any = "1"
    expect
    x == y
  }
}

class Bean {
  var integer: Int = 0
  var string: String = ""

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Bean) return false
    return integer == other.integer && string == other.string
  }

  override fun hashCode(): Int = 31 * integer + string.hashCode()
}

class CustomObjectDiffSpec : Specification() {
  fun `test`() {
    val b1 = Bean().apply {
      integer = 1
      string = "fun"
    }
    val b2 = Bean().apply {
      integer = 2
      string = "fun2"
    }
    expect
    b1 == b2
  }
}

class StringEqualsMethodCallSpec : Specification() {
  fun `test`() {
    val str = "hello"
    expect
    str.equals("xyz")
  }
}

class VerifyPropertyAccessSpec : Specification() {
  fun `test`() {
    val str = "hello"
    expect
    verify(str) { length == 3 }
  }
}

class VerifyAllPropertyAccessSpec : Specification() {
  fun `test`() {
    val str = "hello"
    expect
    verifyAll(str) { length == 3 }
  }
}

class VerifyEachPropertyAccessSpec : Specification() {
  fun `test`() {
    val strings = listOf("hello", "hi")
    expect
    verifyEach(strings) { length == 3 }
  }
}

class NestedVerifyInVerifyAllSpec : Specification() {
  fun `test`() {
    val str = "hello"
    expect
    verifyAll {
      verify(str) { length == 3 }
    }
  }
}

class SimpleConditionSpec : Specification() {
  fun `test`() {
    val x = 2
    expect
    x == 1
  }
}

class MultiLineConditionSpec : Specification() {
  fun `test`() {
    expect
    1 +
      2 ==

      4 -

      2
  }
}

class ImplicitTargetCallSpec : Specification() {
  private fun one(@Suppress("UNUSED_PARAMETER") x: Int) = false

  fun `test`() {
    val a = 1
    expect
    one(a)
  }
}

class ExplicitTargetCallSpec : Specification() {
  fun `test`() {
    val a = listOf(1)
    val b = 0
    expect
    a.get(b) == null
  }
}

class TopLevelMethodCallSpec : Specification() {
  fun `test`() {
    val a = listOf(false)
    val b = 0
    expect
    a.get(b)
  }
}

class JavaStaticMethodCallSpec : Specification() {
  fun `test`() {
    val a = 1
    val b = 2
    expect
    Math.max(a, b) == null
  }
}

class NamedArgumentsCallSpec : Specification() {
  class Person2 {
    fun eat(what: String, where: String) = what.isEmpty() && where.isEmpty()

    override fun toString() = "p"
  }

  fun `test`() {
    val person = Person2()
    val steak = "steak"
    val tokyo = "tokyo"
    expect
    person.eat(what = steak, where = tokyo)
  }
}

class StaticImportCallSpec : Specification() {
  fun `test`() {
    val a = 1
    val b = 2
    expect
    min(a, b) == null
  }
}

class TopLevelStaticCallSpec : Specification() {
  fun `test`() {
    val a = "no"
    expect
    parseBoolean(a)
  }
}

class BinaryOperatorSpec : Specification() {
  fun `test`() {
    val a = 0
    val b = 1
    expect
    a * b != 0
  }
}

class IndexAccessSpec : Specification() {
  fun `test`() {
    val a = listOf(false)
    val b = 0
    expect
    a[b]
  }
}

class PrefixIncrementSpec : Specification() {
  fun `test`() {
    var x = 0
    expect
    ++x == null
  }
}

class VariableAsConditionSpec : Specification() {
  fun `test`() {
    val a = false
    expect
    a
  }
}

class InclusiveRangeSpec : Specification() {
  fun `test`() {
    val a = 1
    val b = 2
    expect
    (a..b) == null
  }
}

class ExclusiveRangeSpec : Specification() {
  fun `test`() {
    val a = 1
    val b = 2
    expect
    (a..<b) == null
  }
}

class CollectionPropertySpec : Specification() {
  class Nine {
    val empty = false

    override fun toString() = "[9]"
  }

  fun `test`() {
    val a = Nine()
    expect
    a.empty == true
  }
}

class JavaStaticFieldSpec : Specification() {
  fun `test`() {
    expect
    Integer.MIN_VALUE == null
  }
}

class ConstantsOnlySpec : Specification() {
  fun `test`() {
    expect
    "abc" == "abd"
  }
}

class SameNameClassesSpec : Specification() {
  fun `test`() {
    val date = java.util.Date(0)
    expect
    date.javaClass == Date::class.java
  }
}

class SpreadArgumentSpec : Specification() {
  private fun two(@Suppress("UNUSED_PARAMETER") vararg a: Int) = false

  fun `test`() {
    val a = intArrayOf(1, 2)
    expect
    two(*a)
  }
}

class NotOperatorSpec : Specification() {
  fun `test`() {
    val a = true
    expect
    !a
  }
}

class UnaryMinusSpec : Specification() {
  fun `test`() {
    val a = 1
    expect
    -a == null
  }
}

class UnaryPlusSpec : Specification() {
  fun `test`() {
    val a = 1
    expect
    +a == null
  }
}

class CastToBooleanSpec : Specification() {
  fun `test`() {
    val a: Any = false
    expect
    a as Boolean
  }
}

class ThreeArgumentCallSpec : Specification() {
  private fun three(
    @Suppress("UNUSED_PARAMETER") a: Int,
    @Suppress("UNUSED_PARAMETER") b: Int,
    @Suppress("UNUSED_PARAMETER") c: Int
  ) = false

  fun `test`() {
    val a = 1
    val b = 2
    val c = 3
    expect
    three(a, b, c)
  }
}

class StaticImportFieldSpec : Specification() {
  fun `test`() {
    expect
    MAX_VALUE == 0
  }
}

class StaticImportEnumSpec : Specification() {
  fun `test`() {
    expect
    BLOCKED == null
  }
}

class BackslashEscapingSpec : Specification() {
  fun `test`() {
    expect
    ":\":\\" == null
  }
}

class SpreadIntoVarargsSpec : Specification() {
  fun `test`() {
    val a = intArrayOf(2, 3)
    expect
    intArrayOf(1, *a).isEmpty()
  }
}

class VariableExpressionSpec : Specification() {
  fun `test`() {
    val x = false
    expect
    x
  }
}

class ConstructorCallSpec : Specification() {
  fun `test`() {
    val a = 1
    expect
    ArrayList<Int>(a) == null
  }
}

class InnerClassConstructorCallSpec : Specification() {
  inner class NonStaticInnerClass {
    override fun toString() = "nsi"
  }

  fun `test`() {
    expect
    NonStaticInnerClass() == null
  }
}

class ConstructorNamedArgumentsSpec : Specification() {
  class Person2(val name: String, val age: Int) {
    override fun toString() = "p"
  }

  fun `test`() {
    val fred = "fred"
    val fredsAge = 25
    expect
    Person2(name = fred, age = fredsAge) == null
  }
}

class ClassLiteralSpec : Specification() {
  fun `test`() {
    expect
    List::class.java == String::class.java
  }
}

@Suppress("ktlint:standard:comment-wrapping")
class ClassLiteralWithCommentsSpec : Specification() {
  fun `test`() {
    expect
    java.util./*.awt.*/List::class.java == java.lang.String::class.java // I. Like. Dots.
  }
}

class TypeCheckSpec : Specification() {
  fun `test`() {
    val date: Any = java.util.Date(0)
    expect
    date is Date
  }
}
