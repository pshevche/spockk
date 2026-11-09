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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.opentest4j.MultipleFailuresError
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.PendingFeature
import spock.lang.Specification
import kotlin.reflect.KClass

class VerifyAllSpecification : Specification() {

  private fun failuresOf(spec: KClass<*>): List<Throwable> =
    execute(selectClass(spec.java)).failed().list()
      .map { it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow() }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll")
  fun verifyAll() {
    `when`
    val thrown = failuresOf(VerifyAllImplicitFailuresSpec::class)

    then
    thrown.size == 1
    verify(thrown[0] as MultipleFailuresError) {
      failures.size == 2
      verify(failures[0] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "2"
        actual.stringRepresentation.trim() == "1"
      }
      verify(failures[1] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "4"
        actual.stringRepresentation.trim() == "3"
      }
    }
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll works with only explicit conditions")
  fun `verifyAll works with only explicit conditions`() {
    `when`
    val thrown = failuresOf(VerifyAllExplicitFailuresSpec::class)

    then
    thrown.size == 1
    verify(thrown[0] as MultipleFailuresError) {
      failures.size == 2
      verify(failures[0] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "2"
        actual.stringRepresentation.trim() == "1"
      }
      verify(failures[1] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "4"
        actual.stringRepresentation.trim() == "3"
      }
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1070")
  @MigratedFrom(
    "org.spockframework.verifyall.VerifyAllSpecification#if exception is not in condition, all already failed conditions should be reported"
  )
  fun `if exception is not in condition, all already failed conditions should be reported`() {
    `when`
    val thrown = failuresOf(VerifyAllUnrelatedExceptionSpec::class)

    then
    thrown.size == 1
    verify(thrown[0] as MultipleFailuresError) {
      failures.size == 2
      verify(failures[0] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "3"
        actual.stringRepresentation.trim() == "2"
      }
      verify(failures[1] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "4"
        actual.stringRepresentation.trim() == "3"
      }
    }
  }

  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll with target and failures")
  fun `verifyAll with target and failures`() {
    `when`
    val thrown = failuresOf(VerifyAllTargetFailuresSpec::class)

    then
    thrown.size == 1
    verify(thrown[0] as MultipleFailuresError) {
      failures.size == 2
      verify(failures[0] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "Bob"
        actual.stringRepresentation.trim() == "Fred"
      }
      verify(failures[1] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "137"
        actual.stringRepresentation.trim() == "42"
      }
    }
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1069")
  @MigratedFrom("org.spockframework.verifyall.VerifyAllSpecification#verifyAll with nested verifyAll")
  fun `verifyAll with nested verifyAll`() {
    `when`
    val thrown = failuresOf(VerifyAllNestedFailuresSpec::class)

    then
    thrown.size == 1
    verify(thrown[0] as MultipleFailuresError) {
      failures.size == 3
      verify(failures[0] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "2"
        actual.stringRepresentation.trim() == "1"
      }
      verify(failures[1] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "4"
        actual.stringRepresentation.trim() == "3"
      }
      verify(failures[2] as SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "5"
        actual.stringRepresentation.trim() == "4"
      }
    }
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
