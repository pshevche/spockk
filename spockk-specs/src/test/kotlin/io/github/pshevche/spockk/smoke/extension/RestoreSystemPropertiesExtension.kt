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
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.setup
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class RestoreSystemPropertiesExtension : Specification() {

  fun setupSpec() {
    System.setProperty("RestoreSystemPropertiesExtension.prop1", "original value")
    System.setProperty("RestoreSystemPropertiesExtension.prop2", "original value")
  }

  @RestoreSystemProperties
  @MigratedFrom(
    "org.spockframework.smoke.extension.RestoreSystemPropertiesExtension#restores system properties for methods annotated with @RestoreSystemProperties"
  )
  fun `restores system properties for methods annotated with @RestoreSystemProperties`() {
    setup
    System.setProperty("RestoreSystemPropertiesExtension.prop1", "new value")

    expect
    System.getProperty("RestoreSystemPropertiesExtension.prop1") == "new value"
  }

  @MigratedFrom(
    "org.spockframework.smoke.extension.RestoreSystemPropertiesExtension#does not restore system properties for methods not annotated with @RestoreSystemProperties"
  )
  fun `does not restore system properties for methods not annotated with @RestoreSystemProperties`() {
    setup
    System.setProperty("RestoreSystemPropertiesExtension.prop2", "new value")

    expect
    System.getProperty("RestoreSystemPropertiesExtension.prop2") == "new value"
  }

  fun cleanupSpec() {
    assert(System.getProperty("RestoreSystemPropertiesExtension.prop1") == "original value")
    assert(System.getProperty("RestoreSystemPropertiesExtension.prop2") == "new value")
  }
}
