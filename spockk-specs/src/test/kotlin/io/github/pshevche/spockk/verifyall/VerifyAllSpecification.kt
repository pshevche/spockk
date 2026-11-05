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

package io.github.pshevche.spockk.verifyall

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.verifyall.Person
import io.github.pshevche.spockk.fixtures.runtime.samples.verifyall.VerifyAllExplicitFailuresSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.verifyall.VerifyAllImplicitFailuresSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.verifyall.VerifyAllNestedFailuresSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.verifyall.VerifyAllTargetFailuresSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.verifyall.VerifyAllUnrelatedExceptionSpec
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.verifyAll
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.opentest4j.MultipleFailuresError
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class VerifyAllSpecification : Specification() {

  private fun failureOf(spec: KClass<*>): Throwable {
    val events = execute(selectClass(spec.java))
    events.assertStatistics { it.started(1).failed(1) }
    return events.failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
  }

  private fun comparisons(error: Throwable): List<Pair<String, String>> {
    val multi = error as MultipleFailuresError
    return multi.failures.map {
      val c = it as SpockComparisonFailure
      c.expected.stringRepresentation.trim() to c.actual.stringRepresentation.trim()
    }
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll")
  fun verifyAll() {
    expect
    comparisons(failureOf(VerifyAllImplicitFailuresSpec::class)) == listOf("2" to "1", "4" to "3")
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll works with only explicit conditions")
  fun `verifyAll works with only explicit conditions`() {
    expect
    comparisons(failureOf(VerifyAllExplicitFailuresSpec::class)) == listOf("2" to "1", "4" to "3")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1070")
  @MigratedFrom(
    "org.spockframework.verifyall.VerifyAllSpecification#if exception is not in condition, all already failed conditions should be reported"
  )
  fun `if exception is not in condition, all already failed conditions should be reported`() {
    expect
    comparisons(failureOf(VerifyAllUnrelatedExceptionSpec::class)) == listOf("3" to "2", "4" to "3")
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll with target and failures")
  fun `verifyAll with target and failures`() {
    expect
    comparisons(failureOf(VerifyAllTargetFailuresSpec::class)) == listOf("Bob" to "Fred", "137" to "42")
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1069")
  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll with nested verifyAll")
  fun `verifyAll with nested verifyAll`() {
    expect
    comparisons(failureOf(VerifyAllNestedFailuresSpec::class)) == listOf("2" to "1", "4" to "3", "5" to "4")
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll with target")
  fun `verifyAll with target`() {
    given
    val p = Person()

    expect
    verifyAll(p) {
      name == "Fred"
      age == 42
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/886")
  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll works with void methods")
  fun `verifyAll works with void methods`() {
    expect
    checkCondition()
    verifyAll {
      checkCondition()
      verifyAll {
        checkCondition()
        verifyAll {
          checkCondition()
        }
      }
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/886")
  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll works with void methods of delegates")
  fun `verifyAll works with void methods of delegates`() {
    given
    val person = Person()

    expect
    person.check()
    verifyAll(person) {
      check()
      verifyAll {
        check()
        verifyAll {
          check()
        }
      }
    }
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#method condition is invoked on closure but not on the spec")
  fun `method condition is invoked on closure but not on the spec`() {
    val map = mapOf("value1" to 1, "value2" to 2)

    expect
    verifyAll(map) {
      size == 2
      containsKey("value2")
    }
  }

  @MigratedFrom(
    "org.spockframework.verifyall.VerifyAllSpecification#nested method conditions are invoked on closure but not on the spec"
  )
  fun `nested method conditions are invoked on closure but not on the spec`() {
    val map = mapOf("value1" to 1, "value2" to 2)

    expect
    verifyAll(map) {
      containsKey("value2")

      val list = listOf(1, 2, 3)
      verifyAll(list) {
        contains(2)
      }
    }
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#a closure encloses a with clause that has a method condition")
  fun `a closure encloses a with clause that has a method condition`() {
    val list = listOf(1, 2, 3)

    expect
    (1..3).forEach { number ->
      verifyAll(list) {
        contains(number)
      }
    }
  }

  @MigratedFrom(
    "org.spockframework.verifyall.VerifyAllSpecification#spec has methods with the same signature as the with target object"
  )
  fun `spec has methods with the same signature as the with target object`() {
    val list = listOf(1, 2, 3)

    expect
    size() == 42
    contains(4)
    verifyAll(list) {
      size == 3
      contains(3)
      this@VerifyAllSpecification.contains(4)
    }
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#don't turn nested with expressions into condition")
  fun `don't turn nested with expressions into condition`() {
    val list = listOf(listOf(listOf("start", "end")))

    expect
    verify(list) {
      verify(this[0]) {
        verifyAll(this[0]) {
          verifyAll(this) {
            this[0] == "start"
            this[1] == "end"
          }
          size == 2
        }
      }
    }
  }

  private fun size() = 42

  private fun contains(any: Any?) = any == 4

  private fun checkCondition() {
    assert(true)
  }
}
