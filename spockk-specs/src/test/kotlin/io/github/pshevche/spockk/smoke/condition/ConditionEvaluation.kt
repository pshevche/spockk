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

package io.github.pshevche.spockk.smoke.condition

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.ConditionFailedWithExceptionError
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.Specification
import java.lang.Integer.MAX_VALUE
import java.lang.Math.max
import java.lang.Math.min
import java.lang.Thread.State.BLOCKED
import java.util.Arrays
import java.util.regex.Pattern

/**
 * Checks that conditions built from each expression shape are transformed without a compilation
 * error, evaluate without a runtime error, and produce the expected result.
 */
class ConditionEvaluation : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#multi-line condition")
  fun `multi-line condition`() {
    expect
    2 *
      3 ==

      6
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#failing multi-line condition")
  fun `failing multi-line condition`() {
    expect
    2 *
      3 ==

      7
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#MethodCallExpression")
  fun `MethodCallExpression`() {
    expect
    listOf(1, 2, 3).size == 3
    listOf(1, 2, 3).javaClass.getMethod("size").parameterTypes.size == 0
    Integer.valueOf(String.format("%d", 10)) == 10
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#MethodCallExpression with named arguments")
  fun `MethodCallExpression with named arguments`() {
    expect
    Person().eat(what = "steak", where = "tokyo") == mapOf("what" to "steak", "where" to "tokyo")
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#MethodCallExpression with named arguments passed as map")
  fun `MethodCallExpression with named arguments passed as map`() {
    expect
    Person().eat(mapOf("what" to "steak", "where" to "tokyo")) == mapOf("what" to "steak", "where" to "tokyo")
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#StaticMethodCallExpression")
  fun `StaticMethodCallExpression`() {
    expect
    max(1, 2) == 2
    max(min(1, 2), 3) == 3
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#ConstructorCallExpression")
  fun `ConstructorCallExpression`() {
    expect
    ArrayList<Any>().isEmpty()
    String("abc".toCharArray()) == "abc"
    String(String("abc".toCharArray()).toCharArray()) == "abc"
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#ConstructorCallExpression with named arguments")
  fun `ConstructorCallExpression with named arguments`() {
    expect
    Person(name = "fred", age = 25).name == "fred"
    Person(name = "fred", age = 25).age == 25
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#BinaryExpression")
  fun `BinaryExpression`() {
    expect
    1 == 1
    2 > 1 && 1 < 2
    1 * 1 / 1 + 1 - 1 == 1
    1 == listOf(listOf(listOf(listOf(listOf(1)))))[0][0][0][0][0]
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#PrefixExpression")
  fun `PrefixExpression`() {
    var x = 0

    expect
    ++x == 1
    --x == 0
    x == 0
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#ClosureExpression")
  fun `ClosureExpression`() {
    var x = 0

    `when`
    val second = { assert(++x == 2) }
    val third = { assert(++x == 3) }
    val test = {
      assert(++x == 1)
      second()
      third
    }()

    then
    x == 2

    `when`
    test()

    then
    x == 3
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#MapExpression")
  fun `MapExpression`() {
    expect
    emptyMap<String, Int>().isEmpty()
    mapOf("a" to 1) + mapOf("b" to 2) == mapOf("a" to 1, "b" to 2)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#ListExpression")
  fun `ListExpression`() {
    expect
    listOf(1, 2, 3).size == 3
    emptyList<Int>() + listOf(1) + listOf(2, 3) == listOf(1, 2, 3)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#RangeExpression")
  fun `RangeExpression`() {
    expect
    (1..3).contains(3)
    !((1 until 3).contains(3))
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#PropertyExpression")
  fun `PropertyExpression`() {
    expect
    Person(age = 3).age == 3
    Properties().next.next.next.assign(10) == 10
    Int.MIN_VALUE < Int.MAX_VALUE
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#MethodPointerExpression")
  fun `MethodPointerExpression`() {
    val pointers = MethodPointers()

    expect
    pointers::inc.invoke(1) == 2
    listOf(1, 2, 3).map(pointers::inc) == listOf(2, 3, 4)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#ConstantExpression")
  fun `ConstantExpression`() {
    expect
    1 == 1
    1.toDouble() == 1.0
    "abc".reversed() == "cba"
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#ClassExpression")
  fun `ClassExpression`() {
    expect
    ConditionEvaluation::class.java == javaClass
    ConditionEvaluation::class.java.javaClass == Class::class.java
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#VariableExpression")
  fun `VariableExpression`() {
    val x = 1
    val y = 2

    expect
    x < y
    x + y == 2 * y - x
    Math.max(x, y) == 2
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#RegexExpression")
  fun `RegexExpression`() {
    expect
    Pattern.compile("ab*a").matcher("abbba").matches()
    !Pattern.compile("ab*a").matcher("abcba").matches()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#GStringExpression")
  fun `GStringExpression`() {
    val x = 1
    val y = listOf(1, 2, 3)

    expect
    "$x and ${y.size}" == "1 and 3"
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#ArrayExpression")
  fun `ArrayExpression`() {
    expect
    intArrayOf(1, 2, 3).size == 3
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#SpreadExpression")
  fun `SpreadExpression`() {
    expect
    add(*intArrayOf(1, 2)) == 3
    listOf(1, *arrayOf(2, *arrayOf(3, *arrayOf(4)))) == listOf(1, 2, 3, 4)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#NotExpression")
  fun `NotExpression`() {
    expect
    !false
    !(true && false)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#UnaryMinusExpression")
  fun `UnaryMinusExpression`() {
    expect
    -(-1) == 1
    -1 + -2 == -3
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#UnaryPlusExpression")
  fun `UnaryPlusExpression`() {
    expect
    +(+2) == 2
    +1 + +2 == +3
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#BitwiseNegationExpression")
  fun `BitwiseNegationExpression`() {
    expect
    1.inv() == -2
    1.inv().inv() == 1
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#CastExpression")
  fun `CastExpression`() {
    expect
    (listOf(1, 2, 3) as Collection<*>).isNotEmpty()
    intArrayOf(1, 2, 3).javaClass.isArray
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#ArgumentListExpression")
  fun `ArgumentListExpression`() {
    expect
    3.toString() == "3"
    Arrays.asList(1, 2, 3) == listOf(1, 2, 3)
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#statically imported field")
  fun `statically imported field`() {
    expect
    MAX_VALUE == 2147483647
  }

  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#statically imported enum value")
  fun `statically imported enum value`() {
    expect
    BLOCKED is Thread.State
  }

  @FailsWith(ConditionFailedWithExceptionError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ConditionEvaluation#regular implicit condition")
  fun `regular implicit condition`() {
    expect
    customContains("foo", "bar")
  }

  private fun add(vararg xs: Int) = xs.sum()

  private fun customContains(a: String, b: String): Boolean {
    if (!a.contains(b)) {
      throw AssertionError("'$a' does not contain '$b'")
    }
    return true
  }

  private class Properties {
    val next get() = this
    var x: Any? = null

    fun assign(value: Any?): Any? {
      x = value
      return value
    }
  }

  private class MethodPointers {
    fun inc(x: Int) = x + 1
  }

  private class Person(val name: Any? = null, val age: Any? = null) {
    fun eat(args: Map<String, String>) = args

    fun eat(what: String, where: String) = mapOf("what" to what, "where" to where)
  }
}
