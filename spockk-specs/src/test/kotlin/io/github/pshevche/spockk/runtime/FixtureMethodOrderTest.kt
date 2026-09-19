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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureOrderDerived
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureOrderTracker
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class FixtureMethodOrderTest : Specification() {

  @MigratedFrom("org.spockframework.smoke.SpecInheritance#fixture methods are run in correct order")
  fun `overridden fixture methods run base then derived on setup, derived then base on cleanup`() {
    `when`
    FixtureOrderTracker.log.clear()
    val events = execute(selectClass(FixtureOrderDerived::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
    FixtureOrderTracker.log == listOf("ss1", "ss2", "s1", "s2", "c2", "c1", "cs2", "cs1")
  }
}
