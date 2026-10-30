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

package io.github.pshevche.spockk.smoke.condition

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.DeepNestingSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.ExceptionWhileInvokingConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NestedAssertionErrorSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullPointerInConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullPointerInMethodConditionSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.condition.SpockExceptionInConditionSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockException
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class ExceptionsInConditions : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/857")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#null pointer exception in condition")
  fun `null pointer exception in condition`() {
    `when`
    val e = failure(NullPointerInConditionSpec::class) as ConditionNotSatisfiedError

    then
    unifyExecutionLineInStacktrace(e.condition.rendering) ==
      """
      |map.get("key")!!.length == 0
      ||   |            |
      |[:] null         java.lang.NullPointerException
      |                 ${"\t"}at io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullPointerInConditionSpec.a feature(${NORMALIZED_EXECUTION_LINE})
      |
      """.trimMargin()
    e.cause is NullPointerException
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/857")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#null pointer exception in method condition")
  fun `null pointer exception in method condition`() {
    `when`
    val e = failure(NullPointerInMethodConditionSpec::class) as ConditionNotSatisfiedError

    then
    unifyExecutionLineInStacktrace(e.condition.rendering) ==
      """
      |map.get("key")!!.isEmpty()
      ||   |            |
      |[:] null         java.lang.NullPointerException
      |                 ${"\t"}at io.github.pshevche.spockk.fixtures.runtime.samples.condition.NullPointerInMethodConditionSpec.a feature(${NORMALIZED_EXECUTION_LINE})
      |
      """.trimMargin()
    e.cause is NullPointerException
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/857")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#exception while invoking condition")
  fun `exception while invoking condition`() {
    `when`
    val e = failure(ExceptionWhileInvokingConditionSpec::class) as ConditionNotSatisfiedError

    then
    unifyExecutionLineInStacktrace(e.condition.rendering) ==
      """
      |getKey(map, "key").length == 0
      ||      |
      ||      [:]
      |java.lang.IllegalArgumentException: key does not exists
      |${"\t"}at io.github.pshevche.spockk.fixtures.runtime.samples.condition.ExceptionWhileInvokingConditionSpec.a feature${'$'}lambda${'$'}0(${NORMALIZED_EXECUTION_LINE})
      |${"\t"}at io.github.pshevche.spockk.fixtures.runtime.samples.condition.ExceptionWhileInvokingConditionSpec.a feature(${NORMALIZED_EXECUTION_LINE})
      |
      """.trimMargin()
    e.cause is IllegalArgumentException
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/804")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#spock assertion errors should be processed as is")
  fun `spock assertion errors should be processed as is`() {
    `when`
    val e = failure(NestedAssertionErrorSpec::class) as ConditionNotSatisfiedError

    then
    e.condition.rendering ==
      """
      |result != null
      ||      |
      |null   false
      |
      """.trimMargin()
    e.cause == null
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#spock exceptions should be processed as is")
  fun `spock exceptions should be processed as is`() {
    `when`
    val e = failure(SpockExceptionInConditionSpec::class) as SpockException

    then
    e.message == "key does not exists"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/857")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#deep nesting")
  fun `deep nesting`() {
    `when`
    val e = failure(DeepNestingSpec::class) as ConditionNotSatisfiedError

    then
    unifyExecutionLineInStacktrace(e.message!!) ==
      """
      |Condition failed with Exception:
      |
      |map.get("key")!!.b.c.d.e()
      ||   |            |
      |[:] null         java.lang.NullPointerException
      |                 ${"\t"}at io.github.pshevche.spockk.fixtures.runtime.samples.condition.DeepNestingSpec.a feature(${NORMALIZED_EXECUTION_LINE})
      |
      """.trimMargin()
  }

  private fun failure(clazz: KClass<*>): Throwable =
    execute(selectClass(clazz.java)).failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()

  private fun unifyExecutionLineInStacktrace(stacktrace: String): String =
    stacktrace.replace(EXECUTION_LINE_PATTERN, NORMALIZED_EXECUTION_LINE)

  companion object {
    private val EXECUTION_LINE_PATTERN = Regex("""ExceptionsInConditionsSpecs\.kt:\d+""")
    private const val NORMALIZED_EXECUTION_LINE = "ExceptionsInConditionsSpecs.kt:X"
  }
}
