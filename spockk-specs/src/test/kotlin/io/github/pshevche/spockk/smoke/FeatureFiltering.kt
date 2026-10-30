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
import io.github.pshevche.spockk.fixtures.runtime.samples.filtering.FilteringDerivedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.filtering.FilteringLog
import io.github.pshevche.spockk.fixtures.runtime.samples.filtering.FilteringLogSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.ResourceLock
import spock.lang.Specification

@ResourceLock("FilteringLog")
@Execution(ExecutionMode.SAME_THREAD)
class FeatureFiltering : Specification() {

  @MigratedFrom("org.spockframework.smoke.FeatureFiltering#filter selected feature methods")
  fun `filter selected feature methods`() {
    FilteringLog.value = ""

    `when`
    val events = execute(selectMethod(FilteringLogSpec::class.java, "feature2"))

    then
    FilteringLog.value == "2"
    events.succeeded().count() == 1L
    events.failed().count() == 0L
    events.skipped().count() == 0L
  }

  @MigratedFrom("org.spockframework.smoke.FeatureFiltering#filtering all feature methods results in empty result")
  fun `filtering all feature methods results in empty result`() {
    FilteringLog.value = ""

    `when`
    val events = execute(selectMethod(FilteringLogSpec::class.java, "xxx"))

    then
    noExceptionThrown()
    events.started().count() == 0L
    FilteringLog.value == ""
  }

  @MigratedFrom("org.spockframework.smoke.FeatureFiltering#filtering across inheritance chain")
  fun `filtering across inheritance chain`() {
    `when`
    val events =
      execute(
        selectMethod(FilteringDerivedSpec::class.java, "feature1"),
        selectMethod(FilteringDerivedSpec::class.java, "feature3")
      )

    then
    events.succeeded().count() == 2L
    events.failed().count() == 0L
    events.skipped().count() == 0L
  }
}
