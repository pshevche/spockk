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

package io.github.pshevche.spockk.intellij

import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkUnusedDataTableStatementSuppressorTest : BaseSpockkUnusedExpressionInspectionSuppressorTest() {

  @BeforeEach
  private fun setUp() {
    myFixture.configureFromDefaultFile()
  }

  @Test
  fun testSuppressUnusedWarningsForDataTableStatements() {
    // expect
    listOf("variable1", "variable2").forEach {
      assertTrue(isSuppressedFor(it, KtNameReferenceExpression::class.java, "UnusedExpression"))
    }

    // and
    listOf("val11", "val21", "val12", "val22").forEach {
      assertTrue(isSuppressedFor(it, KtLiteralStringTemplateEntry::class.java, "UnusedExpression"))
    }
  }

  @Test
  fun testWarnsAboutDataTableStatementsInNonFeatures() {
    // expect
    listOf("variable1", "variable2").forEach {
      assertFalse(isSuppressedFor(it, KtNameReferenceExpression::class.java, "UnusedExpression"))
    }

    // and
    listOf("val11", "val21", "val12", "val22").forEach {
      assertFalse(isSuppressedFor(it, KtLiteralStringTemplateEntry::class.java, "UnusedExpression"))
    }
  }
}
