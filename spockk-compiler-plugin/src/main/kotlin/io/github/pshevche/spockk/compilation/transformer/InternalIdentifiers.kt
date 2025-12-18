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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.common.SpockkTransformationContext.FeatureContext
import org.jetbrains.kotlin.name.Name

internal object InternalIdentifiers {

  // function names in Kotlin cannot start with $
  fun getFeatureName(context: FeatureContext): String =
    "spock_feature_${context.specDepth}_${context.ordinal}"

  fun getDataProviderName(featureContext: FeatureContext, providerIndex: Int): Name =
    Name.identifier("${getFeatureName(featureContext)}prov$providerIndex")

  fun getDataProcessorName(featureContext: FeatureContext): Name =
    Name.identifier("${getFeatureName(featureContext)}proc")
}
