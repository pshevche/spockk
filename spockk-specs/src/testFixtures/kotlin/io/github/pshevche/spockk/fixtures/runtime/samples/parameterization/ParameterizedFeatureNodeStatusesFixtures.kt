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

package io.github.pshevche.spockk.fixtures.runtime.samples.parameterization

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.opentest4j.TestAbortedException
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import spock.lang.Rollup
import spock.lang.Specification

abstract class AbortingSpecification : Specification() {
  protected fun abort(): Unit = throw TestAbortedException()

  protected fun raiseError(): Unit = throw Error()
}

class SkippedIfAllUprolledAreSkippedFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    abort()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class SkippedIfThereIsOneUprolledIterationAndItIsSkippedFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    abort()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1)
  }
}

class SuccessfulIfSomeUprolledAreSuccessfulAndSomeSkippedFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) abort()

    expect
    a == 2

    where
    variable(a).from(1, 2)
  }
}

class FailingIfSomeUprolledAreFailingAndSomeSkippedFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) abort()

    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class FailingIfThereIsOneUprolledIterationAndItIsFailingFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    expect
    a == 3

    where
    variable(a).from(1)
  }
}

class ErrorIfSomeUprolledAreErroringAndSomeSkippedFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) abort()
    raiseError()

    expect
    true

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUprolledAreSuccessfulFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class FailingIfSomeUprolledAreFailingAndSomeSuccessfulFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    expect
    a == 1

    where
    variable(a).from(1, 2)
  }
}

class ErrorIfSomeUprolledAreErroringAndSomeSuccessfulFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) raiseError()

    expect
    a == 2

    where
    variable(a).from(1, 2)
  }
}

class FailingIfAllUprolledAreFailingFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class FailingIfSomeUprolledAreErroringAndSomeFailingFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) raiseError()

    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class FailingIfAllUprolledAreErroringFixture : AbortingSpecification() {
  @Rollup
  fun `a feature`(a: Int) {
    raiseError()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUnrolledAreSkippedFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    abort()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreSuccessfulAndSomeSkippedFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    if (a == 1) abort()

    expect
    a == 2

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreFailingAndSomeSkippedFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    if (a == 1) abort()

    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreErroringAndSomeSkippedFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    if (a == 1) abort()
    raiseError()

    expect
    true

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUnrolledAreSuccessfulFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreFailingAndSomeSuccessfulFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    expect
    a == 1

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreErroringAndSomeSuccessfulFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    if (a == 1) raiseError()

    expect
    a == 2

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUnrolledAreFailingFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreErroringAndSomeFailingFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    if (a == 1) raiseError()

    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUnrolledAreErroringFixture : AbortingSpecification() {
  fun `a feature`(a: Int) {
    raiseError()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

/** Iteration interceptor that aborts the iteration with index 2, then lets the others proceed. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@ExtensionAnnotation(StepwiseIterationsExtension::class)
annotation class StepwiseIterations

class StepwiseIterationsExtension : IAnnotationDrivenExtension<StepwiseIterations> {
  override fun visitFeatureAnnotation(annotation: StepwiseIterations, feature: FeatureInfo) {
    feature.addIterationInterceptor(ThrowingInterceptor())
  }

  private class ThrowingInterceptor : IMethodInterceptor {
    override fun intercept(invocation: IMethodInvocation) {
      if (invocation.iteration.iterationIndex == 2) {
        throw TestAbortedException("Test aborted")
      }
      invocation.proceed()
    }
  }
}

class StepwiseIterationsTest : Specification() {
  @StepwiseIterations
  fun `myFeature #count`(count: Int) {
    expect
    1 == 1

    where
    variable(count).from((1..5).toList())
  }
}
