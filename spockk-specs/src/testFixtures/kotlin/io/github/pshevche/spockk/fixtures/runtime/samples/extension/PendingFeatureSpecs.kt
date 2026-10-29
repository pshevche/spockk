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

package io.github.pshevche.spockk.fixtures.runtime.samples.extension

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.opentest4j.TestAbortedException
import spock.lang.PendingFeature
import spock.lang.Rollup
import spock.lang.Specification

class PendingFeatureFailing : Specification() {
  @PendingFeature
  fun bar() {
    expect
    assert(false)
  }
}

class PendingFeatureWithReason : Specification() {
  @PendingFeature(reason = "42")
  fun bar() {
    expect
    assert(false)
  }
}

class PendingFeatureThrowing : Specification() {
  @PendingFeature
  fun bar() {
    expect
    throw Exception()
  }
}

class PendingFeatureUnhandledException : Specification() {
  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  fun bar() {
    expect
    throw IllegalArgumentException()
  }
}

class PendingFeaturePassing : Specification() {
  @PendingFeature
  fun bar() {
    expect
    assert(true)
  }
}

class PendingFeatureAborted : Specification() {
  @PendingFeature
  fun bar() {
    expect
    throw TestAbortedException("ignored because I can")
  }
}

class PendingFeatureAllIterationsFail : Specification() {
  @PendingFeature
  @Rollup
  fun rolled(test: Boolean) {
    expect
    test

    where
    variable(test).from(false, false, false)
  }

  @PendingFeature
  fun unrolled(test: Boolean) {
    expect
    test

    where
    variable(test).from(false, false, false)
  }
}

class PendingFeatureOneIterationFails : Specification() {
  @PendingFeature
  @Rollup
  fun rolled(test: Boolean) {
    expect
    test

    where
    variable(test).from(true, false, true)
  }

  @PendingFeature
  fun unrolled(test: Boolean) {
    expect
    test

    where
    variable(test).from(true, false, true)
  }
}

class PendingFeatureAllIterationsPass : Specification() {
  @PendingFeature
  @Rollup
  fun rolled(test: Boolean) {
    expect
    test

    where
    variable(test).from(true, true, true)
  }

  @PendingFeature
  fun unrolled(test: Boolean) {
    expect
    test

    where
    variable(test).from(true, true, true)
  }
}

class PendingFeatureUnhandledInIterations : Specification() {
  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  @Rollup
  fun rolled(test: Boolean) {
    expect
    throw IllegalArgumentException("test=$test")

    where
    variable(test).from(false, false, false)
  }

  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  fun unrolled(test: Boolean) {
    expect
    throw IllegalArgumentException("test=$test")

    where
    variable(test).from(false, false, false)
  }
}

class PendingFeatureUnhandledInSomeIterations : Specification() {
  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  @Rollup
  fun rolled(test: Boolean) {
    expect
    throwIf(test, IllegalArgumentException())
    throw IndexOutOfBoundsException()

    where
    variable(test).from(false, true, false)
  }

  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  fun unrolled(test: Boolean) {
    expect
    throwIf(test, IllegalArgumentException())
    throw IndexOutOfBoundsException()

    where
    variable(test).from(false, true, false)
  }
}

class PendingFeatureUnhandledWithPassingIterations : Specification() {
  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  @Rollup
  fun rolled(test: Boolean) {
    expect
    throwIf(!test, IllegalArgumentException())
    test

    where
    variable(test).from(false, true, false)
  }

  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  fun unrolled(test: Boolean) {
    expect
    throwIf(!test, IllegalArgumentException())
    test

    where
    variable(test).from(false, true, false)
  }
}

class PendingFeatureAllIterationsAborted : Specification() {
  @PendingFeature
  @Rollup
  fun rolled(test: Boolean) {
    expect
    throw TestAbortedException("ignored because I can")

    where
    variable(test).from(true, true, true)
  }

  @PendingFeature
  fun unrolled(test: Boolean) {
    expect
    throw TestAbortedException("ignored because I can")

    where
    variable(test).from(true, true, true)
  }
}

class PendingFeatureAbortedAndPassingIterations : Specification() {
  @PendingFeature
  @Rollup
  fun rolled(test: Boolean) {
    expect
    abortIf(!test)
    test

    where
    variable(test).from(true, false, true)
  }

  @PendingFeature
  fun unrolled(test: Boolean) {
    expect
    abortIf(!test)
    test

    where
    variable(test).from(true, false, true)
  }
}

class PendingFeatureAbortedAndFailingIterations : Specification() {
  @PendingFeature
  @Rollup
  fun rolled(test: Boolean) {
    expect
    abortIf(test)
    test

    where
    variable(test).from(true, false, true)
  }

  @PendingFeature
  fun unrolled(test: Boolean) {
    expect
    abortIf(test)
    test

    where
    variable(test).from(true, false, true)
  }
}

class PendingFeatureUnhandledWithAbortedIterations : Specification() {
  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  @Rollup
  fun rolled(test: Boolean) {
    expect
    throwIf(test, IllegalArgumentException())
    throw TestAbortedException("ignored because I can")

    where
    variable(test).from(false, true, false)
  }

  @PendingFeature(exceptions = [IndexOutOfBoundsException::class])
  fun unrolled(test: Boolean) {
    expect
    throwIf(test, IllegalArgumentException())
    throw TestAbortedException("ignored because I can")

    where
    variable(test).from(false, true, false)
  }
}

private fun abortIf(condition: Boolean) {
  if (condition) throw TestAbortedException("ignored because I can")
}

private fun throwIf(condition: Boolean, exception: Exception) {
  if (condition) throw exception
}
