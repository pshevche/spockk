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

package io.github.pshevche.spockk.compilation.condition

import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import io.github.pshevche.spockk.compilation.BaseCompilationTest
import io.github.pshevche.spockk.compilation.TestDataFactory.specWithBody
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import spock.lang.PendingFeature

@OptIn(ExperimentalCompilerApi::class)
class VerifyMethodAnnotationValidationTest : BaseCompilationTest() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.VerifyAllMethodsSpecification#@Verify with @VerifyAll does not compile")
  fun `@Verify with @VerifyAll does not compile`() {
    `when`
    val result =
      transform(
        kotlin(
          "Assertions.kt",
          """
          object Assertions {
            @spock.lang.Verify
            @spock.lang.VerifyAll
            fun isPositiveAndEven(x: Int) {
              x > 0
              x % 2 == 0
            }
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains(
      "Method 'isPositiveAndEven' cannot be annotated with both @Verify and @VerifyAll"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.VerifyAllMethodsSpecification#@Verify with @VerifyAll does not compile in Spec")
  fun `@Verify with @VerifyAll does not compile in Spec`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
          @spock.lang.Verify
          @spock.lang.VerifyAll
          private fun isPositiveAndEven(x: Int) {
            x > 0
            x % 2 == 0
          }

          fun feature() {
            io.github.pshevche.spockk.lang.expect
            isPositiveAndEven(-3)
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains(
      "Method 'isPositiveAndEven' cannot be annotated with both @Verify and @VerifyAll"
    )
  }
}
