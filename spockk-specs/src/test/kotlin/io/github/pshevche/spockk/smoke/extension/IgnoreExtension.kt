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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.executeAll
import io.github.pshevche.spockk.fixtures.runtime.samples.ignore.IgnoredFeaturesDerived
import io.github.pshevche.spockk.fixtures.runtime.samples.ignore.IgnoredFeaturesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.ignore.IgnoredFixtureMethodSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.ignore.IgnoredSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.ignore.InheritedIgnoreTest
import io.github.pshevche.spockk.fixtures.runtime.samples.ignore.NonInheritedIgnoreTest
import io.github.pshevche.spockk.fixtures.runtime.samples.ignore.NotIgnoredDerivedSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineExecutionResults
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/issues/134")
class IgnoreExtension : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreExtension#ignore spec")
  fun `ignore spec`() {
    `when`
    val result = executeAll(selectClass(IgnoredSpec::class.java))

    then
    result.containerEvents().started().count() == 1L
    result.containerEvents().failed().count() == 0L
    result.containerEvents().skipped().count() == 1L
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreExtension#ignored feature methods")
  fun `ignored feature methods`() {
    `when`
    val result = executeAll(selectClass(IgnoredFeaturesSpec::class.java))

    then
    result.testEvents().succeeded().count() == 1L
    result.testEvents().failed().count() == 0L
    result.testEvents().skipped().count() == 2L
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreExtension#fixture methods cannot be ignored")
  fun `fixture methods cannot be ignored`() {
    `when`
    val result = executeAll(selectClass(IgnoredFixtureMethodSpec::class.java))
    val failures = failureCauses(result)

    then
    failures.size == 1
    failures[0] is InvalidSpecException
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreExtension#ignore methods in base and derived class")
  fun `ignore methods in base and derived class`() {
    `when`
    val result = executeAll(selectClass(IgnoredFeaturesDerived::class.java))

    then
    result.testEvents().succeeded().count() == 2L
    result.testEvents().failed().count() == 0L
    result.testEvents().skipped().count() == 2L
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.IgnoreExtension#ignoring base class has no effect on running derived class"
  )
  fun `ignoring base class has no effect on running derived class`() {
    `when`
    val result = executeAll(selectClass(NotIgnoredDerivedSpec::class.java))

    then
    result.testEvents().succeeded().count() == 2L
    result.testEvents().failed().count() == 0L
    result.testEvents().skipped().count() == 0L
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreExtension#Ignore can be configured to be inherited")
  fun `Ignore can be configured to be inherited`(leaf: Class<*>, testStartAndSucceededCount: Long, specSkippedCount: Long) {
    `when`
    val result = executeAll(*hierarchySelectors(leaf))

    then
    result.testEvents().started().count() == testStartAndSucceededCount
    result.testEvents().failed().count() == 0L
    result.testEvents().skipped().count() == 0L
    result.testEvents().aborted().count() == 0L
    result.testEvents().succeeded().count() == testStartAndSucceededCount
    result.containerEvents().skipped().count() == specSkippedCount

    where
    leaf                           ; testStartAndSucceededCount ; specSkippedCount
    NonInheritedIgnoreTest::class.java ; 1L + 0L + 3L + 4L ; 1L
    InheritedIgnoreTest::class.java    ; 1L                ; 3L
  }

  private fun hierarchySelectors(leaf: Class<*>) =
    generateSequence(leaf) { it.superclass }
      .takeWhile { it != Specification::class.java }
      .map { selectClass(it) }
      .toList()
      .toTypedArray()

  private fun failureCauses(result: EngineExecutionResults) =
    result.containerEvents().failed().list().map {
      it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElse(null)
    }
}
