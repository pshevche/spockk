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

package io.github.pshevche.spockk.compilation.parametrization

import io.github.pshevche.spockk.compilation.BaseCompilationTest
import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.lang.expect
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class DataPipesCompilationTest : BaseCompilationTest() {

  fun `single variable single value`() {
    expect
    assertTransformation(sampleFromResource("parametrization/SingleVariableSingleValueSpec"))
  }

  fun `single variable vararg values`() {
    expect
    assertTransformation(sampleFromResource("parametrization/SingleVariableVarargValuesSpec"))
  }

  fun `single variable values list`() {
    expect
    assertTransformation(sampleFromResource("parametrization/SingleVariableValuesListSpec"))
  }

  fun `single data pipe with multiple variables`() {
    expect
    assertTransformation(sampleFromResource("parametrization/SinglePipeMultiVariableSpec"))
  }

  fun `multiple data pipes`() {
    expect
    assertTransformation(sampleFromResource("parametrization/MultiPipeMultiVariableSpec"))
  }
}
