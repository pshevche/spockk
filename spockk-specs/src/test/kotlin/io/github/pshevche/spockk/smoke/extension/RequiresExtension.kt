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

import groovy.lang.MissingPropertyException
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.executeAll
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.ArbitraryExceptionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoredSpecWithCustomMessage
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InheritanceBase
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InheritedBar
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InheritedFoo
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InheritedTest
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InstanceFieldAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.InstanceSharedFieldAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.NotInheritedBar
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.NotInheritedFoo
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.NotInheritedTest
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RequiresExtensionExamples
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RequiresFlag
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SharedFieldAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SpecUsageWithFalse
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SpecUsageWithInstanceFieldAccess
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SpecUsageWithSharedFieldAccess
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SpecUsageWithTrue
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SpecUsageWithUnqualifiedStaticFieldAccess
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.SpecUsageWithUnqualifiedStaticMethodAccess
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.StaticFieldAccessSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.UninstantiableConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.UnknownDataVariableSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.UnknownPropertySpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineExecutionResults
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.PendingFeature
import spock.lang.Specification

class RequiresExtension : Specification() {
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom(
    "org.spockframework.smoke.extension.RequiresExtension#runs feature if precondition is satisfied",
    "org.spockframework.smoke.extension.RequiresExtension#skips feature if precondition is not satisfied",
    "org.spockframework.smoke.extension.RequiresExtension#provides OS information",
    "org.spockframework.smoke.extension.RequiresExtension#provides JVM information",
    "org.spockframework.smoke.extension.RequiresExtension#provides access to environment variables",
    "org.spockframework.smoke.extension.RequiresExtension#provides access to system properties",
    "org.spockframework.smoke.extension.RequiresExtension#can use closure argument for an easy option to typecast and use IDE support",
    "org.spockframework.smoke.extension.RequiresExtension#can evaluate for single iterations if data variables are accessed",
    "org.spockframework.smoke.extension.RequiresExtension#can skip data providers completely if no data variables are accessed",
    "org.spockframework.smoke.extension.RequiresExtension#can skip data providers completely if no data variables are accessed by checking a `shared.` field",
    "org.spockframework.smoke.extension.RequiresExtension#allows determinate use of multiple filters",
    "org.spockframework.smoke.extension.RequiresExtension#feature is ignored if at least one Requires annotation is false",
    "org.spockframework.smoke.extension.RequiresExtension#feature is not ignored if all Requires annotations are true",
    "org.spockframework.smoke.extension.RequiresExtension#feature is ignored if at least one Requires annotation is false",
    "org.spockframework.smoke.extension.RequiresExtension#feature is not ignored if all Requires annotations are true"
  )
  fun verifyExecution() {
    `when`
    val results = executeAll(selectClass(RequiresExtensionExamples::class.java))
    val tests = results.testEvents()
    val skippedNames = tests.skipped().map { it.testDescriptor.displayName }.toList()

    then
    tests.succeeded().count() == 9L
    tests.failed().count() == 0L
    tests.skipped().count() == 4L
    skippedNames == listOf(
      "skips feature if precondition is not satisfied",
      "can skip data providers completely if no data variables are accessed",
      "allows determinate use of multiple filters",
      "feature is ignored if at least one Requires annotation is false"
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#fails directly when referencing an unknown property")
  fun `fails directly when referencing an unknown property`() {
    `when`
    val cause = extensionFailureCause(UnknownPropertySpec::class.java)

    then
    cause is MissingPropertyException
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#fails directly when referencing an unknown variable")
  fun `fails directly when referencing an unknown variable`() {
    `when`
    val cause = extensionFailureCause(UnknownDataVariableSpec::class.java)

    then
    cause is PreconditionContext.DataVariableContextException
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#fails directly when throwing an arbitrary exception")
  fun `fails directly when throwing an arbitrary exception`() {
    `when`
    val cause = extensionFailureCause(ArbitraryExceptionSpec::class.java)

    then
    cause is UnsupportedOperationException
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#spec usage with true")
  fun `spec usage with true`() {
    `when`
    val tests = executeAll(selectClass(SpecUsageWithTrue::class.java)).testEvents()

    then
    tests.started().count() == 1L
    tests.failed().count() == 0L
    tests.skipped().count() == 0L
    tests.aborted().count() == 0L
    tests.succeeded().count() == 1L
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#spec usage with unqualified static method access")
  fun `spec usage with unqualified static method access`(shouldRun: Boolean, testStartAndSucceededCount: Long, specSkippedCount: Long) {
    `when`
    RequiresFlag.shouldRun = shouldRun
    val results = executeAll(selectClass(SpecUsageWithUnqualifiedStaticMethodAccess::class.java))
    val tests = results.testEvents()

    then
    tests.started().count() == testStartAndSucceededCount
    tests.failed().count() == 0L
    tests.skipped().count() == 0L
    tests.aborted().count() == 0L
    tests.succeeded().count() == testStartAndSucceededCount
    results.containerEvents().skipped().count() == specSkippedCount

    where
    variables(shouldRun, testStartAndSucceededCount, specSkippedCount).from(listOf(true, false), listOf(1L, 0L), listOf(0L, 1L))
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#spec usage with unqualified static field access")
  fun `spec usage with unqualified static field access`(shouldRun: Boolean, testStartAndSucceededCount: Long, specSkippedCount: Long) {
    `when`
    RequiresFlag.shouldRun = shouldRun
    val results = executeAll(selectClass(SpecUsageWithUnqualifiedStaticFieldAccess::class.java))
    val tests = results.testEvents()

    then
    tests.started().count() == testStartAndSucceededCount
    tests.failed().count() == 0L
    tests.skipped().count() == 0L
    tests.aborted().count() == 0L
    tests.succeeded().count() == testStartAndSucceededCount
    results.containerEvents().skipped().count() == specSkippedCount

    where
    variables(shouldRun, testStartAndSucceededCount, specSkippedCount).from(listOf(true, false), listOf(1L, 0L), listOf(0L, 1L))
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#spec usage with shared field access")
  fun `spec usage with shared field access`(shouldRun: Boolean, testStartAndSucceededCount: Long, specAbortedCount: Long) {
    `when`
    RequiresFlag.shouldRun = shouldRun
    val results = executeAll(selectClass(SpecUsageWithSharedFieldAccess::class.java))
    val tests = results.testEvents()

    then
    tests.started().count() == testStartAndSucceededCount
    tests.failed().count() == 0L
    tests.skipped().count() == 0L
    tests.aborted().count() == 0L
    tests.succeeded().count() == testStartAndSucceededCount
    results.containerEvents().started().count() == 2L
    results.containerEvents().aborted().count() == specAbortedCount

    where
    variables(shouldRun, testStartAndSucceededCount, specAbortedCount).from(listOf(true, false), listOf(1L, 0L), listOf(0L, 1L))
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#spec usage with instance field access")
  fun `spec usage with instance field access`(shouldRun: Boolean, testSucceededCount: Long, testAbortedCount: Long) {
    `when`
    RequiresFlag.shouldRun = shouldRun
    val tests = executeAll(selectClass(SpecUsageWithInstanceFieldAccess::class.java)).testEvents()

    then
    tests.started().count() == 1L
    tests.failed().count() == 0L
    tests.skipped().count() == 0L
    tests.aborted().count() == testAbortedCount
    tests.succeeded().count() == testSucceededCount

    where
    variables(shouldRun, testSucceededCount, testAbortedCount).from(listOf(true, false), listOf(1L, 0L), listOf(0L, 1L))
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#spec usage with false")
  fun `spec usage with false`() {
    `when`
    val tests = executeAll(selectClass(SpecUsageWithFalse::class.java)).testEvents()

    then
    tests.started().count() == 0L
    tests.failed().count() == 0L
    tests.skipped().count() == 0L
    tests.aborted().count() == 0L
    tests.succeeded().count() == 0L
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#ignored spec contains custom message")
  fun `ignored spec contains custom message`() {
    `when`
    val results = executeAll(selectClass(IgnoredSpecWithCustomMessage::class.java))
    val skipped = results.allEvents().skipped().list()

    then
    skipped[0].payload.get() == "Ignored via @Requires: dummy message"
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#fails if condition cannot be instantiated")
  fun `fails if condition cannot be instantiated`() {
    `when`
    val failure = extensionFailure(UninstantiableConditionSpec::class.java)

    then
    failure.message == "Failed to instantiate condition"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/895")
  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#@Requires provides condition access to Specification instance shared fields")
  fun `@Requires provides condition access to Specification instance shared fields`() {
    `when`
    val tests = executeAll(selectClass(InstanceSharedFieldAccessSpec::class.java)).testEvents()

    then
    tests.started().count() == 4L
    tests.succeeded().count() == 3L
    tests.aborted().count() == 1L
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#@Requires provides condition access to Specification shared fields")
  fun `@Requires provides condition access to Specification shared fields`() {
    `when`
    val tests = executeAll(selectClass(SharedFieldAccessSpec::class.java)).testEvents()

    then
    tests.started().count() == 4L
    tests.succeeded().count() == 3L
    tests.aborted().count() == 1L
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#@Requires provides condition access to Specification instance fields")
  fun `@Requires provides condition access to Specification instance fields`() {
    `when`
    val tests = executeAll(selectClass(InstanceFieldAccessSpec::class.java)).testEvents()

    then
    tests.started().count() == 4L
    tests.succeeded().count() == 3L
    tests.aborted().count() == 1L
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#@Requires provides condition access to static Specification fields")
  fun `@Requires provides condition access to static Specification fields`() {
    `when`
    val tests = executeAll(selectClass(StaticFieldAccessSpec::class.java)).testEvents()

    then
    tests.started().count() == 1L
    tests.skipped().count() == 1L
    tests.succeeded().count() == 1L
    tests.failed().count() == 0L
  }

  @MigratedFrom("org.spockframework.smoke.extension.RequiresExtension#Requires can be configured to be inherited")
  fun `Requires can be configured to be inherited`(
    inherited: Boolean,
    testStartAndSucceededCount: Long,
    specSkippedCount: Long
  ) {
    `when`
    val results = if (inherited) {
      executeAll(
        selectClass(InheritanceBase::class.java),
        selectClass(InheritedFoo::class.java),
        selectClass(InheritedBar::class.java),
        selectClass(InheritedTest::class.java)
      )
    } else {
      executeAll(
        selectClass(InheritanceBase::class.java),
        selectClass(NotInheritedFoo::class.java),
        selectClass(NotInheritedBar::class.java),
        selectClass(NotInheritedTest::class.java)
      )
    }
    val tests = results.testEvents()

    then
    tests.started().count() == testStartAndSucceededCount
    tests.failed().count() == 0L
    tests.skipped().count() == 0L
    tests.aborted().count() == 0L
    tests.succeeded().count() == testStartAndSucceededCount
    results.containerEvents().skipped().count() == specSkippedCount

    where
    variables(inherited, testStartAndSucceededCount, specSkippedCount)
      .from(listOf(false, true), listOf(1L + 0 + 3 + 4, 1L), listOf(1L, 3L))
  }

  private fun extensionFailure(spec: Class<*>): Throwable {
    val results = executeAll(selectClass(spec))
    return results.containerEvents().failed().list()
      .map { it.getRequiredPayload(TestExecutionResult::class.java).throwable.get() }
      .first { it is ExtensionException }
  }

  private fun extensionFailureCause(spec: Class<*>): Throwable? = extensionFailure(spec).cause
}
