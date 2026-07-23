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

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

private val SPOCKK_BLOCKS_FQN =
  setOf(
    "io.github.pshevche.spockk.lang.given",
    "io.github.pshevche.spockk.lang.setup",
    "io.github.pshevche.spockk.lang.expect",
    "io.github.pshevche.spockk.lang.`when`",
    "io.github.pshevche.spockk.lang.then",
    "io.github.pshevche.spockk.lang.and",
    "io.github.pshevche.spockk.lang.where",
    "io.github.pshevche.spockk.lang.cleanup"
  )

private val DATA_PROVIDER_BLOCK_IDX_KEY =
  Key.create<CachedValue<Int>>("spockk.data.provider.block.idx")

private val CLEANUP_BLOCK_IDX_KEY =
  Key.create<CachedValue<Int>>("spockk.cleanup.block.idx")

internal fun PsiElement.isSpockkBlock(): Boolean {
  val firstBrace = text.indexOf("(")
  val nameWithoutArgs = if (firstBrace > -1) text.substring(0, firstBrace) else text
  return SPOCKK_BLOCKS_FQN.contains(nameWithoutArgs) ||
    getSpockkImportDirectives(containingFile).any { it.endsWith(nameWithoutArgs) }
}

internal fun PsiElement.isDataProviderBlock(): Boolean =
  text.contains("where") && isSpockkBlock()

internal fun PsiElement.isCleanupBlock(): Boolean =
  text.contains("cleanup") && isSpockkBlock()

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

/**
 * This is a very rough estimation of whether the statement is used in the cleanup block. The
 * method simply checks that the statement comes after the `cleanup` block label reference. At this
 * point, I don't think it makes sense to overthink it, and we can address false positives later.
 */
internal fun PsiElement.isPartOfCleanupBlock(): Boolean {
  val feature = getParentFeature() ?: return false
  val cleanupBlockPosition = getCleanupBlockPosition(feature) ?: return false
  val elementPositionInFeature = textRange.startOffset
  return elementPositionInFeature > cleanupBlockPosition
}

internal fun PsiElement.getParentFeature(): KtFunction? = PsiTreeUtil.getParentOfType(this, KtFunction::class.java)

private fun getCleanupBlockPosition(feature: KtFunction): Int? = getBlockPosition(feature, CLEANUP_BLOCK_IDX_KEY) {
  it.isCleanupBlock()
}

private fun getDataProviderBlockPosition(feature: KtFunction) = getBlockPosition(feature, DATA_PROVIDER_BLOCK_IDX_KEY) {
  it.isDataProviderBlock()
}

private fun getBlockPosition(
  feature: KtFunction,
  userDataKey: Key<CachedValue<Int>>,
  blockPredicate: (KtExpression) -> Boolean
): Int? {
  val blockPosition =
    feature.getUserData(userDataKey)
      ?: run {
        val cached =
          CachedValuesManager.getManager(feature.project).createCachedValue {
            val index = feature.bodyBlockExpression
              ?.statements
              ?.firstOrNull(blockPredicate)
              ?.textRange
              ?.endOffset
              ?: -1
            CachedValueProvider.Result(index, PsiModificationTracker.MODIFICATION_COUNT)
          }

        feature.putUserData(userDataKey, cached)
        cached
      }

  return blockPosition.value.takeIf { it > -1 }
}

internal fun PsiElement.getLineNumber(): Int = PsiDocumentManager.getInstance(project)
  .getDocument(containingFile)
  ?.getLineNumber(textRange.startOffset)!!

private val SPOCKK_SPEC_KEY = Key.create<CachedValue<Boolean>>("spockk.spec")
private val SPOCKK_FEATURE_KEY = Key.create<CachedValue<Boolean>>("spockk.feature")

internal fun PsiElement.isSpockkSpec(): Boolean {
  val element = this
  return ReadAction.compute<Boolean, RuntimeException> {
    var parent: PsiElement? = element
    var foundClass: KtClassOrObject? = null
    while (parent != null) {
      if (parent is KtClassOrObject && parent !is KtObjectDeclaration) {
        foundClass = parent
        break
      }
      parent = parent.parent
    }
    if (foundClass == null) return@compute false
    if (foundClass is KtClass && (foundClass.isInterface() || foundClass.isEnum())) return@compute false
    val classText = foundClass.text
    if (classText.contains(": spock.lang.Specification")) return@compute true
    if (classText.contains(": Specification")) return@compute true
    val ktFile = foundClass.containingFile as? KtFile ?: return@compute false
    for (other in ktFile.declarations) {
      if (other is KtClassOrObject && other != foundClass &&
        (other.text.contains(": spock.lang.Specification") || other.text.contains(": Specification"))) {
        if (classText.contains(": ${other.name}")) return@compute true
      }
    }
    return@compute false
  }
}

internal fun PsiElement.isSpockkFeature(): Boolean {
  val element = this
  return ReadAction.compute<Boolean, RuntimeException> {
    var node: PsiElement? = element
    var ktFunction: KtNamedFunction? = null
    while (node != null) {
      if (node is KtNamedFunction) { ktFunction = node; break }
      node = node.parent
    }
    if (ktFunction == null) return@compute false
    if (ktFunction.isLocal) return@compute false
    if (isFixtureMethod(ktFunction)) return@compute false
    var p: PsiElement? = ktFunction.parent
    var inSpec = false
    while (p != null) {
      if (p.isSpockkSpec()) { inSpec = true; break }
      p = p.parent
    }
    if (!inSpec) return@compute false
    CachedValuesManager.getManager(ktFunction.project).createCachedValue<Boolean> {
      val hasBlockLabel = PsiTreeUtil.collectElementsOfType(ktFunction, KtNameReferenceExpression::class.java)
        .any { it.isSpockkBlock() }
      CachedValueProvider.Result(hasBlockLabel, PsiModificationTracker.MODIFICATION_COUNT)
    }.value
  }
}

private fun isFixtureMethod(function: KtNamedFunction): Boolean {
  val name = function.name
  return name == "setup" || name == "cleanup" || name == "setupSpec" || name == "cleanupSpec"
}
