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

import groovy.lang.Closure
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.DirectoryFixture
import spock.util.io.FileSystemFixture
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FileSystemFixtureSpec : Specification() {

  @TempDir
  lateinit var myFile: MyFile

  @TempDir
  lateinit var myPath: MyPath

  @TempDir
  lateinit var fsFixture: FileSystemFixture

  @MigratedFrom(
    "org.spockframework.smoke.extension.FileSystemFixtureSpec#can use helper classes if they have a constructor accepting File or Path"
  )
  fun `can use helper classes if they have a constructor accepting File or Path`() {
    expect
    myPath.root != null
    myFile.root != null
    fsFixture.currentPath != null
  }

  @MigratedFrom("org.spockframework.smoke.extension.FileSystemFixtureSpec#FileSystemFixture can create a directory structure")
  fun `FileSystemFixture can create a directory structure`() {
    `when`
    fsFixture.create(
      spec {
        dir(
          "src",
          spec {
            dir(
              "main",
              spec {
                dir(
                  "groovy",
                  spec {
                    Files.writeString(file("HelloWorld.java"), "println \"Hello World\"")
                  }
                )
              }
            )
            dir(
              "test/resources",
              spec {
                Files.writeString(file("META-INF/MANIFEST.MF"), "bogus entry")
              }
            )
          }
        )
      }
    )

    then
    Files.isDirectory(fsFixture.resolve("src/main/groovy"))
    Files.isDirectory(fsFixture.resolve("src/test/resources/META-INF"))
    Files.readString(fsFixture.resolve("src/main/groovy/HelloWorld.java")) == "println \"Hello World\""
    Files.readString(fsFixture.resolve("src/test/resources/META-INF/MANIFEST.MF")) == "bogus entry"
  }

  @MigratedFrom("org.spockframework.smoke.extension.FileSystemFixtureSpec#can copy files from classpath")
  fun `can copy files from classpath`() {
    given
    var result: Path? = null
    var result2: Path? = null

    `when`
    fsFixture.create(
      spec {
        dir(
          "target",
          spec {
            result = copyFromClasspath("/io/github/pshevche/spockk/smoke/extension/SampleFile.txt")
            result2 = copyFromClasspath("/io/github/pshevche/spockk/smoke/extension/SampleFile.txt", "SampleFile2.txt")
          }
        )
      }
    )

    then
    result!!.fileName.toString() == "SampleFile.txt"
    Files.readString(result!!) == "HelloWorld\n"
    Files.readString(result2!!) == "HelloWorld\n"
  }

  @MigratedFrom("org.spockframework.smoke.extension.FileSystemFixtureSpec#can copy files from classpath using explicit context class")
  fun `can copy files from classpath using explicit context class`() {
    given
    var result: Path? = null
    var result2: Path? = null

    `when`
    fsFixture.create(
      spec {
        dir(
          "target",
          spec {
            result = copyFromClasspath("SampleFile.txt", FileSystemFixtureSpec::class.java)
            result2 = copyFromClasspath("SampleFile.txt", "SampleFile2.txt", FileSystemFixtureSpec::class.java)
          }
        )
      }
    )

    then
    result!!.fileName.toString() == "SampleFile.txt"
    Files.readString(result!!) == "HelloWorld\n"
    Files.readString(result2!!) == "HelloWorld\n"
  }

  private fun spec(body: DirectoryFixture.() -> Unit): Closure<Unit> = object : Closure<Unit>(this) {
    @Suppress("unused")
    fun doCall() = body(delegate as DirectoryFixture)
  }
}

class MyFile(val root: File)

class MyPath(val root: Path)
