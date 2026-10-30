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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.includeexcludespecs.Fast
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.includeexcludespecs.Slow
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.includeexcludespecs.Spec1
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.includeexcludespecs.Spec2
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.includeexcludespecs.Spec3
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
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

class IncludeExcludeSpecs : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.IncludeExcludeSpecs#include specs based on annotations")
  fun `include specs based on annotations`(annotationTypes: List<Class<out Annotation>>, runCount: Int) {
    val configure: RunnerConfiguration.() -> Unit = { include = criteria(annotationTypes) }

    `when`
    val events = runSpecs(configure)

    then
    events.succeeded().count() == runCount.toLong()
    events.failed().count() == 0L
    events.skipped().count() == 0L

    where
    variable(annotationTypes).from(listOf(Slow::class.java), listOf(Fast::class.java), listOf(Slow::class.java, Fast::class.java))
    variable(runCount).from(1, 1, 2)
  }

  @MigratedFrom("org.spockframework.smoke.extension.IncludeExcludeSpecs#exclude specs based on annotations")
  fun `exclude specs based on annotations`(annotationTypes: List<Class<out Annotation>>, runCount: Int) {
    val configure: RunnerConfiguration.() -> Unit = { exclude = criteria(annotationTypes) }

    `when`
    val events = runSpecs(configure)

    then
    events.succeeded().count() == runCount.toLong()
    events.failed().count() == 0L
    events.skipped().count() == 0L

    where
    variable(annotationTypes).from(listOf(Slow::class.java), listOf(Fast::class.java), listOf(Slow::class.java, Fast::class.java))
    variable(runCount).from(2, 2, 1)
  }

  @MigratedFrom("org.spockframework.smoke.extension.IncludeExcludeSpecs#include and exclude specs based on annotations")
  fun `include and exclude specs based on annotations`(
    annTypes1: List<Class<out Annotation>>,
    annTypes2: List<Class<out Annotation>>,
    runCount: Int
  ) {
    val configure: RunnerConfiguration.() -> Unit = {
      include = criteria(annTypes1)
      exclude = criteria(annTypes2)
    }

    `when`
    val events = runSpecs(configure)

    then
    events.succeeded().count() == runCount.toLong()
    events.failed().count() == 0L
    events.skipped().count() == 0L

    where
    variable(annTypes1).from(
      listOf(Slow::class.java), listOf(Slow::class.java), listOf(Slow::class.java),
      listOf(Fast::class.java), listOf(Fast::class.java), listOf(Fast::class.java),
      listOf(Slow::class.java, Fast::class.java), listOf(Slow::class.java, Fast::class.java), listOf(Slow::class.java, Fast::class.java)
    )
    variable(annTypes2).from(
      listOf(Slow::class.java), listOf(Fast::class.java), listOf(Slow::class.java, Fast::class.java),
      listOf(Slow::class.java), listOf(Fast::class.java), listOf(Slow::class.java, Fast::class.java),
      listOf(Slow::class.java), listOf(Fast::class.java), listOf(Slow::class.java, Fast::class.java)
    )
    variable(runCount).from(0, 1, 0, 1, 0, 0, 1, 1, 0)
  }

  private fun criteria(annotationTypes: List<Class<out Annotation>>) =
    IncludeExcludeCriteria().apply { annotations.addAll(annotationTypes) }

  private fun runSpecs(configure: RunnerConfiguration.() -> Unit) =
    RunContext.withNewContext<Events, RuntimeException>(
      "IncludeExcludeSpecs",
      RunContext.get().spockUserHome,
      null,
      listOf(IncludeExcludeExtension::class.java),
      listOf(RunnerConfiguration::class.java),
      false,
      IThrowableFunction { context ->
        context.getConfiguration(RunnerConfiguration::class.java).configure()
        EngineTestKit.engine("spock")
          .selectors(selectClass(Spec1::class.java), selectClass(Spec2::class.java), selectClass(Spec3::class.java))
          .execute()
          .testEvents()
      }
    )
}
