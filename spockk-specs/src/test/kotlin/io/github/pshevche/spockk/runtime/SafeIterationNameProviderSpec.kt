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

package io.github.pshevche.spockk.runtime

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.does
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.SafeIterationNameProvider
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.NameProvider
import spock.lang.Specification

class SafeIterationNameProviderSpec : Specification() {

  private val featureInfo = FeatureInfo().apply {
    name = "feature"
    isReportIterations = false
  }
  private val nameProviderType = NameProvider::class.java as Class<NameProvider<IterationInfo>>
  private val other: NameProvider<IterationInfo> = Mock(nameProviderType)
  private var provider = SafeIterationNameProvider(other)

  @MigratedFrom("org.spockframework.runtime.SafeIterationNameProviderSpec#delegates to other provider")
  fun `delegates to other provider`() {
    given
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
    }

    `when`
    provider.getName(iteration)

    then
    1 * other.getName(iteration)
  }

  @MigratedFrom("org.spockframework.runtime.SafeIterationNameProviderSpec#returns default if there is no other provider")
  fun `returns default if there is no other provider`() {
    given
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
    }
    provider = SafeIterationNameProvider(null)

    expect
    provider.getName(iteration) == "feature"
  }

  @MigratedFrom("org.spockframework.runtime.SafeIterationNameProviderSpec#returns default if other provider returns nothing")
  fun `returns default if other provider returns nothing`() {
    given
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
    }
    val other = Mock(nameProviderType) {
      getName(iteration) returns null
    }
    provider = SafeIterationNameProvider(other)

    expect
    provider.getName(iteration) == "feature"
  }

  @MigratedFrom("org.spockframework.runtime.SafeIterationNameProviderSpec#returns default if other provider blows up")
  fun `returns default if other provider blows up`() {
    given
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
    }
    val other = Mock(nameProviderType) {
      getName(iteration) does { throw RuntimeException() }
    }
    provider = SafeIterationNameProvider(other)

    expect
    provider.getName(iteration) == "feature"
  }
}
