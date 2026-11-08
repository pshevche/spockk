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

package io.github.pshevche.spockk.runtime.extension

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ExtensionCountASpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ExtensionCountBSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ExtensionCountCSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ExtensionCountEverywhereSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ExtensionCounter
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.RunContext
import org.spockframework.util.IThrowableFunction
import spock.lang.Specification
import java.util.concurrent.atomic.AtomicInteger

class AnnotationDrivenExtensionSpec : Specification() {

  @MigratedFrom(
    "org.spockframework.runtime.extension.AnnotationDrivenExtensionSpec#stateless extensions get re-used and stateful extensions get fresh instances per spec"
  )
  fun `stateless extensions get re-used and stateful extensions get fresh instances per spec`() {
    given
    val stateful = AtomicInteger()
    val stateless = AtomicInteger()

    `when`
    runWithCounters(
      stateful,
      stateless,
      selectClass(ExtensionCountASpec::class.java),
      selectClass(ExtensionCountBSpec::class.java),
      selectClass(ExtensionCountCSpec::class.java)
    )

    then
    stateful.get() == 3
    stateless.get() == 1
  }

  @MigratedFrom(
    "org.spockframework.runtime.extension.AnnotationDrivenExtensionSpec#both stateful and stateless only have one instance per spec regardless of use"
  )
  fun `both stateful and stateless only have one instance per spec regardless of use`() {
    given
    val stateful = AtomicInteger()
    val stateless = AtomicInteger()

    `when`
    runWithCounters(stateful, stateless, selectClass(ExtensionCountEverywhereSpec::class.java))

    then
    stateful.get() == 1
    stateless.get() == 1
  }

  // Counters are set on the config object directly: Groovy config scripts can't be loaded here.
  private fun runWithCounters(
    stateful: AtomicInteger,
    stateless: AtomicInteger,
    vararg selectors: DiscoverySelector
  ) {
    RunContext.withNewContext<Unit, RuntimeException>(
      "AnnotationDrivenExtensionSpec",
      RunContext.get().spockUserHome,
      null,
      emptyList(),
      listOf(ExtensionCounter::class.java),
      false,
      IThrowableFunction {
        val counter = RunContext.get().getConfiguration(ExtensionCounter::class.java)
        counter.statefulCounter = stateful
        counter.statelessCounter = stateless
        execute(*selectors).assertStatistics { it.failed(0) }
      }
    )
  }
}
