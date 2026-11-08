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

package io.github.pshevche.spockk.util

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.model.parallel.ResourceAccessMode
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.Isolated
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.util.environment.OperatingSystem
import spock.util.environment.RestoreSystemProperties

@Isolated("Modifies `os.name` system property")
class OperatingSystemSpec : Specification() {

  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
  @MigratedFrom("spock.util.OperatingSystemSpec#determines name based on os.name system property")
  fun `determines name based on os_name system property`() {
    expect
    OperatingSystem.getCurrent().name == System.getProperty("os.name")
  }

  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
  @MigratedFrom("spock.util.OperatingSystemSpec#determines version based on os.name system property")
  fun `determines version based on os_name system property`() {
    expect
    OperatingSystem.getCurrent().version == System.getProperty("os.version")
  }

  @RestoreSystemProperties
  @MigratedFrom("spock.util.OperatingSystemSpec#determines family based on os.name system property")
  fun `determines family based on os_name system property`(osName: String, os: OperatingSystem.Family) {
    System.setProperty("os.name", osName)

    expect
    OperatingSystem.getCurrent().family == os

    where
    osName               ; os
    "Windows 7"          ; OperatingSystem.Family.WINDOWS
    "Ubuntu Linux"       ; OperatingSystem.Family.LINUX
    "Mac OS 10.8.2"      ; OperatingSystem.Family.MAC_OS
    "Some SunOS version" ; OperatingSystem.Family.SOLARIS
    "FreeBSD 9.0"        ; OperatingSystem.Family.OTHER
  }
}
