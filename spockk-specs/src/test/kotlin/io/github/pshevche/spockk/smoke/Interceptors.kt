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

package io.github.pshevche.spockk.smoke

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.interceptors.LifecycleRecorderAndContextTesterExtension
import io.github.pshevche.spockk.fixtures.runtime.samples.interceptors.nonmethod.SubSpec
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import spock.lang.ResourceLock
import spock.lang.Specification

class Interceptors : Specification() {

  private fun lifecycleOutlineOf(specClass: Class<*>): String {
    LifecycleRecorderAndContextTesterExtension.lifecycleOutline = ""
    execute(selectClass(specClass)).assertStatistics { it.failed(0) }
    return LifecycleRecorderAndContextTesterExtension.lifecycleOutline
  }

  private fun snapshot(name: String): String =
    javaClass.getResource("/snapshots/smoke/Interceptors/$name.txt")!!.readText()

  @ResourceLock("LifecycleRecorderAndContextTesterExtension.lifecycleOutline")
  @MigratedFrom(
    "org.spockframework.smoke.Interceptors#non-method interceptors are called even without the respective method"
  )
  fun `non-method interceptors are called even without the respective method`() {
    `when`
    val outline = lifecycleOutlineOf(
      SubSpec::class.java
    )

    then
    outline == snapshot("non-method interceptors are called even without the respective method")
  }
}
