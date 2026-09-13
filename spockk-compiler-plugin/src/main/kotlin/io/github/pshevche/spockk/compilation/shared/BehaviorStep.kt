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

package io.github.pshevche.spockk.compilation.shared

import io.github.pshevche.spockk.compilation.collector.BlockOrderValidatingFeatureStatementsCollector
import io.github.pshevche.spockk.compilation.transformer.condition.hasExceptionCondition
import io.github.pshevche.spockk.compilation.transformer.interaction.hasInteractionStatement

/**
 * Feature blocks pre-classified at collection time
 * so the rewrite phase never has to look ahead at neighboring blocks or re-scan statements to determine
 * the rewriting strategy.
 */
internal sealed interface BehaviorStep {
  /** `setup`/`given` (plus any merged `and`): entered/exited verbatim, no condition handling. */
  data class Plain(val block: FeatureBlock) : BehaviorStep

  /** `expect` (plus any merged `and`): every statement may be a condition. */
  data class Condition(val block: FeatureBlock) : BehaviorStep

  /**
   * A `when` block and the `then` block immediately following it - always paired, per the feature
   * grammar (see [io.github.pshevche.spockk.compilation.collector.BlockOrderValidatingFeatureStatementsCollector]).
   * [thenHasInteractions] and [thenHasExceptionCondition] are computed once here, from the `then` block's original statements,
   * rather than re-derived by every rewriter that needs to know.
   */
  data class WhenThen(
    val whenBlock: FeatureBlock,
    val thenBlock: FeatureBlock,
    val thenHasInteractions: Boolean,
    val thenHasExceptionCondition: Boolean
  ) : BehaviorStep

  companion object {
    /**
     * Pairs a feature's flat behavior blocks into [BehaviorStep]s: a `when` block with the `then` block
     * immediately following it (always adjacent - [BlockOrderValidatingFeatureStatementsCollector] rejects
     * every other arrangement before this ever runs), everything else standalone.
     */
    internal fun fromFeatureBlocks(blocks: List<FeatureBlock>): List<BehaviorStep> {
      val steps = mutableListOf<BehaviorStep>()
      var index = 0
      while (index < blocks.size) {
        val block = blocks[index]
        when (block.element.label) {
          FeatureBlockLabel.WHEN -> {
            val thenBlock = blocks[index + 1]
            steps += WhenThen(
              whenBlock = block,
              thenBlock = thenBlock,
              thenHasInteractions = thenBlock.statements.hasInteractionStatement(),
              thenHasExceptionCondition = thenBlock.statements.hasExceptionCondition()
            )
            index += 2
          }

          FeatureBlockLabel.EXPECT -> {
            steps += Condition(block)
            index += 1
          }

          else -> {
            steps += Plain(block)
            index += 1
          }
        }
      }
      return steps
    }
  }
}
