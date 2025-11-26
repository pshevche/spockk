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

package io.github.pshevche.spockk.intellij.extensions

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile

private val SPOCKK_BLOCKS_FQN =
  setOf(
    "io.github.pshevche.spockk.lang.given",
    "io.github.pshevche.spockk.lang.expect",
    "io.github.pshevche.spockk.lang.`when`",
    "io.github.pshevche.spockk.lang.then",
    "io.github.pshevche.spockk.lang.and"
  )

fun PsiElement.isSpockkBlock(): Boolean =
  getSpockkImportDirectives(containingFile).any { it.endsWith(text) }

private fun getSpockkImportDirectives(file: PsiFile): List<String> {
  if (file is KtFile) {
    return file.importDirectives
      .mapNotNull { it.importedReference?.text }
      .filter { SPOCKK_BLOCKS_FQN.contains(it) }
  }

  return listOf()
}
