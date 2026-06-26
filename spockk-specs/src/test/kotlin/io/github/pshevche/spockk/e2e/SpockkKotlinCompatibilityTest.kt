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

package io.github.pshevche.spockk.e2e

import io.github.pshevche.spockk.fixtures.e2e.GradleVersions
import io.github.pshevche.spockk.fixtures.e2e.KotlinVersions
import io.github.pshevche.spockk.fixtures.e2e.Workspace
import io.github.pshevche.spockk.lang.setup
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import spock.lang.Specification
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SpockkKotlinCompatibilityTest : Specification() {

  private val workspace = Workspace()

  /**
   * The Kotlin Gradle plugin publishes Gradle-version-specific variants. When a workspace build
   * runs under a newer Gradle than the Kotlin version supports, resolution fails because Gradle
   * requests a capability that the old Kotlin version never published.
   *
   * Compatibility table: https://kotlinlang.org/docs/gradle-configure-project.html
   */
  fun `supports Kotlin #kotlinVersion`(kotlinVersion: KotlinVersion, gradleVersion: GradleVersion) {
    setup
    workspace.kotlinVersion(kotlinVersion).gradleVersion(gradleVersion).setup()
    workspace.addSuccessfulSpec()
    workspace.addSpecWithDataPipes()

    `when`
    val result = workspace.build("test", "--stacktrace")

    then
    assertEquals(TaskOutcome.SUCCESS, result.task(":test")!!.outcome)
    result.output.let {
      assertContains(it, "SuccessfulSpec > passing feature 1 PASSED")
      assertContains(it, "SuccessfulSpec > passing feature 2 PASSED")
      assertContains(it, "DataPipeSpec > data pipe feature > data pipe feature")
    }

    where
    kotlinVersion          ; gradleVersion
    KotlinVersions.V2_0_21 ; GradleVersions.V8_8
    KotlinVersions.V2_1_21 ; GradleVersions.V8_12_1
    KotlinVersions.V2_2_21 ; GradleVersions.V8_14_5
    KotlinVersions.V2_3_21 ; GradleVersions.V9_3_1
  }
}
