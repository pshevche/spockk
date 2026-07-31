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

package io.github.pshevche.spockk.intellij

import org.jetbrains.kotlin.psi.KtNamedFunction
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkImplicitUsageProviderTest : BaseSpockkIntelliJPluginTestCase() {

  private val provider = SpockkImplicitUsageProvider()

  @BeforeEach
  private fun setUp() {
    myFixture.addSpecificationStub()
    myFixture.configureFromDefaultFile()
  }

  @Test
  fun testReportsFeatureAndFixtureMethods() {
    assertImplicitUsage("fun `a feature`")
    assertImplicitUsage("fun setup()")
    assertImplicitUsage("fun cleanup()")
    assertImplicitUsage("fun setupSpec()")
    assertImplicitUsage("fun cleanupSpec()")
    assertNotImplicitUsage("fun helperMethod()")
  }

  @Test
  fun testIgnoresMethodsOutsideSpecs() {
    assertNotImplicitUsage("fun topLevelFun()")
    assertNotImplicitUsage("fun regularMethod()")
  }

  @Test
  fun testReportsMethodsInInheritedSpec() {
    myFixture.configureByFiles(
      "testReportsMethodsInInheritedSpec.kt",
      "InheritedBaseSpec.kt"
    )
    assertImplicitUsage("fun `derived feature`")
    assertImplicitUsage("fun setup()")
  }

  @Test
  fun testIgnoresMethodsInNestedNonSpecClass() {
    assertImplicitUsage("fun `real feature`")
    assertNotImplicitUsage("fun setup()")
    assertNotImplicitUsage("fun `looks like a feature`")
  }

  private fun assertImplicitUsage(functionText: String) =
    assertTrue(isImplicitUsage(functionText))

  private fun assertNotImplicitUsage(functionText: String) =
    assertFalse(isImplicitUsage(functionText))

  private fun isImplicitUsage(functionText: String): Boolean =
    runInReadAction { provider.isImplicitUsage(findRequiredElementByTextAndType(functionText, KtNamedFunction::class.java)) }
}
