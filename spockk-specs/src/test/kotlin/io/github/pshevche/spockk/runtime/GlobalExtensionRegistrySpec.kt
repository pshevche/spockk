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
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.GlobalExtensionRegistry
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.extension.builtin.IgnoreExtension
import org.spockframework.runtime.extension.builtin.IncludeExcludeExtension
import org.spockframework.util.InternalSpockError
import spock.config.ConfigurationObject
import spock.config.RunnerConfiguration
import spock.lang.Ignore
import spock.lang.Specification
import java.lang.reflect.InvocationTargetException

class GlobalExtensionRegistrySpec : Specification() {

  // The registry constructor is package-private in Spock; unwrap so tests see the original exception.
  private fun newRegistry(extensions: List<Class<*>>, configs: List<Class<*>>): GlobalExtensionRegistry {
    val constructor = GlobalExtensionRegistry::class.java.getDeclaredConstructor(List::class.java, List::class.java)
    constructor.isAccessible = true
    try {
      return constructor.newInstance(extensions, configs)
    } catch (e: InvocationTargetException) {
      throw e.targetException
    }
  }

  @MigratedFrom("org.spockframework.runtime.GlobalExtensionRegistrySpec#only accepts configuration objects annotated with @ConfigurationObject")
  fun `only accepts configuration objects annotated with @ConfigurationObject`() {
    `when`
    newRegistry(listOf(), listOf(RunnerConfiguration::class.java))

    then
    noExceptionThrown()

    `when`
    newRegistry(listOf(), listOf(Any::class.java))

    then
    thrown(InternalSpockError::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.GlobalExtensionRegistrySpec#only accepts extension classes extending IGlobalExtension")
  fun `only accepts extension classes extending IGlobalExtension`() {
    `when`
    newRegistry(listOf(IncludeExcludeExtension::class.java), listOf()).initializeGlobalExtensions()

    then
    noExceptionThrown()

    `when`
    newRegistry(listOf(IgnoreExtension::class.java), listOf()).initializeGlobalExtensions()

    then
    thrown(ExtensionException::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.GlobalExtensionRegistrySpec#instantiates extensions using their no-arg constructor")
  fun `instantiates extensions using their no-arg constructor`() {
    `when`
    newRegistry(listOf(MyExtension::class.java), listOf()).initializeGlobalExtensions()

    then
    MyExtension.instantiated

    `when`
    newRegistry(listOf(MissingNoArgCtorExtension::class.java), listOf()).initializeGlobalExtensions()

    then
    thrown(ExtensionException::class.java)
  }

  @MigratedFrom("org.spockframework.runtime.GlobalExtensionRegistrySpec#auto-instantiates and provides access to global configuration objects")
  fun `auto-instantiates and provides access to global configuration objects`() {
    `when`
    val registry = newRegistry(listOf(), listOf(RunnerConfiguration::class.java, MySettings::class.java))

    then
    registry.getConfigurationByName("runner") is RunnerConfiguration
    registry.getConfigurationByType(RunnerConfiguration::class.java) is RunnerConfiguration
    registry.getConfigurationByName("settings") is MySettings
    registry.getConfigurationByType(MySettings::class.java) is MySettings
  }

  @MigratedFrom(
    "org.spockframework.runtime.GlobalExtensionRegistrySpec#auto-instantiates and provides access to configuration objects referenced by extensions"
  )
  fun `auto-instantiates and provides access to configuration objects referenced by extensions`() {
    `when`
    val registry = newRegistry(listOf(FieldInjectableExtension::class.java), listOf())
    registry.initializeGlobalExtensions()

    then
    registry.getConfigurationByName("runner") is RunnerConfiguration
    registry.getConfigurationByType(RunnerConfiguration::class.java) is RunnerConfiguration
    registry.getConfigurationByName("settings") is MySettings
    registry.getConfigurationByType(MySettings::class.java) is MySettings
  }

  @MigratedFrom("org.spockframework.runtime.GlobalExtensionRegistrySpec#injects configuration objects into extensions via fields")
  fun `injects configuration objects into extensions via fields`() {
    val registry = newRegistry(listOf(FieldInjectableExtension::class.java), listOf(RunnerConfiguration::class.java))

    `when`
    registry.initializeGlobalExtensions()

    then
    val extension = registry.globalExtensions[0] as FieldInjectableExtension
    extension.config is RunnerConfiguration
    extension.settings is MySettings
  }

  @MigratedFrom("org.spockframework.runtime.GlobalExtensionRegistrySpec#injects configuration objects into extensions via constructors")
  fun `injects configuration objects into extensions via constructors`() {
    val registry = newRegistry(
      listOf(ConstructorInjectableExtension::class.java),
      listOf(RunnerConfiguration::class.java)
    )

    `when`
    registry.initializeGlobalExtensions()

    then
    val extension = registry.globalExtensions[0] as ConstructorInjectableExtension
    extension.config is RunnerConfiguration
    extension.settings is MySettings
  }

  @MigratedFrom("org.spockframework.runtime.GlobalExtensionRegistrySpec#maintains a single instance of each configuration object type")
  fun `maintains a single instance of each configuration object type`() {
    val registry = newRegistry(
      listOf(FieldInjectableExtension::class.java, SettingsExtension::class.java),
      listOf()
    )

    `when`
    registry.initializeGlobalExtensions()

    then
    val first = registry.globalExtensions[0] as FieldInjectableExtension
    val second = registry.globalExtensions[1] as SettingsExtension
    registry.getConfigurationByType(MySettings::class.java) === first.settings
    first.settings === second.settings
  }

  @MigratedFrom("org.spockframework.runtime.GlobalExtensionRegistrySpec#allows to instantiate local extensions via #desc injection")
  fun `allows to instantiate local extensions via #desc injection`(clazz: Class<out IAnnotationDrivenExtension<*>>, desc: String) {
    val registry = newRegistry(listOf(), listOf(RunnerConfiguration::class.java))
    registry.initializeGlobalExtensions()

    `when`
    val extension = registry.instantiateExtension(clazz)

    then
    val config = if (extension is LocalFieldExtension) extension.config else (extension as LocalConstructorExtension).config
    config is RunnerConfiguration

    where
    clazz                                 ; desc
    LocalFieldExtension::class.java       ; "field"
    LocalConstructorExtension::class.java ; "constructor"
  }

  @MigratedFrom(
    "org.spockframework.runtime.GlobalExtensionRegistrySpec#complains if local extension references unknown configuration class (#desc injection)"
  )
  fun `complains if local extension references unknown configuration class (#desc injection)`(clazz: Class<out IAnnotationDrivenExtension<*>>, desc: String) {
    val registry = newRegistry(listOf(), listOf())
    registry.initializeGlobalExtensions()

    `when`
    registry.instantiateExtension(clazz)

    then
    val e: ExtensionException = thrown()
    e.cause!!.message!!.contains("unknown configuration class")

    where
    clazz                                 ; desc
    LocalFieldExtension::class.java       ; "field"
    LocalConstructorExtension::class.java ; "constructor"
  }

  class MyExtension : IGlobalExtension {
    // An init block crashes the compiler plugin: https://github.com/pshevche/spockk/issues/739
    @Suppress("unused")
    private val marker = markInstantiated()

    companion object {
      @Volatile
      var instantiated = false

      private fun markInstantiated() {
        instantiated = true
      }
    }
  }

  class MissingNoArgCtorExtension(@Suppress("UNUSED_PARAMETER") x: Int) : IGlobalExtension

  class FieldInjectableExtension : IGlobalExtension {
    @JvmField
    var config: RunnerConfiguration? = null

    @JvmField
    var settings: MySettings? = null
  }

  class ConstructorInjectableExtension(val config: RunnerConfiguration, val settings: MySettings) : IGlobalExtension

  class SettingsExtension : IGlobalExtension {
    @JvmField
    var settings: MySettings? = null
  }

  class LocalFieldExtension : IAnnotationDrivenExtension<Ignore> {
    @JvmField
    var config: RunnerConfiguration? = null
  }

  class LocalConstructorExtension(val config: RunnerConfiguration) : IAnnotationDrivenExtension<Ignore>

  @ConfigurationObject("settings")
  class MySettings {
    var name: String? = null
    var age: Int = 0
  }
}
