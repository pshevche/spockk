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

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class SpockkUnusedExpressionInspectionSuppressor : InspectionSuppressor {
  private val dataProviderBlockIdxKey =
    Key.create<CachedValue<Int>>("spockk.data.provider.block.idx")

  override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
    if (toolId == "UnusedExpression") {
      if (element is KtNameReferenceExpression) {
        return element.isSpockkBlock() || isPartOfDataProviderBlock(element)
      }

      return isPartOfDataProviderBlock(element)
    }

    return false
  }

  override fun getSuppressActions(
    element: PsiElement?,
    toolId: String
  ): Array<out SuppressQuickFix?> = SuppressQuickFix.EMPTY_ARRAY

  /**
   * This is a very rough estimation of whether the statement is used in the where block. The method
   * simply checks that the statement comes after the `where` block label reference. At this point,
   * I don't think it makes sense to overthink it, and we can address false positives later.
   */
  private fun isPartOfDataProviderBlock(element: PsiElement): Boolean {
    val feature = PsiTreeUtil.getParentOfType(element, KtFunction::class.java) ?: return false
    val whereBlockPosition = getDataProviderBlockPosition(feature) ?: return false
    val elementPositionInFeature =
      getStatementPosition(feature) { it == element || it == element.parent } ?: return false
    return elementPositionInFeature > whereBlockPosition
  }

  private fun getDataProviderBlockPosition(feature: KtFunction): Int? {
    val dataProviderBlockPosition =
      feature.getUserData(dataProviderBlockIdxKey)
        ?: run {
          val cached =
            CachedValuesManager.getManager(feature.project).createCachedValue {
              val index = getStatementPosition(feature) { it.isDataProviderBlock() } ?: -1
              CachedValueProvider.Result(index, PsiModificationTracker.MODIFICATION_COUNT)
            }

          feature.putUserData(dataProviderBlockIdxKey, cached)
          cached
        }

    return dataProviderBlockPosition.value.takeIf { it > -1 }
  }

  private fun getStatementPosition(feature: KtFunction, predicate: (PsiElement) -> Boolean): Int? =
    feature.bodyBlockExpression?.statements?.indexOfFirst(predicate)?.takeIf { it > -1 }
}
