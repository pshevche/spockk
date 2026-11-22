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
import io.github.pshevche.spockk.lang.noMoreInteractions
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.StandardStreamsCapturer
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.ResourceLock
import spock.lang.Specification

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
class StandardStreamsCapturerSpec : Specification() {

  private val capturer = StandardStreamsCapturer()
  private val listener = Mock(IStandardStreamsListener::class.java)

  fun setup() {
    capturer.addStandardStreamsListener(listener)
    capturer.start()
    capturer.muteStandardStreams()
  }

  fun cleanup() {
    capturer.stop()
  }

  @MigratedFrom("org.spockframework.runtime.StandardStreamsCapturerSpec#supports multiple listeners")
  fun `supports multiple listeners`() {
    val listener2 = Mock(IStandardStreamsListener::class.java)
    capturer.addStandardStreamsListener(listener2)

    `when`
    println("some message")

    then
    1 * listener.standardOut("some message\n")
    1 * listener2.standardOut("some message\n")
  }

  @MigratedFrom("org.spockframework.runtime.StandardStreamsCapturerSpec#allows to unregister listeners")
  fun `allows to unregister listeners`() {
    `when`
    capturer.removeStandardStreamsListener(listener)
    println("some message")

    then
    noMoreInteractions(listener)
  }
}
