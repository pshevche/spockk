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
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingBeforeRetryFeatureSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingIfBeforeRetryFeatureSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingIfRetryIterationParameterizedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingRetryFeatureParameterizedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingRetryIterationParameterizedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.PendingRetryIterationSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryBeforePendingFeatureSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.RetryBeforePendingIfFeatureSpec
import io.github.pshevche.spockk.lang.expect
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.Specification

class RetryPendingFeatureInteropSpec : Specification() {

  @MigratedFrom("org.spockframework.smoke.extension.RetryPendingFeatureInteropSpec#Retry feature shall be skipped when using PendingFeature")
  fun `Retry feature shall be skipped when using PendingFeature`() {
    expect
    execute(selectClass(PendingBeforeRetryFeatureSpec::class.java)).assertStatistics {
      it.started(1).skipped(0).aborted(1).succeeded(0).failed(0)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryPendingFeatureInteropSpec#Retry feature inverse annotation order shall be skipped when using PendingFeature")
  fun `Retry feature inverse annotation order shall be skipped when using PendingFeature`() {
    expect
    execute(selectClass(RetryBeforePendingFeatureSpec::class.java)).assertStatistics {
      it.started(1).skipped(0).aborted(1).succeeded(0).failed(0)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryPendingFeatureInteropSpec#Retry feature shall be skipped when using PendingFeatureIf")
  fun `Retry feature shall be skipped when using PendingFeatureIf`() {
    expect
    execute(selectClass(PendingIfBeforeRetryFeatureSpec::class.java)).assertStatistics {
      it.started(1).skipped(0).aborted(1).succeeded(0).failed(0)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryPendingFeatureInteropSpec#Retry feature inverse annotation order shall be skipped when using PendingFeatureIf")
  fun `Retry feature inverse annotation order shall be skipped when using PendingFeatureIf`() {
    expect
    execute(selectClass(RetryBeforePendingIfFeatureSpec::class.java)).assertStatistics {
      it.started(1).skipped(0).aborted(1).succeeded(0).failed(0)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryPendingFeatureInteropSpec#Retry iteration shall be skipped when using PendingFeature")
  fun `Retry iteration shall be skipped when using PendingFeature`() {
    expect
    execute(selectClass(PendingRetryIterationSpec::class.java)).assertStatistics {
      it.started(1).skipped(0).aborted(1).succeeded(0).failed(0)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryPendingFeatureInteropSpec#Retry feature parameterized shall be skipped when using PendingFeature")
  fun `Retry feature parameterized shall be skipped when using PendingFeature`() {
    expect
    execute(selectClass(PendingRetryFeatureParameterizedSpec::class.java)).assertStatistics {
      it.started(3).skipped(0).aborted(2).succeeded(1).failed(0)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryPendingFeatureInteropSpec#Retry iteration parameterized shall be skipped when using PendingFeature")
  fun `Retry iteration parameterized shall be skipped when using PendingFeature`() {
    expect
    execute(selectClass(PendingRetryIterationParameterizedSpec::class.java)).assertStatistics {
      it.started(3).skipped(0).aborted(2).succeeded(1).failed(0)
    }
  }

  @MigratedFrom("org.spockframework.smoke.extension.RetryPendingFeatureInteropSpec#Retry iteration parameterized shall be skipped when using PendingFeatureIf")
  fun `Retry iteration parameterized shall be skipped when using PendingFeatureIf`() {
    expect
    execute(selectClass(PendingIfRetryIterationParameterizedSpec::class.java)).assertStatistics {
      it.started(3).skipped(0).aborted(2).succeeded(1).failed(0)
    }
  }
}
