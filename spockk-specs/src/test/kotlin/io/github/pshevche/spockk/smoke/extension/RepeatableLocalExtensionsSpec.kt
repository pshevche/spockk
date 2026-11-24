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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations10Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations11Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations1Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations2Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations3Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations4Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations5Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations6Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations7Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations8Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFeatureAnnotations9Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations10Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations11Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations1Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations2Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations3Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations4Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations5Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations6Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations7Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations8Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFieldAnnotations9Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations10Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations11Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations1Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations2Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations3Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations4Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations5Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations6Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations7Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations8Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedFixtureAnnotations9Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations10Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations11Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations1Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations2Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations3Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations4Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations5Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations6Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations7Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations8Spec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RepeatedSpecAnnotations9Spec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.PendingFeature
import spock.lang.Specification

class RepeatableLocalExtensionsSpec : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.RepeatableLocalExtensionsSpec#repeated annotations work properly on spec")
  fun `repeated annotations work properly on spec`(fixture: Class<*>) {
    `when`
    val events = execute(selectClass(fixture))

    then
    events.assertStatistics { it.started(1).succeeded(1) }

    where
    variable(fixture).from(
      RepeatedSpecAnnotations1Spec::class.java,
      RepeatedSpecAnnotations2Spec::class.java,
      RepeatedSpecAnnotations3Spec::class.java,
      RepeatedSpecAnnotations4Spec::class.java,
      RepeatedSpecAnnotations5Spec::class.java,
      RepeatedSpecAnnotations6Spec::class.java,
      RepeatedSpecAnnotations7Spec::class.java,
      RepeatedSpecAnnotations8Spec::class.java,
      RepeatedSpecAnnotations9Spec::class.java,
      RepeatedSpecAnnotations10Spec::class.java,
      RepeatedSpecAnnotations11Spec::class.java
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.RepeatableLocalExtensionsSpec#repeated annotations work properly on feature")
  fun `repeated annotations work properly on feature`(fixture: Class<*>) {
    `when`
    val events = execute(selectClass(fixture))

    then
    events.assertStatistics { it.started(1).succeeded(1) }

    where
    variable(fixture).from(
      RepeatedFeatureAnnotations1Spec::class.java,
      RepeatedFeatureAnnotations2Spec::class.java,
      RepeatedFeatureAnnotations3Spec::class.java,
      RepeatedFeatureAnnotations4Spec::class.java,
      RepeatedFeatureAnnotations5Spec::class.java,
      RepeatedFeatureAnnotations6Spec::class.java,
      RepeatedFeatureAnnotations7Spec::class.java,
      RepeatedFeatureAnnotations8Spec::class.java,
      RepeatedFeatureAnnotations9Spec::class.java,
      RepeatedFeatureAnnotations10Spec::class.java,
      RepeatedFeatureAnnotations11Spec::class.java
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.RepeatableLocalExtensionsSpec#repeated annotations work properly on fixture")
  fun `repeated annotations work properly on fixture`(fixture: Class<*>) {
    `when`
    val events = execute(selectClass(fixture))

    then
    events.assertStatistics { it.started(1).succeeded(1) }

    where
    variable(fixture).from(
      RepeatedFixtureAnnotations1Spec::class.java,
      RepeatedFixtureAnnotations2Spec::class.java,
      RepeatedFixtureAnnotations3Spec::class.java,
      RepeatedFixtureAnnotations4Spec::class.java,
      RepeatedFixtureAnnotations5Spec::class.java,
      RepeatedFixtureAnnotations6Spec::class.java,
      RepeatedFixtureAnnotations7Spec::class.java,
      RepeatedFixtureAnnotations8Spec::class.java,
      RepeatedFixtureAnnotations9Spec::class.java,
      RepeatedFixtureAnnotations10Spec::class.java,
      RepeatedFixtureAnnotations11Spec::class.java
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.RepeatableLocalExtensionsSpec#repeated annotations work properly on field")
  fun `repeated annotations work properly on field`(fixture: Class<*>) {
    `when`
    val events = execute(selectClass(fixture))

    then
    events.assertStatistics { it.started(1).succeeded(1) }

    where
    variable(fixture).from(
      RepeatedFieldAnnotations1Spec::class.java,
      RepeatedFieldAnnotations2Spec::class.java,
      RepeatedFieldAnnotations3Spec::class.java,
      RepeatedFieldAnnotations4Spec::class.java,
      RepeatedFieldAnnotations5Spec::class.java,
      RepeatedFieldAnnotations6Spec::class.java,
      RepeatedFieldAnnotations7Spec::class.java,
      RepeatedFieldAnnotations8Spec::class.java,
      RepeatedFieldAnnotations9Spec::class.java,
      RepeatedFieldAnnotations10Spec::class.java,
      RepeatedFieldAnnotations11Spec::class.java
    )
  }
}
