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
