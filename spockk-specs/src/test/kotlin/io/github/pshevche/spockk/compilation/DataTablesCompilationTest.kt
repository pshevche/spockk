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

package io.github.pshevche.spockk.compilation

import io.github.pshevche.spockk.compilation.TestDataFactory.specWithFeature
import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class DataTablesCompilationTest : BaseCompilationTest() {

  fun `rejects tables with not enough rows`() {
    `when`
    val result =
      transform(
        specWithFeature(
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
    assert(!result.isSuccess())
    assert(result.compilation.messages.contains("Spec.kt:6:5"))
    assert(
      result.compilation.messages.contains(
        """
            Problem with `where`
            Details: Data table must contain at least two rows: a single header row and at least one values row
            """
          .trimIndent()
      )
    )
  }

  fun `rejects tables with malformed rows`() {
    `when`
    val result =
      transform(
        specWithFeature(
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
    assert(!result.isSuccess())
    assert(result.compilation.messages.contains("Spec.kt:6:5"))
    assert(
      result.compilation.messages.contains(
        """
            Problem with `where`
            Details: All data table rows must have the same number of elements
            """
          .trimIndent()
      )
    )
  }

  fun `rejects rows with not enough elements`() {
    `when`
    val result =
      transform(
        specWithFeature(
          """
                fun `parameterized feature`(a: Int, b: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert((a + b) % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    a
                    1
                }
                """
            .trimIndent()
        )
      )

    then
    assert(!result.isSuccess())
    assert(result.compilation.messages.contains("Spec.kt:6:5"))
    assert(
      result.compilation.messages.contains(
        """
            Problem with `where`
            Details: Data table rows must have at least two elements
            """
          .trimIndent()
      )
    )
  }

  fun `rejects data tables with malformed headers`() {
    `when`
    val result =
      transform(
        specWithFeature(
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
    assert(!result.isSuccess())
    assert(result.compilation.messages.contains("Spec.kt:6:5"))
    assert(
      result.compilation.messages.contains(
        """
            Problem with `where`
            Details: The first row of the data table must be a header referencing feature parameters
            """
          .trimIndent()
      )
    )
  }

  fun `single data table with multiple variables`() {
    expect
    assertTransformation(sampleFromResource("FeatureWithDataTable"))
  }
}
