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
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.CauseLoopSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.ClosureInFieldInitializerSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.ClosureInFixtureMethodSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.ClosureInHelperMethodSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingBaseSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingCleanupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingCleanupSpecSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingDataProcessorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingDataProviderCreationSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingDerivedDataProcessorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingDerivedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingMultiVariableDataProcessorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingSetupBlockConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingSetupSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.FailingSetupSpecSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.JavaCallChain
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.JavaCallChainSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.KotlinCallChain
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.KotlinCallChainSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.NestedExplicitConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.SuppressedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.UnsatisfiedExplicitConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.UnsatisfiedImplicitConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.stacktrace.WrongExceptionCauseSpec
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
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

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#exception in call chain")
  fun `exception in call chain`(spec: Class<*>, chain: Class<*>, specLine: Int) {
    `when`
    val e = failureOf(spec)

    then
    e is CallChainException
    e.frames() == listOf(
      "${chain.name}|c|27",
      "${chain.name}\$StaticInner|staticInner|37",
      "${chain.name}\$Inner|inner|31",
      "${chain.name}|b|24",
      "${chain.name}|a|20",
      "${spec.name}|a feature|$specLine"
    )

    where
    spec ; chain ; specLine
    KotlinCallChainSpec::class.java ; KotlinCallChain::class.java ; 44
    JavaCallChainSpec::class.java ; JavaCallChain::class.java ; 54
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#exception in closure in helper method")
  fun `exception in closure in helper method`() {
    `when`
    val e = failureOf(ClosureInHelperMethodSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf(
      "${ClosureInHelperMethodSpec::class.java.name}|helper\$lambda\$0|70",
      "${ClosureInHelperMethodSpec::class.java.name}|helper|71",
      "${ClosureInHelperMethodSpec::class.java.name}|a feature|66"
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
      "${ClosureInFixtureMethodSpec::class.java.name}|setup\$lambda\$0|77",
      "${ClosureInFixtureMethodSpec::class.java.name}|setup|78"
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
      "${ClosureInFieldInitializerSpec::class.java.name}|\$spock_initializeFields\$lambda\$0|88",
      "${ClosureInFieldInitializerSpec::class.java.name}|\$spock_initializeFields|88"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#stack trace is truncated below invocation of fixture method")
  fun `stack trace is truncated below invocation of fixture method`(spec: Class<*>, fixtureMethod: String, line: Int) {
    `when`
    val e = failureOf(spec)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf("${spec.name}|$fixtureMethod|$line")

    where
    spec ; fixtureMethod ; line
    FailingSetupSpec::class.java ; "setup" ; 98
    FailingCleanupSpec::class.java ; "cleanup" ; 109
    FailingSetupSpecSpec::class.java ; "setupSpec" ; 120
    FailingCleanupSpecSpec::class.java ; "cleanupSpec" ; 131
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#feature method names are also restored for base specs")
  fun `feature method names are also restored for base specs`() {
    `when`
    val e = failureOf(FailingDerivedSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.frames() == listOf("${FailingBaseSpec::class.java.name}|let me fail|143")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#when creation of data provider fails, stack trace points to corresponding parameterization")
  fun `when creation of data provider fails, stack trace points to corresponding parameterization`() {
    `when`
    val e = failureOf(FailingDataProviderCreationSpec::class.java)

    then
    e is RuntimeException
    e.frames() == listOf(
      "${FailingDataProviderCreationSpec.Companion::class.java.name}|failingProvider|161",
      "${FailingDataProviderCreationSpec::class.java.name}|foo|156"
    )
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/805")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#when data processor fails, stack trace source position points to corresponding parameterization")
  fun `when data processor fails, stack trace source position points to corresponding parameterization`(spec: Class<*>, line: Int) {
    `when`
    val e = failureOf(spec)

    then
    e is ClassCastException
    e.frames() == listOf("${spec.name}|foo|$line")

    where
    spec ; line
    FailingDataProcessorSpec::class.java ; 172
    FailingDerivedDataProcessorSpec::class.java ; 192
    FailingMultiVariableDataProcessorSpec::class.java ; 203
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/761")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#stack trace for explicit condition in setup block has line number")
  fun `stack trace for explicit condition in setup block has line number`() {
    `when`
    val e = failureOf(FailingSetupBlockConditionSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.stackTrace[0].lineNumber == 209
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/326")
  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#stack trace for nested explicit condition has line number")
  fun `stack trace for nested explicit condition has line number`() {
    `when`
    val e = failureOf(NestedExplicitConditionSpec::class.java)

    then
    e is ConditionNotSatisfiedError
    e.stackTrace[0].lineNumber == 221
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#causes get filtered as well")
  fun `causes get filtered as well`() {
    `when`
    val e = failureOf(WrongExceptionCauseSpec::class.java)

    then
    e is WrongExceptionThrownError
    e.frames() == listOf("${WrongExceptionCauseSpec::class.java.name}|a feature|234")
    e.cause!!.frames() == listOf("${WrongExceptionCauseSpec::class.java.name}|a feature|231")
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
    e.cause!!.frames() == listOf("${CauseLoopSpec::class.java.name}|a feature|241")
    e.cause!!.cause.frames() == listOf("${CauseLoopSpec::class.java.name}|a feature|242")
    e.cause!!.cause!!.cause.frames() == listOf("${CauseLoopSpec::class.java.name}|a feature|241")
  }

  @MigratedFrom("org.spockframework.smoke.StackTraceFiltering#suppressed get filtered as well")
  fun `suppressed get filtered as well`() {
    `when`
    val e = failureOf(SuppressedSpec::class.java)

    then
    e is WrongExceptionThrownError
    e.cause!!.frames() == listOf("${SuppressedSpec::class.java.name}|a feature|254")
    e.cause!!.suppressed.first().frames() == listOf("${SuppressedSpec::class.java.name}|a feature|255")
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
