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
import io.github.pshevche.spockk.lang.where
import spock.lang.Specification
import kotlin.math.max
import kotlin.test.assertEquals

class DataTablesTest : Specification() {

  //  companion object {
  //    private const val STATIC_FIELD = 42
  //
  //    data class Person(val age: Int)
  //  }
  //
  //  @Shared
  //  private val sharedField = 42
  //
  fun `basic usage`(a: Int, b: Int, c: Int) {
    expect
    assertEquals(c, max(a, b))

    where
    // spotless:off
    a; b; c
    5; 7; 7
    3; 1; 3
    9; 9; 9
    // spotless:on
  }

  fun `estimate iterations correctly`(a: Int, b: Int, c: Int) {
    expect
    assertEquals(3, specificationContext.currentIteration.estimatedNumIterations)

    where
    // spotless:off
    a; b; c
    5; 7; 7
    3; 1; 3
    9; 9; 9
    // spotless:on
  }

  //  fun `can use pseudo-column to enable one-column table`(a: Int) {
  //      expect
  //      assertEquals(1, a)
  //
  //      where
  //    // spotless:off
//    a ; `_`
//    1 ; `_`
//    // spotless:on
  //  }
  //
  //  fun `pseudo-column can be declared as parameter`(a: Int, `_`: Int) {
  //      expect
  //      assertEquals(3, a)
  //
  //      where
  //    // spotless:off
//    a ; `_`
//    3 ; `_`
//    // spotless:on
  //  }
  //
  //  fun `tables can be mixed with other parametrization`(a: Int, b: Int, c: Int, d: Int) {
  //      expect
  //      assertEquals(listOf(1, 2, 3, 4), listOf(a, b, c, d))
  //
  //      where
  //    variable(a).from(1)
  //    // spotless:off
//    b ; c
//    2 ; 3
//    // spotless:on
  //    variable(d).from(c + 1)
  //  }
  //
  //  fun `cells may contain arbitrary expressions`(a: String, b: Person, c: Int) {
  //      expect
  //      assertEquals("oo", a)
  //      assertEquals(23, b.age)
  //      assertEquals(5, c)
  //
  //      where
  //    // spotless:off
//    a                  ; b          ; c
//    "foo".substring(1) ; Person(23) ; max(4, 5)
//    // spotless:on
  //  }
  //
  //  fun `cells can reference shared and static fields`(a: Int, b: Int) {
  //      expect
  //      assertEquals(42, a)
  //      assertEquals(42, b)
  //
  //      where
  //    // spotless:off
//    a            ; b
//    STATIC_FIELD ; sharedField
//    // spotless:on
  //  }
  //
  //  fun `cells can reference previous cells`(a: Int, b: Int, c: Int) {
  //      expect
  //      assertEquals(listOf(0, 1, 2), listOf(a, b, c))
  //
  //      where
  //    // spotless:off
//    a ; b     ; c
//    0 ; a + 1 ; b + 1
//    // spotless:on
  //  }
  //
  //  fun `cells in a data table can refer to the current value for a column to the left (plain
  // reference)`(
  //    a: Int,
  //    b: Int,
  //    c: Int
  //  ) {
  //      expect
  //      assertEquals(c, max(a, b))
  //
  //      where
  //    // spotless:off
//    a  ; b  ; c
//    10 ; 20 ; b
//    5  ; 3  ; a
//    7  ; 9  ; b
//    15 ; 11 ; a
//    // spotless:on
  //  }
  //
  //  fun `cell references are pointing to the current row`(a: Int, b: Int, c: Int) {
  //      expect
  //      assertEquals(1 + a * 2, b)
  //      assertEquals(3 * b, c)
  //
  //      where
  //    // spotless:off
//    a ; b         ; c
//    0 ; 1 + a * 2 ; 3 * b
//    1 ; 1 + a * 2 ; 3 * b
//    2 ; 1 + a * 2 ; 3 * b
//    // spotless:on
  //  }
  //
  //  fun `cell references are evaluated correctly in the method's name (a = #a, b = #b)`(
  //    a: Int,
  //    b: Int
  //  ) {
  //      expect
  //    true
  //
  //      where
  //    // spotless:off
//    a ; b
//    0 ; a + 1
//    2 ; a
//    // spotless:on
  //  }
  //
  //  fun `data tables can be referenced from following variables`(a: Int, b: Int, c: Int) {
  //      expect
  //      assertEquals(3, c)
  //
  //      where
  //    // spotless:off
//    a ; b
//    1 ; 2
//    1 ; a + 1
//    // spotless:on
  //
  //    variable(c).from(b + 1)
  //  }
  //
  //  fun `derived data variables do not break data table previous column references`(
  //    x: Int,
  //    y: Int,
  //    z: Int,
  //    a: Int
  //  ) {
  //      expect
  //      assertEquals(y, z)
  //
  //      where
  //    variable(x).from(1)
  //
  //      and
  //    // spotless:off
//    y ; z ; a
//    1 ; y ; y + z
//    2 ; y ; y + z
//    // spotless:on
  //  }
  //
  //  fun `data pipe variables do not break data table previous column references`(
  //    x: Int,
  //    y: Int,
  //    z: Int,
  //    a: Int
  //  ) {
  //      expect
  //      assertEquals(y, z)
  //
  //      where
  //    variable(x).from(listOf(1, 2))
  //
  //      and
  //    // spotless:off
//    y ; z ; a
//    3 ; y ; y + z
//    4 ; y ; y + z
//    // spotless:on
  //  }
}
