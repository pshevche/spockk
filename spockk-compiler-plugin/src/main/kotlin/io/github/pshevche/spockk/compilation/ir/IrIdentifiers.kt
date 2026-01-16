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
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object IrIdentifiers {

  private fun String.asName() = Name.identifier(this)

  internal object Spock {
    // FqName
    val RUNTIME_PKG_FQN = FqName("org.spockframework.runtime")
    val SPECIFICATION_FQN = FqName("spock.lang.Specification")
    val WILDCARD_FQN = SPECIFICATION_FQN.child("_".asName())

    // ClassId
    val SPEC_INTERNALS_CLASS_ID = ClassId(RUNTIME_PKG_FQN, "SpecInternals".asName())
  }

  internal object Spockk {
    // FqName
    val LANG_PKG_FQN = FqName("io.github.pshevche.spockk.lang")
    val SINGLE_VARIABLE_INIT_FQN = LANG_PKG_FQN.child("variable".asName())
    val MULTI_VARIABLE_INIT_FQN = LANG_PKG_FQN.child("variables".asName())

    // Misc
    val FROM_FQN_REGEX = "io\\.github\\.pshevche\\.spockk\\.lang\\.DataVariable.+\\.from".toRegex()
  }

  internal object Kotlin {
    // FqName
    val LIST_FQN = COLLECTIONS_PACKAGE_FQ_NAME.child("List".asName())

    // CallableId
    val KCLASS_JAVA_CALLABLE_ID = CallableId(BUILT_INS_PACKAGE_FQ_NAME.child("jvm".asName()), Name.identifier("java"))
    val LIST_OF_CALLABLE_ID = CallableId(COLLECTIONS_PACKAGE_FQ_NAME, "listOf".asName())
    val ARRAY_OF_CALLABLE_ID = CallableId(BUILT_INS_PACKAGE_FQ_NAME, Name.identifier("arrayOf"))
  }
}
