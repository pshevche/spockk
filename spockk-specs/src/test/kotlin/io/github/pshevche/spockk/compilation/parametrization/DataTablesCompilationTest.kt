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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.assertContains
import kotlin.test.assertFalse

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
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:6:5")
    assertContains(
      result.compilation.messages,
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
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:6:5")
    assertContains(
      result.compilation.messages,
      """
        Problem with `where`
        Details: Data table must have more than just the header row
        """
        .trimIndent()
    )
  }

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
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:6:5")
    assertContains(
      result.compilation.messages,
      """
        Problem with `where`
        Details: Row #2 in the data table has a wrong number of elements (3 instead of 2)
        """
        .trimIndent()
    )
  }

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
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:6:5")
    assertContains(
      result.compilation.messages,
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
}
