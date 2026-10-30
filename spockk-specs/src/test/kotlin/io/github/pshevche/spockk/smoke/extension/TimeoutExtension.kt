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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.ClassLevelTimeoutSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.InterruptedThreeTimesSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.InterruptedTwiceSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.TimeoutSettingsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.TimeoutStackTraceSpec
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.testkit.engine.EngineExecutionResults
import org.junit.platform.testkit.engine.EngineTestKit
import org.spockframework.builder.DelegatingScript
import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.RunContext
import org.spockframework.runtime.SpockTimeoutError
import org.spockframework.runtime.StandardStreamsCapturer
import org.spockframework.runtime.extension.builtin.PreconditionContext
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType
import org.spockframework.runtime.extension.builtin.TimeoutConfiguration
import org.spockframework.util.IThrowableFunction
import spock.config.RunnerConfiguration
import spock.lang.FailsWith
import spock.lang.IgnoreIf
import spock.lang.Isolated
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Timeout
import spock.util.environment.RestoreSystemProperties
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.spockframework.runtime.extension.builtin.GlobalTimeoutExtension as SpockGlobalTimeoutExtension

class TimeoutJava21Compatible(owner: Any?, thisObject: Any?) : Closure<Boolean>(owner, thisObject) {
  @Suppress("unused")
  fun doCall(context: Any?): Boolean = (context as PreconditionContext<*>).jvm.isJava21Compatible
}

@Isolated("The timings are quite tight and it can get flaky on weak machines if run in parallel, and thread dumps interfere with other test output.")
@Retry
class TimeoutExtension : Specification() {

  @Shared
  var testFrameworkThread: Thread = Thread.currentThread()

  @TempDir
  lateinit var tempDir: Path

  private val outputCapturer = StandardStreamsCapturer()
  private val outputListener = OutputListener()

  fun setup() {
    outputCapturer.addStandardStreamsListener(outputListener)
    outputCapturer.start()
    outputCapturer.muteStandardStreams()
  }

  fun cleanup() {
    outputCapturer.stop()
  }

  @Timeout(1)
  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method that completes in time")
  fun `method that completes in time`() {
    given
    Thread.sleep(500)

    expect
    assert(true)
  }

  @FailsWith(SpockTimeoutError::class)
  @Timeout(1)
  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method that doesn't complete in time")
  fun `method that doesn't complete in time`() {
    given
    Thread.sleep(1100)

    expect
    assert(true)
  }

  @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method that completes in time (millis)")
  fun `method that completes in time (millis)`() {
    given
    Thread.sleep(250)

    expect
    assert(true)
  }

  @FailsWith(SpockTimeoutError::class)
  @Timeout(value = 250, unit = TimeUnit.MILLISECONDS)
  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method that doesn't complete in time (millis)")
  fun `method that doesn't complete in time (millis)`() {
    given
    Thread.sleep(500)

    expect
    assert(true)
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#stack trace shows where thread is hung")
  fun `stack trace shows where thread is hung`() {
    `when`
    val events = execute(selectClass(TimeoutStackTraceSpec::class.java))

    then
    val e = timeoutErrors(events).single()
    val frames = e.stackTrace.filter { it.className == TimeoutStackTraceSpec::class.java.name }
      .map { it.methodName }.take(2)
    e is SpockTimeoutError
    frames == listOf("helper", "foo")
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.TimeoutExtension#annotating spec class has same effect as annotating every feature method not already annotated with @Timeout"
  )
  fun `annotating spec class has same effect as annotating every feature method not already annotated with @Timeout`() {
    `when`
    val events = execute(selectClass(ClassLevelTimeoutSpec::class.java))

    then
    val errors = timeoutErrors(events).map { it as SpockTimeoutError }
    val timeouts = errors.map { it.timeout }.toSet()
    errors.size == 2
    timeouts == setOf(0.25, 0.1)
  }

  @Timeout(1)
  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method invocation occurs on regular test framework thread")
  fun `method invocation occurs on regular test framework thread`() {
    expect
    Thread.currentThread() == testFrameworkThread
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#SpockTimeoutError indicates timeout settings")
  fun `SpockTimeoutError indicates timeout settings`() {
    `when`
    val events = execute(selectClass(TimeoutSettingsSpec::class.java))

    then
    val e = timeoutErrors(events).single() as SpockTimeoutError
    e.timeout == 0.1
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#repeatedly interrupts timed out method until it returns")
  fun `repeatedly interrupts timed out method until it returns`() {
    `when`
    val results = runWithConfig(InterruptedThreeTimesSpec::class.java) {}

    then
    timeoutErrors(results).single() is SpockTimeoutError
    outputListener.count("Method 'foo' has not stopped") == 2
    outputListener.count("Thread dump of current JVM (JCMD)") == 0
    outputListener.count("\"Signal Dispatcher\" #") == 0
    outputListener.count("No further thread dumps will be logged and no timeout listeners will be run") == 0
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#can capture thread dumps on interrupt attempts")
  fun `can capture thread dumps on interrupt attempts`() {
    `when`
    val results = runWithConfig(InterruptedThreeTimesSpec::class.java) { it.printThreadDumpsOnInterruptAttempts = true }

    then
    timeoutErrors(results).single() is SpockTimeoutError
    outputListener.count("Method 'foo' has not stopped") == 2
    outputListener.count("Thread dump of current JVM (JCMD)") == 2
    outputListener.count("\"Signal Dispatcher\" #") == 2
    outputListener.count("No further thread dumps will be logged and no timeout listeners will be run") == 0
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#can set the maximum number of interrupt attempts with thread dump captured")
  fun `can set the maximum number of interrupt attempts with thread dump captured`() {
    `when`
    val results = runWithConfig(InterruptedThreeTimesSpec::class.java) {
      it.printThreadDumpsOnInterruptAttempts = true
      it.maxInterruptAttemptsWithThreadDumps = 1
    }

    then
    timeoutErrors(results).single() is SpockTimeoutError
    outputListener.count("Method 'foo' has not stopped") == 2
    outputListener.count("Thread dump of current JVM (JCMD)") == 1
    outputListener.count("\"Signal Dispatcher\" #") == 1
    outputListener.count("No further thread dumps will be logged and no timeout listeners will be run") == 1
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#can capture thread dumps using jstack")
  fun `can capture thread dumps using jstack`() {
    `when`
    val results = runWithConfig(InterruptedThreeTimesSpec::class.java) {
      it.printThreadDumpsOnInterruptAttempts = true
      it.threadDumpUtilityType = ThreadDumpUtilityType.JSTACK
    }

    then
    timeoutErrors(results).single() is SpockTimeoutError
    outputListener.count("Method 'foo' has not stopped") == 2
    outputListener.count("Thread dump of current JVM (JSTACK)") == 2
    outputListener.count("\"Signal Dispatcher\" #") == 2
    outputListener.count("No further thread dumps will be logged and no timeout listeners will be run") == 0
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#notifies custom interrupt listeners")
  fun `notifies custom interrupt listeners`() {
    given
    val someListenerCalls = AtomicInteger()
    val anotherListenerCalls = AtomicInteger()

    `when`
    val results = runWithConfig(InterruptedThreeTimesSpec::class.java) {
      it.printThreadDumpsOnInterruptAttempts = true
      it.maxInterruptAttemptsWithThreadDumps = 1
      it.interruptAttemptListeners.add(Runnable { someListenerCalls.incrementAndGet() })
      it.interruptAttemptListeners.add(Runnable { anotherListenerCalls.incrementAndGet() })
      it.interruptAttemptListeners.add(Runnable { println("inline interrupt listener") })
    }

    then
    timeoutErrors(results).single() is SpockTimeoutError
    outputListener.count("Method 'foo' has not stopped") == 2
    outputListener.count("Thread dump of current JVM (JCMD)") == 1
    outputListener.count("\"Signal Dispatcher\" #") == 1
    outputListener.count("No further thread dumps will be logged and no timeout listeners will be run") == 1
    someListenerCalls.get() == 2
    anotherListenerCalls.get() == 2
    outputListener.count("inline interrupt listener") == 2
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#excludes dump-capturing thread from thread dumps")
  fun `excludes dump-capturing thread from thread dumps`() {
    `when`
    val results = runWithConfig(InterruptedTwiceSpec::class.java) { it.printThreadDumpsOnInterruptAttempts = true }

    then
    timeoutErrors(results).single() is SpockTimeoutError
    outputListener.count("Thread dump of current JVM") == 1
    outputListener.count("\"[spock.lang.Timeout] Watcher for method 'foo'\" #") == 0
  }

  @RestoreSystemProperties
  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#does not fail if thread dump capturing util is not available")
  fun `does not fail if thread dump capturing util is not available`() {
    given
    System.setProperty("java.home", tempDir.toString())

    `when`
    val results = runWithConfig(InterruptedTwiceSpec::class.java) { it.printThreadDumpsOnInterruptAttempts = true }

    then
    timeoutErrors(results).single() is SpockTimeoutError
    outputListener.count("Could not find requested thread dump capturing utility") == 1
    outputListener.count("Thread dump of current JVM") == 0
  }

  @RestoreSystemProperties
  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#does not fail if java home is not set")
  fun `does not fail if java home is not set`() {
    given
    System.clearProperty("java.home")

    `when`
    val results = runWithConfig(InterruptedTwiceSpec::class.java) { it.printThreadDumpsOnInterruptAttempts = true }

    then
    timeoutErrors(results).single() is SpockTimeoutError
    outputListener.count("Could not determine java home directory") == 1
    outputListener.count("Thread dump of current JVM") == 0
  }

  @Timeout(1)
  @IgnoreIf(value = TimeoutJava21Compatible::class, reason = "The TimeoutExtension uses virtual threads if available, which can't be enumerated")
  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#watcher thread has descriptive name")
  fun `watcher thread has descriptive name`() {
    given
    val group = Thread.currentThread().threadGroup
    val threads = arrayOfNulls<Thread>(group.activeCount())
    group.enumerate(threads)

    val names = threads.map { it?.name }

    expect
    "[spock.lang.Timeout] Watcher for method 'watcher thread has descriptive name'" in names
  }

  // The specs classpath carries Gradle's Groovy 4 (via TestKit), which cannot compile a Spock config script.
  private fun runWithConfig(spec: Class<*>, configure: (TimeoutConfiguration) -> Unit): EngineExecutionResults {
    val script = object : DelegatingScript() {
      override fun run(): Any? {
        configure(getProperty("timeout") as TimeoutConfiguration)
        return null
      }
    }
    return RunContext.withNewContext<EngineExecutionResults, RuntimeException>(
      "timeout-extension",
      File("timeout-extension"),
      script,
      listOf(SpockGlobalTimeoutExtension::class.java),
      listOf(RunnerConfiguration::class.java, TimeoutConfiguration::class.java),
      false,
      IThrowableFunction {
        EngineTestKit.engine("spock").selectors(selectClass(spec)).execute()
      }
    )
  }

  private fun timeoutErrors(events: org.junit.platform.testkit.engine.Events): List<Throwable> =
    events.failed().list().map {
      it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    }

  private fun timeoutErrors(results: EngineExecutionResults): List<Throwable> = timeoutErrors(results.testEvents())

  private class OutputListener : IStandardStreamsListener {
    private val messages = StringBuffer()

    override fun standardOut(message: String) {
      messages.append(message)
    }

    override fun standardErr(message: String) {
      messages.append(message)
    }

    fun count(sequence: String): Int = messages.toString().lines().count { it.contains(sequence) }
  }
}
