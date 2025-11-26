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

package io.github.pshevche.spockk.e2e

import io.github.pshevche.spockk.fixtures.e2e.Workspace
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

class SpockkKotlinCompatibilityTest : Specification() {

  val workspace = Workspace()

  fun `supports Kotlin 2_1_21`() {
    given
    workspace.setup("2.1.21")
    workspace.addSuccessfulSpec()

    `when`
    val result = workspace.build("test")

    then
    assert(result.task(":test")!!.outcome == TaskOutcome.SUCCESS)
    result.output.let {
      assert(it.contains("SuccessfulSpec > passing feature 1 PASSED"))
      assert(it.contains("SuccessfulSpec > passing feature 2 PASSED"))
    }
  }

  fun `supports Kotlin 2_0_21`() {
    given
    workspace.setup("2.0.21")
    workspace.addSuccessfulSpec()

    `when`
    val result = workspace.build("test")

    then
    assert(result.task(":test")!!.outcome == TaskOutcome.SUCCESS)
    result.output.let {
      assert(it.contains("SuccessfulSpec > passing feature 1 PASSED"))
      assert(it.contains("SuccessfulSpec > passing feature 2 PASSED"))
    }
  }

  fun `supports Kotlin 1_9_25`() {
    given
    workspace.setup("1.9.25")
    workspace.addSuccessfulSpec()

    `when`
    val result = workspace.build("test")

    then
    assert(result.task(":test")!!.outcome == TaskOutcome.SUCCESS)
    result.output.let {
      assert(it.contains("SuccessfulSpec > passing feature 1 PASSED"))
      assert(it.contains("SuccessfulSpec > passing feature 2 PASSED"))
    }
  }

  fun `supports Kotlin 1_8_22`() {
    given
    workspace.setup("1.8.22")
    workspace.addSuccessfulSpec()

    `when`
    val result = workspace.build("test")

    then
    assert(result.task(":test")!!.outcome == TaskOutcome.SUCCESS)
    result.output.let {
      assert(it.contains("SuccessfulSpec > passing feature 1 PASSED"))
      assert(it.contains("SuccessfulSpec > passing feature 2 PASSED"))
    }
  }
}
