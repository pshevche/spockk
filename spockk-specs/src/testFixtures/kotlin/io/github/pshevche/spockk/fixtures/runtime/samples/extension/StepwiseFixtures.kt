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

package io.github.pshevche.spockk.fixtures.runtime.samples.extension

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Rollup
import spock.lang.Specification
import spock.lang.Stepwise

/** Reports back the number of started iterations of [StepwiseRollupFixture]. */
object RollupIterationCounter {
  val count: ThreadLocal<Int> = ThreadLocal.withInitial { 0 }
}

class StepwiseBasicFixture : Specification() {
  @Stepwise
  fun step1(count: Int) {
    expect
    count != 3

    where
    variable(count).from((1..5).toList())
  }
}

class StepwiseRollupFixture : Specification() {
  @Stepwise
  @Rollup
  fun step1(count: Int) {
    RollupIterationCounter.count.set(RollupIterationCounter.count.get() + 1)

    expect
    count != 3

    where
    variable(count).from((1..5).toList())
  }
}

class StepwiseNotDataDrivenFixture : Specification() {
  @Stepwise
  fun step1() {
    expect
    true
  }
}

class StepwiseExecutionModeFixture : Specification() {
  @Stepwise
  fun step1(count: Int) {
    expect
    specificationContext.currentFeature.executionMode.get() == ExecutionMode.SAME_THREAD

    where
    variable(count).from((1..2).toList())
  }
}
