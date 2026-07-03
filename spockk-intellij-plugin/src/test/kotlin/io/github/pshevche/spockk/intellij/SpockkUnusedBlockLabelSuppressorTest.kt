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

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkUnusedBlockLabelSuppressorTest : BaseSpockkUnusedExpressionInspectionSuppressorTest() {

  @BeforeEach
  fun setUpFixture() {
    myFixture.configureFromDefaultFile()
  }

  @Test
  fun testSuppressUnusedWarningsForSpockkBlockObjectReferences() {
    // expect
    assertTrue(isSuppressedFor("expect"))
  }

  @Test
  fun testWarnsAboutSpockkObjectReferencesForOtherInspections() {
    // expect
    assertFalse(
      ReadAction.compute<Boolean, RuntimeException> {
        suppressor.isSuppressedFor(
          myFixture.findElementByText("expect", PsiElement::class.java),
          "UnusedDeclaration"
        )
      }
    )
  }

  @Test
  fun testWarnsAboutUnusedNonSpockkObjectReferences() {
    // expect
    assertFalse(isSuppressedFor("expect"))
  }
}
