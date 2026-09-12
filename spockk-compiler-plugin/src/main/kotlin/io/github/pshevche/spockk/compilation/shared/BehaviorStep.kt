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

/**
 * One unit of `FeatureRewriter`'s dispatch over a feature's behavior blocks - pre-paired and
 * pre-classified at collection time ([io.github.pshevche.spockk.compilation.collector.pairBehaviorBlocks])
 * so the rewrite phase never has to look ahead at neighboring blocks or re-scan statements to decide
 * what it's looking at.
 */
internal sealed interface BehaviorStep {
  /** `setup`/`given` (plus any merged `and`): entered/exited verbatim, no condition handling. */
  data class Plain(val block: FeatureBlock) : BehaviorStep

  /** `expect` (plus any merged `and`): every statement may be a condition. */
  data class Condition(val block: FeatureBlock) : BehaviorStep

  /**
   * A `when` block and the `then` block immediately following it - always paired, per the feature
   * grammar ([io.github.pshevche.spockk.compilation.collector.BlockOrderValidatingFeatureStatementsCollector]
   * rejects any other arrangement before this pairing ever runs). [thenHasInteractions] and
   * [thenHasExceptionCondition] are computed once here, from the `then` block's original statements,
   * rather than re-derived by every rewriter that needs to know.
   */
  data class WhenThen(
    val whenBlock: FeatureBlock,
    val thenBlock: FeatureBlock,
    val thenHasInteractions: Boolean,
    val thenHasExceptionCondition: Boolean
  ) : BehaviorStep
}
