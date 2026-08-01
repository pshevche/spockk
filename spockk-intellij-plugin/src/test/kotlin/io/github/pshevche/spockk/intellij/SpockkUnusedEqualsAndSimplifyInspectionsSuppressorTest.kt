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

import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkUnusedEqualsAndSimplifyInspectionsSuppressorTest : BaseSpockkUnusedExpressionInspectionSuppressorTest() {

  @BeforeEach
  private fun setUp() {
    myFixture.configureFromDefaultFile()
  }

  @Test
  fun testSuppressUnusedEqualsInThenBlock() {
    assertTrue(
      isSuppressedFor("x > y", KtBinaryExpression::class.java, "UnusedEquals")
    )
  }

  @Test
  fun testSuppressSimplifyBooleanWithConstantsInExpectBlock() {
    assertTrue(
      isSuppressedFor("x > y", KtBinaryExpression::class.java, "SimplifyBooleanWithConstants")
    )
  }

  @Test
  fun testSuppressSimplifyNegatedBinaryExpressionInExpectBlock() {
    assertTrue(
      isSuppressedFor("x > y", KtBinaryExpression::class.java, "SimplifyNegatedBinaryExpression")
    )
  }

  @Test
  fun testDoesNotSuppressOutsideSpockkBlock() {
    assertFalse(
      isSuppressedFor("a != b", KtBinaryExpression::class.java, "UnusedEquals")
    )
  }

  @Test
  fun testDoesNotSuppressForUnrelatedToolId() {
    assertFalse(
      isSuppressedFor("x > y", KtBinaryExpression::class.java, "UnusedDeclaration")
    )
  }
}
