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
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
class FieldAccessCompilationTest : BaseCompilationTest() {

  fun `data pipes can reference companion object fields`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                companion object {
                  private val staticField = 2
                }

                fun `field access`(a: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    io.github.pshevche.spockk.lang.variable(a).from(1, staticField)
                }
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `data tables can reference companion object fields`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                companion object {
                  private val staticField = 2
                }

                fun `field access`(a: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    a ; `_`
                    1 ; `_`
                    staticField ; `_`
                }
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `data pipes can invoke companion methods`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                companion object {
                  fun values() = listOf(1, 2)
                }

                fun `method access`(a: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    io.github.pshevche.spockk.lang.variable(a).from(values())
                }
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `data tables can invoke companion methods`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                companion object {
                  fun two() = 2
                }

                fun `method access`(a: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    a ; `_`
                    1 ; `_`
                    two() ; `_`
                }
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `data pipes cannot reference non-companion fields`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                private val where = 2

                fun `field access`(a: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    io.github.pshevche.spockk.lang.variable(a).from(1, where)
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(
      result.compilation.messages,
      """
        Problem with `where`
        Details: Only companion object members may be accessed from here
        """
        .trimIndent()
    )
  }

  fun `data tables cannot reference non-companion fields`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                private val where = 2

                fun `field access`(a: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    a ; `_`
                    1 ; `_`
                    where ; `_`
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(
      result.compilation.messages,
      """
        Problem with `where`
        Details: Only companion object members may be accessed from here
        """
        .trimIndent()
    )
  }

  fun `data pipes cannot invoke non-companion methods`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun values() = listOf(1, 2)

                fun `field access`(a: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    io.github.pshevche.spockk.lang.variable(a).from(values())
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(
      result.compilation.messages,
      """
        Problem with `where`
        Details: Only companion object members may be accessed from here
        """
        .trimIndent()
    )
  }

  fun `data tables cannot invoke non-companion methods`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun two() = 2

                fun `field access`(a: Int) {
                    io.github.pshevche.spockk.lang.expect
                    assert(a % 2 == 0)

                    io.github.pshevche.spockk.lang.where
                    a ; `_`
                    1 ; `_`
                    two() ; `_`
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(
      result.compilation.messages,
      """
        Problem with `where`
        Details: Only companion object members may be accessed from here
        """
        .trimIndent()
    )
  }
}
