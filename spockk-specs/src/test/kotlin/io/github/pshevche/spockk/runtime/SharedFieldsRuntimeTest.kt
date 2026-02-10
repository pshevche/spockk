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

import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.fields.DollarPrefixedFieldNames
import io.github.pshevche.spockk.fixtures.runtime.samples.fields.SharedFieldAccessFromSubclass
import io.github.pshevche.spockk.fixtures.runtime.samples.fields.SharedFieldLifecycle
import io.github.pshevche.spockk.fixtures.runtime.samples.fields.SharedFieldTypeCompatibility
import io.github.pshevche.spockk.fixtures.runtime.samples.fields.StaticFieldLifecycle
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.PendingFeature
import spock.lang.Specification

class SharedFieldsRuntimeTest : Specification() {

  fun `shared field can be accessed from subclass`() {
    `when`
    val events = execute(selectClass(SharedFieldAccessFromSubclass::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
  }

  @PendingFeature
  fun `shared fields with dollar prefixed names work correctly`() {
    `when`
    val events = execute(selectClass(DollarPrefixedFieldNames::class.java))

    then
    events.assertStatistics { it.started(3).succeeded(3) }
  }

  fun `shared field getters and setters use declared type`() {
    `when`
    val events = execute(selectClass(SharedFieldTypeCompatibility::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
  }

  @PendingFeature
  fun `shared fields reset between spec runs`() {
    `when`
    val firstRun = execute(selectClass(SharedFieldLifecycle::class.java))
    val secondRun = execute(selectClass(SharedFieldLifecycle::class.java))

    then
    firstRun.assertStatistics { it.started(2).succeeded(2) }
    secondRun.assertStatistics { it.started(2).succeeded(2) }
  }

  fun `static fields persist between spec runs`() {
    `when`
    val firstRun = execute(selectClass(StaticFieldLifecycle::class.java))
    val secondRun = execute(selectClass(StaticFieldLifecycle::class.java))

    then
    firstRun.assertStatistics { it.started(2).succeeded(2) }
    secondRun.assertStatistics { it.started(2).failed(2) }
  }
}
