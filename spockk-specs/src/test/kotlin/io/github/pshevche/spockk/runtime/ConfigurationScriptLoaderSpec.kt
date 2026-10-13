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
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.ConfigurationScriptLoader
import org.spockframework.runtime.model.parallel.Resources
import spock.config.ConfigurationException
import spock.lang.ResourceLock
import spock.lang.Specification

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class ConfigurationScriptLoaderSpec : Specification() {

  private companion object {
    const val PROP_KEY = "spockConfigSystemProperty"
    const val VALID_CONFIG = "io/github/pshevche/spockk/runtime/ValidConfig.txt"
    const val INVALID_CONFIG = "io/github/pshevche/spockk/runtime/InvalidConfig.txt"
  }

  private fun validConfigLocation(): String = javaClass.classLoader.getResource(VALID_CONFIG)!!.path

  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#load configuration script from system property that points to class path location"
  )
  fun `load configuration script from system property that points to class path location`() {
    given
    System.setProperty(PROP_KEY, VALID_CONFIG)
    val loader = ConfigurationScriptLoader(PROP_KEY, "foo", "bar")

    expect
    loader.loadAutoDetectedScript() != null

    cleanup
    System.clearProperty(PROP_KEY)
  }

  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#load configuration script from system property that points to file system location"
  )
  fun `load configuration script from system property that points to file system location`() {
    given
    System.setProperty(PROP_KEY, validConfigLocation())
    val loader = ConfigurationScriptLoader(PROP_KEY, "foo", "bar")

    expect
    loader.loadAutoDetectedScript() != null

    cleanup
    System.clearProperty(PROP_KEY)
  }

  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#system property that points to unexisting location is rejected"
  )
  fun `system property that points to unexisting location is rejected`() {
    given
    System.setProperty(PROP_KEY, "does/not/exist")
    val loader = ConfigurationScriptLoader(PROP_KEY, "foo", "bar")

    `when`
    loader.loadAutoDetectedScript()

    then
    thrown(ConfigurationException::class.java)

    cleanup
    System.clearProperty(PROP_KEY)
  }

  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#configuration script that cannot be compiled is rejected"
  )
  fun `configuration script that cannot be compiled is rejected`() {
    given
    System.setProperty(PROP_KEY, INVALID_CONFIG)
    val loader = ConfigurationScriptLoader(PROP_KEY, "foo", "bar")

    `when`
    loader.loadAutoDetectedScript()

    then
    thrown(ConfigurationException::class.java)

    cleanup
    System.clearProperty(PROP_KEY)
  }

  @MigratedFrom("org.spockframework.runtime.ConfigurationScriptLoaderSpec#load configuration script from class path location")
  fun `load configuration script from class path location`() {
    given
    val loader = ConfigurationScriptLoader("does/not/exist", VALID_CONFIG, "bar")

    expect
    loader.loadAutoDetectedScript() != null
  }

  @MigratedFrom("org.spockframework.runtime.ConfigurationScriptLoaderSpec#load configuration script from file system location")
  fun `load configuration script from file system location`() {
    given
    val loader = ConfigurationScriptLoader("does/not/exist", "foo", validConfigLocation())

    expect
    loader.loadAutoDetectedScript() != null
  }
}
