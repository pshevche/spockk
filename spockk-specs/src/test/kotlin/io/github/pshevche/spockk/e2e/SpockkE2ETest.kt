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

class SpockkE2ETest : Specification() {

  val workspace = Workspace()

  fun `can execute spockk tests as part of the Gradle build`() {
    given
    workspace.setup()
    workspace.addSuccessfulSpec()
    workspace.addFailingSpec()

    `when`
    val result = workspace.buildAndFail("test")

    then
    assert(result.task(":test")!!.outcome == TaskOutcome.FAILED)
    result.output.let {
      assert(it.contains("SuccessfulSpec > passing feature 1 PASSED"))
      assert(it.contains("SuccessfulSpec > passing feature 2 PASSED"))
      assert(it.contains("FailingSpec > failing feature 1 FAILED"))
      assert(it.contains("FailingSpec > failing feature 2 FAILED"))
    }
  }

  fun `respects spec filters`() {
    given
    workspace.setup()
    workspace.addSuccessfulSpec()
    workspace.addFailingSpec()

    `when`
    val result = workspace.build("test", "--tests", "SuccessfulSpec")

    then
    assert(result.task(":test")!!.outcome == TaskOutcome.SUCCESS)
    result.output.let {
      assert(it.contains("SuccessfulSpec > passing feature 1 PASSED"))
      assert(it.contains("SuccessfulSpec > passing feature 2 PASSED"))
      assert(!it.contains("FailingSpec > failing feature 1 FAILED"))
      assert(!it.contains("FailingSpec > failing feature 2 FAILED"))
    }
  }

  fun `respects feature filters`() {
    given
    workspace.setup()
    workspace.addSuccessfulSpec()
    workspace.addFailingSpec()

    `when`
    val result = workspace.build("test", "--tests", "SuccessfulSpec.passing feature 1")

    then
    assert(result.task(":test")!!.outcome == TaskOutcome.SUCCESS)
    result.output.let {
      assert(it.contains("SuccessfulSpec > passing feature 1 PASSED"))
      assert(!it.contains("SuccessfulSpec > passing feature 2 PASSED"))
      assert(!it.contains("FailingSpec > failing feature 1 FAILED"))
      assert(!it.contains("FailingSpec > failing feature 2 FAILED"))
    }
  }
}
