/*
 * Copyright 2025 the original author or authors.
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

package io.github.pshevche.spockk.smoke.parametrization

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.where
import spock.lang.Shared
import spock.lang.Specification
import kotlin.math.max
import kotlin.test.assertEquals

class DataPipesSmokeTest : Specification() {

//  companion object {
//    private const val STATIC_FIELD = 42
//
//    data class Person(val age: Int)
//  }
//
//  @Shared
//  private val sharedField = 42
//
//  fun `single variable single value`(a: Int) {
//    expect
//    assertEquals(1, a)
//
//    where
//    variable(a).from(1)
//  }
//
//  fun `single variable vararg values`(a: Int) {
//    expect
//    assertEquals(0, a % 2)
//
//    where
//    variable(a).from(2, 4, 6)
//  }
//
//  fun `single variable values list`(a: Int) {
//    expect
//    assertEquals(0, a % 2)
//
//    where
//    variable(a).from(listOf(2, 4, 6))
//  }
//
//  fun `single data pipe with multiple variables`(a: Int, b: Int, c: Int) {
//    expect
//    assertEquals(c, a + b)
//
//    where
//    variables(a, b, c).from(listOf(1, 2), listOf(3, 4), listOf(4, 6))
//  }
//
//  fun `multiple data pipes`(a: Int, b: Int, c: Int) {
//    expect
//    assertEquals(c, a + b)
//
//    where
//    variables(a, b).from(listOf(1, 2), listOf(3, 4))
//    variable(c).from(listOf(4, 6))
//  }
//
//  fun `pipes may contain arbitrary expressions`(a: String, b: Person, c: Int) {
//    expect
//    assertEquals("oo", a)
//    assertEquals(23, b.age)
//    assertEquals(5, c)
//
//    where
//    variables(a, b, c).from(
//      listOf("foo".substring(1)),
//      listOf(Person(23)),
//      listOf(max(4, 5))
//    )
//  }
//
//  fun `pipes can reference shared and static fields`(a: Int, b: Int) {
//    expect
//    assertEquals(42, a)
//    assertEquals(42, b)
//
//    where
//    variable(a).from(STATIC_FIELD)
//    variable(b).from(sharedField)
//  }
//
//  fun `pipes can reference previous variables`(a: Int, b: Int, c: Int) {
//    expect
//    assertEquals(listOf(0, 1, 2), listOf(a, b, c))
//
//    where
//    variables(a, b, c).from(listOf(0), listOf(a + 1), listOf(b + 1))
//  }
}
