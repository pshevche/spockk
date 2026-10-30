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
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf
import spock.lang.Retry
import spock.lang.Specification

class AlwaysPending(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: PreconditionContext<*>): Boolean = true
}

class PendingBeforeRetryFeatureSpec : Specification() {
  @PendingFeature
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun feature() {
    expect
    false
  }
}

class RetryBeforePendingFeatureSpec : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  @PendingFeature
  fun feature() {
    expect
    false
  }
}

class PendingIfBeforeRetryFeatureSpec : Specification() {
  @PendingFeatureIf(AlwaysPending::class)
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun feature() {
    expect
    false
  }
}

class RetryBeforePendingIfFeatureSpec : Specification() {
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  @PendingFeatureIf(AlwaysPending::class)
  fun feature() {
    expect
    false
  }
}

class PendingRetryIterationSpec : Specification() {
  @PendingFeature
  @Retry(mode = Retry.Mode.ITERATION)
  fun feature() {
    expect
    false
  }
}

class PendingRetryFeatureParameterizedSpec : Specification() {
  @PendingFeature
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  fun feature(i: Int) {
    expect
    if (i != 0) throw Exception()

    where
    variable(i).from(listOf(1, 2))
  }
}

class PendingRetryIterationParameterizedSpec : Specification() {
  @PendingFeature
  @Retry(mode = Retry.Mode.ITERATION)
  fun feature(i: Int) {
    expect
    if (i != 0) throw Exception()

    where
    variable(i).from(listOf(1, 2))
  }
}

class PendingIfRetryIterationParameterizedSpec : Specification() {
  @PendingFeatureIf(AlwaysPending::class)
  @Retry(mode = Retry.Mode.ITERATION)
  fun feature(i: Int) {
    expect
    if (i != 0) throw Exception()

    where
    variable(i).from(listOf(1, 2))
  }
}
