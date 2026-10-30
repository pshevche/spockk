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
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.ConfigurationScriptLoader
import org.spockframework.runtime.model.parallel.Resources
import spock.config.ConfigurationException
import spock.lang.PendingFeature
import spock.lang.ResourceLock
import spock.lang.Specification
import java.lang.reflect.InvocationTargetException

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class ConfigurationScriptLoaderSpec : Specification() {

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/718")
  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#load configuration script from system property that points to class path location"
  )
  fun `load configuration script from system property that points to class path location`() {
    System.setProperty(PROP_KEY, VALID_CONFIG)
    val loader = loader(PROP_KEY, "foo", "bar")

    expect
    load(loader) != null

    cleanup
    System.clearProperty(PROP_KEY)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/718")
  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#load configuration script from system property that points to file system location"
  )
  fun `load configuration script from system property that points to file system location`() {
    val location = javaClass.classLoader.getResource(VALID_CONFIG)!!.path
    System.setProperty(PROP_KEY, location)
    val loader = loader(PROP_KEY, "foo", "bar")

    expect
    load(loader) != null

    cleanup
    System.clearProperty(PROP_KEY)
  }

  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#system property that points to unexisting location is rejected"
  )
  fun `system property that points to unexisting location is rejected`() {
    System.setProperty(PROP_KEY, "does/not/exist")
    val loader = loader(PROP_KEY, "foo", "bar")

    `when`
    load(loader)

    then
    thrown(ConfigurationException::class.java)

    cleanup
    System.clearProperty(PROP_KEY)
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/718")
  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#load configuration script from class path location"
  )
  fun `load configuration script from class path location`() {
    val loader = loader("does/not/exist", VALID_CONFIG, "bar")

    expect
    load(loader) != null
  }

  @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/718")
  @MigratedFrom(
    "org.spockframework.runtime.ConfigurationScriptLoaderSpec#load configuration script from file system location"
  )
  fun `load configuration script from file system location`() {
    val location = javaClass.classLoader.getResource(VALID_CONFIG)!!.path
    val loader = loader("does/not/exist", "foo", location)

    expect
    load(loader) != null
  }

  // The loader's constructor and method are package-private.
  private fun loader(systemProperty: String, classPathLocation: String, fileLocation: String): Any =
    ConfigurationScriptLoader::class.java
      .getDeclaredConstructor(String::class.java, String::class.java, String::class.java)
      .apply { isAccessible = true }
      .newInstance(systemProperty, classPathLocation, fileLocation)

  private fun load(loader: Any): Any? = try {
    ConfigurationScriptLoader::class.java.getDeclaredMethod("loadAutoDetectedScript")
      .apply { isAccessible = true }
      .invoke(loader)
  } catch (e: InvocationTargetException) {
    throw e.targetException
  }

  private companion object {
    const val PROP_KEY = "spockConfigSystemProperty"
    const val VALID_CONFIG = "io/github/pshevche/spockk/runtime/ValidConfig.txt"
  }
}
