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
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.InvalidStubInteractionCalledFieldStubSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.InvalidStubInteractionFieldMockSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.InvalidStubInteractionOtherMethodFieldStubSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.InvalidStubInteractionUncalledFieldStubSpec
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.InvalidSpecException
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class InvalidStubInteraction : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/940")
  @MigratedFrom("org.spockframework.smoke.mock.InvalidStubInteraction#required interaction on field mock but not on stub")
  fun `required interaction on field mock but not on stub`() {
    expect
    execute(selectClass(InvalidStubInteractionFieldMockSpec::class.java)).failed().list().isEmpty()
  }

  @MigratedFrom("org.spockframework.smoke.mock.InvalidStubInteraction#required interaction on local mock but not on stub")
  fun `required interaction on local mock but not on stub`() {
    `when`
    val listMock = Mock(List::class.java) {
      0 * size
    }
    val list = Stub(List::class.java)
    list.size

    then
    notThrown(InvalidSpecException::class.java)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/940")
  @MigratedFrom("org.spockframework.smoke.mock.InvalidStubInteraction#required interaction on field stub that is called")
  fun `required interaction on field stub that is called`() {
    expect
    failureOf(InvalidStubInteractionCalledFieldStubSpec::class).contains("turn the stub into a mock")
  }

  @MigratedFrom("org.spockframework.smoke.mock.InvalidStubInteraction#required interaction on local stub that is called")
  fun `required interaction on local stub that is called`() {
    `when`
    val list = Stub(List::class.java) {
      0 * size
    }
    list.size

    then
    val ise = thrown(InvalidSpecException::class.java)
    ise.message!!.contains("turn the stub into a mock")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/940")
  @MigratedFrom(
    "org.spockframework.smoke.mock.InvalidStubInteraction#required interaction on field stub that is not called but another method is"
  )
  fun `required interaction on field stub that is not called but another method is`() {
    expect
    failureOf(InvalidStubInteractionOtherMethodFieldStubSpec::class).contains("turn the stub into a mock")
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.InvalidStubInteraction#required interaction on local stub that is not called but another method is"
  )
  fun `required interaction on local stub that is not called but another method is`() {
    `when`
    val list = Stub(List::class.java) {
      0 * size
    }
    list.isEmpty()

    then
    val ise = thrown(InvalidSpecException::class.java)
    ise.message!!.contains("turn the stub into a mock")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/940")
  @MigratedFrom("org.spockframework.smoke.mock.InvalidStubInteraction#required interaction on field stub that is not called at all")
  fun `required interaction on field stub that is not called at all`() {
    expect
    failureOf(InvalidStubInteractionUncalledFieldStubSpec::class).contains("turn the stub into a mock")
  }

  @MigratedFrom("org.spockframework.smoke.mock.InvalidStubInteraction#required interaction on local stub that is not called at all")
  fun `required interaction on local stub that is not called at all`() {
    `when`
    val list = Stub(List::class.java) {
      0 * size
    }

    then
    val ise = thrown(InvalidSpecException::class.java)
    ise.message!!.contains("turn the stub into a mock")
  }

  private fun failureOf(spec: KClass<*>): String =
    execute(selectClass(spec.java)).failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow().message ?: ""
}
