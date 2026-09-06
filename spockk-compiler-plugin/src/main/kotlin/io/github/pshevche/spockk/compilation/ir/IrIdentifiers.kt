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

package io.github.pshevche.spockk.compilation.ir

import org.jetbrains.kotlin.builtins.StandardNames.BUILT_INS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.builtins.StandardNames.COLLECTIONS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object IrIdentifiers {

  private fun String.asName() = Name.identifier(this)
  private fun FqName.child(name: String) = child(name.asName())

  internal object Spock {
    // FqName
    private val LANG_PKG_FQN = FqName("spock.lang")
    val SPECIFICATION_FQN = LANG_PKG_FQN.child("Specification")
    val WILDCARD_FQN = SPECIFICATION_FQN.child("_")
    val SHARED_ANNOTATION_FQN = LANG_PKG_FQN.child("Shared")
    val THROWN_FQN = SPECIFICATION_FQN.child("thrown")
    val NOT_THROWN_FQN = SPECIFICATION_FQN.child("notThrown")
    val NO_EXCEPTION_THROWN_FQN = SPECIFICATION_FQN.child("noExceptionThrown")

    private val RUNTIME_PKG_FQN = FqName("org.spockframework.runtime")
    val ERROR_COLLECTOR_FQN = RUNTIME_PKG_FQN.child("ErrorCollector")
    val ERROR_RETHROWER_FQN = RUNTIME_PKG_FQN.child("ErrorRethrower")
    val SPECIFICATION_CONTEXT_FQN = RUNTIME_PKG_FQN.child("SpecificationContext")
    val SPEC_INTERNALS_FQN = RUNTIME_PKG_FQN.child("SpecInternals")
    val SPOCK_RUNTIME_FQN = RUNTIME_PKG_FQN.child("SpockRuntime")
    val VALUE_RECORDER_FQN = RUNTIME_PKG_FQN.child("ValueRecorder")

    private val RUNTIME_MODEL_PKG_FQN = RUNTIME_PKG_FQN.child("model")
    val BLOCK_INFO_FQN = RUNTIME_MODEL_PKG_FQN.child("BlockInfo")
    val BLOCK_KIND_FQN = RUNTIME_MODEL_PKG_FQN.child("BlockKind")
    val BLOCK_METADATA_FQN = RUNTIME_MODEL_PKG_FQN.child("BlockMetadata")
    val DATA_PROVIDER_METADATA_FQN = RUNTIME_MODEL_PKG_FQN.child("DataProviderMetadata")
    val DATA_PROCESSOR_METADATA_FQN = RUNTIME_MODEL_PKG_FQN.child("DataProcessorMetadata")
    val FEATURE_METADATA_FQN = RUNTIME_MODEL_PKG_FQN.child("FeatureMetadata")
    val FIELD_METADATA_FQN = RUNTIME_MODEL_PKG_FQN.child("FieldMetadata")
    val SPEC_METADATA_FQN = RUNTIME_MODEL_PKG_FQN.child("SpecMetadata")

    private val MOCK_PKG_FQN = FqName("org.spockframework.mock")
    private val MOCK_RUNTIME_PKG_FQN = MOCK_PKG_FQN.child("runtime")
    val INTERACTION_BUILDER_FQN = MOCK_RUNTIME_PKG_FQN.child("InteractionBuilder")
    val MOCK_CONTROLLER_FQN = MOCK_RUNTIME_PKG_FQN.child("MockController")

    // Spock's own interaction-matching sentinels (org.spockframework.lang.Wildcard/SpreadWildcard) -
    // distinct from the Spockk-unrelated Specification._ field already namespaced above as
    // WILDCARD_FQN.
    private val SPOCK_LANG_PKG_FQN = FqName("org.spockframework.lang")
    val SPOCK_WILDCARD_FQN = SPOCK_LANG_PKG_FQN.child("Wildcard")
    val SPOCK_SPREAD_WILDCARD_FQN = SPOCK_LANG_PKG_FQN.child("SpreadWildcard")
  }

  internal object Spockk {
    // FqName
    private val LANG_PKG_FQN = FqName("io.github.pshevche.spockk.lang")

    val SETUP_BLOCK_FQN = LANG_PKG_FQN.child("setup")
    val GIVEN_BLOCK_FQN = LANG_PKG_FQN.child("given")
    val WHEN_BLOCK_FQN = LANG_PKG_FQN.child("when")
    val THEN_BLOCK_FQN = LANG_PKG_FQN.child("then")
    val EXPECT_BLOCK_FQN = LANG_PKG_FQN.child("expect")
    val AND_BLOCK_FQN = LANG_PKG_FQN.child("and")
    val WHERE_BLOCK_FQN = LANG_PKG_FQN.child("where")
    val CLEANUP_BLOCK_FQN = LANG_PKG_FQN.child("cleanup")

    val SINGLE_VARIABLE_INIT_FQN = LANG_PKG_FQN.child("variable")
    val MULTI_VARIABLE_INIT_FQN = LANG_PKG_FQN.child("variables")

    val VERIFY_FQN = LANG_PKG_FQN.child("verify")
    val VERIFY_ALL_FQN = LANG_PKG_FQN.child("verifyAll")
    val VERIFY_EACH_FQN = LANG_PKG_FQN.child("verifyEach")

    val ANY_FQN = LANG_PKG_FQN.child("any")
    val ANY_METHOD_FQN = LANG_PKG_FQN.child("anyMethod")
    val TIMES_FQN = LANG_PKG_FQN.child("times")
    val DOES_FQN = LANG_PKG_FQN.child("does")
    val DID_FQN = LANG_PKG_FQN.child("did")
    val RETURNS_FQN = LANG_PKG_FQN.child("returns")
    val RETURNED_FQN = LANG_PKG_FQN.child("returned")
    val NO_MORE_INTERACTIONS_FQN = LANG_PKG_FQN.child("noMoreInteractions")

    // The Spockk-only 2-arg builder-block overloads, distinct from the inherited 1-arg (or Spy's
    // 2-arg-with-real-object) `MockingApi.Mock`/`Stub`/`Spy` members `MockingApiTransformer` already
    // detects by name+declaring-class - these are plain top-level functions, detected by FQN instead.
    val MOCK_BUILDER_BLOCK_FQN = LANG_PKG_FQN.child("Mock")
    val STUB_BUILDER_BLOCK_FQN = LANG_PKG_FQN.child("Stub")
    val SPY_BUILDER_BLOCK_FQN = LANG_PKG_FQN.child("Spy")

    // Misc
    val FROM_FQN_REGEX = "io\\.github\\.pshevche\\.spockk\\.lang\\.DataVariable.+\\.from".toRegex()
  }

  internal object Kotlin {
    // FqName
    val LIST_FQN = COLLECTIONS_PACKAGE_FQ_NAME.child("List")
    val ASSERT_FQN = BUILT_INS_PACKAGE_FQ_NAME.child("assert")
    val BOOLEAN_NOT_FQN = BUILT_INS_PACKAGE_FQ_NAME.child("Boolean").child("not")
    val JAVA_LANG_CLASS_FQN = FqName("java.lang.Class")

    private val JVM_PKG_FQN = BUILT_INS_PACKAGE_FQ_NAME.child("jvm")
    val VOLATILE_FQN = JVM_PKG_FQN.child("Volatile")

    // CallableId
    val KCLASS_JAVA_CALLABLE_ID = CallableId(JVM_PKG_FQN, "java".asName())
    val LIST_OF_CALLABLE_ID = CallableId(COLLECTIONS_PACKAGE_FQ_NAME, "listOf".asName())
    val ARRAY_OF_CALLABLE_ID = CallableId(BUILT_INS_PACKAGE_FQ_NAME, "arrayOf".asName())
    val ADD_SUPPRESSED_CALLABLE_ID = CallableId(BUILT_INS_PACKAGE_FQ_NAME, "addSuppressed".asName())
  }
}
