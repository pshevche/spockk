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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import java.io.File

class JUnit4RuleSmokeTest : Specification() {

  @get:Rule
  val tmp = TemporaryFolder()

  fun `rule creates the temp folder before the feature runs`() {
    expect
    tmp.root.isDirectory
  }

  fun `first feature writes a marker file into its temp folder`() {
    given
    tmp.newFile("marker.txt")

    expect
    File(tmp.root, "marker.txt").exists()
  }

  fun `next feature gets a fresh temp folder without the previous marker file`() {
    expect
    !File(tmp.root, "marker.txt").exists()
  }
}
