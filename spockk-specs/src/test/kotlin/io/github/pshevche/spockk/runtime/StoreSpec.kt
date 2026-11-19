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

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.samples.store.FailingStoreSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.store.StoreRecorder
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.runtime.model.MethodKind
import org.spockframework.util.InternalSpockError
import spock.lang.ResourceLock
import spock.lang.Specification

@ResourceLock("StoreRecorder")
class StoreSpec : Specification() {

  companion object {
    private val SUPPORTED_METHOD_KINDS =
      MethodKind.values().toList() -
        listOf(
          MethodKind.DATA_PROCESSOR,
          MethodKind.DATA_PROVIDER,
          MethodKind.DATA_VARIABLE_MULTIPLICATIONS,
          MethodKind.WHERE_VARIABLES,
          MethodKind.FILTER
        )
    private val METHOD_KIND_SETS = listOf(SUPPORTED_METHOD_KINDS) + SUPPORTED_METHOD_KINDS.map { listOf(it) }
  }

  private fun allAreCleanupFailures(failures: List<Throwable>) =
    failures.all { it is InternalSpockError && it.message!!.startsWith("Failing at ") }

  private fun sortedNames(kinds: List<MethodKind>) = kinds.map { it.name }.sorted()

  @MigratedFrom("org.spockframework.runtime.StoreSpec#properly handles failures during store cleanup")
  fun `properly handles failures during store cleanup`(methodKinds: List<MethodKind>) {
    `when`
    StoreRecorder.cleanupOrder = mutableListOf()
    StoreRecorder.failingKinds = methodKinds.toSet()
    val failures =
      EngineTestKit.engine("spock")
        .selectors(selectClass(FailingStoreSpec::class.java))
        .execute()
        .allEvents()
        .executions()
        .finished()
        .list()
        .map { it.terminationInfo.executionResult }
        .filter { it.status == TestExecutionResult.Status.FAILED }
        .map { it.throwable.get() }

    then
    failures.isNotEmpty()
    allAreCleanupFailures(failures)
    sortedNames(StoreRecorder.cleanupOrder) == sortedNames(SUPPORTED_METHOD_KINDS)

    where
    variable(methodKinds).from(METHOD_KIND_SETS)
  }
}
