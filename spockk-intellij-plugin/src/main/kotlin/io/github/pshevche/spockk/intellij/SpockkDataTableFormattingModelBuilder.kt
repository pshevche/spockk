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
import org.jetbrains.kotlin.idea.formatter.KotlinBlock
import org.jetbrains.kotlin.idea.formatter.KotlinSpacingBuilderUtilImpl
import org.jetbrains.kotlin.idea.formatter.NodeAlignmentStrategy
import org.jetbrains.kotlin.idea.formatter.createSpacingBuilder

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

    val spockkBlock = SpockkBlock(defaultKotlinBlock)

    return FormattingModelProvider.createFormattingModelForPsiFile(
      containingFile,
      spockkBlock,
      settings
    )
  }

  class SpockkBlock(private val delegate: KotlinBlock) : Block by delegate {

    override fun getSpacing(p0: Block?, p1: Block): Spacing? {
      return delegate.getSpacing(p0, p1)
    }

    override fun getAlignment(): Alignment? {
      return delegate.alignment
    }
  }
}
