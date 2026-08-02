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

package io.github.pshevche.spockk.compilation.transformer

import io.github.pshevche.spockk.compilation.shared.SpockkTransformationContext.FeatureContext
import org.jetbrains.kotlin.name.Name

internal object InternalIdentifiers {

  val INITIALIZE_FIELDS_METHOD: Name = Name.identifier($$"$spock_initializeFields")
  val INITIALIZE_SHARED_FIELDS_METHOD: Name = Name.identifier($$"$spock_initializeSharedFields")
  val FEATURE_THROWABLE_VAR = Name.identifier($$"$spock_feature_throwable")
  val FAILED_BLOCK_VAR = Name.identifier($$"$spock_failedBlock")
  val TMP_THROWABLE_VAR = Name.identifier($$"$spock_tmp_throwable")
  val ERROR_COLLECTOR_VAR = Name.identifier($$"$spock_errorCollector")
  val VALUE_RECORDER_VAR = Name.identifier($$"$spock_valueRecorder")
  val CONDITION_THROWABLE_VAR = Name.identifier($$"$spock_condition_throwable")

  fun getFeatureName(context: FeatureContext): String =
    $$"$spock_feature_$${context.specDepth}_$${context.ordinal}"

  fun getSharedFieldName(originalName: String): Name =
    Name.identifier($$"$spock_sharedField_$${originalName}")

  fun getFinalFieldName(originalName: String): Name =
    Name.identifier($$"$spock_finalField_$${originalName}")

  fun getDataProviderName(featureContext: FeatureContext, providerIndex: Int): Name =
    Name.identifier("${getFeatureName(featureContext)}prov$providerIndex")

  fun getDataProcessorName(featureContext: FeatureContext): Name =
    Name.identifier("${getFeatureName(featureContext)}proc")
}
