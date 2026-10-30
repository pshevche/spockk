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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfAllFalseSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfAlways
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfAnyJvm
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfAnyOs
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfCondition
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfDataAIsNotOne
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfDataAIsOne
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfEnvVarMissing
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfFalse
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfFeatureInstanceFieldSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfFeatureInstanceSharedFieldSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfFeatureSharedFieldSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfFeatureStaticFieldSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfInheritanceBaseSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfInheritedBarSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfInheritedFooSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfInheritedTestSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfInstanceFieldAbortedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfInstanceFieldRunsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfNever
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfNotInheritedBarSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfNotInheritedFooSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfNotInheritedTestSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfSharedFieldAbortedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfSharedFieldRunsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfSpecFalseSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfSpecTrueSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfSpecWithReasonSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfStaticFieldRunsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfStaticFieldSkippedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfStaticMethodRunsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfStaticMethodSkippedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfSysPropPresent
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfThrowingConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfTrue
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfUninstantiableConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfUnknownDataVariableSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfUnknownPropertySpec
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineExecutionResults
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf
import spock.lang.PendingFeature
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

class IgnoreIfShouldIgnoreShared(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<IgnoreIfExtension>): Any? = context.shared.shouldIgnore
}

class IgnoreIfTypedContext(owner: Any?, thisObject: Any?) : IgnoreIfCondition(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<IgnoreIfExtension>): Any? =
    !context.sys.contains("java.version")
}

private fun failingDataProvider(): Int = throw RuntimeException()

private fun EngineExecutionResults.failure(): Throwable =
  allEvents().failed().list().single().getPayload(TestExecutionResult::class.java).get().throwable.get()

class IgnoreIfExtension : Specification() {

  @Shared
  val shouldIgnore = true

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#basic usage")
  @IgnoreIf(IgnoreIfAlways::class)
  fun `basic usage`() {
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#provides OS information")
  @IgnoreIf(IgnoreIfAnyOs::class)
  fun `provides OS information`() {
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#provides JVM information")
  @IgnoreIf(IgnoreIfAnyJvm::class)
  fun `provides JVM information`() {
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#provides access to environment variables")
  @IgnoreIf(IgnoreIfEnvVarMissing::class)
  fun `provides access to environment variables`() {
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#provides access to system properties")
  @IgnoreIf(IgnoreIfSysPropPresent::class)
  fun `provides access to system properties`() {
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#can use closure argument for an easy option to typecast and use IDE support")
  @IgnoreIf(IgnoreIfTypedContext::class)
  fun `can use closure argument for an easy option to typecast and use IDE support`() {
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#can evaluate for single iterations if data variables are accessed")
  @IgnoreIf(IgnoreIfDataAIsOne::class)
  fun `can evaluate for single iterations if data variables are accessed`(a: Int) {
    expect
    a == 2

    where
    variable(a).from(1, 2)
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#can skip data providers completely if no data variables are accessed")
  @IgnoreIf(IgnoreIfTrue::class)
  fun `can skip data providers completely if no data variables are accessed`(a: Int) {
    expect
    false

    where
    variable(a).from(failingDataProvider())
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#can skip data providers completely if no data variables are accessed by checking a `shared.` field")
  @IgnoreIf(IgnoreIfShouldIgnoreShared::class)
  fun `can skip data providers completely if no data variables are accessed by checking a shared field`(a: Int) {
    expect
    false

    where
    variable(a).from(failingDataProvider())
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#fails directly when referencing an unknown property")
  fun `fails directly when referencing an unknown property`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfUnknownPropertySpec::class.java))

    then
    (events.failure() as ExtensionException).cause is MissingPropertyException
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#fails directly when referencing an unknown variable")
  fun `fails directly when referencing an unknown variable`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfUnknownDataVariableSpec::class.java))

    then
    (events.failure() as ExtensionException).cause is PreconditionContext.DataVariableContextException
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#fails directly when throwing an arbitrary exception")
  fun `fails directly when throwing an arbitrary exception`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfThrowingConditionSpec::class.java))

    then
    (events.failure() as ExtensionException).cause is UnsupportedOperationException
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#allows determinate use of multiple filters")
  @Requires(IgnoreIfFalse::class)
  @IgnoreIf(IgnoreIfFalse::class)
  fun `allows determinate use of multiple filters`() {
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#spec usage with true")
  fun `spec usage with true`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfSpecTrueSpec::class.java))

    then
    events.testEvents().started().count() == 0L
    events.testEvents().failed().count() == 0L
    events.testEvents().skipped().count() == 0L
    events.testEvents().aborted().count() == 0L
    events.testEvents().succeeded().count() == 0L
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#spec usage with false")
  fun `spec usage with false`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfSpecFalseSpec::class.java))

    then
    events.testEvents().started().count() == 1L
    events.testEvents().failed().count() == 0L
    events.testEvents().skipped().count() == 0L
    events.testEvents().aborted().count() == 0L
    events.testEvents().succeeded().count() == 1L
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#ignored spec contains custom message")
  fun `ignored spec contains custom message`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfSpecWithReasonSpec::class.java))

    then
    events.allEvents().skipped().list().first().getPayload(String::class.java).get() ==
      "Ignored via @IgnoreIf: dummy message"
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#spec usage with unqualified static method access")
  fun `spec usage with unqualified static method access`(
    spec: Class<*>,
    testStartAndSucceededCount: Long,
    specSkippedCount: Long
  ) {
    `when`
    val events = executeAll(selectClass(spec))

    then
    events.testEvents().started().count() == testStartAndSucceededCount
    events.testEvents().failed().count() == 0L
    events.testEvents().skipped().count() == 0L
    events.testEvents().aborted().count() == 0L
    events.testEvents().succeeded().count() == testStartAndSucceededCount
    events.containerEvents().skipped().count() == specSkippedCount

    where
    variables(spec, testStartAndSucceededCount, specSkippedCount).from(
      listOf(IgnoreIfStaticMethodRunsSpec::class.java, IgnoreIfStaticMethodSkippedSpec::class.java),
      listOf(1L, 0L),
      listOf(0L, 1L)
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#spec usage with unqualified static field access")
  fun `spec usage with unqualified static field access`(
    spec: Class<*>,
    testStartAndSucceededCount: Long,
    specSkippedCount: Long
  ) {
    `when`
    val events = executeAll(selectClass(spec))

    then
    events.testEvents().started().count() == testStartAndSucceededCount
    events.testEvents().failed().count() == 0L
    events.testEvents().skipped().count() == 0L
    events.testEvents().aborted().count() == 0L
    events.testEvents().succeeded().count() == testStartAndSucceededCount
    events.containerEvents().skipped().count() == specSkippedCount

    where
    variables(spec, testStartAndSucceededCount, specSkippedCount).from(
      listOf(IgnoreIfStaticFieldRunsSpec::class.java, IgnoreIfStaticFieldSkippedSpec::class.java),
      listOf(1L, 0L),
      listOf(0L, 1L)
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#spec usage with shared field access")
  fun `spec usage with shared field access`(
    spec: Class<*>,
    testStartAndSucceededCount: Long,
    specAbortedCount: Long
  ) {
    `when`
    val events = executeAll(selectClass(spec))

    then
    events.testEvents().started().count() == testStartAndSucceededCount
    events.testEvents().failed().count() == 0L
    events.testEvents().skipped().count() == 0L
    events.testEvents().aborted().count() == 0L
    events.testEvents().succeeded().count() == testStartAndSucceededCount
    events.containerEvents().started().count() == 2L
    events.containerEvents().aborted().count() == specAbortedCount

    where
    variables(spec, testStartAndSucceededCount, specAbortedCount).from(
      listOf(IgnoreIfSharedFieldRunsSpec::class.java, IgnoreIfSharedFieldAbortedSpec::class.java),
      listOf(1L, 0L),
      listOf(0L, 1L)
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#spec usage with instance field access")
  fun `spec usage with instance field access`(
    spec: Class<*>,
    testSucceededCount: Long,
    testAbortedCount: Long
  ) {
    `when`
    val events = executeAll(selectClass(spec))

    then
    events.testEvents().started().count() == 1L
    events.testEvents().failed().count() == 0L
    events.testEvents().skipped().count() == 0L
    events.testEvents().aborted().count() == testAbortedCount
    events.testEvents().succeeded().count() == testSucceededCount

    where
    variables(spec, testSucceededCount, testAbortedCount).from(
      listOf(IgnoreIfInstanceFieldRunsSpec::class.java, IgnoreIfInstanceFieldAbortedSpec::class.java),
      listOf(1L, 0L),
      listOf(0L, 1L)
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#@IgnoreIf can be configured to be inherited")
  fun `@IgnoreIf can be configured to be inherited`(
    specs: List<Class<*>>,
    testStartAndSucceededCount: Long,
    specSkippedCount: Long
  ) {
    `when`
    val events = executeAll(*specs.map { selectClass(it) }.toTypedArray())

    then
    events.testEvents().started().count() == testStartAndSucceededCount
    events.testEvents().failed().count() == 0L
    events.testEvents().skipped().count() == 0L
    events.testEvents().aborted().count() == 0L
    events.testEvents().succeeded().count() == testStartAndSucceededCount
    events.containerEvents().skipped().count() == specSkippedCount

    where
    variables(specs, testStartAndSucceededCount, specSkippedCount).from(
      listOf(
        listOf(
          IgnoreIfInheritanceBaseSpec::class.java,
          IgnoreIfNotInheritedFooSpec::class.java,
          IgnoreIfNotInheritedBarSpec::class.java,
          IgnoreIfNotInheritedTestSpec::class.java
        ),
        listOf(
          IgnoreIfInheritanceBaseSpec::class.java,
          IgnoreIfInheritedFooSpec::class.java,
          IgnoreIfInheritedBarSpec::class.java,
          IgnoreIfInheritedTestSpec::class.java
        )
      ),
      listOf(1L + 0 + 3 + 4, 1L),
      listOf(1L, 3L)
    )
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#fails if condition cannot be instantiated")
  fun `fails if condition cannot be instantiated`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfUninstantiableConditionSpec::class.java))

    then
    val failure = events.failure()
    failure is ExtensionException
    failure.message == "Failed to instantiate condition"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#feature is ignored if at least one IgnoreIf annotation is true")
  @IgnoreIf(IgnoreIfTrue::class)
  @IgnoreIf(IgnoreIfFalse::class)
  fun `feature is ignored if at least one IgnoreIf annotation is true`() {
    expect
    false
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#feature is not ignored if all IgnoreIf annotations are false")
  fun `feature is not ignored if all IgnoreIf annotations are false`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfAllFalseSpec::class.java))

    then
    events.failure() is ConditionNotSatisfiedError
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#feature is ignored if data variable accessing IgnoreIf annotation is true")
  @IgnoreIf(IgnoreIfDataAIsOne::class)
  @IgnoreIf(IgnoreIfFalse::class)
  fun `feature is ignored if data variable accessing IgnoreIf annotation is true`(a: Int) {
    expect
    false

    where
    variable(a).from(1)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#feature is ignored if at least one data variable accessing IgnoreIf annotation is true")
  @IgnoreIf(IgnoreIfDataAIsOne::class)
  @IgnoreIf(IgnoreIfDataAIsNotOne::class)
  fun `feature is ignored if at least one data variable accessing IgnoreIf annotation is true`(a: Int) {
    expect
    false

    where
    variable(a).from(1)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/894")
  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#feature is ignored if non data variable accessing IgnoreIf annotation is true")
  @IgnoreIf(IgnoreIfTrue::class)
  @IgnoreIf(IgnoreIfDataAIsNotOne::class)
  fun `feature is ignored if non data variable accessing IgnoreIf annotation is true`(a: Int) {
    expect
    false

    where
    variable(a).from(1)
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#@IgnoreIf provides condition access to Specification shared fields")
  fun `@IgnoreIf provides condition access to Specification shared fields`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfFeatureSharedFieldSpec::class.java))

    then
    events.testEvents().started().count() == 4L
    events.testEvents().succeeded().count() == 3L
    events.testEvents().aborted().count() == 1L
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/895")
  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#@IgnoreIf provides condition access to Specification instance shared fields")
  fun `@IgnoreIf provides condition access to Specification instance shared fields`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfFeatureInstanceSharedFieldSpec::class.java))

    then
    events.testEvents().started().count() == 4L
    events.testEvents().succeeded().count() == 3L
    events.testEvents().aborted().count() == 1L
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#@IgnoreIf provides condition access to Specification instance fields")
  fun `@IgnoreIf provides condition access to Specification instance fields`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfFeatureInstanceFieldSpec::class.java))

    then
    events.testEvents().started().count() == 4L
    events.testEvents().succeeded().count() == 3L
    events.testEvents().aborted().count() == 1L
  }

  @MigratedFrom("org.spockframework.smoke.extension.IgnoreIfExtension#@IgnoreIf provides condition access to static Specification fields")
  fun `@IgnoreIf provides condition access to static Specification fields`() {
    `when`
    val events = executeAll(selectClass(IgnoreIfFeatureStaticFieldSpec::class.java))

    then
    events.testEvents().started().count() == 1L
    events.testEvents().skipped().count() == 1L
    events.testEvents().succeeded().count() == 1L
    events.testEvents().failed().count() == 0L
  }
}
