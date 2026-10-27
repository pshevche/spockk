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
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.runtime.ConditionFailedWithExceptionError
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockException
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class ExceptionsInConditions : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/857")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#null pointer exception in condition")
  fun `null pointer exception in condition`() {
    val e = failure(NullPointerInConditionSpec::class)
    expect
    e is ConditionFailedWithExceptionError
    e.cause is NullPointerException
    e.message ==
      """
      |Condition failed with Exception:
      |
      |map["key"]!!.length == 0
      ||
      |null
      |java.lang.NullPointerException
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/857")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#null pointer exception in method condition")
  fun `null pointer exception in method condition`() {
    val e = failure(NullPointerInMethodConditionSpec::class)
    expect
    e is ConditionFailedWithExceptionError
    e.cause is NullPointerException
    e.message ==
      """
      |Condition failed with Exception:
      |
      |map["key"]!!.isEmpty()
      ||
      |null
      |java.lang.NullPointerException
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/857")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#exception while invoking condition")
  fun `exception while invoking condition`() {
    val e = failure(ExceptionWhileInvokingConditionSpec::class)
    expect
    e is ConditionFailedWithExceptionError
    e.cause is IllegalArgumentException
    e.message ==
      """
      |Condition failed with Exception:
      |
      |getKey(map, "key").length == 0
      |       |
      |       [:]
      |java.lang.IllegalArgumentException: key does not exists
      |
      """.trimMargin()
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/804")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#spock assertion errors should be processed as is")
  fun `spock assertion errors should be processed as is`() {
    val e = failure(NestedAssertionErrorSpec::class)
    expect
    e is ConditionNotSatisfiedError
    e.cause == null
    e.message ==
      """
      |Condition not satisfied:
      |
      |result != null
      ||      |
      |null   false
      |
      """.trimMargin()
  }

  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#spock exceptions should be processed as is")
  fun `spock exceptions should be processed as is`() {
    val e = failure(SpockExceptionInConditionSpec::class)
    expect
    e is SpockException
    e.message == "key does not exists"
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/857")
  @MigratedFrom("org.spockframework.smoke.condition.ExceptionsInConditions#deep nesting")
  fun `deep nesting`() {
    val e = failure(DeepNestingSpec::class)
    expect
    e is ConditionFailedWithExceptionError
    e.message ==
      """
      |Condition failed with Exception:
      |
      |map["key"]!!.child!!.child!!.child!!.name.isEmpty()
      ||
      |null
      |java.lang.NullPointerException
      |
      """.trimMargin()
  }

  private fun failure(clazz: KClass<*>): Throwable =
    execute(selectClass(clazz.java)).failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
}
