package io.github.pshevche.spockk.intellij

import com.intellij.psi.PsiElement
import com.intellij.testFramework.LightJavaCodeInsightFixtureTestCase5
import org.junit.jupiter.api.Test

class SpockkTestFrameworkTest : LightJavaCodeInsightFixtureTestCase5() {

  private val framework: SpockkTestFramework by lazy { SpockkTestFramework() }

  override fun getTestDataPath(): String {
    return javaClass.getResource("/SpockkTestFrameworkTest/")!!.path
  }

  @Test
  fun testDetectSpecClass() {
    myFixture.configureByFile("/testDetectSpecClass.kt")
    val specClass = myFixture.findElementByText("class MySpec", PsiElement::class.java)
    assertTrue(framework.isTestClass(specClass))
  }

  @Test
  fun testDetectNonSpecClass() {
    myFixture.configureByFile("/testDetectSpecClass.kt")
    val nonSpecClass = myFixture.findElementByText("class NotASpec", PsiElement::class.java)
    assertFalse(framework.isTestClass(nonSpecClass))
  }

  @Test
  fun testDetectFeatureMethod() {
    myFixture.configureByFile("/testDetectFeatureMethod.kt")
    val featureMethod = myFixture.findElementByText(
      "fun `a passing feature`", PsiElement::class.java
    )
    assertTrue(framework.isTestMethod(featureMethod))
  }

  @Test
  fun testExcludeFixtureMethods() {
    myFixture.configureByFile("/testExcludeFixtureMethods.kt")
    assertFalse(framework.isTestMethod(myFixture.findElementByText("fun setup()", PsiElement::class.java)))
    assertFalse(framework.isTestMethod(myFixture.findElementByText("fun cleanup()", PsiElement::class.java)))
  }

  @Test
  fun testExcludeRegularMethods() {
    myFixture.configureByFile("/testExcludeRegularMethods.kt")
    assertFalse(framework.isTestMethod(myFixture.findElementByText("fun helperMethod()", PsiElement::class.java)))
    assertTrue(framework.isTestMethod(
      myFixture.findElementByText("fun `a real feature`", PsiElement::class.java)
    ))
  }

  @Test
  fun testDetectAbstractSpec() {
    myFixture.configureByFile("/testDetectAbstractSpec.kt")
    assertTrue(framework.isTestClass(myFixture.findElementByText("class AbstractBaseSpec", PsiElement::class.java)))
    assertTrue(framework.isTestClass(myFixture.findElementByText("class ConcreteSpec", PsiElement::class.java)))
    assertTrue(framework.isTestMethod(
      myFixture.findElementByText("fun `base feature`", PsiElement::class.java)
    ))
  }
}
