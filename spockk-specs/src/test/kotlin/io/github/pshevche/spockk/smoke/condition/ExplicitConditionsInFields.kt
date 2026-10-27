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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification

class ExplicitConditionsInFields : Specification() {
  val instanceField: () -> Unit = { assert(false) }

  @Shared
  val sharedField: () -> Unit = { assert(false) }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/804")
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFields#explicit condition in instance field")
  fun `explicit condition in instance field`() {
    `when`
    instanceField()

    then
    thrown(ConditionNotSatisfiedError::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/804")
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFields#explicit condition in shared field")
  fun `explicit condition in shared field`() {
    `when`
    sharedField()

    then
    thrown(ConditionNotSatisfiedError::class.java)
  }
}
