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

package io.github.pshevche.spockk.smoke.extension

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InheritanceFixture
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InheritanceFixtures
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SkippedSpecFixtures
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import spock.lang.Specification

class MixedSpecSkippingExtensions : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.MixedSpecSkippingExtensions#feature skipping condition is not evaluated in skipped spec")
  fun `feature skipping condition is not evaluated in skipped spec`(fixture: Class<*>) {
    `when`
    val results = EngineTestKit.engine("spock").selectors(selectClass(fixture)).execute()

    then
    results.testEvents().assertStatistics { it.started(0).failed(0).skipped(0).aborted(0).succeeded(0) }
    results.containerEvents().assertStatistics { it.skipped(1).failed(0) }

    where
    variable(fixture).from(SkippedSpecFixtures.all)
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.MixedSpecSkippingExtensions#feature skipping condition is not evaluated in skipped spec with inheritance"
  )
  fun `feature skipping condition is not evaluated in skipped spec with inheritance`(
    fixture: InheritanceFixture,
    inherited: Boolean,
    containersSkippedCount: Long,
    containersFailedCount: Long
  ) {
    `when`
    val results =
      EngineTestKit.engine("spock").selectors(*fixture.classes.map { selectClass(it) }.toTypedArray()).execute()

    then
    results.testEvents().assertStatistics { it.started(1).failed(0).skipped(0).aborted(0).succeeded(1) }
    results.containerEvents().assertStatistics { it.skipped(containersSkippedCount).failed(containersFailedCount) }

    where
    variable(fixture).from(InheritanceFixtures.all)
    variable(inherited).from(fixture.inherited)
    variable(containersSkippedCount).from(if (inherited) 3L else 1L)
    variable(containersFailedCount).from(if (inherited) 0L else 2L)
  }
}
