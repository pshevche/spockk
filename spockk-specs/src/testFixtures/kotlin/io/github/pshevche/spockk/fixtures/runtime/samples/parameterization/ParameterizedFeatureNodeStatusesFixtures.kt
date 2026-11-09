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

class SkippedIfAllUprolledAreSkippedFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    throw TestAbortedException()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class SkippedIfThereIsOneUprolledIterationAndItIsSkippedFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    throw TestAbortedException()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1)
  }
}

class SuccessfulIfSomeUprolledAreSuccessfulAndSomeSkippedFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) throw TestAbortedException()

    expect
    a == 2

    where
    variable(a).from(1, 2)
  }
}

class FailingIfSomeUprolledAreFailingAndSomeSkippedFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) throw TestAbortedException()

    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class FailingIfThereIsOneUprolledIterationAndItIsFailingFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    expect
    a == 3

    where
    variable(a).from(1)
  }
}

class ErrorIfSomeUprolledAreErroringAndSomeSkippedFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) throw TestAbortedException()
    throw Error()

    expect
    true

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUprolledAreSuccessfulFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class FailingIfSomeUprolledAreFailingAndSomeSuccessfulFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    expect
    a == 1

    where
    variable(a).from(1, 2)
  }
}

class ErrorIfSomeUprolledAreErroringAndSomeSuccessfulFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) throw Error()

    expect
    a == 2

    where
    variable(a).from(1, 2)
  }
}

class FailingIfAllUprolledAreFailingFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class FailingIfSomeUprolledAreErroringAndSomeFailingFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    if (a == 1) throw Error()

    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class FailingIfAllUprolledAreErroringFixture : Specification() {
  @Rollup
  fun `a feature`(a: Int) {
    throw Error()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUnrolledAreSkippedFixture : Specification() {
  fun `a feature`(a: Int) {
    throw TestAbortedException()

    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreSuccessfulAndSomeSkippedFixture : Specification() {
  fun `a feature`(a: Int) {
    if (a == 1) throw TestAbortedException()

    expect
    a == 2

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreFailingAndSomeSkippedFixture : Specification() {
  fun `a feature`(a: Int) {
    if (a == 1) throw TestAbortedException()

    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreErroringAndSomeSkippedFixture : Specification() {
  fun `a feature`(a: Int) {
    if (a == 1) throw TestAbortedException()
    throw Error()

    expect
    true

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUnrolledAreSuccessfulFixture : Specification() {
  fun `a feature`(a: Int) {
    expect
    a in listOf(1, 2)

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreFailingAndSomeSuccessfulFixture : Specification() {
  fun `a feature`(a: Int) {
    expect
    a == 1

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreErroringAndSomeSuccessfulFixture : Specification() {
  fun `a feature`(a: Int) {
    if (a == 1) throw Error()

    expect
    a == 2

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUnrolledAreFailingFixture : Specification() {
  fun `a feature`(a: Int) {
    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfSomeUnrolledAreErroringAndSomeFailingFixture : Specification() {
  fun `a feature`(a: Int) {
    if (a == 1) throw Error()

    expect
    a == 3

    where
    variable(a).from(1, 2)
  }
}

class SuccessfulIfAllUnrolledAreErroringFixture : Specification() {
  fun `a feature`(a: Int) {
    throw Error()

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
