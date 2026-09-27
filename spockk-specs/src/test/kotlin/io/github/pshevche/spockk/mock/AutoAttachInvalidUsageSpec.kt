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

package io.github.pshevche.spockk.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.AutoAttachNonMockFieldSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.AutoAttachNullFieldSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.AutoAttachSharedFieldSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.runtime.SpockException
import spock.lang.Specification

class AutoAttachInvalidUsageSpec : Specification() {

  @MigratedFrom("org.spockframework.mock.AutoAttachInvalidUsageSpec#not on shared")
  fun `not on shared`() {
    `when`
    // @AutoAttach on a @Shared field fails while the spec is being built, before any
    // feature runs, so it surfaces as a container failure, not a test failure.
    val results = EngineTestKit.engine("spock")
      .selectors(selectClass(AutoAttachSharedFieldSpec::class.java))
      .execute()

    then
    results.containerEvents().assertStatistics { it.started(2).succeeded(1).failed(1) }
    val throwable = results.containerEvents().failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is SpockException
    throwable.message ==
      "@AutoAttach is only supported for instance fields " +
      "(offending field: ${AutoAttachSharedFieldSpec::class.java.name}.mockShared)"
  }

  @MigratedFrom("org.spockframework.mock.AutoAttachInvalidUsageSpec#null field")
  fun `null field`() {
    `when`
    val events = execute(selectClass(AutoAttachNullFieldSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is SpockException
    // Line number is where `field` is declared in AutoAttachNullFieldSpec.kt.
    throwable.message == "Cannot AutoAttach 'null' for field field:25"
  }

  @MigratedFrom("org.spockframework.mock.AutoAttachInvalidUsageSpec#No mock value for field")
  fun `No mock value for field`() {
    `when`
    val events = execute(selectClass(AutoAttachNonMockFieldSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is SpockException
    // Line number is where `field` is declared in AutoAttachNonMockFieldSpec.kt.
    throwable.message == "AutoAttach failed 'Value' is not a mock for field field:25"
  }
}
