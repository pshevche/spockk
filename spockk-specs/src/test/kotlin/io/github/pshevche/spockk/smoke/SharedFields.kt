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
import io.github.pshevche.spockk.fixtures.runtime.samples.fields.SharedFieldAccessFromSubclass
import io.github.pshevche.spockk.fixtures.runtime.samples.fields.SharedFieldDollarName
import io.github.pshevche.spockk.fixtures.runtime.samples.fields.SharedFieldTypeCompatibility
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.PendingFeature
import spock.lang.Specification

class SharedFields : Specification() {

  @MigratedFrom("org.spockframework.smoke.SharedFields#can be accessed from subclass")
  fun `can be accessed from subclass`() {
    `when`
    val events = execute(selectClass(SharedFieldAccessFromSubclass::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/797")
  @MigratedFrom("org.spockframework.smoke.SharedFields#can have name that starts with $")
  fun `can have name that starts with $`() {
    `when`
    val events = execute(selectClass(SharedFieldDollarName::class.java))

    then
    events.assertStatistics { it.started(3).succeeded(3) }
  }

  @MigratedFrom(
    "org.spockframework.smoke.SharedFields#getters and setters generated for shared fields use the declared type of the field"
  )
  fun `getters and setters generated for shared fields use the declared type of the field`() {
    `when`
    val events = execute(selectClass(SharedFieldTypeCompatibility::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
  }
}
