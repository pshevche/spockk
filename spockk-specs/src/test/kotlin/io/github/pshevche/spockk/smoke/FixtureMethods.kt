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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureMethodsFailingInitializerLog
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureMethodsFailingInitializerSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureMethodsFailingSetupLog
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureMethodsFailingSetupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureMethodsHierarchyDerived
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureMethodsHierarchyLog
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureMethodsOrderLog
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureMethodsOrderSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class FixtureMethods : Specification() {

  @MigratedFrom("org.spockframework.smoke.FixtureMethods#are run in correct order")
  fun `are run in correct order`() {
    `when`
    FixtureMethodsOrderLog.log.clear()
    val events = execute(selectClass(FixtureMethodsOrderSpec::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
    FixtureMethodsOrderLog.log == listOf("ss", "s", "c", "cs")
  }

  @MigratedFrom("org.spockframework.smoke.FixtureMethods#are run in correct order across class hierarchy")
  fun `are run in correct order across class hierarchy`() {
    `when`
    FixtureMethodsHierarchyLog.log.clear()
    val events = execute(selectClass(FixtureMethodsHierarchyDerived::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
    FixtureMethodsHierarchyLog.log == listOf("ss1", "ss2", "s1", "s2", "c2", "c1", "cs2", "cs1")
  }

  @MigratedFrom("org.spockframework.smoke.FixtureMethods#cleanup() is run when setup() fails")
  fun `cleanup() is run when setup() fails`() {
    `when`
    FixtureMethodsFailingSetupLog.log.clear()
    val events = execute(selectClass(FixtureMethodsFailingSetupSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable =
      events.failed().list().single().getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is RuntimeException
    FixtureMethodsFailingSetupLog.log == listOf("cleanup")
  }

  @MigratedFrom("org.spockframework.smoke.FixtureMethods#cleanup() is not run when field initializer fails")
  fun `cleanup() is not run when field initializer fails`() {
    `when`
    FixtureMethodsFailingInitializerLog.log.clear()
    val events = execute(selectClass(FixtureMethodsFailingInitializerSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable =
      events.failed().list().single().getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is RuntimeException
    FixtureMethodsFailingInitializerLog.log.isEmpty()
  }
}
