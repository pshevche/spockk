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

package io.github.pshevche.spockk.compilation

import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly

object TestDataFactory {
  fun specWithBody(feature: String): SourceFile =
    kotlin(
      "Spec.kt",
      """
            class Spec : spock.lang.Specification() {
                $feature
            }
            """
        .trimIndent()
    )

  fun specWithFeatureBody(featureBody: String): SourceFile =
    specWithBody(
      """
      fun `some feature`() {
          $featureBody
      }
    """
        .trimIndent()
    )

  fun specWithSingleFeature(label: String) =
    TransformationSample(
      specWithFeatureBody(
        """
        io.github.pshevche.spockk.lang.$label
        assert(true)
      """
          .trimIndent()
      ),
      kotlin(
        "Spec.kt",
        $$"""
                @org.spockframework.runtime.model.SpecMetadata(filename = "Spec.kt", line = 1)
                class Spec : spock.lang.Specification() {
                    @org.spockframework.runtime.model.FeatureMetadata(
                        ordinal = 0,
                        name = "some feature",
                        line = 2,
                        parameterNames = [],
                        blocks = [org.spockframework.runtime.model.BlockMetadata(
                            org.spockframework.runtime.model.BlockKind.$${label.toUpperCaseAsciiOnly()},
                            [""]
                        )]
                    )
                    fun `$spock_feature_0_0`() {
                        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
                        assert(true)
                        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
                    }
                }
            """
      )
    )
}
