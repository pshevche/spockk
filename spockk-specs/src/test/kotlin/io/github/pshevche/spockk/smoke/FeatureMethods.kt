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
import io.github.pshevche.spockk.fixtures.runtime.samples.featuremethods.OriginalNameSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class FeatureMethods : Specification() {

  @MigratedFrom("org.spockframework.smoke.FeatureMethods#are nevertheless reported with their original name")
  fun `are nevertheless reported with their original name`() {
    `when`
    val events = execute(selectClass(OriginalNameSpec::class.java))

    then
    events.started().list()[0].testDescriptor.displayName == "original name"
    events.finished().list()[0].testDescriptor.displayName == "original name"
  }
}
