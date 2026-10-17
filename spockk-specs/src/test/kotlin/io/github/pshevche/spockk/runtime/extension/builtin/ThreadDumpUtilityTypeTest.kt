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

package io.github.pshevche.spockk.runtime.extension.builtin

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType.JCMD
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType.JSTACK
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ThreadDumpUtilityTypeTest : Specification() {

  companion object {
    private const val PID = 42L

    private val fileNameGetter = ThreadDumpUtilityType::class.java.getDeclaredMethod("getFileName").apply { isAccessible = true }

    private val ThreadDumpUtilityType.utilityFileName: String
      get() = fileNameGetter.invoke(this) as String
  }

  @field:Shared
  @field:TempDir
  lateinit var tempDir: Path

  fun setupSpec() {
    val baseUtilPath = tempDir.resolve("bin")
    Files.createDirectories(baseUtilPath)
    ThreadDumpUtilityType.values().forEach { Files.createFile(baseUtilPath.resolve(it.utilityFileName)) }
  }

  @MigratedFrom("org.spockframework.runtime.extension.builtin.ThreadDumpUtilityTypeTest##utility utility provides correct command arguments")
  fun `#utility utility provides correct command arguments`(utility: ThreadDumpUtilityType, javaHome: Path, additionalArgs: List<String>) {
    expect
    utility.getCommand(javaHome, PID) == listOf(tempDir.resolve("bin").resolve(utility.utilityFileName).toString(), PID.toString()) + additionalArgs

    where
    utility ; javaHome                ; additionalArgs
    JSTACK  ; tempDir                 ; emptyList<String>()
    JSTACK  ; tempDir.resolve("jre")  ; emptyList<String>()
    JCMD    ; tempDir                 ; listOf("Thread.print")
    JCMD    ; tempDir.resolve("jre")  ; listOf("Thread.print")
  }
}
