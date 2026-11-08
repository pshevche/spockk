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

package io.github.pshevche.spockk.smoke.extension

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfThenRetryParameterizedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.IgnoreIfThenRetrySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RequiresDataFeatureRetrySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RequiresDataIterationRetrySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RequiresFalseThenRetrySpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryFeatureAbortedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryFeatureParameterizedAbortedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryIterationAbortedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryIterationParameterizedAbortedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryThenIgnoreIfParameterizedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryThenIgnoreIfSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class RetryTestAbortedExceptionInteropSpec : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry iteration shall be skipped if a TestAbortedException is thrown")
  fun `Retry iteration shall be skipped if a TestAbortedException is thrown`() {
    `when`
    val events = execute(selectClass(RetryIterationAbortedSpec::class.java))

    then
    events.assertStatistics { it.started(1).skipped(0).aborted(1).succeeded(0).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry feature shall be skipped if a TestAbortedException is thrown")
  fun `Retry feature shall be skipped if a TestAbortedException is thrown`() {
    `when`
    val events = execute(selectClass(RetryFeatureAbortedSpec::class.java))

    then
    events.assertStatistics { it.started(1).skipped(0).aborted(1).succeeded(0).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry iteration parameterized shall be skipped if a TestAbortedException is thrown")
  fun `Retry iteration parameterized shall be skipped if a TestAbortedException is thrown`() {
    `when`
    val events = execute(selectClass(RetryIterationParameterizedAbortedSpec::class.java))

    then
    events.assertStatistics { it.started(3).skipped(0).aborted(2).succeeded(1).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry feature parameterized shall be skipped if a TestAbortedException is thrown")
  fun `Retry feature parameterized shall be skipped if a TestAbortedException is thrown`() {
    `when`
    val events = execute(selectClass(RetryFeatureParameterizedAbortedSpec::class.java))

    then
    events.assertStatistics { it.started(3).skipped(0).aborted(2).succeeded(1).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry shall be skipped when feature is ignored")
  fun `Retry shall be skipped when feature is ignored`() {
    `when`
    val events = execute(selectClass(IgnoreIfThenRetrySpec::class.java))

    then
    events.assertStatistics { it.started(0).skipped(1).aborted(0).succeeded(0).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry inverse shall be skipped when feature is ignored")
  fun `Retry inverse shall be skipped when feature is ignored`() {
    `when`
    val events = execute(selectClass(RetryThenIgnoreIfSpec::class.java))

    then
    events.assertStatistics { it.started(0).skipped(1).aborted(0).succeeded(0).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry parameterized shall be skipped when feature is ignored")
  fun `Retry parameterized shall be skipped when feature is ignored`() {
    `when`
    val events = execute(selectClass(IgnoreIfThenRetryParameterizedSpec::class.java))

    then
    events.assertStatistics { it.started(0).skipped(1).aborted(0).succeeded(0).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry inverse parameterized shall be skipped when feature is ignored")
  fun `Retry inverse parameterized shall be skipped when feature is ignored`() {
    `when`
    val events = execute(selectClass(RetryThenIgnoreIfParameterizedSpec::class.java))

    then
    events.assertStatistics { it.started(0).skipped(1).aborted(0).succeeded(0).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry shall be skipped  when using Requires")
  fun `Retry shall be skipped  when using Requires`() {
    `when`
    val events = execute(selectClass(RequiresFalseThenRetrySpec::class.java))

    then
    events.assertStatistics { it.started(0).skipped(1).aborted(0).succeeded(0).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry iteration parameterized shall be skipped when using Requires")
  fun `Retry iteration parameterized shall be skipped when using Requires`() {
    `when`
    val events = execute(selectClass(RequiresDataIterationRetrySpec::class.java))

    then
    events.assertStatistics { it.started(3).skipped(0).aborted(2).succeeded(1).failed(0) }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryTestAbortedExceptionInteropSpec#Retry feature parameterized shall be skipped when using Requires")
  fun `Retry feature parameterized shall be skipped when using Requires`() {
    `when`
    val events = execute(selectClass(RequiresDataFeatureRetrySpec::class.java))

    then
    events.assertStatistics { it.started(3).skipped(0).aborted(2).succeeded(1).failed(0) }
  }
}
