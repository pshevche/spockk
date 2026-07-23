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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SpockkGradleConfigurationProducerTest : BaseSpockkIntelliJPluginTestCase() {

  @Test
  fun testClassProducerIsApplicable() {
    val producer = SpockkTestClassGradleConfigurationProducer()
    assertTrue(producer.isApplicable(myFixture.module))
  }

  @Test
  fun testClassProducerHasTestFramework() {
    val producer = SpockkTestClassGradleConfigurationProducer()
    assertTrue(producer.hasTestFramework)
  }

  @Test
  fun testClassProducerForceGradleRunner() {
    val producer = SpockkTestClassGradleConfigurationProducer()
    assertFalse(producer.forceGradleRunner)
  }

  @Test
  fun testMethodProducerIsApplicable() {
    val producer = SpockkTestMethodGradleConfigurationProducer()
    assertTrue(producer.isApplicable(myFixture.module))
  }

  @Test
  fun testMethodProducerHasTestFramework() {
    val producer = SpockkTestMethodGradleConfigurationProducer()
    assertTrue(producer.hasTestFramework)
  }

  @Test
  fun testMethodProducerForceGradleRunner() {
    val producer = SpockkTestMethodGradleConfigurationProducer()
    assertFalse(producer.forceGradleRunner)
  }
}
