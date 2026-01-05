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

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction

private val SPOCKK_BLOCKS_FQN =
  setOf(
    "io.github.pshevche.spockk.lang.given",
    "io.github.pshevche.spockk.lang.expect",
    "io.github.pshevche.spockk.lang.`when`",
    "io.github.pshevche.spockk.lang.then",
    "io.github.pshevche.spockk.lang.and",
    "io.github.pshevche.spockk.lang.where"
  )

private val DATA_PROVIDER_BLOCK_IDX_KEY =
  Key.create<CachedValue<Int>>("spockk.data.provider.block.idx")

internal fun PsiElement.isSpockkBlock(): Boolean {
  val firstBrace = text.indexOf("(")
  val nameWithoutArgs = if (firstBrace > -1) text.substring(0, firstBrace) else text
  return SPOCKK_BLOCKS_FQN.contains(nameWithoutArgs) ||
    getSpockkImportDirectives(containingFile).any { it.endsWith(nameWithoutArgs) }
}

internal fun PsiElement.isDataProviderBlock(): Boolean =
  text.contains("where") && isSpockkBlock()

private fun getSpockkImportDirectives(file: PsiFile): List<String> {
  if (file is KtFile) {
    return file.importDirectives
      .mapNotNull { it.importedReference?.text }
      .filter { SPOCKK_BLOCKS_FQN.contains(it) }
  }

  return listOf()
}

/**
 * This is a very rough estimation of whether the statement is used in the where block. The method
 * simply checks that the statement comes after the `where` block label reference. At this point,
 * I don't think it makes sense to overthink it, and we can address false positives later.
 */
internal fun PsiElement.isPartOfDataProviderBlock(): Boolean {
  val feature = getParentFeature() ?: return false
  val whereBlockPosition = getDataProviderBlockPosition(feature) ?: return false
  val elementPositionInFeature = textRange.startOffset
  return elementPositionInFeature > whereBlockPosition
}

internal fun PsiElement.getParentFeature(): KtFunction? = PsiTreeUtil.getParentOfType(this, KtFunction::class.java)

private fun getDataProviderBlockPosition(feature: KtFunction): Int? {
  val dataProviderBlockPosition =
    feature.getUserData(DATA_PROVIDER_BLOCK_IDX_KEY)
      ?: run {
        val cached =
          CachedValuesManager.getManager(feature.project).createCachedValue {
            val index = feature.bodyBlockExpression
              ?.statements
              ?.firstOrNull { it.isDataProviderBlock() }
              ?.textRange
              ?.endOffset
              ?: -1
            CachedValueProvider.Result(index, PsiModificationTracker.MODIFICATION_COUNT)
          }

        feature.putUserData(DATA_PROVIDER_BLOCK_IDX_KEY, cached)
        cached
      }

  return dataProviderBlockPosition.value.takeIf { it > -1 }
}

internal fun PsiElement.getLineNumber(): Int = PsiDocumentManager.getInstance(project)
  .getDocument(containingFile)
  ?.getLineNumber(textRange.startOffset)!!
