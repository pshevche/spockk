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

package io.github.pshevche.spockk.smoke.extension

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.TempDir
import java.nio.file.Files
import java.nio.file.Path

@Issue("https://github.com/spockframework/spock/issues/1282")
class ParallelTempDirSpec : Specification() {

  @TempDir
  lateinit var tmpDir: Path

  @MigratedFrom("org.spockframework.smoke.extension.ParallelTempDirSpec#Try creating multiple dirs")
  fun `Try creating multiple dirs`(name: String) {
    `when`
    val dir = Files.createDirectory(tmpDir.resolve(name))

    then
    Files.exists(dir)

    where
    variable(name).from("aaa", "bbb", "ccc")
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelTempDirSpec#Try creating single dir")
  fun `Try creating single dir`() {
    `when`
    val dir = Files.createDirectory(tmpDir.resolve("foo"))

    then
    Files.exists(dir)
  }

  @MigratedFrom("org.spockframework.smoke.extension.ParallelTempDirSpec#Do nothing just test the temp dir existence")
  fun `Do nothing just test the temp dir existence`() {
    expect
    Files.exists(tmpDir)
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.ParallelTempDirSpec#Do nothing just test the temp dir existence this time with unroll"
  )
  fun `Do nothing just test the temp dir existence this time with unroll`(n: Int) {
    expect
    Files.exists(tmpDir)

    where
    variable(n).from(1, 2, 3)
  }
}
