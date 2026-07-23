package io.github.pshevche.spockk.intellij

import com.intellij.execution.configurations.ConfigurationFromContext
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.extensions.JavaEntity
import org.jetbrains.kotlin.idea.extensions.JavaTestEntity
import org.jetbrains.kotlin.idea.extensions.KotlinTestFrameworkProvider
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

class SpockkTestFrameworkProvider : KotlinTestFrameworkProvider {

  override fun getCanRunJvmTests(): Boolean = true

  override fun isProducedByJava(context: ConfigurationFromContext): Boolean = false

  override fun isProducedByKotlin(context: ConfigurationFromContext): Boolean = true

  override fun isTestJavaClass(clazz: PsiClass): Boolean =
    clazz.isSpockkSpec()

  override fun isTestJavaMethod(method: PsiMethod): Boolean =
    method.isSpockkFeature()

  override fun isTestFrameworkAvailable(element: PsiElement): Boolean =
    element.isSpockkSpec() || element.isSpockkFeature()

  override fun getJavaTestEntity(element: PsiElement, allowMethods: Boolean): JavaTestEntity? {
    val ktClass = PsiTreeUtil.getParentOfType(element, KtClassOrObject::class.java) ?: return null
    if (!ktClass.isSpockkSpec()) return null
    val psiClass = ktClass as? PsiClass ?: return null
    if (!allowMethods) return JavaTestEntity(psiClass, null)
    val ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
    if (ktFunction != null && ktFunction.isSpockkFeature()) {
      val psiMethod = ktFunction as? PsiMethod
      return JavaTestEntity(psiClass, psiMethod)
    }
    return JavaTestEntity(psiClass, null)
  }

  override fun getJavaEntity(element: PsiElement): JavaEntity? {
    val ktClass = PsiTreeUtil.getParentOfType(element, KtClassOrObject::class.java) ?: return null
    val psiClass = ktClass as? PsiClass ?: return null
    val ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
    val psiMethod = ktFunction?.let { it as? PsiMethod }
    return JavaEntity(psiClass, psiMethod)
  }
}
