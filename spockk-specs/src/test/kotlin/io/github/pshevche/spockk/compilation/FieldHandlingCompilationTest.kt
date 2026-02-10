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

import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.lang.expect
import spock.lang.PendingFeature

class FieldHandlingCompilationTest : BaseCompilationTest() {

  @PendingFeature
  fun `transforms instance var field`() {
    expect
    assertTransformation(sampleFromResource("fields/InstanceVarField"))
  }

  @PendingFeature
  fun `transforms instance val field`() {
    expect
    assertTransformation(sampleFromResource("fields/InstanceValField"))
  }

  @PendingFeature
  fun `transforms instance lateinit var field`() {
    expect
    assertTransformation(sampleFromResource("fields/InstanceLateinitVarField"))
  }

  @PendingFeature
  fun `transforms shared var field and replaces references`() {
    expect
    assertTransformation(sampleFromResource("fields/SharedVarField"))
  }

  @PendingFeature
  fun `transforms shared val field and replaces references`() {
    expect
    assertTransformation(sampleFromResource("fields/SharedValField"))
  }

  @PendingFeature
  fun `transforms shared lateinit var field and replaces references`() {
    expect
    assertTransformation(sampleFromResource("fields/SharedLateinitVarField"))
  }

  fun `ignores static field`() {
    expect
    assertTransformation(sampleFromResource("fields/StaticField"))
  }

  @PendingFeature
  fun `transforms multiple fields preserving declaration order`() {
    expect
    assertTransformation(sampleFromResource("fields/MultipleFields"))
  }
}
