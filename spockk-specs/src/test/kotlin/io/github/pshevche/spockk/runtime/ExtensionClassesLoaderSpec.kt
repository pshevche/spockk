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

package io.github.pshevche.spockk.runtime

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.junit4.ClassRuleExtension
import org.spockframework.junit4.ExceptionAdapterExtension
import org.spockframework.junit4.JUnit4AnnotationLifecycleMethodsExtension
import org.spockframework.junit4.RuleExtension
import org.spockframework.mock.runtime.MockMakerConfiguration
import org.spockframework.report.log.ReportLogConfiguration
import org.spockframework.runtime.ExtensionClassesLoader
import org.spockframework.runtime.extension.builtin.GlobalTimeoutExtension
import org.spockframework.runtime.extension.builtin.IncludeExcludeExtension
import org.spockframework.runtime.extension.builtin.OptimizeRunOrderExtension
import org.spockframework.runtime.extension.builtin.SnapshotConfig
import org.spockframework.runtime.extension.builtin.TimeoutConfiguration
import org.spockframework.runtime.extension.builtin.UnrollExtension
import org.spockframework.tempdir.TempDirConfiguration
import spock.config.RunnerConfiguration
import spock.lang.Specification

class ExtensionClassesLoaderSpec : Specification() {

  @MigratedFrom("org.spockframework.runtime.ExtensionClassesLoaderSpec#loads GlobalExtensions")
  fun `loads GlobalExtensions`() {
    `when`
    val result = ExtensionClassesLoader().loadExtensionClassesFromDefaultLocation()

    then
    // spockk-specs has spock-junit4 on its classpath, which contributes the first four extensions.
    result == listOf(
      RuleExtension::class.java,
      ClassRuleExtension::class.java,
      JUnit4AnnotationLifecycleMethodsExtension::class.java,
      ExceptionAdapterExtension::class.java,
      GlobalTimeoutExtension::class.java,
      IncludeExcludeExtension::class.java,
      OptimizeRunOrderExtension::class.java,
      UnrollExtension::class.java
    )
  }

  @MigratedFrom("org.spockframework.runtime.ExtensionClassesLoaderSpec#loads global ConfigObjects")
  fun `loads global ConfigObjects`() {
    `when`
    val result = ExtensionClassesLoader().loadConfigClassesFromDefaultLocation()

    then
    result == listOf(
      RunnerConfiguration::class.java,
      ReportLogConfiguration::class.java,
      TempDirConfiguration::class.java,
      SnapshotConfig::class.java,
      TimeoutConfiguration::class.java,
      MockMakerConfiguration::class.java
    )
  }
}
