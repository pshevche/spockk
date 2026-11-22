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

import groovy.lang.Closure
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import org.opentest4j.TestAbortedException
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Retry
import spock.lang.Specification

class AlwaysTrue(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Boolean = true
}

class AlwaysFalse(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Boolean = false
}

/** Mirrors upstream's `data.get("i") && false`: reads the data variable, then yields false. */
class DataVariableAndFalse(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Boolean = context.data["i"] != null && false
}

class RetryIterationAbortedSpec : Specification() {
  @Retry(mode = Retry.Mode.ITERATION)
  fun feature() {
    expect
    throw TestAbortedException()
  }
}

class RetryFeatureAbortedSpec : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun feature() {
    expect
    throw TestAbortedException()
  }
}

class RetryIterationParameterizedAbortedSpec : Specification() {
  @Retry(mode = Retry.Mode.ITERATION)
  fun feature(i: Int) {
    expect
    if (i > 0) throw TestAbortedException(i.toString())

    where
    variable(i).from(listOf(1, 2))
  }
}

class RetryFeatureParameterizedAbortedSpec : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun feature(i: Int) {
    expect
    if (i > 0) throw TestAbortedException(i.toString())

    where
    variable(i).from(listOf(1, 2))
  }
}

class IgnoreIfThenRetrySpec : Specification() {
  @IgnoreIf(AlwaysTrue::class)
  @Retry
  fun feature() {
    expect
    false
  }
}

class RetryThenIgnoreIfSpec : Specification() {
  @Retry
  @IgnoreIf(AlwaysTrue::class)
  fun feature() {
    expect
    false
  }
}

class IgnoreIfThenRetryParameterizedSpec : Specification() {
  @IgnoreIf(AlwaysTrue::class)
  @Retry
  fun feature(i: Int) {
    expect
    false

    where
    variable(i).from(listOf(1, 2))
  }
}

class RetryThenIgnoreIfParameterizedSpec : Specification() {
  @Retry
  @IgnoreIf(AlwaysTrue::class)
  fun feature(i: Int) {
    expect
    false

    where
    variable(i).from(listOf(1, 2))
  }
}

class RequiresFalseThenRetrySpec : Specification() {
  @Requires(AlwaysFalse::class)
  @Retry
  fun feature() {
    expect
    false
  }
}

class RequiresDataIterationRetrySpec : Specification() {
  @Requires(DataVariableAndFalse::class)
  @Retry(mode = Retry.Mode.ITERATION)
  fun feature(i: Int) {
    expect
    false

    where
    variable(i).from(listOf(1, 2))
  }
}

class RequiresDataFeatureRetrySpec : Specification() {
  @Requires(DataVariableAndFalse::class)
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun feature(i: Int) {
    expect
    false

    where
    variable(i).from(listOf(1, 2))
  }
}
