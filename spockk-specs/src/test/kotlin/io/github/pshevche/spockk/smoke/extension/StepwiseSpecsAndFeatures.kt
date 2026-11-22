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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseSpecWithPlainDataDrivenFeature
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StepwiseSpecWithStepwiseFeature
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class StepwiseSpecsAndFeatures : Specification() {

  @MigratedFrom(
    "org.spockframework.smoke.extension.StepwiseSpecsAndFeatures#spec annotation does not skip iterations on feature without annotation"
  )
  fun `spec annotation does not skip iterations on feature without annotation`() {
    `when`
    val events = execute(selectClass(StepwiseSpecWithPlainDataDrivenFeature::class.java))

    then
    events.assertStatistics { it.started(5 + 1).succeeded(4 + 1).failed(1).skipped(0) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.StepwiseSpecsAndFeatures#spec and feature annotations can be combined"
  )
  fun `spec and feature annotations can be combined`() {
    `when`
    val events = execute(selectClass(StepwiseSpecWithStepwiseFeature::class.java))

    then
    events.assertStatistics { it.started(1 + 3 + 1).succeeded(1 + 2 + 1).failed(1).skipped(2 + 1) }
  }
}
