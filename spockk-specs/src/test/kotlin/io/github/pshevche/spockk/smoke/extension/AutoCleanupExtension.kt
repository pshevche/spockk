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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupAfterCleanupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupAfterCleanupSpecSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupCustomMethodSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupErrorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupFieldInitFailsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupIndependentSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupQuietSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupResources
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.AutoCleanupSharedFieldInitFailsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.BoomException
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.Events
import org.opentest4j.MultipleFailuresError
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.Specification

@Execution(ExecutionMode.SAME_THREAD)
class AutoCleanupExtension : Specification() {

  private fun failures(events: Events): List<Throwable> =
    events.failed().stream().map { it.getRequiredPayload(TestExecutionResult::class.java).throwable.get() }.toList()

  private fun multipleFailures(events: Events): MultipleFailuresError =
    failures(events).single() as MultipleFailuresError

  private fun anyBoom(events: Events): Boolean = failures(events).any { it is BoomException }

  fun setup() {
    AutoCleanupResources.reset()
  }

  @MigratedFrom("org.spockframework.smoke.extension.AutoCleanupExtension#@AutoCleanup resources are cleaned up after cleanup()")
  fun `@AutoCleanup resources are cleaned up after cleanup()`() {
    assert(!AutoCleanupResources.closable.called)

    `when`
    val events = execute(selectClass(AutoCleanupAfterCleanupSpec::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
    AutoCleanupResources.closable.called
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.AutoCleanupExtension#@Shared @AutoCleanup resources are cleaned up after cleanupSpec()"
  )
  fun `@Shared @AutoCleanup resources are cleaned up after cleanupSpec()`() {
    assert(!AutoCleanupResources.closable.called)

    `when`
    val events = execute(selectClass(AutoCleanupAfterCleanupSpecSpec::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
    AutoCleanupResources.closable.called
  }

  @MigratedFrom("org.spockframework.smoke.extension.AutoCleanupExtension#may specify custom method to be called for cleanup")
  fun `may specify custom method to be called for cleanup`() {
    `when`
    val events = execute(selectClass(AutoCleanupCustomMethodSpec::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
    AutoCleanupResources.disposable.called
  }

  @MigratedFrom("org.spockframework.smoke.extension.AutoCleanupExtension#error during cleanup will fail feature")
  fun `error during cleanup will fail feature`() {
    `when`
    val events = execute(selectClass(AutoCleanupErrorSpec::class.java))

    then
    events.assertStatistics { it.started(1).failed(1) }
    failures(events).single() is BoomException
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.AutoCleanupExtension#error during cleanup won't fail feature if 'quiet' option is used"
  )
  fun `error during cleanup won't fail feature if 'quiet' option is used`() {
    `when`
    val events = execute(selectClass(AutoCleanupQuietSpec::class.java))

    then
    events.assertStatistics { it.started(1).succeeded(1) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.AutoCleanupExtension#resources are cleaned up independently")
  fun `resources are cleaned up independently`() {
    `when`
    val events = execute(selectClass(AutoCleanupIndependentSpec::class.java))

    then
    AutoCleanupResources.boom1.called
    AutoCleanupResources.boom2.called

    and
    failures(events).size == 1
    verify(multipleFailures(events)) {
      failures[0] is BoomException
      failures[1] is BoomException
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.AutoCleanupExtension#no cleanup if field initialization fails")
  fun `no cleanup if field initialization fails`() {
    assert(!AutoCleanupResources.closable.called)

    `when`
    val events = execute(selectClass(AutoCleanupFieldInitFailsSpec::class.java))

    then
    anyBoom(events)
    !AutoCleanupResources.closable.called
  }

  @MigratedFrom("org.spockframework.smoke.extension.AutoCleanupExtension#no cleanup if shared field initialization fails")
  fun `no cleanup if shared field initialization fails`() {
    assert(!AutoCleanupResources.closable.called)

    `when`
    val events = EngineTestKit.engine("spock")
      .selectors(selectClass(AutoCleanupSharedFieldInitFailsSpec::class.java))
      .execute()
      .containerEvents()

    then
    anyBoom(events)
    !AutoCleanupResources.closable.called
  }
}
