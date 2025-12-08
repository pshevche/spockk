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

import io.github.pshevche.spockk.compilation.TestDataFactory.specWithFeatureBody
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import spock.lang.Specification
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
class FeatureBlockStructureValidationTest : Specification() {

  fun `accepts valid block sequences (single expectation)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.expect
                assert(true)
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `accepts valid block sequences (multiple expectations)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.expect
                assert(true)

                io.github.pshevche.spockk.lang.and
                assert(true)

                io.github.pshevche.spockk.lang.and
                assert(true)
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `accepts valid block sequences (single expectation with precondition)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.given
                val a = 1

                io.github.pshevche.spockk.lang.expect
                assert(a == 1)
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `accepts valid block sequences (single expectation with multiple preconditions)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.given
                val a = 1

                io.github.pshevche.spockk.lang.and
                val b = 1

                io.github.pshevche.spockk.lang.expect
                assert(a + b == 2)
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `accepts valid block sequences (single expectation with single action and precondition)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.given
                val a = 1
                val b = 1

                io.github.pshevche.spockk.lang.`when`
                val c = a + b

                io.github.pshevche.spockk.lang.then
                assert(c == 2)
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `accepts valid block sequences (single expectation with multiple actions)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.`when`
                val a = 1

                io.github.pshevche.spockk.lang.and
                val b = 1

                io.github.pshevche.spockk.lang.and
                val c = a + b

                io.github.pshevche.spockk.lang.then
                assert(c == 2)
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `accepts valid block sequences (multiple expectations with single action)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.`when`
                val a = 1
                val b = 1

                io.github.pshevche.spockk.lang.then
                assert(a == 1)

                io.github.pshevche.spockk.lang.and
                assert(b == 1)
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `accepts valid block sequences (multiple expectations with multiple actions)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.`when`
                val a = 1

                io.github.pshevche.spockk.lang.then
                assert(a == 1)

                io.github.pshevche.spockk.lang.`when`
                val b = 1

                io.github.pshevche.spockk.lang.then
                assert(b == 1)
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `discards invalid block sequences (precondition with missing expectation)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.given
                val a = 1
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:3:21")
    assertContains(
      result.compilation.messages,
      """
        Problem with `given`
        Details: Expected to find one of spockk blocks ['and', 'when', 'expect'], but reached the end of the feature method
        """
        .trimIndent()
    )
  }

  fun `discards invalid block sequences (action with missing expectation)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.`when`
                val a = 1
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:3:21")
    assertContains(
      result.compilation.messages,
      """
        Problem with `when`
        Details: Expected to find one of spockk blocks ['and', 'then'], but reached the end of the feature method
        """
        .trimIndent()
    )
  }

  fun `discards invalid block sequences (invalid block order)`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
                io.github.pshevche.spockk.lang.`when`
                val a = 1

                io.github.pshevche.spockk.lang.expect
                assert(a == 1)
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(result.compilation.messages, "Spec.kt:6:1")
    assertContains(
      result.compilation.messages,
      """
        Problem with `expect`
        Details: Expected to find one of spockk blocks ['and', 'then'], but encountered 'expect'
        """
        .trimIndent()
    )
  }
}
