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

package io.github.pshevche.spockk.config

import groovy.lang.Closure
import groovy.lang.GroovyObject
import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.builder.DelegatingScript
import org.spockframework.runtime.ConfigurationBuilder
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.runtime.IConfigurationRegistry
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.config.RunnerConfiguration
import spock.lang.Specification
import spock.lang.Subject
import java.math.BigDecimal

class ParallelConfigurationSpec : Specification() {

  private val availableProcessors = Runtime.getRuntime().availableProcessors()

  @Subject
  private val runnerConfiguration = RunnerConfiguration()

  private val configurationRegistry = object : IConfigurationRegistry {
    override fun <T : Any?> getConfigurationByType(clazz: Class<T>): T? = null

    override fun getConfigurationByName(name: String): Any? = if (name == "runner") runnerConfiguration else null

    override fun <T : Any?> instantiateExtension(extension: Class<T>): T = extension.getDeclaredConstructor().newInstance()
  }

  private val builder = ConfigurationBuilder()

  @MigratedFrom("spock.config.ParallelConfigurationSpec#check default values")
  fun `check default values`() {
    expect
    verifyAll {
      runnerConfiguration.parallel.enabled == java.lang.Boolean.getBoolean("spock.parallel.enabled")
      runnerConfiguration.parallel.defaultSpecificationExecutionMode == ExecutionMode.CONCURRENT
      runnerConfiguration.parallel.defaultExecutionMode == ExecutionMode.CONCURRENT
      runnerConfiguration.parallel.parallelExecutionConfiguration.parallelism == maxOf(availableProcessors - 2, 1)
    }
  }

  @MigratedFrom("spock.config.ParallelConfigurationSpec#check binding")
  fun `check binding`() {
    given
    val script = parallelScript {
      call("enabled", true)
      call("defaultSpecificationExecutionMode", ExecutionMode.SAME_THREAD)
      call("defaultExecutionMode", ExecutionMode.SAME_THREAD)
    }

    `when`
    builder.build(configurationRegistry, script)

    then
    verifyAll {
      runnerConfiguration.parallel.enabled
      runnerConfiguration.parallel.defaultSpecificationExecutionMode == ExecutionMode.SAME_THREAD
      runnerConfiguration.parallel.defaultExecutionMode == ExecutionMode.SAME_THREAD
    }
  }

  @MigratedFrom("spock.config.ParallelConfigurationSpec#check fixed")
  fun `check fixed`() {
    given
    val script = parallelScript {
      call("enabled", true)
      call("fixed", 55)
    }

    `when`
    builder.build(configurationRegistry, script)

    then
    verifyAll {
      runnerConfiguration.parallel.parallelExecutionConfiguration.parallelism == 55
      runnerConfiguration.parallel.parallelExecutionConfiguration.maxPoolSize == 55 + 256
      runnerConfiguration.parallel.parallelExecutionConfiguration.minimumRunnable == 55
      runnerConfiguration.parallel.parallelExecutionConfiguration.corePoolSize == 55
      runnerConfiguration.parallel.parallelExecutionConfiguration.keepAliveSeconds == 30
    }
  }

  @MigratedFrom("spock.config.ParallelConfigurationSpec#check dynamic")
  fun `check dynamic`() {
    given
    val expectedParallelism = availableProcessors * 2
    val script = parallelScript {
      call("enabled", true)
      call("dynamic", BigDecimal(2))
    }

    `when`
    builder.build(configurationRegistry, script)

    then
    verifyAll {
      runnerConfiguration.parallel.parallelExecutionConfiguration.parallelism == expectedParallelism
      runnerConfiguration.parallel.parallelExecutionConfiguration.maxPoolSize == expectedParallelism + 256
      runnerConfiguration.parallel.parallelExecutionConfiguration.minimumRunnable == expectedParallelism
      runnerConfiguration.parallel.parallelExecutionConfiguration.corePoolSize == expectedParallelism
      runnerConfiguration.parallel.parallelExecutionConfiguration.keepAliveSeconds == 30
    }
  }

  @MigratedFrom("spock.config.ParallelConfigurationSpec#check dynamicWithReservedProcessors")
  fun `check dynamicWithReservedProcessors`() {
    given
    val expectedParallelism = maxOf(availableProcessors - 1, 1)
    val script = parallelScript {
      call("enabled", true)
      call("dynamicWithReservedProcessors", BigDecimal(1), 1)
    }

    `when`
    builder.build(configurationRegistry, script)

    then
    verifyAll {
      runnerConfiguration.parallel.parallelExecutionConfiguration.parallelism == expectedParallelism
      runnerConfiguration.parallel.parallelExecutionConfiguration.maxPoolSize == expectedParallelism + 256
      runnerConfiguration.parallel.parallelExecutionConfiguration.minimumRunnable == expectedParallelism
      runnerConfiguration.parallel.parallelExecutionConfiguration.corePoolSize == expectedParallelism
      runnerConfiguration.parallel.parallelExecutionConfiguration.keepAliveSeconds == 30
    }
  }

  @MigratedFrom("spock.config.ParallelConfigurationSpec#dynamicWithReservedProcessors throws for negative reservedThreads")
  fun `dynamicWithReservedProcessors throws for negative reservedThreads`() {
    given
    val script = parallelScript {
      call("enabled", true)
      call("dynamicWithReservedProcessors", BigDecimal(1), -1)
    }

    `when`
    builder.build(configurationRegistry, script)

    then
    thrown(IllegalArgumentException::class.java)
  }

  @MigratedFrom("spock.config.ParallelConfigurationSpec#dynamicWithReservedProcessors throws for factor larger than 1")
  fun `dynamicWithReservedProcessors throws for factor larger than 1`() {
    given
    val script = parallelScript {
      call("enabled", true)
      call("dynamicWithReservedProcessors", BigDecimal(2), 1)
    }

    `when`
    builder.build(configurationRegistry, script)

    then
    thrown(IllegalArgumentException::class.java)
  }

  /** Equivalent of `runner { parallel { ... } }` in a Groovy configuration script. */
  private fun parallelScript(body: ParallelDsl.() -> Unit): DelegatingScript = object : DelegatingScript() {
    private var scriptDelegate: Any? = null

    override fun `$setDelegate`(delegate: Any?) {
      scriptDelegate = delegate
    }

    override fun run(): Any? {
      call(scriptDelegate, "runner", nested { delegate -> call(delegate, "parallel", nested { it.dsl(body) }) })
      return null
    }
  }

  private fun call(target: Any?, name: String, vararg args: Any?): Any? =
    (target as? GroovyObject)?.invokeMethod(name, args) ?: GroovyRuntimeUtil.invokeMethod(target, name, *args)

  private fun Any?.dsl(body: ParallelDsl.() -> Unit) = ParallelDsl(this).body()

  private fun nested(body: (Any?) -> Unit): Closure<Any?> = object : Closure<Any?>(this) {
    @Suppress("unused")
    fun doCall(configObject: Any?): Any? {
      body(delegate)
      return null
    }
  }

  private inner class ParallelDsl(private val delegate: Any?) {
    fun call(name: String, vararg args: Any?) {
      call(delegate, name, *args)
    }
  }
}
