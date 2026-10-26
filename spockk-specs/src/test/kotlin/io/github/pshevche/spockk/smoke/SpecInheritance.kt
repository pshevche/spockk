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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.executeAndCollectFailures
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureOrderDerived
import io.github.pshevche.spockk.fixtures.runtime.samples.fixture.FixtureOrderTracker
import io.github.pshevche.spockk.fixtures.runtime.samples.inheritance.CountsDerived
import io.github.pshevche.spockk.fixtures.runtime.samples.inheritance.FeatureOrderDerived
import io.github.pshevche.spockk.fixtures.runtime.samples.inheritance.InheritanceTracker
import io.github.pshevche.spockk.fixtures.runtime.samples.inheritance.OverriddenFeatureDerived
import io.github.pshevche.spockk.fixtures.runtime.samples.inheritance.PrivateFeatureDerived
import io.github.pshevche.spockk.fixtures.runtime.samples.inheritance.SuperHelperDerived
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.PendingFeature
import spock.lang.Specification
import java.io.IOException

class SpecInheritance : Specification() {

  @MigratedFrom("org.spockframework.smoke.SpecInheritance#fixture methods are run in correct order")
  fun `fixture methods are run in correct order`() {
    `when`
    FixtureOrderTracker.log.clear()
    val events = execute(selectClass(FixtureOrderDerived::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
    FixtureOrderTracker.log == listOf("ss1", "ss2", "s1", "s2", "c2", "c1", "cs2", "cs1")
  }

  @MigratedFrom("org.spockframework.smoke.SpecInheritance#feature methods are run in correct order")
  fun `feature methods are run in correct order`() {
    `when`
    InheritanceTracker.featureOrder.clear()
    val events = execute(selectClass(FeatureOrderDerived::class.java))

    then
    events.assertStatistics { it.started(4).succeeded(4) }
    InheritanceTracker.featureOrder == listOf(1, 2, 3, 4)
  }

  @MigratedFrom("org.spockframework.smoke.SpecInheritance#exception in base class fixture method causes failure")
  fun `exception in base class fixture method causes failure`(fixtureMethod: String) {
    `when`
    val failures = executeAndCollectFailures(selectClass("$INHERITANCE_SAMPLES.Derived${fixtureMethod.replaceFirstChar { it.uppercase() }}Throws"))

    then
    failures.filterIsInstance<IOException>().isNotEmpty()

    where
    variable(fixtureMethod).from("setup", "cleanup", "setupSpec", "cleanupSpec")
  }

  @MigratedFrom("org.spockframework.smoke.SpecInheritance#junit reports correct run/failure/ignore counts")
  fun `junit reports correct run failure ignore counts`() {
    `when`
    val events = execute(selectClass(CountsDerived::class.java))

    then
    events.assertStatistics { it.started(4).succeeded(2).failed(2).skipped(1) }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/801")
  @MigratedFrom("org.spockframework.smoke.SpecInheritance#feature methods cannot be overridden")
  fun `feature methods cannot be overridden`() {
    `when`
    InheritanceTracker.overriddenFeature.clear()
    val events = execute(selectClass(OverriddenFeatureDerived::class.java))

    then
    InheritanceTracker.overriddenFeature == listOf(1, 2)
    events.assertStatistics { it.succeeded(2) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.SpecInheritance#private feature methods with same name can coexist peacefully in inheritance chain"
  )
  fun `private feature methods with same name can coexist peacefully in inheritance chain`() {
    `when`
    InheritanceTracker.privateFeature.clear()
    val events = execute(selectClass(PrivateFeatureDerived::class.java))

    then
    InheritanceTracker.privateFeature == listOf(1, 2)
    events.assertStatistics { it.succeeded(2) }
  }

  @MigratedFrom("org.spockframework.smoke.SpecInheritance#can invoke super.helperMethod()")
  fun `can invoke super helperMethod()`() {
    `when`
    val events = execute(selectClass(SuperHelperDerived::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
  }
}

private const val INHERITANCE_SAMPLES = "io.github.pshevche.spockk.fixtures.runtime.samples.inheritance"
