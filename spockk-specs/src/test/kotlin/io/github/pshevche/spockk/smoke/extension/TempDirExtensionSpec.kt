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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.extension.TempDirLocationSpec
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.spockframework.builder.DelegatingScript
import org.spockframework.runtime.ConfigurationScriptLoader
import org.spockframework.runtime.RunContext
import org.spockframework.runtime.model.parallel.Resources
import org.spockframework.util.IThrowableFunction
import spock.lang.PendingFeature
import spock.lang.ResourceLock
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.TempDir
import spock.util.environment.RestoreSystemProperties
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Stepwise
class TempDirExtensionSpec : Specification() {

  @Shared
  @TempDir
  lateinit var sharedDir: Path

  @Shared
  var previousShared: Path? = null

  @TempDir
  lateinit var iterationDir: File

  @Shared
  var previousIteration: File? = null

  @TempDir
  lateinit var untypedPath: Any

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/928")
  @MigratedFrom("org.spockframework.smoke.extension.TempDirExtensionSpec#temp dir exist")
  fun `temp dir exist`() {
    previousShared = sharedDir
    previousIteration = iterationDir

    expect
    Files.exists(sharedDir)
    iterationDir.exists()
    iterationDir.isDirectory
    untypedPath is Path
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/928")
  @MigratedFrom(
    "org.spockframework.smoke.extension.TempDirExtensionSpec#@TempDir creates only one dir for one spec, if the annotated field is 'shared'"
  )
  fun `@TempDir creates only one dir for one spec, if the annotated field is 'shared'`() {
    expect
    sharedDir == previousShared
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/928")
  @MigratedFrom(
    "org.spockframework.smoke.extension.TempDirExtensionSpec#get a different path for non-shared field, and previous was deleted"
  )
  fun `get a different path for non-shared field, and previous was deleted`() {
    expect
    iterationDir != previousIteration
    !previousIteration!!.exists()
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.TempDirExtensionSpec#@TempDir creates one temp directory per iteration for normal field"
  )
  fun `@TempDir creates one temp directory per iteration for normal field`(i: Int) {
    expect
    iterationDir != previousIteration

    cleanup
    previousIteration = iterationDir

    where
    variable(i).from(1, 2, 3)
  }

  @MigratedFrom("org.spockframework.smoke.extension.TempDirExtensionSpec#have non-writeable directory and file in temp directory")
  fun `have non-writeable directory and file in temp directory`(i: Int) {
    expect
    if (i == 0) {
      val aaa = iterationDir.toPath().resolve("aaa")
      val aaabbb = aaa.resolve("bbb")
      val tempFile = aaabbb.resolve("tmp.txt")
      Files.createDirectories(aaabbb)
      Files.write(tempFile, "ewfwf".toByteArray())
      aaabbb.toFile().setWritable(false)
      aaa.toFile().setWritable(false)
      tempFile.toFile().setWritable(false)
      previousIteration = iterationDir
    } else if (i == 1) {
      assert(!previousIteration!!.exists())
    }

    where
    variable(i).from(0, 1)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/1118")
  @RestoreSystemProperties
  @ResourceLock(Resources.SYSTEM_PROPERTIES)
  @MigratedFrom("org.spockframework.smoke.extension.TempDirExtensionSpec#define temp directory location using configuration script")
  fun `define temp directory location using configuration script`() {
    given
    val userDefinedBase = (untypedPath as Path).resolve("build")
    val configFile = Files.createTempFile((untypedPath as Path), "SpockConfig", ".groovy")
    Files.writeString(configFile, "tempdir { baseDir java.nio.file.Paths.get('${userDefinedBase.toString().replace("\\", "/")}') }")
    System.setProperty("spock.configuration", configFile.toString())
    val userHome = File(RunContext.get().spockUserHome, "Embedded")

    `when`
    val events = inNewContext(ConfigurationScriptLoader(userHome).loadAutoDetectedScript()) {
      execute(selectClass(TempDirLocationSpec::class.java))
    }

    then
    events.assertStatistics { it.started(1).succeeded(1) }
  }

  private fun <T> inNewContext(script: DelegatingScript?, block: () -> T): T {
    val current = RunContext.get()
    return RunContext.withNewContext<T, RuntimeException>(
      current.name + "/Embedded",
      File(current.spockUserHome, "Embedded"),
      script,
      emptyList(),
      emptyList(),
      true,
      IThrowableFunction { block() }
    )
  }
}
