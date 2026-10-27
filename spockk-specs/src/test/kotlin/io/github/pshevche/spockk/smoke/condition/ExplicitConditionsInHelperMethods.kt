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

package io.github.pshevche.spockk.smoke.condition

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.PendingFeature
import spock.lang.Specification

class ExplicitConditionsInHelperMethods : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInHelperMethods#at top level of helper method")
  fun `at top level of helper method`() {
    expect
    topLevel()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/859")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInHelperMethods#nested in helper method")
  fun `nested in helper method`() {
    expect
    nested()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/859")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInHelperMethods#deeply nested in helper method")
  fun `deeply nested in helper method`() {
    expect
    deeplyNested()
  }

  private fun topLevel() {
    assert(false)
  }

  private fun nested() {
    if (true) assert(false)
  }

  private fun deeplyNested() {
    if (true) {
      { _: Int -> assert(false) }(0)
    }
  }
}
