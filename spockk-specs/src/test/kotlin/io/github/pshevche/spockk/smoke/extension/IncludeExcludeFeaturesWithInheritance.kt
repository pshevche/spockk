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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.inheritance.Fast
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.inheritance.IncludeExcludeBaseSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.inheritance.IncludeExcludeDerivedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.inheritance.Slow
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.Events
import org.spockframework.runtime.RunContext
import org.spockframework.runtime.extension.builtin.IncludeExcludeExtension
import org.spockframework.util.IThrowableFunction
import spock.config.IncludeExcludeCriteria
import spock.config.RunnerConfiguration
import spock.lang.Specification

class IncludeExcludeFeaturesWithInheritance : Specification() {

  @MigratedFrom(
    "org.spockframework.smoke.extension.IncludeExcludeFeaturesWithInheritance#include methods based on annotations"
  )
  fun `include methods based on annotations`(annotationTypes: List<Class<out Annotation>>, runCount: Int) {
    `when`
    val events = runSpecs { include = IncludeExcludeCriteria().apply { annotations.addAll(annotationTypes) } }

    then
    events.succeeded().count() == runCount.toLong()
    events.failed().count() == 0L
    events.skipped().count() == 0L

    where
    annotationTypes                            ; runCount
    listOf(Slow::class.java)                   ; 2
    listOf(Fast::class.java)                   ; 1
    listOf(Slow::class.java, Fast::class.java) ; 3
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.IncludeExcludeFeaturesWithInheritance#exclude methods based on annotations"
  )
  fun `exclude methods based on annotations`(annotationTypes: List<Class<out Annotation>>, runCount: Int) {
    `when`
    val events = runSpecs { exclude = IncludeExcludeCriteria().apply { annotations.addAll(annotationTypes) } }

    then
    events.succeeded().count() == runCount.toLong()
    events.failed().count() == 0L
    events.skipped().count() == 0L

    where
    annotationTypes                            ; runCount
    listOf(Slow::class.java)                   ; 3
    listOf(Fast::class.java)                   ; 4
    listOf(Slow::class.java, Fast::class.java) ; 2
  }

  private fun runSpecs(configure: RunnerConfiguration.() -> Unit) =
    RunContext.withNewContext<Events, RuntimeException>(
      "IncludeExcludeFeaturesWithInheritance",
      RunContext.get().spockUserHome,
      null,
      listOf(IncludeExcludeExtension::class.java),
      listOf(RunnerConfiguration::class.java),
      false,
      IThrowableFunction { context ->
        context.getConfiguration(RunnerConfiguration::class.java).configure()
        EngineTestKit
          .engine("spock")
          .selectors(selectClass(IncludeExcludeBaseSpec::class.java), selectClass(IncludeExcludeDerivedSpec::class.java))
          .execute()
          .testEvents()
      }
    )
}
