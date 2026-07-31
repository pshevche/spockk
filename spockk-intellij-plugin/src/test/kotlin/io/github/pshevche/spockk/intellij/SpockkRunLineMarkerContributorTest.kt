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

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkRunLineMarkerContributorTest : BaseSpockkIntelliJPluginTestCase() {

  private val contributor = SpockkRunLineMarkerContributor()

  @BeforeEach
  private fun setUp() {
    myFixture.addSpecificationStub()
  }

  @Test
  fun testMarksFeatureNotSpecClass() {
    myFixture.configureFromDefaultFile()
    assertNotNull(slowMarkerFor("fun `direct feature`"))
    // The spec-class gutter icon is left to the built-in Spock plugin, so this contributor does not
    // mark the class (which would produce a duplicate icon).
    assertNull(slowMarkerFor("class DirectSpec"))
    // Detection resolves the hierarchy, so it is deferred to the slow pass; the fast pass stays empty.
    assertNull(fastMarkerFor("fun `direct feature`"))
  }

  @Test
  fun testMarksInheritedFeature() {
    myFixture.configureByFiles(
      "testMarksInheritedFeature.kt",
      "InheritedBaseSpec.kt"
    )
    assertNotNull(slowMarkerFor("fun `derived feature`"), "expected a run marker on the feature of an inherited spec")
    assertNull(slowMarkerFor("class DerivedSpec"))
  }

  @Test
  fun testDoesNotMarkNonSpec() {
    myFixture.configureFromDefaultFile()
    assertNull(slowMarkerFor("class NotASpec"))
    assertNull(slowMarkerFor("fun regularMethod()"))
  }

  private fun fastMarkerFor(declarationText: String): RunLineMarkerContributor.Info? =
    markerFor(declarationText) { contributor.getInfo(it) }

  private fun slowMarkerFor(declarationText: String): RunLineMarkerContributor.Info? =
    markerFor(declarationText) { contributor.getSlowInfo(it) }

  private fun markerFor(
    declarationText: String,
    info: (PsiElement) -> RunLineMarkerContributor.Info?
  ): RunLineMarkerContributor.Info? =
    runInReadAction {
      val nameIdentifier =
        findRequiredElementByTextAndType(declarationText, KtNamedDeclaration::class.java).nameIdentifier!!
      info(nameIdentifier)
    }
}
