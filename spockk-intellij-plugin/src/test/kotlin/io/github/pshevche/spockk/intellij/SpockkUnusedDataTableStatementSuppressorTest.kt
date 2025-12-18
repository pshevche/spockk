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

package io.github.pshevche.spockk.intellij

import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import java.nio.file.Paths

class SpockkUnusedDataTableStatementSuppressorTest : SpockkLightJavaCodeInsightFixtureTestCase() {
  private lateinit var suppressor: SpockkUnusedExpressionInspectionSuppressor

  override fun setUp() {
    super.setUp()
    suppressor = SpockkUnusedExpressionInspectionSuppressor()
  }

  override fun getTestDataPath(): String {
    val path =
      Paths.get("./src/test/resources/SpockkUnusedDataTableStatementSuppressorTest/")
        .toAbsolutePath()
    return path.toString()
  }

  fun testSuppressUnusedWarningsForDataTableStatements() {
    // given
    myFixture.configureByFile("/testSuppressUnusedWarningsForDataTableStatements/Spec.kt")

    // expect
    listOf("variable1", "variable2").forEach {
      assertTrue(
        suppressor.isSuppressedFor(
          findRequiredElementByTextAndType(it, KtNameReferenceExpression::class.java),
          "UnusedExpression"
        )
      )
    }

    // and
    listOf("val11", "val21", "val12", "val22").forEach {
      assertTrue(
        suppressor.isSuppressedFor(
          findRequiredElementByTextAndType(it, KtLiteralStringTemplateEntry::class.java),
          "UnusedExpression"
        )
      )
    }
  }

  fun testWarnsAboutDataTableStatementsInNonFeatures() {
    // given
    myFixture.configureByFile("/testWarnsAboutDataTableStatementsInNonFeatures/NonSpec.kt")

    // expect
    listOf("variable1", "variable2").forEach {
      assertFalse(
        suppressor.isSuppressedFor(
          findRequiredElementByTextAndType(it, KtNameReferenceExpression::class.java),
          "UnusedExpression"
        )
      )
    }

    // and
    listOf("val11", "val21", "val12", "val22").forEach {
      assertFalse(
        suppressor.isSuppressedFor(
          findRequiredElementByTextAndType(it, KtLiteralStringTemplateEntry::class.java),
          "UnusedExpression"
        )
      )
    }
  }
}
