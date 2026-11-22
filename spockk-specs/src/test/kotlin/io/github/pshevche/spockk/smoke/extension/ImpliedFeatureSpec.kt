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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ImpliedBySub
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ImpliedFeatureSameSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ImpliesSub
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.FilterResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.launcher.PostDiscoveryFilter
import org.junit.platform.testkit.engine.EventConditions.displayName
import spock.lang.Specification

class ImpliedFeatureSpec : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.ImpliedFeatureSpec#implying a feature in the same specification")
  fun `implying a feature in the same specification`() {
    `when`
    val events = execute(selectClass(ImpliedFeatureSameSpec::class.java), onlyFeature("bar"))

    then
    events.succeeded().assertEventsMatchExactly(displayName("foo"), displayName("bar"))
  }

  @MigratedFrom("org.spockframework.smoke.extension.ImpliedFeatureSpec#implying a feature in a super specification")
  fun `implying a feature in a super specification`() {
    `when`
    val events = execute(selectClass(ImpliesSub::class.java), onlyFeature("bar"))

    then
    events.succeeded().assertEventsMatchExactly(displayName("foo"), displayName("bar"))
  }

  @MigratedFrom("org.spockframework.smoke.extension.ImpliedFeatureSpec#implying a feature in a sub specification")
  fun `implying a feature in a sub specification`() {
    `when`
    val events = execute(selectClass(ImpliedBySub::class.java), onlyFeature("foo"))

    then
    events.succeeded().assertEventsMatchExactly(displayName("foo"), displayName("bar"))
  }

  private fun onlyFeature(name: String) =
    PostDiscoveryFilter { FilterResult.includedIf(it.displayName == name) }
}
