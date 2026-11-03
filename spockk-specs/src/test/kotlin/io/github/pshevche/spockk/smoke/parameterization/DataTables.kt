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

package io.github.pshevche.spockk.smoke.parameterization

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.datatables.CellReferencesInMethodNameFixture
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import kotlin.math.max

class DataTables : Specification() {

  companion object {
    private const val STATIC_FIELD = 42

    data class Person(val age: Int)
  }

  @Shared
  private val sharedField = 42

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.DataTables#basic usage",
    "org.spockframework.smoke.parameterization.DataTables#basic usage with semicolon"
  )
  fun `basic usage`(a: Int, b: Int, c: Int) {
    expect
    max(a, b) == c

    where
    a ; b ; c
    5 ; 7 ; 7
    3 ; 1 ; 3
    9 ; 9 ; 9
  }

  fun `basic usage with collections`(a: List<Int>, b: List<Int>, c: List<Int>) {
    expect
    a + b == c

    where
    a            ; b            ; c
    listOf(1, 2) ; listOf(3)    ; listOf(1, 2, 3)
    listOf(1, 2) ; listOf(3, 4) ; listOf(1, 2, 3, 4)
  }

  fun `basic usage with custom objects`(younger: Person, older: Person) {
    expect
    younger.age < older.age

    where
    younger    ; older
    Person(10) ; Person(20)
    Person(20) ; Person(30)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#columns can be declared as parameters")
  fun `columns can be declared as parameters`(a: Any?, b: String) {
    expect
    a == 3
    b == "wow"

    where
    a ; b
    3 ; "wow"
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#semicolon separated tables can use boolean operators in cells")
  fun `semicolon separated tables can use boolean operators in cells`(a: Boolean, b: Boolean) {
    expect
    a
    b

    where
    a            ; b
    true or false ; true || false
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#data table previous column references work in closures")
  fun `data table previous column references work in closures`(x: Int, y: Int, z: Int) {
    expect
    x == y
    z == 2 * x

    where
    x ; y           ; z
    1 ; run { x }; x + y
    2 ; run { x }; x + y
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#data table previous column references work across data tables")
  fun `data table previous column references work across data tables`(
    x: Int,
    y: Int,
    z: Int,
    f: Int,
    a: Int,
    b: Int,
    c: Int
  ) {
    expect
    x == y
    z == 2 * x
    a == x
    b == x
    c == z

    where
    x ; y         ; z
    1 ; x         ; x + y
    2 ; run { x }; x + y

    and
    variable(f).from(1)

    and
    a ; b         ; c
    1 ; run { x }; x + y
    2 ; run { x }; x + y
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#estimate iterations correctly")
  fun `estimate iterations correctly`(a: Int, b: Int, c: Int) {
    expect
    specificationContext.currentIteration.estimatedNumIterations == 3

    where
    a ; b ; c
    5 ; 7 ; 7
    3 ; 1 ; 3
    9 ; 9 ; 9
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.DataTables#can use pseudo-column to enable one-column table",
    "org.spockframework.smoke.parameterization.DataTables#can use pseudo-column to enable one-column table with semicolon",
    "org.spockframework.smoke.parameterization.DataTables#pseudo-column can be omitted from parameter list"
  )
  fun `can use pseudo-column to enable one-column table`(a: Int) {
    expect
    a == 1

    where
    a ; `_`
    1 ; `_`
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#pseudo-column can be declared as parameter")
  fun `pseudo-column can be declared as parameter`(a: Int, `_`: Any) {
    expect
    a == 3

    where
    a ; `_`
    3 ; `_`
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.DataTables#tables can be mixed with other parameterizations",
    "org.spockframework.smoke.parameterization.DataTables#tables with semicolon can be mixed with other parameterizations"
  )
  fun `tables can be mixed with other parameterizations`(a: Int, b: Int, c: Int, d: Int) {
    expect
    listOf(a, b, c, d) == listOf(1, 2, 3, 4)

    where
    variable(a).from(1)

    and
    b ; c
    2 ; 3
    variable(d).from(c + 1)
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.DataTables#cells may contain arbitrary expressions",
    "org.spockframework.smoke.parameterization.DataTables#cells may contain arbitrary expressions with semicolon"
  )
  fun `cells may contain arbitrary expressions`(a: String, b: Person, c: Int) {
    expect
    a == "oo"
    b.age == 23
    c == 5

    where
    a                  ; b          ; c
    "foo".substring(1) ; Person(23) ; max(4, 5)
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.DataTables#cells can reference shared and static fields",
    "org.spockframework.smoke.parameterization.DataTables#cells can reference shared and static fields with semicolon"
  )
  fun `cells can reference shared and static fields`(a: Int, b: Int) {
    expect
    a == 42
    b == 42

    where
    a            ; b
    STATIC_FIELD ; sharedField
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.DataTables#cells can reference previous cells",
    "org.spockframework.smoke.parameterization.DataTables#cells can reference previous cells with semicolon"
  )
  fun `cells can reference previous cells`(a: Int, b: Int, c: Int) {
    expect
    listOf(a, b, c) == listOf(0, 1, 2)

    where
    a ; b     ; c
    0 ; a + 1 ; b + 1
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#cells in a data table can refer to the current value for a column to the left (plain reference)")
  fun `cells in a data table can refer to the current value for a column to the left (plain reference)`(
    a: Int,
    b: Int,
    c: Int
  ) {
    expect
    max(a, b) == c

    where
    a  ; b  ; c
    10 ; 20 ; b
    5  ; 3  ; a
    7  ; 9  ; b
    15 ; 11 ; a
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#cell references are pointing to the current row")
  fun `cell references are pointing to the current row`(a: Int, b: Int, c: Int) {
    expect
    b == 1 + a * 2
    c == 3 * b

    where
    a ; b         ; c
    0 ; 1 + a * 2 ; 3 * b
    1 ; 1 + a * 2 ; 3 * b
    2 ; 1 + a * 2 ; 3 * b
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#cell references are evaluated correctly in the method's name")
  fun `cell references are evaluated correctly in the method's name`() {
    `when`
    val events = execute(selectClass(CellReferencesInMethodNameFixture::class.java))
    val displayNames = events.finished().list().map { it.testDescriptor.displayName }

    then
    displayNames == listOf("a = 0, b = 1", "a = 2, b = 2", "a = #a, b = #b")
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#data tables can be referenced from following variables")
  fun `data tables can be referenced from following variables`(a: Int, b: Int, c: Int) {
    expect
    c == 3

    where
    a ; b
    1 ; 2
    1 ; a + 1

    variable(c).from(b + 1)
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#derived data variables do not break data table previous column references")
  fun `derived data variables do not break data table previous column references`(
    x: Int,
    y: Int,
    z: Int,
    a: Int
  ) {
    expect
    z == y

    where
    variable(x).from(1)

    and
    y ; z ; a
    1 ; y ; y + z
    2 ; y ; y + z
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.DataTables#data pipe variables do not break data table previous column references")
  fun `data pipe variables do not break data table previous column references`(
    x: Int,
    y: Int,
    z: Int,
    a: Int
  ) {
    expect
    y == z

    where
    variable(x).from(listOf(1, 2))

    y ; z ; a
    3 ; y ; y + z
    4 ; y ; y + z
  }

  @Issue("https://github.com/pshevche/spockk/issues/276")
  fun `data tables with optional arguments`(
    actual: Boolean?,
    expected: Boolean?
  ) {
    expect
    specificationContext.currentIteration.estimatedNumIterations == 3
    actual == expected

    where
    actual ; expected
    false  ; false
    true   ; true
    null   ; null
  }
}
