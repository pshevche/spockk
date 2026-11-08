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

package io.github.pshevche.spockk.compilation.parametrization

import io.github.pshevche.spockk.compilation.BaseCompilationTest
import io.github.pshevche.spockk.compilation.TestDataFactory.specWithBody
import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import spock.lang.PendingFeature

@OptIn(ExperimentalCompilerApi::class)
class DataTablesCompilationTest : BaseCompilationTest() {

  fun `basic usage`() {
    expect
    assertTransformation(sampleFromResource("parametrization/BasicDataTableSpec"))
  }

  fun `rejects unrecognized data provider syntax`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun `parameterized feature`(a: Int, b: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert((a + b) % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    listOf(a, b)
                }
                """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Spec.kt:6:5")
    result.compilation.messages.contains(
      """
        Problem with `where`
        Details: where-blocks may only contain parametrization, e.g.
           a ; b
           1 ; 2
           2 ; 3
        """
        .trimIndent()
    )
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#table with just a header is not allowed",
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#table with just a header is not allowed with semicolon"
  )
  fun `rejects tables with not enough rows`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun `parameterized feature`(a: Int, b: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert((a + b) % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    a ; b
                }
                """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Spec.kt:6:5")
    result.compilation.messages.contains(
      """
        Problem with `where`
        Details: Data table must have more than just the header row
        """
        .trimIndent()
    )
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#rows must have same number of elements as header",
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#rows must have same number of elements as header with semicolon"
  )
  fun `rejects tables with malformed rows`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun `parameterized feature`(a: Int, b: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert((a + b) % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    a ; b
                    1 ; 1 ; 2
                }
                """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Spec.kt:6:5")
    result.compilation.messages.contains(
      """
        Problem with `where`
        Details: Row #2 in the data table has a wrong number of elements (3 instead of 2)
        """
        .trimIndent()
    )
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#header may only contain variable names",
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#header may only contain variable names with semicolon"
  )
  fun `rejects data tables with malformed headers`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun `parameterized feature`(a: Int, b: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert((a + b) % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    a ; "bla"
                    1 ; 2
                }
                """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Spec.kt:6:5")
    result.compilation.messages.contains(
      """
        Problem with `where`
        Details: Header of data table may only contain variable names
        """
        .trimIndent()
    )
  }

  fun `wildcard usage`() {
    expect
    assertTransformation(sampleFromResource("parametrization/WildcardDataTableSpec"))
  }

  fun `reference another feature variable`() {
    expect
    assertTransformation(sampleFromResource("parametrization/ReferenceFeatureVariableInDataTableSpec"))
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#table must have at least two columns"
  )
  fun `table must have at least two columns`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
          fun `parameterized feature`(a: Int) {
              io.github.pshevche.spockk.lang.expect
              assert(a > 0)

              io.github.pshevche.spockk.lang.where
              a
              1
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("where-blocks may only contain parametrization")
  }

  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#header variable names must not clash with local variables",
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#header variable names must not clash with local variables with semicolon"
  )
  fun `header variable names must not clash with local variables`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
          fun `parameterized feature`(a: Int, local: Int) {
              val local = 1
              io.github.pshevche.spockk.lang.expect
              assert(a > 0)

              io.github.pshevche.spockk.lang.where
              a ; local
              1 ; 1
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Data provider must reference a feature method variable")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1047")
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#header variable names must not clash with each other",
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#header variable names must not clash with each other with semicolon"
  )
  fun `header variable names must not clash with each other`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
          fun `parameterized feature`(a: Int) {
              io.github.pshevche.spockk.lang.expect
              assert(a > 0)

              io.github.pshevche.spockk.lang.where
              a ; a
              1 ; 1
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Duplicate declaration of data variable 'a'")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1049")
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#cells cannot reference local variables (won't be found)",
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#cells cannot reference local variables (won't be found) with semicolon"
  )
  fun `cells cannot reference local variables (won't be found)`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
          fun `parameterized feature`(a: Int, b: Int) {
              val local = 42
              io.github.pshevche.spockk.lang.expect
              assert(true)

              io.github.pshevche.spockk.lang.where
              a ; b
              local ; 1
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Problem with `where`")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1049")
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#cells can't reference next cells"
  )
  fun `cells can't reference next cells`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
          fun `parameterized feature`(a: Int, b: Int) {
              io.github.pshevche.spockk.lang.expect
              assert(false)

              io.github.pshevche.spockk.lang.where
              a ; b
              b ; 1
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Problem with `where`")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1049")
  @MigratedFrom(
    "org.spockframework.smoke.parameterization.InvalidWhereBlocks#cells can't reference themselves"
  )
  fun `cells can't reference themselves`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
          fun `parameterized feature`(a: Int, b: Int) {
              io.github.pshevche.spockk.lang.expect
              assert(false)

              io.github.pshevche.spockk.lang.where
              a ; b
              1 ; b + 1
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Problem with `where`")
  }
}
