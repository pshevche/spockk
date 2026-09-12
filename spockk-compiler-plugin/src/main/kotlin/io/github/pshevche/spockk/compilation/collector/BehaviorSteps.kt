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

import io.github.pshevche.spockk.compilation.shared.BehaviorStep
import io.github.pshevche.spockk.compilation.shared.FeatureBlock
import io.github.pshevche.spockk.compilation.shared.FeatureBlockLabel
import io.github.pshevche.spockk.compilation.transformer.condition.hasExceptionCondition
import io.github.pshevche.spockk.compilation.transformer.interaction.hasInteractionStatement

/**
 * Pairs a feature's flat behavior blocks into [BehaviorStep]s: a `when` block with the `then` block
 * immediately following it (always adjacent - [BlockOrderValidatingFeatureStatementsCollector] rejects
 * every other arrangement before this ever runs), everything else standalone. A pure function over
 * already-collected blocks, so it needs no `DeclarationIrBuilder`/rewriter context and is unit-testable
 * on its own.
 */
internal fun pairBehaviorBlocks(blocks: List<FeatureBlock>): List<BehaviorStep> {
  val steps = mutableListOf<BehaviorStep>()
  var index = 0
  while (index < blocks.size) {
    val block = blocks[index]
    when (block.element.label) {
      FeatureBlockLabel.WHEN -> {
        val thenBlock = blocks.getOrNull(index + 1)
        check(thenBlock != null && thenBlock.element.label == FeatureBlockLabel.THEN) {
          "a 'when' block must always be immediately followed by a 'then' block"
        }
        steps += BehaviorStep.WhenThen(
          whenBlock = block,
          thenBlock = thenBlock,
          thenHasInteractions = thenBlock.statements.hasInteractionStatement(),
          thenHasExceptionCondition = thenBlock.statements.hasExceptionCondition()
        )
        index += 2
      }

      FeatureBlockLabel.EXPECT -> {
        steps += BehaviorStep.Condition(block)
        index += 1
      }

      else -> {
        steps += BehaviorStep.Plain(block)
        index += 1
      }
    }
  }
  return steps
}
