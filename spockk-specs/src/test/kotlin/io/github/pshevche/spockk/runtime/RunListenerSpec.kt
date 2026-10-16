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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.runlistener.ErrorLocation
import io.github.pshevche.spockk.fixtures.runtime.samples.runlistener.ErrorLocationSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.runlistener.IgnoredListenedSpec
import io.github.pshevche.spockk.fixtures.runtime.samples.runlistener.RunListenerDelegate
import io.github.pshevche.spockk.lang.any
import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.did
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.noMoreInteractions
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.spockframework.runtime.IRunListener
import org.spockframework.runtime.model.BlockKind
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Execution
import spock.lang.Issue
import spock.lang.Specification

@Execution(value = ExecutionMode.SAME_THREAD, reason = "Uses a shared field on a delegate")
class RunListenerSpec : Specification() {

  private val runListener = Mock(IRunListener::class.java)

  @Issue("https://github.com/spockframework/spock/issues/1662")
  @MigratedFrom("org.spockframework.runtime.RunListenerSpec#IRunListener is called for skipped specs")
  fun `IRunListener is called for skipped specs`() {
    given
    RunListenerDelegate.delegate = runListener

    `when`
    execute(selectClass(IgnoredListenedSpec::class.java))

    then
    1 * runListener.specSkipped(any())
    noMoreInteractions(runListener)

    cleanup
    RunListenerDelegate.delegate = null
  }

  @MigratedFrom("org.spockframework.runtime.RunListenerSpec#IRunListener gets called for different error locations")
  fun `IRunListener gets called for different error locations`(
    errorLocation: String,
    block: BlockKind?,
    blockTexts: List<String>
  ) {
    given
    RunListenerDelegate.delegate = runListener
    ErrorLocation.failAt = errorLocation
    var errorInfo: ErrorInfo? = null

    `when`
    val feature = if (errorLocation == "feature expect") "an expect test" else "a test"
    execute(selectMethod(ErrorLocationSpec::class.java, feature))

    then
    1 * runListener.error(any()) did { args -> errorInfo = args[0] as ErrorInfo }
    val actualBlock = errorInfo!!.errorContext.block
    val actualKind = if (actualBlock == null) null else actualBlock.kind
    val actualTexts = if (actualBlock == null) emptyList<String>() else actualBlock.texts
    actualKind == block
    actualTexts == blockTexts

    cleanup
    RunListenerDelegate.delegate = null
    ErrorLocation.failAt = ""

    where
    errorLocation     ; block             ; blockTexts
    "setupSpec"       ; null              ; emptyList<String>()
    "setup"           ; null              ; emptyList<String>()
    "feature start"   ; null              ; emptyList<String>()
    "feature setup"   ; BlockKind.SETUP   ; listOf("setup label")
    "feature expect"  ; BlockKind.EXPECT  ; listOf("expect label")
    "feature when"    ; BlockKind.WHEN    ; listOf("when label")
    "feature then"    ; BlockKind.THEN    ; listOf("then label")
    "feature cleanup" ; BlockKind.CLEANUP ; listOf("cleanup label")
    "cleanup"         ; null              ; emptyList<String>()
    "cleanupSpec"     ; null              ; emptyList<String>()
  }
}
