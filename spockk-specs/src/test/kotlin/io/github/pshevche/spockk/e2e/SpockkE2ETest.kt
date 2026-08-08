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

import io.github.pshevche.spockk.fixtures.e2e.Workspace
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.`when`
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

class SpockkE2ETest : Specification() {

  private lateinit var workspace: Workspace

  fun setup() {
    workspace = Workspace().apply {
      setup()
      addSuccessfulSpec()
      addFailingSpec()
    }
  }

  fun `can execute spockk tests as part of the Gradle build`() {
    `when`
    val result = workspace.buildAndFail("test")

    then
    result.task(":test")!!.outcome == TaskOutcome.FAILED
    verify(result.output) {
      contains("SuccessfulSpec > passing feature 1 PASSED")
      contains("SuccessfulSpec > passing feature 2 PASSED")
      contains("FailingSpec > failing feature 1 FAILED")
      contains("FailingSpec > failing feature 2 FAILED")
    }
  }

  fun `respects spec filters`() {
    `when`
    val result = workspace.build("test", "--tests", "SuccessfulSpec")

    then
    result.task(":test")!!.outcome == TaskOutcome.SUCCESS
    verify(result.output) {
      contains("SuccessfulSpec > passing feature 1 PASSED")
      contains("SuccessfulSpec > passing feature 2 PASSED")
      !contains("FailingSpec > failing feature 1 FAILED")
      !contains("FailingSpec > failing feature 2 FAILED")
    }
  }

  fun `respects feature filters`() {
    `when`
    val result = workspace.build("test", "--tests", "SuccessfulSpec.passing feature 1")

    then
    result.task(":test")!!.outcome == TaskOutcome.SUCCESS
    verify(result.output) {
      contains("SuccessfulSpec > passing feature 1 PASSED")
      !contains("SuccessfulSpec > passing feature 2 PASSED")
      !contains("FailingSpec > failing feature 1 FAILED")
      !contains("FailingSpec > failing feature 2 FAILED")
    }
  }

  fun `compiler plugin does not fail if there are no specs to transform`() {
    given
    workspace = Workspace().apply {
      configureRepositories()
      applyPlugins()
      writeSource(
        "Foo",
        """
        data class Foo(val irrelevant: String)
        """.trimIndent()
      )
    }

    expect
    workspace.build("test")
  }
}
