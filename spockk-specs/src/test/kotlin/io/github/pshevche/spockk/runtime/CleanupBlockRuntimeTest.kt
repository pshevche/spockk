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

package io.github.pshevche.spockk.runtime

import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.cleanup.CleanupBlockExceptionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.cleanup.CleanupBlockOnFailureSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.cleanup.CleanupBlockTracker
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import spock.lang.Specification

class CleanupBlockRuntimeTest : Specification() {

  fun `cleanup block runs even when feature fails`() {
    `when`
    CleanupBlockTracker.cleanupExecuted = false
    val events = execute(selectClass(CleanupBlockOnFailureSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    CleanupBlockTracker.cleanupExecuted
  }

  fun `cleanup exception is suppressed when feature also fails`() {
    `when`
    val events = execute(
      selectMethod(
        CleanupBlockExceptionSpec::class.java,
        "cleanup exception is suppressed when feature also fails"
      )
    )

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is IllegalStateException
    throwable.suppressed.single() is IllegalArgumentException
  }

  fun `cleanup exception propagates when feature succeeds`() {
    `when`
    val events = execute(
      selectMethod(
        CleanupBlockExceptionSpec::class.java,
        "cleanup exception propagates when feature succeeds"
      )
    )

    then
    events.assertStatistics { it.started(1).failed(1) }
  }
}
