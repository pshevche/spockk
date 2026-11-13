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

package io.github.pshevche.spockk.smoke.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.InvalidMockCreationFieldSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Specification

class InvalidMockCreation : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.InvalidMockCreation#field w/ incompatible type")
  fun `field with incompatible type`() {
    `when`
    val events = execute(selectClass(InvalidMockCreationFieldSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    val throwable = events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    throwable is ClassCastException
  }

  @MigratedFrom("org.spockframework.smoke.mock.InvalidMockCreation#local w/ incompatible type")
  fun `local with incompatible type`() {
    `when`
    val list: List<*> = Mock(Map::class.java) as List<*>

    then
    thrown(ClassCastException::class.java)
  }

  @MigratedFrom("org.spockframework.smoke.mock.InvalidMockCreation#expr w/ missing type")
  fun `expr with missing type`() {
    `when`
    Mock<Any>()

    then
    thrown(InvalidSpecException::class.java)
  }
}
