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
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.where

class ImplicitConditionsCompilationTest : BaseCompilationTest() {

  fun `basic usage`() {
    expect
    assertTransformation(sampleFromResource("condition/BasicImplicitCondition"))
  }

  fun `method call condition`() {
    expect
    assertTransformation(sampleFromResource("condition/ImplicitMethodCallCondition"))
  }

  fun `negation condition`() {
    expect
    assertTransformation(sampleFromResource("condition/ImplicitNegationCondition"))
  }

  fun `verify call`() {
    expect
    assertTransformation(sampleFromResource("condition/VerifyBasic"))
  }

  fun `verifyAll call`() {
    expect
    assertTransformation(sampleFromResource("condition/VerifyAllBasic"))
  }

  fun `verifyEach call`() {
    expect
    assertTransformation(sampleFromResource("condition/VerifyEachBasic"))
  }

  fun `nested verify inside verifyAll`() {
    expect
    assertTransformation(sampleFromResource("condition/NestedVerifyInVerifyAll"))
  }

  fun `verify call inside a helper method`() {
    expect
    assertTransformation(sampleFromResource("condition/VerifyInHelperMethod"))
  }

  fun `verify call with a non-literal lambda is left unrewritten`() {
    expect
    assertTransformation(sampleFromResource("condition/VerifyWithNonLiteralLambda"))
  }

  fun `verify call inside a top-level extension function is left unrewritten`() {
    expect
    assertTransformation(sampleFromResource("condition/VerifyInExtensionFunction"))
  }

  @MigratedFrom("org.spockframework.smoke.ast.condition.ConditionMethodsAstSpec#condition method #conditionMethod")
  fun `condition method #conditionMethod`(conditionMethod: String, sampleName: String) {
    expect
    assertTransformation(sampleFromResource("condition/ConditionMethod$sampleName"))

    where
    conditionMethod ; sampleName
    "verify"        ; "Verify"
    "verifyAll"     ; "VerifyAll"
    "verifyEach"    ; "VerifyEach"
  }

  @MigratedFrom("org.spockframework.smoke.ast.condition.ConditionMethodsAstSpec#condition method #conditionMethod with exception")
  fun `condition method #conditionMethod with exception`(conditionMethod: String, sampleName: String) {
    expect
    assertTransformation(sampleFromResource("condition/ConditionMethod${sampleName}WithException"))

    where
    conditionMethod ; sampleName
    "verify"        ; "Verify"
    "verifyAll"     ; "VerifyAll"
    "verifyEach"    ; "VerifyEach"
  }

  @MigratedFrom("org.spockframework.smoke.ast.condition.ConditionMethodsAstSpec#condition method #conditionMethod with only exception")
  fun `condition method #conditionMethod with only exception`(conditionMethod: String, sampleName: String) {
    expect
    assertTransformation(sampleFromResource("condition/ConditionMethod${sampleName}WithOnlyException"))

    where
    conditionMethod ; sampleName
    "verify"        ; "Verify"
    "verifyAll"     ; "VerifyAll"
    "verifyEach"    ; "VerifyEach"
  }

  @MigratedFrom("org.spockframework.smoke.ast.condition.ConditionMethodsAstSpec#condition method #conditionMethod within condition method #conditionMethod")
  fun `condition method #conditionMethod within condition method #conditionMethod`(conditionMethod: String, sampleName: String) {
    expect
    assertTransformation(sampleFromResource("condition/ConditionMethod${sampleName}InsideItself"))

    where
    conditionMethod ; sampleName
    "verify"        ; "Verify"
    "verifyAll"     ; "VerifyAll"
    "verifyEach"    ; "VerifyEach"
  }

  @MigratedFrom("org.spockframework.smoke.ast.condition.ConditionMethodsAstSpec#condition method #conditionMethod within condition method #conditionMethod with exception")
  fun `condition method #conditionMethod within condition method #conditionMethod with exception`(conditionMethod: String, sampleName: String) {
    expect
    assertTransformation(sampleFromResource("condition/ConditionMethod${sampleName}InsideItselfWithException"))

    where
    conditionMethod ; sampleName
    "verify"        ; "Verify"
    "verifyAll"     ; "VerifyAll"
    "verifyEach"    ; "VerifyEach"
  }

  @MigratedFrom("org.spockframework.smoke.ast.condition.ConditionMethodsAstSpec#condition method #conditionMethod within condition method #conditionMethod with only exception")
  fun `condition method #conditionMethod within condition method #conditionMethod with only exception`(conditionMethod: String, sampleName: String) {
    expect
    assertTransformation(sampleFromResource("condition/ConditionMethod${sampleName}InsideItselfWithOnlyException"))

    where
    conditionMethod ; sampleName
    "verify"        ; "Verify"
    "verifyAll"     ; "VerifyAll"
    "verifyEach"    ; "VerifyEach"
  }
}
