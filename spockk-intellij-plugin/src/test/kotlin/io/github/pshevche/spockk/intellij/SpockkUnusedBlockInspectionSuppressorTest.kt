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

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import java.nio.file.Paths

class SpockkUnusedBlockInspectionSuppressorTest : LightJavaCodeInsightFixtureTestCase() {
  private lateinit var suppressor: SpockkUnusedBlockInspectionSuppressor

  override fun setUp() {
    super.setUp()
    suppressor = SpockkUnusedBlockInspectionSuppressor()
  }

  override fun getTestDataPath(): String {
    val path =
      Paths.get("./src/test/resources/SpockkUnusedLabelInspectionSuppressorTest/")
        .toAbsolutePath()
    return path.toString()
  }

  fun testSuppressUnusedWarningsForSpockkBlockObjectReferences() {
    // given
    myFixture.configureByFile(
      "/testSuppressUnusedWarningsForSpockkBlockObjectReferences/SimpleSpec.kt"
    )

    // expect
    assertTrue(
      suppressor.isSuppressedFor(
        myFixture.findElementByText("expect", PsiElement::class.java),
        "UnusedExpression"
      )
    )
  }

  fun testWarnsAboutSpockkObjectReferencesForOtherInspections() {
    // given
    myFixture.configureByFile(
      "/testWarnsAboutSpockkObjectReferencesForOtherInspections/SimpleSpec.kt"
    )

    // expect
    assertFalse(
      suppressor.isSuppressedFor(
        myFixture.findElementByText("expect", PsiElement::class.java),
        "UnusedDeclaration"
      )
    )
  }

  fun testWarnsAboutUnusedNonSpockkObjectReferences() {
    // given
    myFixture.configureByFile(
      "/testWarnsAboutUnusedNonSpockkObjectReferences/UnusedObjectReference.kt"
    )

    // expect
    assertFalse(
      suppressor.isSuppressedFor(
        myFixture.findElementByText("expect", PsiElement::class.java),
        "UnusedExpression"
      )
    )
  }
}
