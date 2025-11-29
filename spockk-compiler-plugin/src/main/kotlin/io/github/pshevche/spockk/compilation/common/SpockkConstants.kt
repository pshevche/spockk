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

package io.github.pshevche.spockk.compilation.common

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object SpockkConstants {
  val SPOCK_RUNTIME_PKG = FqName("org.spockframework.runtime")
  val SPECIFICATION_FQN = FqName("spock.lang.Specification")
  val SPEC_INTERNALS_CLASS_ID = ClassId(SPOCK_RUNTIME_PKG, Name.identifier("SpecInternals"))
  val KCLASS_JAVA_PROPERTY_ID = CallableId(FqName("kotlin.jvm"), Name.identifier("java"))
}
