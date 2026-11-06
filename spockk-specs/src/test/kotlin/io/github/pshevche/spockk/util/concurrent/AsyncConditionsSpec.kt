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

package io.github.pshevche.spockk.util.concurrent

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.SpockAssertionError
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.FailsWith
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions
import kotlin.concurrent.thread

class AsyncConditionsSpec : Specification() {

  @MigratedFrom("spock.util.concurrent.AsyncConditionsSpec#passing example - check passes")
  fun `passing example - check passes`() {
    given
    val conds = AsyncConditions()

    `when`
    thread {
      conds.evaluate {
        assert(true)
      }
    }

    then
    conds.await()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/804")
  @FailsWith(SpockAssertionError::class)
  @MigratedFrom("spock.util.concurrent.AsyncConditionsSpec#failing example 1 - eval fails")
  fun `failing example 1 - eval fails`() {
    given
    val conds = AsyncConditions()

    `when`
    thread {
      conds.evaluate {
        assert(false)
      }
    }

    then
    conds.await()
  }

  @FailsWith(SpockTimeoutError::class)
  @MigratedFrom("spock.util.concurrent.AsyncConditionsSpec#failing example 2 - eval is missing")
  fun `failing example 2 - eval is missing`() {
    given
    val conds = AsyncConditions()

    expect
    conds.await()
  }

  @FailsWith(SpockTimeoutError::class)
  @MigratedFrom("spock.util.concurrent.AsyncConditionsSpec#one passing and one missing eval")
  fun `one passing and one missing eval`() {
    given
    val conds = AsyncConditions(2)

    `when`
    thread {
      conds.evaluate {
        assert(true)
      }
    }

    then
    conds.await()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/804")
  @FailsWith(SpockAssertionError::class)
  @MigratedFrom("spock.util.concurrent.AsyncConditionsSpec#one failing and one missing eval")
  fun `one failing and one missing eval`() {
    given
    val conds = AsyncConditions(2)

    `when`
    thread {
      conds.evaluate {
        assert(false)
      }
    }

    then
    conds.await()
  }

  @MigratedFrom("spock.util.concurrent.AsyncConditionsSpec#multiple passing evals")
  fun `multiple passing evals`() {
    given
    val conds = AsyncConditions(3)

    `when`
    thread {
      conds.evaluate {
        assert(true)
        assert(true)
      }
      conds.evaluate {
        assert(true)
      }
    }

    and
    thread {
      conds.evaluate {
        assert(true)
      }
    }

    then
    conds.await()
  }
}
