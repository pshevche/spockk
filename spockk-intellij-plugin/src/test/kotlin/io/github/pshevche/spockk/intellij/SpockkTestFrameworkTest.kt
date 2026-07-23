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

import com.intellij.psi.PsiElement
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkTestFrameworkTest : BaseSpockkIntelliJPluginTestCase() {

  private val framework: SpockkTestFramework by lazy { SpockkTestFramework() }

  @BeforeEach
  private fun setUp() {
    myFixture.configureFromDefaultFile()
  }

  @Test
  fun testDetectSpecClass() {
    assertTrue(framework.isTestClass(findRequiredElementByTextAndType("class MySpec", PsiElement::class.java)))
  }

  @Test
  fun testDetectNonSpecClass() {
    assertFalse(framework.isTestClass(findRequiredElementByTextAndType("class NotASpec", PsiElement::class.java)))
  }

  @Test
  fun testDetectFeatureMethod() {
    assertTrue(
      framework.isTestMethod(
        findRequiredElementByTextAndType("fun `a passing feature`", PsiElement::class.java)
      )
    )
  }

  @Test
  fun testExcludeFixtureMethods() {
    assertFalse(framework.isTestMethod(findRequiredElementByTextAndType("fun setup()", PsiElement::class.java)))
    assertFalse(framework.isTestMethod(findRequiredElementByTextAndType("fun cleanup()", PsiElement::class.java)))
  }

  @Test
  fun testExcludeRegularMethods() {
    assertFalse(framework.isTestMethod(findRequiredElementByTextAndType("fun helperMethod()", PsiElement::class.java)))
    assertTrue(
      framework.isTestMethod(
        findRequiredElementByTextAndType("fun `a real feature`", PsiElement::class.java)
      )
    )
  }

  @Test
  fun testDetectAbstractSpec() {
    assertTrue(framework.isTestClass(findRequiredElementByTextAndType("class AbstractBaseSpec", PsiElement::class.java)))
    assertTrue(
      framework.isTestMethod(
        findRequiredElementByTextAndType("fun `base feature`", PsiElement::class.java)
      )
    )
  }
}
