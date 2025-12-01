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

package io.github.pshevche.spockk.fixtures.e2e

import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import spock.util.environment.OperatingSystem
import java.nio.file.Files
import java.nio.file.Paths

class Workspace {

  val projectDir =
    Files.createDirectories(
      Paths.get(
        System.getProperty("spockk.workspaceDir"),
        "workspace-${System.currentTimeMillis()}"
      )
    )
  private val settingsFile = projectDir.resolve("settings.gradle.kts").toFile()
  private val buildFile = projectDir.resolve("build.gradle.kts").toFile()
  private val sourcesDir = projectDir.resolve("src/test/kotlin").toFile()

  fun setup(kotlinVersion: String = System.getProperty("spockk.kotlinVersion")) {
    configureRepositories()
    applyPlugins(kotlinVersion)
    configureTestTasks()
  }

  fun build(vararg args: String) = runner(args.toList()).build()

  fun buildAndFail(vararg args: String) = runner(args.toList()).buildAndFail()

  private fun runner(args: List<String>): GradleRunner =
    GradleRunner.create().withProjectDir(projectDir.toFile()).withArguments(args).forwardOutput()

  private fun configureRepositories() {
    settingsFile.writeText(
      """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    ${repoUnderTest("gradlePluginRepo")}
                }
            }
            """
        .trimIndent()
    )
    buildFile.writeText(
      """
            repositories {
                mavenCentral()
                ${repoUnderTest("compilerPluginRepo")}
                ${repoUnderTest("coreRepo")}
            }
            """
        .trimIndent()
    )
  }

  private fun repoUnderTest(property: String): String {
    var uri = System.getProperty("spockk.$property")
    if (OperatingSystem.getCurrent().isWindows) {
      uri = uri.replace("\\", "/")
    }
    return """
          maven {
              url = uri("$uri")
          }
      """
      .trimIndent()
  }

  private fun applyPlugins(kotlinVersion: String) {
    buildFile.appendText(
      """

            plugins {
                kotlin("jvm") version "$kotlinVersion"
                id("io.github.pshevche.spockk") version "latest.integration"
            }
            """
        .trimIndent()
    )
  }

  private fun configureTestTasks() {
    buildFile.appendText(
      """

            dependencies {
                testImplementation("io.github.pshevche.spockk:spockk-core:latest.integration")
                testImplementation("org.spockframework:spock-core:${System.getProperty("spockk.spockVersion")}")
                testRuntimeOnly("org.junit.platform:junit-platform-launcher:${System.getProperty("spockk.junitPlatformVersion")}")
            }
            tasks.test {
                useJUnitPlatform {
                    includeEngines.add("spock")
                }
                testLogging {
                    events.addAll(listOf(
                        org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                        org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                        org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
                    ))
                    displayGranularity = 0
                }
            }
            """
        .trimIndent()
    )
  }

  fun addSuccessfulSpec(name: String = "SuccessfulSpec") {
    writeSpec(
      name,
      """
            class $name : spock.lang.Specification() {
                fun `passing feature 1`() {
                    io.github.pshevche.spockk.lang.expect
                    assert(true)
                }

                fun `passing feature 2`() {
                    io.github.pshevche.spockk.lang.expect
                    assert(true)
                }

            }
        """
        .trimIndent()
    )
  }

  fun addFailingSpec(name: String = "FailingSpec") {
    writeSpec(
      name,
      """
            class $name : spock.lang.Specification() {
                fun `failing feature 1`() {
                    io.github.pshevche.spockk.lang.expect
                    assert(false)
                }

                fun `failing feature 2`() {
                    io.github.pshevche.spockk.lang.expect
                    assert(false)
                }

            }
        """
        .trimIndent()
    )
  }

  private fun writeSpec(name: String, @Language("kotlin") content: String) {
    Files.createDirectories(sourcesDir.toPath())
    sourcesDir.resolve("$name.kt").writeText(content)
  }
}
