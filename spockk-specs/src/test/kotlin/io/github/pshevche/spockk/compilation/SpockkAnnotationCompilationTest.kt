/*
 * Copyright 2025 the original author or authors.
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

package io.github.pshevche.spockk.compilation

import io.github.pshevche.spockk.compilation.TestDataFactory.specWithSingleFeature
import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.where

class SpockkAnnotationCompilationTest : BaseCompilationTest() {

  fun `keeps classes without spockk labels untransformed`() {
    expect
    assertTransformation(sampleFromResource("NonSpec"))
  }

  fun `annotates classes with feature methods with @SpecMetadata`() {
    expect
    assertTransformation(sampleFromResource("SingleFeatureSpec"))
  }

  fun `annotates features with spockk labels with @FeatureMetadata`() {
    expect
    assertTransformation(specWithSingleFeature("expect"))
  }

  fun `annotates #scenario spec classes`(scenario: String, sampleName: String) {
    expect
    assertTransformation(sampleFromResource(sampleName))

    where
    // spotless:off
    scenario; sampleName
    "abstract"; "AbstractBaseSpec"
    "open"; "OpenBaseSpec"
    // spotless:on
  }

  fun `annotates child classes with @SpecMetadata if parent contains features`() {
    expect
    assertTransformation(sampleFromResource("SpecWithOnlyInheritedFeatures"))
  }

  fun `captures blocks and their descriptions`() {
    expect
    assertTransformation(sampleFromResource("FeatureWithMultipleBlocksAndDescriptions"))
  }
}
