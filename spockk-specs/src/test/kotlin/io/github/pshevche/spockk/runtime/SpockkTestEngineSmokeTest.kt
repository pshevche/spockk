/*
 * Copyright 2025 the original author or authors.
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
import io.github.pshevche.spockk.fixtures.runtime.samples.SimpleSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId
import spock.lang.Specification
import java.util.stream.Collectors.toSet

class SpockkTestEngineSmokeTest : Specification() {
  fun `discovers test class by class name`() {
    `when`
    val events = execute(selectClass(SimpleSpec::class.java))

    then
    events.assertStatistics { it.started(2).succeeded(1).failed(1) }
  }

  fun `discovers test class by unique id`() {
    `when`
    val events =
      execute(
        selectUniqueId(
          UniqueId.forEngine("spock").append("spec", SimpleSpec::class.qualifiedName!!)
        )
      )

    then
    events.assertStatistics { it.started(2).succeeded(1).failed(1) }
  }

  fun `discovers feature method by method name`() {
    `when`
    var events = execute(selectMethod(SimpleSpec::class.java, "successful feature"))

    then
    events.assertStatistics { it.started(1).succeeded(1) }

    `when`
    events = execute(selectMethod(SimpleSpec::class.java, "failing feature"))

    then
    events.assertStatistics { it.started(1).failed(1) }
  }

  fun `discovers feature method by unique id`() {
    `when`
    var events =
      execute(
        selectUniqueId(
          UniqueId.forEngine("spock")
            .append("spec", SimpleSpec::class.qualifiedName!!)
            .append("feature", "spock_feature_0_0")
        )
      )

    then
    events.assertStatistics { it.started(1).succeeded(1) }

    `when`
    events =
      execute(
        selectUniqueId(
          UniqueId.forEngine("spock")
            .append("spec", SimpleSpec::class.qualifiedName!!)
            .append("feature", "spock_feature_0_1")
        )
      )

    then
    events.assertStatistics { it.started(1).failed(1) }
  }

  fun `supports package selectors`() {
    `when`
    val events = execute(selectPackage(SimpleSpec::class.java.packageName))
    val specIds = events.map { it.testDescriptor.uniqueId.removeLastSegment() }.collect(toSet())

    then
    assert(specIds.count() == 5)
  }

  fun `executes tests in the declaration order`() {
    `when`
    val features =
      execute(selectClass(SimpleSpec::class.java))
        .started()
        .map { it.testDescriptor.displayName }
        .toList()

    then
    assert(features == listOf("successful feature", "failing feature"))
  }
}
