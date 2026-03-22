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

package io.github.pshevche.spockk.compilation.collector

import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.shared.FeatureBlockLabel
import io.github.pshevche.spockk.compilation.shared.FeatureBlockLabelIrElement
import io.github.pshevche.spockk.compilation.ir.asIrBlockLabel
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile

internal class DefaultFeatureBlockCollector(private val file: IrFile) : FeatureBlockCollector {
  private var currentLabel: FeatureBlockLabelIrElement? = null
  private val currentBlockStatements = mutableListOf<IrStatement>()
  private val collectedBlocks = mutableListOf<FeatureBlock>()
  private val anonymousStatements = mutableListOf<IrStatement>()

  override fun consume(statement: IrStatement) {
    val blockLabel = statement.asIrBlockLabel(file)
    if (blockLabel == null) {
      currentBlockStatements.add(statement)
    } else {
      completeCurrentBlock()
      currentLabel = blockLabel
    }
  }

  override fun getAnonymousStatements(): List<IrStatement> = anonymousStatements.toList()

  private fun completeCurrentBlock() {
    val label = currentLabel
    val statements = currentBlockStatements.toList()
    currentBlockStatements.clear()

    if (label == null) {
      anonymousStatements.addAll(statements)
      return
    }

    if (label.label == FeatureBlockLabel.AND && collectedBlocks.isNotEmpty()) {
      val last = collectedBlocks.last()
      collectedBlocks[collectedBlocks.lastIndex] = last.copy(
        descriptions = last.descriptions + label.description,
        statements = last.statements + statements
      )
    } else {
      collectedBlocks.add(
        FeatureBlock(
          element = label,
          descriptions = listOf(label.description),
          ordinal = collectedBlocks.size,
          statements = statements
        )
      )
    }
  }

  override fun getBlocks(): List<FeatureBlock> {
    completeCurrentBlock()
    return collectedBlocks.toList()
  }
}
