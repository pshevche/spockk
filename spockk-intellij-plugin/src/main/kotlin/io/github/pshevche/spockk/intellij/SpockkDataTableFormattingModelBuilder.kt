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
import com.intellij.formatting.Spacing
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

    // delegates to the default Kotlin formatter whenever we are not in the context of Spockk data tables
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

  /**
   * Wraps the default Kotlin source code block with the option to add custom alignment and spacing for data table separators.
   */
  private class SpockkBlock(
    private val delegate: Block,
    private val alignmentProviders: MutableMap<KtFunction, DataTableAlignmentProvider> = mutableMapOf()
  ) : Block by delegate {

    /**
     * Wrap all children to support traversal of the blocks.
     */
    override fun getSubBlocks(): List<Block?> = delegate.subBlocks.map { SpockkBlock(it, alignmentProviders) }

    /**
     * Align all data table separators (i.e., semicolons) by their position in the table row.
     * This will align semicolons even if they are not part of the data table, but it seems like a reasonable simplification for now.
     */
    override fun getAlignment(): Alignment? {
      return delegate.asDataTableSeparator()?.let {
        val feature = it.getParentFeature() ?: return delegate.alignment
        val alignmentProvider = alignmentProviders.computeIfAbsent(feature) { DataTableAlignmentProvider() }
        return alignmentProvider.getAlignment(it)
      } ?: delegate.alignment
    }

    /**
     * Adds whitespaces before and after data table separators.
     */
    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
      // child1 is null if child2 is the first block of a document (it can never be a data table separator)
      if (child1 != null) {
        // add a single whitespace after a separator
        (child1 as SpockkBlock).delegate.asDataTableSeparator()?.let {
          return Spacing.createSpacing(1, 1, 0, false, 0)
        }

        // add a single whitespace before a separator
        (child2 as SpockkBlock).delegate.asDataTableSeparator()?.let {
          return Spacing.createSpacing(1, 1, 0, false, 0)
        }
      }

      return delegate.getSpacing(child1, child2)
    }

    private fun Block.asDataTableSeparator(): PsiElement? = (this as? KotlinBlock)?.node?.psi?.takeIf {
      it.text == ";" && it.isPartOfDataProviderBlock()
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
