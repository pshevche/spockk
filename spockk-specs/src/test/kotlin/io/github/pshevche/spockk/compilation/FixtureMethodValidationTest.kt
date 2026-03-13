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

package io.github.pshevche.spockk.compilation

import com.tschuchort.compiletesting.SourceFile
import io.github.pshevche.spockk.compilation.TestDataFactory.specWithBody
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
class FixtureMethodValidationTest : BaseCompilationTest() {

  fun `accepts valid fixture methods`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun setup() { println("setup") }
                fun cleanup() { println("cleanup") }
                fun setupSpec() { println("setupSpec") }
                fun cleanupSpec() { println("cleanupSpec") }

                fun `some feature`() {
                    io.github.pshevche.spockk.lang.expect
                    assert(true)
                }
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `discards fixture method with block labels`(fixtureMethodName: String) {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun $fixtureMethodName() {
                    io.github.pshevche.spockk.lang.given
                    println("body")
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(
      result.compilation.messages,
      "Fixture method '$fixtureMethodName' must not contain block labels, but found 'given'"
    )

    where
    variable(fixtureMethodName).from("setup", "cleanup", "setupSpec", "cleanupSpec")
  }

  fun `discards fixture method calling super`(fixtureMethodName: String) {
    `when`
    val result =
      transform(
        SourceFile.kotlin(
          "Spec.kt",
          """
                abstract class BaseSpec : spock.lang.Specification() {
                    open fun $fixtureMethodName() { println("base") }
                }

                class Spec : BaseSpec() {
                    override fun $fixtureMethodName() {
                        super.$fixtureMethodName()
                    }
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(
      result.compilation.messages,
      "Fixture method '$fixtureMethodName' must not call 'super.$fixtureMethodName()'"
    )

    where
    variable(fixtureMethodName).from("setup", "cleanup", "setupSpec", "cleanupSpec")
  }

  fun `accepts methods with fixture names but with parameters as regular methods`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
                fun setup(x: Int) { println(x) }

                fun `some feature`() {
                    io.github.pshevche.spockk.lang.expect
                    assert(true)
                }
                """
            .trimIndent()
        )
      )

    then
    assertTrue(result.isSuccess())
  }

  fun `discards spec-scoped fixture method accessing instance field`(fixtureMethodName: String) {
    `when`
    val result =
      transform(
        specWithBody(
          """
                var instanceField = "hello"

                fun $fixtureMethodName() {
                    println(instanceField)
                }

                fun `some feature`() {
                    io.github.pshevche.spockk.lang.expect
                    assert(true)
                }
                """
            .trimIndent()
        )
      )

    then
    assertFalse(result.isSuccess())
    assertContains(
      result.compilation.messages,
      "Only companion object members and @Shared fields may be accessed from here"
    )

    where
    variable(fixtureMethodName).from("setupSpec", "cleanupSpec")
  }
}
