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
    assertTrue(framework.isTestMethod(
      findRequiredElementByTextAndType("fun `a passing feature`", PsiElement::class.java)
    ))
  }

  @Test
  fun testExcludeFixtureMethods() {
    assertFalse(framework.isTestMethod(findRequiredElementByTextAndType("fun setup()", PsiElement::class.java)))
    assertFalse(framework.isTestMethod(findRequiredElementByTextAndType("fun cleanup()", PsiElement::class.java)))
  }

  @Test
  fun testExcludeRegularMethods() {
    assertFalse(framework.isTestMethod(findRequiredElementByTextAndType("fun helperMethod()", PsiElement::class.java)))
    assertTrue(framework.isTestMethod(
      findRequiredElementByTextAndType("fun `a real feature`", PsiElement::class.java)
    ))
  }

  @Test
  fun testDetectAbstractSpec() {
    assertTrue(framework.isTestClass(findRequiredElementByTextAndType("class AbstractBaseSpec", PsiElement::class.java)))
    assertTrue(framework.isTestMethod(
      findRequiredElementByTextAndType("fun `base feature`", PsiElement::class.java)
    ))
  }
}
