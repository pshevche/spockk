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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.CallChainException
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.CallChainSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.CauseLoopSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.ClosureInExpectBlockSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.ClosureInFieldInitializerSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.ClosureInFixtureMethodSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.ClosureInHelperMethodSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.ClosureInSetupBlockSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingBaseSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingCleanupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingCleanupSpecSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingDataProcessorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingDataProviderCreationSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingDerivedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingMultiVariableDataProcessorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingSetupBlockConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingSetupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingSetupSpecSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.KotlinCallChain
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.NestedExplicitConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.SuppressedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.UnsatisfiedExplicitConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.UnsatisfiedImplicitConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.WrongExceptionCauseSpec
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.ExpressionInfoValueRenderer
import org.spockframework.runtime.StackTraceFilter
import org.spockframework.runtime.WrongExceptionThrownError
import org.spockframework.runtime.model.ExpressionInfo
import spock.lang.PendingFeature
import spock.lang.Specification

class StackTraceFiltering : Specification() {

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#unsatisfied implicit condition")
  fun `unsatisfied implicit condition`() {
    `when`
    val e = failureOf(UnsatisfiedImplicitConditionSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf("${UnsatisfiedImplicitConditionSpec::class.java.name}|a feature|30")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#unsatisfied explicit condition")
  fun `unsatisfied explicit condition`() {
    `when`
    val e = failureOf(UnsatisfiedExplicitConditionSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf("${UnsatisfiedExplicitConditionSpec::class.java.name}|a feature|37")
  }

  // Upstream also runs this against a Java call chain; Kotlin and Java frames filter identically, so one chain is kept.
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#exception in call chain")
  fun `exception in call chain`() {
    `when`
    val e = failureOf(CallChainSpec::class.java)

    then
    e is CallChainException
    e.frames() == listOf(
      "${KotlinCallChain::class.java.name}|c|62",
      "${KotlinCallChain.StaticInner::class.java.name}|staticInner|72",
      "${KotlinCallChain.Inner::class.java.name}|inner|66",
      "${KotlinCallChain::class.java.name}|b|59",
      "${KotlinCallChain::class.java.name}|a|55",
      "${CallChainSpec::class.java.name}|a feature|44"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/804")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#exception in closure in feature method")
  fun `exception in closure in feature method`() {
    `when`
    val inSetupBlock = failureOf(ClosureInSetupBlockSpec::class.java)
    val inExpectBlock = failureOf(ClosureInExpectBlockSpec::class.java)

    then
    inSetupBlock is ConditionNotSatisfiedError
    inSetupBlock.frames() == listOf(
      "${ClosureInSetupBlockSpec::class.java.name}|a feature\$lambda\$0|80",
      "${ClosureInSetupBlockSpec::class.java.name}|a feature|81"
    )
    inExpectBlock is ConditionNotSatisfiedError
    inExpectBlock.frames() == listOf(
      "${ClosureInExpectBlockSpec::class.java.name}|a feature\$lambda\$0|91",
      "${ClosureInExpectBlockSpec::class.java.name}|a feature|91"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#exception in closure in helper method")
  fun `exception in closure in helper method`() {
    `when`
    val e = failureOf(ClosureInHelperMethodSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf(
      "${ClosureInHelperMethodSpec::class.java.name}|helper\$lambda\$0|102",
      "${ClosureInHelperMethodSpec::class.java.name}|helper|103",
      "${ClosureInHelperMethodSpec::class.java.name}|a feature|98"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#exception in closure in fixture method")
  fun `exception in closure in fixture method`() {
    `when`
    val e = failureOf(ClosureInFixtureMethodSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf(
      "${ClosureInFixtureMethodSpec::class.java.name}|setup\$lambda\$0|109",
      "${ClosureInFixtureMethodSpec::class.java.name}|setup|110"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/804")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#exception in closure in field initializer")
  fun `exception in closure in field initializer`() {
    `when`
    val e = failureOf(ClosureInFieldInitializerSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf(
      "${ClosureInFieldInitializerSpec::class.java.name}|\$spock_initializeFields\$lambda\$0|120",
      "${ClosureInFieldInitializerSpec::class.java.name}|\$spock_initializeFields|120"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#stack trace is truncated below invocation of fixture method")
  fun `stack trace is truncated below invocation of fixture method`() {
    `when`
    val inSetup = failureOf(FailingSetupSpec::class.java)
    val inCleanup = failureOf(FailingCleanupSpec::class.java)
    val inSetupSpec = failureOf(FailingSetupSpecSpec::class.java)
    val inCleanupSpec = failureOf(FailingCleanupSpecSpec::class.java)

    then
    inSetup is ConditionNotSatisfiedError
    inSetup.frames() == listOf("${FailingSetupSpec::class.java.name}|setup|130")
    inCleanup is ConditionNotSatisfiedError
    inCleanup.frames() == listOf("${FailingCleanupSpec::class.java.name}|cleanup|141")
    inSetupSpec is ConditionNotSatisfiedError
    inSetupSpec.frames() == listOf("${FailingSetupSpecSpec::class.java.name}|setupSpec|152")
    inCleanupSpec is ConditionNotSatisfiedError
    inCleanupSpec.frames() == listOf("${FailingCleanupSpecSpec::class.java.name}|cleanupSpec|163")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#feature method names are also restored for base specs")
  fun `feature method names are also restored for base specs`() {
    `when`
    val e = failureOf(FailingDerivedSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf("${FailingBaseSpec::class.java.name}|let me fail|175")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#when creation of data provider fails, stack trace points to corresponding parameterization")
  fun `when creation of data provider fails, stack trace points to corresponding parameterization`() {
    `when`
    val e = failureOf(FailingDataProviderCreationSpec::class.java)

    then
    e is RuntimeException
    e.frames() == listOf(
      "${FailingDataProviderCreationSpec.Companion::class.java.name}|failingProvider|193",
      "${FailingDataProviderCreationSpec::class.java.name}|foo|188"
    )
  }

  // Upstream's third row (`y = 1`) has no Kotlin equivalent: where-block variables cannot be assigned.
  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/805")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#when data processor fails, stack trace source position points to corresponding parameterization")
  fun `when data processor fails, stack trace source position points to corresponding parameterization`() {
    `when`
    val single = failureOf(FailingDataProcessorSpec::class.java)
    val multi = failureOf(FailingMultiVariableDataProcessorSpec::class.java)

    then
    single is ClassCastException
    single.frames() == listOf("${FailingDataProcessorSpec::class.java.name}|foo|204")
    multi is ClassCastException
    multi.frames() == listOf("${FailingMultiVariableDataProcessorSpec::class.java.name}|foo|220")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#stack trace for explicit condition in setup block has line number")
  fun `stack trace for explicit condition in setup block has line number`() {
    `when`
    val e = failureOf(FailingSetupBlockConditionSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.stackTrace[0].lineNumber == 226
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/326")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#stack trace for nested explicit condition has line number")
  fun `stack trace for nested explicit condition has line number`() {
    `when`
    val e = failureOf(NestedExplicitConditionSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.stackTrace[0].lineNumber == 238
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#causes get filtered as well")
  fun `causes get filtered as well`() {
    `when`
    val e = failureOf(WrongExceptionCauseSpec::class.java)

    then
    e is WrongExceptionThrownError
    e.frames() == listOf("${WrongExceptionCauseSpec::class.java.name}|a feature|251")
    e.cause!!.frames() == listOf("${WrongExceptionCauseSpec::class.java.name}|a feature|248")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#stack traces are filtered on rendering expression info value")
  fun `stack traces are filtered on rendering expression info value`() {
    given
    val expressionInfo = ExpressionInfo(null, null, null, emptyList<ExpressionInfo>())
    val exception = Exception()
    exception.stackTrace = filterableStackTrace()
    expressionInfo.setValue(exception)

    `when`
    renderValue(expressionInfo)

    then
    exception.frames() == listOf("org.spockframework.runtime.foo.Baz|bar|0")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#expected stack frames are filtered out")
  fun `expected stack frames are filtered out`() {
    given
    val e = Exception()
    e.stackTrace = filterableStackTrace()

    `when`
    StackTraceFilter(specificationContext.currentSpec).filter(e)

    then
    e.frames() == listOf("org.spockframework.runtime.foo.Baz|bar|0")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#exception cause loops do not cause StackOverflowError")
  fun `exception cause loops do not cause StackOverflowError`() {
    `when`
    val e = failureOf(CauseLoopSpec::class.java)

    then
    e is WrongExceptionThrownError
    e.cause!!.frames() == listOf("${CauseLoopSpec::class.java.name}|a feature|258")
    e.cause!!.cause.frames() == listOf("${CauseLoopSpec::class.java.name}|a feature|259")
    e.cause!!.cause!!.cause.frames() == listOf("${CauseLoopSpec::class.java.name}|a feature|258")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#suppressed get filtered as well")
  fun `suppressed get filtered as well`() {
    `when`
    val e = failureOf(SuppressedSpec::class.java)

    then
    e is WrongExceptionThrownError
    e.cause!!.frames() == listOf("${SuppressedSpec::class.java.name}|a feature|271")
    e.cause!!.suppressed.first().frames() == listOf("${SuppressedSpec::class.java.name}|a feature|272")
  }

  private fun failureOf(spec: Class<*>): Throwable =
    EngineTestKit.engine("spock").selectors(selectClass(spec)).execute().allEvents().failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()

  private fun renderValue(expressionInfo: ExpressionInfo) {
    val method = ExpressionInfoValueRenderer::class.java.getDeclaredMethod("renderValue", ExpressionInfo::class.java)
    method.isAccessible = true
    method.invoke(null, expressionInfo)
  }

  private fun Throwable?.frames(): List<String> =
    this!!.stackTrace.map { "${it.className}|${it.methodName}|${it.lineNumber}" }

  private fun filterableStackTrace() = arrayOf(
    StackTraceElement("org.codehaus.groovy.runtime.Foo", "bar", null, 0),
    StackTraceElement("org.codehaus.groovy.reflection.Foo", "bar", null, 0),
    StackTraceElement("org.codehaus.groovy.FooMetaClassBaz", "bar", null, 0),
    StackTraceElement("groovy.FooMetaClassBaz", "bar", null, 0),
    StackTraceElement("groovy.lang.MetaMethod", "bar", null, 0),
    StackTraceElement("java.lang.reflect.Foo", "bar", null, 0),
    StackTraceElement("java.lang.invoke.LambdaForm", "invoke", null, 0),
    StackTraceElement("sun.reflect.Foo", "bar", null, 0),
    StackTraceElement("jdk.internal.reflect.Foo", "bar", null, 0),
    StackTraceElement("org.spockframework.runtime.Foo", "bar", null, 0),
    StackTraceElement("org.spockframework.runtime.foo.Baz", "bar", null, 0)
  )
}
