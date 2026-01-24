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
class DataPipesCompilationTest : BaseCompilationTest() {

  fun `rejects data pipes with non-inline data pipe declaration`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun `parameterized feature`(a: Int, b: Int) {
                    io.github.pshevche.spockk.lang.given
                    val dataPipeVars = io.github.pshevche.spockk.lang.variables(a, b)

                    io.github.pshevche.spockk.lang.expect
                    assert((a + b) % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    dataPipeVars.from(listOf(1, 2), listOf(3, 4))
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:9:5")
    assertContains(
      result.compilation.messages,
      """
        Problem with `where`
        Details: Data pipe target must be declared inline as 'variable(a)' or 'variables(a, b)'
        """
        .trimIndent()
    )
  }

  fun `rejects data pipes that do not target feature variables`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun `parameterized feature`(a: Int) {
                    io.github.pshevche.spockk.lang.given
                    var nonFeatureVariable: Int? = null

                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    io.github.pshevche.spockk.lang.variables(a, nonFeatureVariable).from(listOf(1, 2), listOf(3, 4))
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:9:5")
    assertContains(
      result.compilation.messages,
      """
        Problem with `where`
        Details: Data provider must reference a feature method variable
        """
        .trimIndent()
    )
  }

  fun `single variable single value`() {
    expect
    assertTransformation(sampleFromResource("parametrization/SingleVariableSingleValueSpec"))
  }

  fun `single variable vararg values`() {
    expect
    assertTransformation(sampleFromResource("parametrization/SingleVariableVarargValuesSpec"))
  }

  fun `single variable values list`() {
    expect
    assertTransformation(sampleFromResource("parametrization/SingleVariableValuesListSpec"))
  }

  fun `single data pipe with multiple variables`() {
    expect
    assertTransformation(sampleFromResource("parametrization/SinglePipeMultiVariableSpec"))
  }

  fun `multiple data pipes`() {
    expect
    assertTransformation(sampleFromResource("parametrization/MultiPipeMultiVariableSpec"))
  }

  fun `reference another feature variable`() {
    expect
    assertTransformation(sampleFromResource("parametrization/ReferenceFeatureVariableSpec"))
  }

  fun `rejects data pipes referencing other variables as iteration values`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun `parameterized feature`(a: Int, b: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(b == a)

                    io.github.pshevche.spockk.lang.where
                    io.github.pshevche.spockk.lang.variable(a).from(1, 2, 3)
                    io.github.pshevche.spockk.lang.variable(b).from(1, a, 3)
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
        Details: Data pipes may reference other feature variables only if the reference is the only value (valid: 'variable(a).from(other + 1)'; invalid: 'variable(a).from(1, other)')
        """
        .trimIndent()
    )
  }

  fun `rejects data pipes referencing other variables in multi-variables variant`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun `parameterized feature`(a: Int, b: Int, c: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(b == a)

                    io.github.pshevche.spockk.lang.where
                    io.github.pshevche.spockk.lang.variable(a).from(listOf(1, 2))
                    io.github.pshevche.spockk.lang.variables(b, c).from(listOf(1, 2), listOf(1, a))
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
        Details: Data pipes may reference other feature variables only if the reference is the only value (valid: 'variable(a).from(other + 1)'; invalid: 'variable(a).from(1, other)')
        """
        .trimIndent()
    )
  }
}
