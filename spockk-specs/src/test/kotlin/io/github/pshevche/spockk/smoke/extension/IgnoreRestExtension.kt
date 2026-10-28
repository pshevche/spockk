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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreRestDerivedSpec1
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreRestDerivedSpec2
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreRestDerivedSpec3
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreRestSomeMethodsSpec1
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreRestSomeMethodsSpec2
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreRestSomeMethodsSpec3
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class IgnoreRestExtension : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreRestExtension#ignore some methods")
  fun `ignore some methods`(spec: Class<*>, run: Int, ignored: Int) {
    `when`
    val events = execute(selectClass(spec))

    then
    events.assertStatistics { it.started(run.toLong()).succeeded(run.toLong()).failed(0).skipped(ignored.toLong()) }

    where
    variables(spec, run, ignored).from(
      listOf(
        IgnoreRestSomeMethodsSpec1::class.java,
        IgnoreRestSomeMethodsSpec2::class.java,
        IgnoreRestSomeMethodsSpec3::class.java
      ),
      listOf(1, 2, 3),
      listOf(2, 1, 0)
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreRestExtension#@IgnoreRest in base and/or derived class")
  fun `@IgnoreRest in base and-or derived class`(derived: Class<*>, runCount: Int, ignoreCount: Int) {
    `when`
    val events = execute(selectClass(derived))

    then
    events.assertStatistics { it.started(runCount.toLong()).succeeded(runCount.toLong()).failed(0).skipped(ignoreCount.toLong()) }

    where
    variables(derived, runCount, ignoreCount).from(
      listOf(
        IgnoreRestDerivedSpec1::class.java,
        IgnoreRestDerivedSpec2::class.java,
        IgnoreRestDerivedSpec3::class.java
      ),
      listOf(1, 1, 2),
      listOf(3, 3, 2)
    )
  }
}
