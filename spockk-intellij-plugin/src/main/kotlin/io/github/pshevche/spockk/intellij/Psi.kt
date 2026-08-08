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

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration

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

private val IMPLICIT_ASSERTION_HELPER_FQN =
  setOf(
    "io.github.pshevche.spockk.lang.verify",
    "io.github.pshevche.spockk.lang.verifyAll",
    "io.github.pshevche.spockk.lang.verifyEach"
  )

private val DATA_PROVIDER_BLOCK_IDX_KEY =
  Key.create<CachedValue<Int>>("spockk.data.provider.block.idx")

private val CLEANUP_BLOCK_IDX_KEY =
  Key.create<CachedValue<Int>>("spockk.cleanup.block.idx")

private val THEN_OR_EXPECT_BLOCK_IDX_KEY =
  Key.create<CachedValue<Int>>("spockk.then.or.expect.block.idx")

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

internal fun PsiElement.isThenOrExpectBlock(): Boolean =
  (text.startsWith("then") || text.startsWith("expect")) && isSpockkBlock()

private fun getSpockkImportDirectives(file: PsiFile, fqns: Set<String> = SPOCKK_BLOCKS_FQN): List<String> {
  if (file is KtFile) {
    return file.importDirectives
      .mapNotNull { it.importedReference?.text }
      .filter { fqns.contains(it) }
  }

  return listOf()
}

/**
 * True when the element sits inside the lambda body of a `verify`/`verifyAll`/`verifyEach` call,
 * however deeply nested inside further such calls. Bare booleans there are implicit conditions too,
 * whether the call appears in a then/expect block or in a dedicated (non-feature) helper method.
 */
internal fun PsiElement.isPartOfImplicitAssertionHelperCall(): Boolean {
  var lambda = PsiTreeUtil.getParentOfType(this, KtLambdaExpression::class.java)
  while (lambda != null) {
    if (lambda.isImplicitAssertionHelperLambda()) return true
    lambda = PsiTreeUtil.getParentOfType(lambda, KtLambdaExpression::class.java)
  }
  return false
}

private fun KtLambdaExpression.isImplicitAssertionHelperLambda(): Boolean {
  val callee = ((parent as? KtLambdaArgument)?.parent as? KtCallExpression)?.calleeExpression ?: return false
  return callee.isImplicitAssertionHelperCallee()
}

private fun PsiElement.isImplicitAssertionHelperCallee(): Boolean {
  val firstBrace = text.indexOf("(")
  val nameWithoutArgs = if (firstBrace > -1) text.substring(0, firstBrace) else text
  return IMPLICIT_ASSERTION_HELPER_FQN.contains(nameWithoutArgs) ||
    getSpockkImportDirectives(containingFile, IMPLICIT_ASSERTION_HELPER_FQN).any { it.endsWith(nameWithoutArgs) }
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

internal fun PsiElement.isPartOfThenOrExpectBlock(): Boolean {
  val feature = getParentFeature() ?: return false
  val blockPosition = getThenOrExpectBlockPosition(feature) ?: return false
  val elementPositionInFeature = textRange.startOffset
  return elementPositionInFeature > blockPosition
}

internal fun PsiElement.getParentFeature(): KtFunction? = PsiTreeUtil.getParentOfType(this, KtFunction::class.java)

private fun getCleanupBlockPosition(feature: KtFunction): Int? = getBlockPosition(feature, CLEANUP_BLOCK_IDX_KEY) {
  it.isCleanupBlock()
}

private fun getDataProviderBlockPosition(feature: KtFunction) = getBlockPosition(feature, DATA_PROVIDER_BLOCK_IDX_KEY) {
  it.isDataProviderBlock()
}

private fun getThenOrExpectBlockPosition(feature: KtFunction) =
  getBlockPosition(feature, THEN_OR_EXPECT_BLOCK_IDX_KEY) {
    it.isThenOrExpectBlock()
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

private const val SPECIFICATION_FQN = "spock.lang.Specification"
private val FIXTURE_METHOD_NAMES = setOf("setup", "cleanup", "setupSpec", "cleanupSpec")

/**
 * A Kotlin class is a Spockk spec if it inherits `spock.lang.Specification`, directly or
 * transitively through a base class in any file. Detection resolves the real superclass hierarchy
 * (via light classes) rather than matching the supertype name textually, so an unrelated class that
 * merely happens to be named `Specification` is not misdetected. The result is cached per class.
 *
 * This resolves the superclass hierarchy and must therefore run off the EDT (e.g. from a run line
 * marker's slow pass, or a background inspection thread), never from a latency-sensitive fast pass.
 */
internal fun PsiElement.isSpockkSpec(): Boolean {
  val foundClass = enclosingSpecCandidateClass() ?: return false
  return CachedValuesManager.getCachedValue(foundClass, SPOCKK_SPEC_KEY) {
    CachedValueProvider.Result.create(isSpockkSpecForClass(foundClass), PsiModificationTracker.MODIFICATION_COUNT)
  }
}

/**
 * Walks up to the nearest enclosing class declaration that could be a Spockk spec. `object`
 * declarations are excluded (specs are conventionally `class` declarations), as are interfaces and
 * enums.
 */
private fun PsiElement.enclosingSpecCandidateClass(): KtClassOrObject? {
  val enclosingClass = enclosingClassDeclaration() ?: return null
  if (enclosingClass is KtClass && (enclosingClass.isInterface() || enclosingClass.isEnum())) return null
  return enclosingClass
}

/** Walks up to the nearest enclosing `class` declaration, skipping `object` declarations. */
private fun PsiElement.enclosingClassDeclaration(): KtClassOrObject? {
  var parent: PsiElement? = this
  while (parent != null) {
    if (parent is KtClassOrObject && parent !is KtObjectDeclaration) return parent
    parent = parent.parent
  }
  return null
}

private fun isSpockkSpecForClass(foundClass: KtClassOrObject): Boolean {
  // Walk the real superclass hierarchy via light classes. `InheritanceUtil.isInheritor` matches by
  // fully-qualified name, so it accepts `Specification` inherited directly or transitively through a
  // base class in another file, and rejects an unrelated same-named class.
  val lightClass = foundClass.toLightClass() ?: return false
  return InheritanceUtil.isInheritor(lightClass, SPECIFICATION_FQN)
}

internal fun PsiElement.isSpockkFeature(): Boolean {
  val function = enclosingNamedFunction() ?: return false
  if (function.isLocal) return false
  if (isFixtureMethod(function)) return false
  if (!function.isDeclaredInSpockkSpec()) return false
  return CachedValuesManager.getCachedValue(function, SPOCKK_FEATURE_KEY) {
    val hasBlockLabel = PsiTreeUtil.collectElementsOfType(function, KtNameReferenceExpression::class.java)
      .any { it.isSpockkBlock() }
    CachedValueProvider.Result.create(hasBlockLabel, PsiModificationTracker.MODIFICATION_COUNT)
  }
}

/**
 * A Spockk fixture method is a lifecycle callback (`setup`, `cleanup`, `setupSpec`, `cleanupSpec`)
 * declared directly inside a spec. Reported alongside features as an implicit usage so it is not
 * flagged as unused.
 */
internal fun PsiElement.isSpockkFixtureMethod(): Boolean {
  val function = enclosingNamedFunction() ?: return false
  if (function.isLocal) return false
  if (!isFixtureMethod(function)) return false
  return function.isDeclaredInSpockkSpec()
}

private fun PsiElement.enclosingNamedFunction(): KtNamedFunction? {
  var node: PsiElement? = this
  while (node != null) {
    if (node is KtNamedFunction) return node
    node = node.parent
  }
  return null
}

/**
 * A method belongs to a spec only when its *directly enclosing* class is a spec. Walking every
 * ancestor would misclassify a method in a nested non-spec helper class that itself sits inside a
 * spec.
 */
private fun KtNamedFunction.isDeclaredInSpockkSpec(): Boolean =
  enclosingClassDeclaration()?.isSpockkSpec() == true

private fun isFixtureMethod(function: KtNamedFunction): Boolean = function.name in FIXTURE_METHOD_NAMES

internal fun PsiElement.findSpockkSetUpMethod(): PsiElement? =
  enclosingClassDeclaration()?.body?.functions?.find { it.name == "setup" }

internal fun PsiElement.findSpockkTearDownMethod(): PsiElement? =
  enclosingClassDeclaration()?.body?.functions?.find { it.name == "cleanup" }

internal fun PsiElement.isSpockkIgnored(): Boolean {
  var parent: PsiElement? = this
  var foundFunction: KtNamedFunction? = null
  while (parent != null) {
    if (parent is KtNamedFunction) {
      foundFunction = parent
      break
    }
    parent = parent.parent
  }
  return foundFunction?.annotationEntries?.any {
    val typeText = it.typeReference?.text
    typeText == "Ignore" || typeText == "spock.lang.Ignore"
  } == true
}
