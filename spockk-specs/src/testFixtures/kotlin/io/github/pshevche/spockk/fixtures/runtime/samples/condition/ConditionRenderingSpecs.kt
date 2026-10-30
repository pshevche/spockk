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
import org.spockframework.runtime.FailedStringComparisonRenderer
import spock.lang.Specification

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

class StringDifferenceSpec : Specification() {
  fun `test`() {
    expect
    "the quick" == "the quirk"
  }
}

class StringNullRightSpec : Specification() {
  fun `test`() {
    expect
    "foo" == null
  }
}

class StringNullLeftSpec : Specification() {
  fun `test`() {
    expect
    null == "foo"
  }
}

class StringDifferenceInSubexpressionSpec : Specification() {
  fun `test`() {
    expect
    ("the quick" == "the quirk") == true
  }
}

class EqualStringsInSubexpressionSpec : Specification() {
  fun `test`() {
    expect
    ("the quick" == "the quick") == false
  }
}

fun largeString(
  source: String = "aaaaaaaaaaaaaaaa",
  length: Int = (FailedStringComparisonRenderer.MAX_EDIT_DISTANCE_MEMORY / 2).toInt()
): StringBuilder {
  val sb = StringBuilder(length + 10)
  var i = 0
  while (i < length) {
    sb.append(source, 0, minOf(length - i, source.length))
    i += source.length
  }
  return sb
}

class LargeStringDifferenceAtStartSpec : Specification() {
  fun `test`() {
    val sb = largeString()
    val a = sb.toString()
    sb.setCharAt(0, 'b')
    val b = sb.toString()
    expect
    a == b
  }
}

class LargeStringDifferenceInMiddleSpec : Specification() {
  fun `test`() {
    val sb = largeString()
    val a = sb.toString()
    sb.setCharAt(sb.length / 2, 'b')
    val b = sb.toString()
    expect
    a == b
  }
}

class LargeStringDifferenceAtEndSpec : Specification() {
  fun `test`() {
    val sb = largeString()
    val a = sb.toString()
    sb.setCharAt(sb.length - 1, 'b')
    val b = sb.toString()
    expect
    a == b
  }
}

class LargeStringDifferenceAtStartAndEndSpec : Specification() {
  fun `test`() {
    val sb = largeString()
    val a = sb.toString()
    sb.setCharAt(0, 'b')
    sb.setCharAt(sb.length - 1, 'b')
    val b = sb.toString()
    expect
    a == b
  }
}

class LargeStringCompleteDifferenceSpec : Specification() {
  fun `test`() {
    val a = largeString().toString()
    val b = largeString("bbbbbbbbbbbbbbbb").toString()
    expect
    a == b
  }
}

class LargeStringWithoutContextSpec : Specification() {
  fun `test`() {
    val stringLength = Math.sqrt(FailedStringComparisonRenderer.MAX_EDIT_DISTANCE_MEMORY.toDouble()).toInt()
    val common = largeString("cccccccccccccccc", stringLength)
    val a = largeString("aaaaaaaaaaaaaaaa", stringLength).append(common).toString()
    val b = largeString("bbbbbbbbbbbbbbbb", stringLength).append(common).toString()
    expect
    a == b
  }
}

class StringDiffIntOverflowSpec : Specification() {
  fun `test`() {
    val stringLength = (Math.sqrt(Int.MAX_VALUE.toDouble()) * 2).toInt()
    val a = largeString("aaaaaaaaaaaaaaaa", stringLength).toString()
    val b = largeString("bbbbbbbbbbbbbbbb", stringLength).toString()
    expect
    a == b
  }
}

class StringLiteralsWithLineBreaksSpec : Specification() {
  fun `test`() {
    expect
    """foo
""" == """bar

"""
  }
}

class StringLiteralsWithNewlineEscapesSpec : Specification() {
  fun `test`() {
    expect
    "foo  " == "bar  "
  }
}

class StringLiteralsWithLineBreaksAndNewlineEscapesSpec : Specification() {
  fun `test`() {
    expect
    "\\\nfoo" == "\\\nfoo       \n"
  }
}

class InterpolatedStringLiteralsSpec : Specification() {
  fun `test`() {
    val a = "foo"
    val b = "bar"
    expect
    "$a\n" == "\\$b \n"
  }
}

class LongStringLiteralsSpec : Specification() {
  fun `test`() {
    expect
    "Lorem ipsum\n\nLorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua." ==
      "Lorem ipsum\nLorem ipsum  dolor sit amet,  consetetur sadipscing elitr, sed  diam nonumy eirmod tempor  invidunt ut labore et  dolore magna aliquyam erat, sed diam voluptua.\n"
  }
}
