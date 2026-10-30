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
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.ClassLevelTimeoutSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.HungInHelperSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.ThreeInterruptAttemptsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.TimeoutSettingsSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.timeout.TwoInterruptAttemptsSpec
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
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType
import org.spockframework.runtime.extension.builtin.TimeoutConfiguration
import org.spockframework.runtime.model.parallel.Resources
import org.spockframework.util.IThrowableFunction
import spock.config.RunnerConfiguration
import spock.lang.FailsWith
import spock.lang.Isolated
import spock.lang.ResourceLock
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Timeout
import spock.util.environment.RestoreSystemProperties
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

@Isolated("The timings are quite tight and it can get flaky on weak machines if run in parallel, and thread dumps interfere with other test output.")
@Retry
@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
class TimeoutExtension : Specification() {

  @field:Shared
  val testFrameworkThread: Thread = Thread.currentThread()

  @field:TempDir
  lateinit var tempDir: Path

  private val capturer = StandardStreamsCapturer()
  private val output = OutputListener()

  fun setup() {
    capturer.addStandardStreamsListener(output)
    capturer.start()
    capturer.muteStandardStreams()
  }

  fun cleanup() {
    capturer.stop()
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method that completes in time")
  @Timeout(1)
  fun `method that completes in time`() {
    given
    Thread.sleep(500)

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method that doesn't complete in time")
  @FailsWith(SpockTimeoutError::class)
  @Timeout(1)
  fun `method that doesn't complete in time`() {
    given
    Thread.sleep(1100)

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method that completes in time (millis)")
  @Timeout(value = 500, unit = MILLISECONDS)
  fun `method that completes in time (millis)`() {
    given
    Thread.sleep(250)

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method that doesn't complete in time (millis)")
  @FailsWith(SpockTimeoutError::class)
  @Timeout(value = 250, unit = MILLISECONDS)
  fun `method that doesn't complete in time (millis)`() {
    given
    Thread.sleep(500)

    expect
    true
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#stack trace shows where thread is hung")
  fun `stack trace shows where thread is hung`() {
    `when`
    val failures = run(HungInHelperSpec::class.java).failures()

    then
    val e = failures.single() as SpockTimeoutError
    val hungFrames = e.stackTrace.filter { it.className == HungInHelperSpec::class.java.name }.map { it.methodName }
    hungFrames.take(2) == listOf("helper", "foo")
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.TimeoutExtension#annotating spec class has same effect as annotating every feature method not already annotated with @Timeout"
  )
  fun `annotating spec class has same effect as annotating every feature method not already annotated with @Timeout`() {
    `when`
    val failures = run(ClassLevelTimeoutSpec::class.java).failures()

    then
    failures.size == 2
    val e1 = failures[0] as SpockTimeoutError
    e1.timeout == 0.25
    val e2 = failures[1] as SpockTimeoutError
    e2.timeout == 0.1
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#method invocation occurs on regular test framework thread")
  @Timeout(1)
  fun `method invocation occurs on regular test framework thread`() {
    expect
    Thread.currentThread() == testFrameworkThread
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#SpockTimeoutError indicates timeout settings")
  fun `SpockTimeoutError indicates timeout settings`() {
    `when`
    val failures = run(TimeoutSettingsSpec::class.java).failures()

    then
    val e = failures.single() as SpockTimeoutError
    e.timeout == 0.1
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#repeatedly interrupts timed out method until it returns")
  fun `repeatedly interrupts timed out method until it returns`() {
    `when`
    val failures = run(ThreeInterruptAttemptsSpec::class.java).failures()

    then
    failures.single() is SpockTimeoutError
    capturedThreadDumps() == expectedThreadDumps(2, 0, false)
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#can capture thread dumps on interrupt attempts")
  fun `can capture thread dumps on interrupt attempts`() {
    given
    val configure: (TimeoutConfiguration) -> Unit = { it.printThreadDumpsOnInterruptAttempts = true }

    `when`
    val failures = run(ThreeInterruptAttemptsSpec::class.java, configure).failures()

    then
    failures.single() is SpockTimeoutError
    capturedThreadDumps() == expectedThreadDumps(2, 2, false)
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.TimeoutExtension#can set the maximum number of interrupt attempts with thread dump captured"
  )
  fun `can set the maximum number of interrupt attempts with thread dump captured`() {
    given
    val configure: (TimeoutConfiguration) -> Unit = {
      it.printThreadDumpsOnInterruptAttempts = true
      it.maxInterruptAttemptsWithThreadDumps = 1
    }

    `when`
    val failures = run(ThreeInterruptAttemptsSpec::class.java, configure).failures()

    then
    failures.single() is SpockTimeoutError
    capturedThreadDumps() == expectedThreadDumps(2, 1, true)
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#can capture thread dumps using jstack")
  fun `can capture thread dumps using jstack`() {
    given
    val configure: (TimeoutConfiguration) -> Unit = {
      it.printThreadDumpsOnInterruptAttempts = true
      it.threadDumpUtilityType = ThreadDumpUtilityType.JSTACK
    }

    `when`
    val failures = run(ThreeInterruptAttemptsSpec::class.java, configure).failures()

    then
    failures.single() is SpockTimeoutError
    capturedThreadDumps(ThreadDumpUtilityType.JSTACK) == expectedThreadDumps(2, 2, false)
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#notifies custom interrupt listeners")
  fun `notifies custom interrupt listeners`() {
    given
    val someListeners = listOf(AtomicInteger(), AtomicInteger())
    val anotherListener = AtomicInteger()
    val configure: (TimeoutConfiguration) -> Unit = {
      it.printThreadDumpsOnInterruptAttempts = true
      it.maxInterruptAttemptsWithThreadDumps = 1
      someListeners.forEach { counter -> it.interruptAttemptListeners.add { counter.incrementAndGet() } }
      it.interruptAttemptListeners.add { anotherListener.incrementAndGet() }
      it.interruptAttemptListeners.add { println("inline interrupt listener") }
    }

    `when`
    val failures = run(ThreeInterruptAttemptsSpec::class.java, configure).failures()

    then
    failures.single() is SpockTimeoutError
    capturedThreadDumps() == expectedThreadDumps(2, 1, true)
    val listenerCounts = someListeners.map { it.get() }
    listenerCounts == listOf(2, 2)
    anotherListener.get() == 2
    output.count("inline interrupt listener") == 2
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#excludes dump-capturing thread from thread dumps")
  fun `excludes dump-capturing thread from thread dumps`() {
    given
    val configure: (TimeoutConfiguration) -> Unit = { it.printThreadDumpsOnInterruptAttempts = true }

    `when`
    val failures = run(TwoInterruptAttemptsSpec::class.java, configure).failures()

    then
    failures.single() is SpockTimeoutError
    output.count("Thread dump of current JVM") == 1
    output.count("\"[spock.lang.Timeout] Watcher for method 'foo'\" #") == 0
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#does not fail if thread dump capturing util is not available")
  @RestoreSystemProperties
  fun `does not fail if thread dump capturing util is not available`() {
    given
    System.setProperty("java.home", tempDir.toString())
    val configure: (TimeoutConfiguration) -> Unit = { it.printThreadDumpsOnInterruptAttempts = true }

    `when`
    val failures = run(TwoInterruptAttemptsSpec::class.java, configure).failures()

    then
    failures.single() is SpockTimeoutError
    output.count("Could not find requested thread dump capturing utility") == 1
    output.count("Thread dump of current JVM") == 0
  }

  @MigratedFrom("org.spockframework.smoke.extension.TimeoutExtension#does not fail if java home is not set")
  @RestoreSystemProperties
  fun `does not fail if java home is not set`() {
    given
    System.clearProperty("java.home")
    val configure: (TimeoutConfiguration) -> Unit = { it.printThreadDumpsOnInterruptAttempts = true }

    `when`
    val failures = run(TwoInterruptAttemptsSpec::class.java, configure).failures()

    then
    failures.single() is SpockTimeoutError
    output.count("Could not determine java home directory") == 1
    output.count("Thread dump of current JVM") == 0
  }

  private data class ThreadDumps(
    val failedInterruptAttempts: Int,
    val dumps: Int,
    val signalDispatcherThreads: Int,
    val captureLimitExceeded: Boolean
  )

  private fun expectedThreadDumps(
    unsuccessfulInterruptAttempts: Int,
    threadDumps: Int,
    exceededCaptureLimit: Boolean
  ) = ThreadDumps(unsuccessfulInterruptAttempts, threadDumps, threadDumps, exceededCaptureLimit)

  private fun capturedThreadDumps(util: ThreadDumpUtilityType = ThreadDumpUtilityType.JCMD) = ThreadDumps(
    output.count("Method 'foo' has not stopped"),
    output.count("Thread dump of current JVM (${util.name})"),
    // just some thread that is always there
    output.count("\"Signal Dispatcher\" #"),
    output.count("No further thread dumps will be logged and no timeout listeners will be run") == 1
  )

  // The specs classpath carries Gradle's Groovy 4 (via TestKit), which cannot compile a Spock config script.
  private fun run(spec: Class<*>, configure: (TimeoutConfiguration) -> Unit = {}): EngineExecutionResults {
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
      emptyList(),
      listOf(RunnerConfiguration::class.java, TimeoutConfiguration::class.java),
      false,
      IThrowableFunction {
        EngineTestKit.engine("spock").selectors(selectClass(spec)).execute()
      }
    )
  }

  private fun EngineExecutionResults.failures(): List<Throwable> =
    testEvents().failed().list().map {
      it.getRequiredPayload(TestExecutionResult::class.java).throwable.orElseThrow()
    }

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
