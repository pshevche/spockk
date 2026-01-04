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

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.formatting.Indent
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.formatter.KotlinBlock
import org.jetbrains.kotlin.idea.formatter.KotlinSpacingBuilderUtilImpl
import org.jetbrains.kotlin.idea.formatter.NodeAlignmentStrategy
import org.jetbrains.kotlin.idea.formatter.createSpacingBuilder
import org.jetbrains.kotlin.psi.KtFunction

class SpockkDataTableFormattingModelBuilder : FormattingModelBuilder {

  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val containingFile = formattingContext.containingFile
    val settings = formattingContext.codeStyleSettings

    val defaultKotlinBlock = KotlinBlock(
      containingFile.node,
      NodeAlignmentStrategy.getNullStrategy(),
      Indent.getNoneIndent(),
      wrap = null,
      settings,
      createSpacingBuilder(settings, KotlinSpacingBuilderUtilImpl)
    )

    return FormattingModelProvider.createFormattingModelForPsiFile(
      containingFile,
      SpockkBlock(defaultKotlinBlock),
      settings
    )
  }

  private class SpockkBlock(
    private val delegate: Block,
    private val alignmentProviders: MutableMap<KtFunction, DataTableAlignmentProvider> = mutableMapOf()
  ) : Block by delegate {

    override fun getSubBlocks(): List<Block?> {
      return delegate.subBlocks.map { SpockkBlock(it, alignmentProviders) }
    }

    /**
     * Align data table separators (i.e., semicolons).
     * All separators with the same index will get the same alignment instance.
     */
    override fun getAlignment(): Alignment? {
      return asDataTableSeparator()?.let {
        val feature = it.getParentFeature() ?: return delegate.alignment
        val alignmentProvider = alignmentProviders.computeIfAbsent(feature) { DataTableAlignmentProvider() }
        return alignmentProvider.getAlignment(it)
      } ?: delegate.alignment
    }

    private fun asDataTableSeparator(): PsiElement? {
      return (delegate as? KotlinBlock)?.node?.psi?.takeIf {
        it.text == ";" && it.isPartOfDataProviderBlock()
      }
    }

    // required as default methods of Java interfaces are not delegated
    override fun getDebugName() = delegate.debugName
  }

  /**
   * Provides alignment for data table separators within a single feature.
   * The implementation relies on the separators to be traversed line-by-line and column-by-column.
   */
  private class DataTableAlignmentProvider {
    private var currentLine = -1
    private var currentSeparatorIndex = 0
    private val alignmentPerSeparatorIndex = mutableListOf<Alignment>()

    fun getAlignment(element: PsiElement): Alignment? {
      val line = element.getLineNumber()

      // ensure that all separators with position currentSeparatorIndex get the same alignment
      if (line != currentLine) {
        currentLine = line
        currentSeparatorIndex = 0
      } else {
        currentSeparatorIndex++
      }

      // create a new alignment if the separator index has not been seen before
      if (currentSeparatorIndex > alignmentPerSeparatorIndex.lastIndex) {
        alignmentPerSeparatorIndex.add(currentSeparatorIndex, Alignment.createAlignment(true))
      }

      return alignmentPerSeparatorIndex[currentSeparatorIndex]
    }
  }
}
