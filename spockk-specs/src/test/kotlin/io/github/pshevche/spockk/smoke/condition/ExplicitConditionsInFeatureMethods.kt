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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.PendingFeature
import spock.lang.Specification

class ExplicitConditionsInFeatureMethods : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/859")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#in anonymous block")
  fun `in anonymous block`() {
    assert(false)
    expect
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/859")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#nested in anonymous block")
  fun `nested in anonymous block`() {
    if (true) assert(false)
    expect
    true
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#in expect block")
  fun `in expect block`() {
    expect
    assert(false)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/859")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#nested in expect block")
  fun `nested in expect block`() {
    expect
    if (true) assert(false)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/326")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#in when block")
  fun `in when block`() {
    `when`
    assert(false)

    then
    true
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/326")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#nested in when block")
  fun `nested in when block`() {
    `when`
    if (true) assert(false)

    then
    true
  }

  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#in then block")
  fun `in then block`() {
    `when`
    "nothing"

    then
    assert(false)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/859")
  @FailsWith(ConditionNotSatisfiedError::class)
  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#nested in then block")
  fun `nested in then block`() {
    `when`
    "nothing"

    then
    listOf(1).forEach { assert(false) }
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExplicitConditionsInFeatureMethods#assert expression contains method call")
  fun `assert expression contains method call`() {
    expect
    assert("test".toString() == "test") { "dummy text" }
  }
}
