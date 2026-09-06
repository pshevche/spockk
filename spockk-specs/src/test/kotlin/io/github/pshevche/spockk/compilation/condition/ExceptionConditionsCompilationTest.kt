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

package io.github.pshevche.spockk.compilation.condition

import io.github.pshevche.spockk.compilation.BaseCompilationTest
import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.lang.expect
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * Covers the `when`-block wrapping mechanism ([io.github.pshevche.spockk.compilation.transformer.condition.WhenBlockRewriter])
 * with exact-IR-shape snapshots. `thrown(Type)`'s own call-site rewrite
 * ([io.github.pshevche.spockk.compilation.transformer.condition.ExceptionConditionRewriter]) is
 * deliberately not snapshot-tested here: the rewritten cast target type carries a Java
 * `@FlexibleNullability` platform-type marker (from `Specification.thrown`'s generic Java
 * signature) that ordinary hand-written Kotlin source cannot reproduce byte-for-byte, so its
 * correctness is covered by [io.github.pshevche.spockk.smoke.condition.ExceptionConditionsSmokeTest]
 * (runtime pass/fail behavior) instead.
 */
@OptIn(ExperimentalCompilerApi::class)
class ExceptionConditionsCompilationTest : BaseCompilationTest() {

  fun `when block wrapped for a paired notThrown call`() {
    expect
    assertTransformation(sampleFromResource("condition/NotThrown"))
  }

  fun `when block wrapped for a paired noExceptionThrown call`() {
    expect
    assertTransformation(sampleFromResource("condition/NoExceptionThrown"))
  }

  fun `when block is left unwrapped without a paired exception condition`() {
    expect
    assertTransformation(sampleFromResource("condition/NoExceptionConditionNoWrapping"))
  }
}
