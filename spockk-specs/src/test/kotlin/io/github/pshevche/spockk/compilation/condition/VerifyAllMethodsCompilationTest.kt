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
import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import spock.lang.PendingFeature

@OptIn(ExperimentalCompilerApi::class)
class VerifyAllMethodsCompilationTest : BaseCompilationTest() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#transforms conditions in methods inside spec classes")
  fun `transforms conditions in methods inside spec classes`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllMethodInSpec"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#transforms conditions in methods outside of spec classes")
  fun `transforms conditions in methods outside of spec classes`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllMethodOutsideSpec"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#transforms conditions in private methods")
  fun `transforms conditions in private methods`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllPrivateMethod"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#transforms conditions in static methods")
  fun `transforms conditions in static methods`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllStaticMethod"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#transforms explicit conditions")
  fun `transforms explicit conditions`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllExplicitConditions"))
  }

  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#methods without condition declarations stay unchanged")
  fun `methods without condition declarations stay unchanged`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllMethodWithoutConditions"))
  }

  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#methods without condition declarations stay unchanged in specs")
  fun `methods without condition declarations stay unchanged in specs`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllMethodWithoutConditionsInSpec"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#interactions are illegal in verify methods")
  fun `interactions are illegal in verify methods`() {
    `when`
    val result =
      transform(
        kotlin(
          "NonAssertions.kt",
          """
          import io.github.pshevche.spockk.lang.times

          class NonAssertions {
            @spock.lang.VerifyAll
            fun copy(a: Int?) {
              1 * spock.mock.DetachedMockFactory().Mock(Any::class.java).toString()
            }
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Interactions are not allowed in '@Verify' and '@VerifyAll' methods.")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#interactions are illegal in verify methods in specs")
  fun `interactions are illegal in verify methods in specs`() {
    `when`
    val result =
      transform(
        kotlin(
          "Spec.kt",
          """
          import io.github.pshevche.spockk.lang.times

          class Spec : spock.lang.Specification() {
            @spock.lang.VerifyAll
            fun copy(a: Int?) {
              1 * Mock(Any::class.java).toString()
            }
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Interactions are not allowed in '@Verify' and '@VerifyAll' methods.")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#ignores methods without annotation")
  fun `ignores methods without annotation`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllIgnoresMethodsWithoutAnnotation"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#transforms conditions at all nesting levels")
  fun `transforms conditions at all nesting levels`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllNestedConditions"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#checks for invalid conditions")
  fun `checks for invalid conditions`() {
    `when`
    val result =
      transform(
        kotlin(
          "Assertions.kt",
          """
          class Assertions {
            companion object {
              @spock.lang.VerifyAll
              @JvmStatic
              fun assignmentNotCondition(a: Int) {
                var b = a
                b = 3
              }
            }
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Expected a condition, but found an assignment. Did you intend to write '==' ?")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#ignores conditions in overwritten methods")
  fun `ignores conditions in overwritten methods`() {
    expect
    assertTransformation(sampleFromResource("condition/verifyall/VerifyAllOverriddenMethod"))
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#fails when verification method has a non-void or non-dynamic return value")
  fun `fails when verification method has a non-void or non-dynamic return value`() {
    `when`
    val result =
      transform(
        kotlin(
          "Assertions.kt",
          """
          class Assertions {
            companion object {
              @spock.lang.VerifyAll
              @JvmStatic
              fun isPositiveWithReturn(a: Int): Boolean {
                a > 0
                return true
              }
            }
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Verification helper method 'isPositiveWithReturn' must have a void return type")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/810")
  @MigratedFrom("org.spockframework.smoke.ast.condition.VerifyAllMethodsAstSpec#fails when verification method has a non-void or non-dynamic return value in Spec")
  fun `fails when verification method has a non-void or non-dynamic return value in Spec`() {
    `when`
    val result =
      transform(
        specWithBody(
          """
          companion object {
            @spock.lang.VerifyAll
            @JvmStatic
            fun isPositiveWithReturn(a: Int): Boolean {
              a > 0
              return true
            }
          }
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("Verification helper method 'isPositiveWithReturn' must have a void return type")
  }
}
