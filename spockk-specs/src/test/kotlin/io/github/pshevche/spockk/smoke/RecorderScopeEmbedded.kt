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
import io.github.pshevche.spockk.fixtures.runtime.samples.smoke.NestedConditionsWithoutTopLevelConditionSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class RecorderScopeEmbedded : Specification() {

  @MigratedFrom(
    "org.spockframework.smoke.RecorderScopeEmbedded#nested conditions without top-level condition"
  )
  fun `nested conditions without top-level condition`() {
    `when`
    val events = execute(selectClass(NestedConditionsWithoutTopLevelConditionSpec::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1).failed(0) }
  }
}
