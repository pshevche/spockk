package io.github.pshevche.spockk.intellij

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.testIntegration.TestFramework
import org.jetbrains.kotlin.idea.KotlinLanguage

class SpockkTestFramework : TestFramework {

  override fun getName(): String = "Spockk"

  override fun getIcon() = com.intellij.icons.AllIcons.RunConfigurations.TestState.Run

  override fun isLibraryAttached(module: com.intellij.openapi.module.Module): Boolean = false

  override fun getLibraryPath(): String = ""

  override fun getDefaultSuperClass(): String = "spock.lang.Specification"

  override fun isTestClass(element: PsiElement): Boolean = element.isSpockkSpec()

  override fun isPotentialTestClass(element: PsiElement): Boolean = isTestClass(element)

  override fun findSetUpMethod(element: PsiElement): PsiElement? = null

  override fun findTearDownMethod(element: PsiElement): PsiElement? = null

  override fun findOrCreateSetUpMethod(element: PsiElement): PsiElement? = null

  override fun getSetUpMethodFileTemplateDescriptor(): com.intellij.ide.fileTemplates.FileTemplateDescriptor =
    com.intellij.ide.fileTemplates.FileTemplateDescriptor("")

  override fun getTearDownMethodFileTemplateDescriptor(): com.intellij.ide.fileTemplates.FileTemplateDescriptor =
    com.intellij.ide.fileTemplates.FileTemplateDescriptor("")

  override fun getTestMethodFileTemplateDescriptor(): com.intellij.ide.fileTemplates.FileTemplateDescriptor =
    com.intellij.ide.fileTemplates.FileTemplateDescriptor("")

  override fun isIgnoredMethod(element: PsiElement): Boolean = false

  override fun isTestMethod(element: PsiElement): Boolean = element.isSpockkFeature()

  override fun getLanguage(): Language = KotlinLanguage.INSTANCE
}
