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

import io.github.pshevche.spockk.compilation.common.FeatureBlock
import io.github.pshevche.spockk.compilation.common.FeatureBlockLabel
import io.github.pshevche.spockk.compilation.common.FeatureBlockLabelIrElement
import io.github.pshevche.spockk.compilation.ir.asIrBlockLabel
import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile

internal class ValidatingFeatureBlockCollector(
  private val file: IrFile,
  private val delegate: FeatureBlockCollector
) : FeatureBlockCollector {
  enum class State {
    INIT {
      override val validBlocks: List<FeatureBlockLabel>
        get() =
          listOf(
            FeatureBlockLabel.SETUP,
            FeatureBlockLabel.GIVEN,
            FeatureBlockLabel.EXPECT,
            FeatureBlockLabel.WHEN
          )

      override val isTerminal: Boolean
        get() = false

      override fun nextOrFailIfInvalid(block: FeatureBlockLabelIrElement): State =
        when (block) {
          is FeatureBlockLabelIrElement.Setup -> PRECONDITION
          is FeatureBlockLabelIrElement.Given -> PRECONDITION
          is FeatureBlockLabelIrElement.Expect -> EXPECTATION_EXPECT
          is FeatureBlockLabelIrElement.When -> ACTION
          else -> failOnInvalidBlock(block)
        }
    },
    PRECONDITION {
      override val validBlocks: List<FeatureBlockLabel>
        get() = listOf(FeatureBlockLabel.AND, FeatureBlockLabel.WHEN, FeatureBlockLabel.EXPECT)

      override val isTerminal: Boolean
        get() = false

      override fun nextOrFailIfInvalid(block: FeatureBlockLabelIrElement): State =
        when (block) {
          is FeatureBlockLabelIrElement.And -> this
          is FeatureBlockLabelIrElement.When -> ACTION
          is FeatureBlockLabelIrElement.Expect -> EXPECTATION_EXPECT
          else -> failOnInvalidBlock(block)
        }
    },
    ACTION {
      override val validBlocks: List<FeatureBlockLabel>
        get() = listOf(FeatureBlockLabel.AND, FeatureBlockLabel.THEN)

      override val isTerminal: Boolean
        get() = false

      override fun nextOrFailIfInvalid(block: FeatureBlockLabelIrElement): State =
        when (block) {
          is FeatureBlockLabelIrElement.And -> this
          is FeatureBlockLabelIrElement.Then -> EXPECTATION_THEN
          else -> failOnInvalidBlock(block)
        }
    },
    EXPECTATION_EXPECT {
      override val validBlocks: List<FeatureBlockLabel>
        get() =
          listOf(FeatureBlockLabel.AND, FeatureBlockLabel.CLEANUP, FeatureBlockLabel.WHERE)

      override val isTerminal: Boolean
        get() = true

      override fun nextOrFailIfInvalid(block: FeatureBlockLabelIrElement): State =
        when (block) {
          is FeatureBlockLabelIrElement.And -> this
          is FeatureBlockLabelIrElement.Cleanup -> CLEANUP
          is FeatureBlockLabelIrElement.Where -> DATA_DEFINITION
          else -> failOnInvalidBlock(block)
        }
    },
    EXPECTATION_THEN {
      override val validBlocks: List<FeatureBlockLabel>
        get() =
          listOf(
            FeatureBlockLabel.AND,
            FeatureBlockLabel.WHEN,
            FeatureBlockLabel.CLEANUP,
            FeatureBlockLabel.WHERE
          )

      override val isTerminal: Boolean
        get() = true

      override fun nextOrFailIfInvalid(block: FeatureBlockLabelIrElement): State =
        when (block) {
          is FeatureBlockLabelIrElement.And -> this
          is FeatureBlockLabelIrElement.When -> ACTION
          is FeatureBlockLabelIrElement.Cleanup -> CLEANUP
          is FeatureBlockLabelIrElement.Where -> DATA_DEFINITION
          else -> failOnInvalidBlock(block)
        }
    },
    CLEANUP {
      override val validBlocks: List<FeatureBlockLabel>
        get() = listOf(FeatureBlockLabel.AND, FeatureBlockLabel.WHERE)

      override val isTerminal: Boolean
        get() = true

      override fun nextOrFailIfInvalid(block: FeatureBlockLabelIrElement): State =
        when (block) {
          is FeatureBlockLabelIrElement.And -> this
          is FeatureBlockLabelIrElement.Where -> DATA_DEFINITION
          else -> failOnInvalidBlock(block)
        }
    },
    DATA_DEFINITION {
      override val validBlocks: List<FeatureBlockLabel>
        get() = listOf(FeatureBlockLabel.AND)

      override val isTerminal: Boolean
        get() = true

      override fun nextOrFailIfInvalid(block: FeatureBlockLabelIrElement): State =
        when (block) {
          is FeatureBlockLabelIrElement.And -> this
          else -> failOnInvalidBlock(block)
        }
    };

    abstract val validBlocks: List<FeatureBlockLabel>

    abstract val isTerminal: Boolean

    abstract fun nextOrFailIfInvalid(block: FeatureBlockLabelIrElement): State

    fun failOnInvalidBlock(currentBlock: FeatureBlockLabelIrElement): Nothing {
      val displayNames = validBlocks.map { "'${it.displayName}'" }
      val message =
        if (displayNames.isEmpty()) {
          "Did not expect to find any spockk blocks, but encountered '${currentBlock.label.displayName}'"
        } else {
          "Expected to find one of spockk blocks $displayNames, but encountered '${currentBlock.label.displayName}'"
        }
      throw CompilationException(message, currentBlock.file, currentBlock.ir)
    }
  }

  private var currentState: State = State.INIT
  private var lastBlock: FeatureBlockLabelIrElement? = null

  override fun consume(statement: IrStatement) {
    statement.asIrBlockLabel(file)?.let {
      currentState = currentState.nextOrFailIfInvalid(it)
      lastBlock = it
    }
    delegate.consume(statement)
  }

  override fun getBlocks(): List<FeatureBlock> {
    assertBlockStructureIsComplete()
    return delegate.getBlocks()
  }

  private fun assertBlockStructureIsComplete() {
    if (currentState != State.INIT && !currentState.isTerminal) {
      val displayNames = currentState.validBlocks.map { "'${it.displayName}'" }
      throw CompilationException(
        "Expected to find one of spockk blocks $displayNames, but reached the end of the feature method",
        lastBlock?.file,
        lastBlock?.ir
      )
    }
  }
}
