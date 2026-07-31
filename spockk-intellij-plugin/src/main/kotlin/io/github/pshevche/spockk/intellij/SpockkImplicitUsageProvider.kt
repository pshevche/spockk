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

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.unwrapped
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Feature and fixture methods are invoked reflectively by the Spock runtime, so they have no direct
 * caller in the source. Without this provider Kotlin's unused-declaration inspection reports them as
 * "never used". Reporting them as implicit usages suppresses that false positive.
 */
class SpockkImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean {
    val function = element as? KtNamedFunction ?: element.unwrapped as? KtNamedFunction ?: return false
    return function.isSpockkFeature() || function.isSpockkFixtureMethod()
  }

  override fun isImplicitRead(element: PsiElement): Boolean = false

  override fun isImplicitWrite(element: PsiElement): Boolean = false
}
