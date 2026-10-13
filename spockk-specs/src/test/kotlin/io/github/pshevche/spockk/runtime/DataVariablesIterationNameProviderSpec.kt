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
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import org.spockframework.runtime.DataVariablesIterationNameProvider
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import spock.lang.Specification

class DataVariablesIterationNameProviderSpec : Specification() {

  private var testee = DataVariablesIterationNameProvider()

  private val featureInfo = FeatureInfo().apply {
    name = "the feature"
    isReportIterations = true
  }

  @MigratedFrom(
    "org.spockframework.runtime.DataVariablesIterationNameProviderSpec#returns feature name when not reporting iterations"
  )
  fun `returns feature name when not reporting iterations`() {
    given
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
    }
    featureInfo.isReportIterations = false

    expect
    testee.getName(iteration) == featureInfo.name
  }

  @MigratedFrom(
    "org.spockframework.runtime.DataVariablesIterationNameProviderSpec#returns feature name with data variables and iteration index when reporting iterations"
  )
  fun `returns feature name with data variables and iteration index when reporting iterations`() {
    given
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
      getDataVariables() returns linkedMapOf<String, Any?>("x" to 1, "y" to 2, "z" to 3)
    }

    expect
    testee.getName(iteration) == "the feature [x: 1, y: 2, z: 3, #99]"
  }

  @MigratedFrom("org.spockframework.runtime.DataVariablesIterationNameProviderSpec#renders data variables in Groovy style")
  fun `renders data variables in Groovy style`() {
    given
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
      getDataVariables() returns
        linkedMapOf<String, Any?>("x" to listOf(1), "y" to linkedMapOf("a" to 2), "z" to intArrayOf(3))
    }

    expect
    testee.getName(iteration) == "the feature [x: [1], y: [a:2], z: [3], #99]"
  }

  @MigratedFrom(
    "org.spockframework.runtime.DataVariablesIterationNameProviderSpec#returns feature name with iteration index when reporting iterations but data variables are null"
  )
  fun `returns feature name with iteration index when reporting iterations but data variables are null`() {
    given
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
      getDataVariables() returns null
    }

    expect
    testee.getName(iteration) == "the feature [#99]"
  }

  @MigratedFrom(
    "org.spockframework.runtime.DataVariablesIterationNameProviderSpec#handles errors during toString rendering gracefully"
  )
  fun `handles errors during toString rendering gracefully`() {
    given
    val erroneousObject =
      object {
        override fun toString(): String = throw RuntimeException("Don't look at me like that")
      }
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
      getDataVariables() returns linkedMapOf<String, Any?>("x" to erroneousObject, "y" to 2, "z" to 3)
    }

    expect
    testee.getName(iteration) == "the feature [x: #Error:RuntimeException during rendering, y: 2, z: 3, #99]"
  }

  @MigratedFrom(
    "org.spockframework.runtime.DataVariablesIterationNameProviderSpec#returns data variables and iteration index when reporting iterations and includeFeatureNameForIterations=false"
  )
  fun `returns data variables and iteration index when reporting iterations and includeFeatureNameForIterations=false`() {
    given
    testee = DataVariablesIterationNameProvider(false)
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
      getDataVariables() returns linkedMapOf<String, Any?>("x" to 1, "y" to 2, "z" to 3)
    }

    expect
    testee.getName(iteration) == "x: 1, y: 2, z: 3, #99"
  }

  @MigratedFrom(
    "org.spockframework.runtime.DataVariablesIterationNameProviderSpec#returns data variables only when reporting iterations and includeFeatureNameForIterations=false, includeIterationIndex=false"
  )
  fun `returns data variables only when reporting iterations and includeFeatureNameForIterations=false, includeIterationIndex=false`() {
    given
    testee = DataVariablesIterationNameProvider(false, false)
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
      getDataVariables() returns linkedMapOf<String, Any?>("x" to 1, "y" to 2, "z" to 3)
    }

    expect
    testee.getName(iteration) == "x: 1, y: 2, z: 3"
  }

  @MigratedFrom(
    "org.spockframework.runtime.DataVariablesIterationNameProviderSpec#renders data variables in Groovy style and includeFeatureNameForIterations=false"
  )
  fun `renders data variables in Groovy style and includeFeatureNameForIterations=false`() {
    given
    testee = DataVariablesIterationNameProvider(false)
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
      getDataVariables() returns
        linkedMapOf<String, Any?>("x" to listOf(1), "y" to linkedMapOf("a" to 2), "z" to intArrayOf(3))
    }

    expect
    testee.getName(iteration) == "x: [1], y: [a:2], z: [3], #99"
  }

  @MigratedFrom(
    "org.spockframework.runtime.DataVariablesIterationNameProviderSpec#returns iteration index when reporting iterations but data variables are null and includeFeatureNameForIterations=false"
  )
  fun `returns iteration index when reporting iterations but data variables are null and includeFeatureNameForIterations=false`() {
    given
    testee = DataVariablesIterationNameProvider(false)
    val iteration = Stub(IterationInfo::class.java) {
      getFeature() returns featureInfo
      getIterationIndex() returns 99
      getDataVariables() returns null
    }

    expect
    testee.getName(iteration) == "#99"
  }
}
