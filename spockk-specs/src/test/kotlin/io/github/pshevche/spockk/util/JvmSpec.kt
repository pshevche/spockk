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
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.where
import spock.lang.Isolated
import spock.lang.Specification
import spock.util.environment.Jvm
import spock.util.environment.RestoreSystemProperties

@Isolated
@RestoreSystemProperties
class JvmSpec : Specification() {

  @MigratedFrom("spock.util.JvmSpec#can check for Java 8 version (old scheme)")
  fun `can check for Java 8 version (old scheme)`(minor: Int) {
    System.setProperty("java.specification.version", "1.$minor")
    val jvm = Jvm.getCurrent()

    expect
    jvm.isJava8 == (minor == 8)

    where
    variable(minor).from((7..8).toList())
  }

  @MigratedFrom("spock.util.JvmSpec#can check for Java 8 version (old scheme) (given major version)")
  fun `can check for Java 8 version (old scheme) (given major version)`(minor: Int) {
    System.setProperty("java.specification.version", "1.$minor")
    val jvm = Jvm.getCurrent()

    expect
    jvm.isJavaVersion(minor) == (minor == 8)

    where
    variable(minor).from((7..8).toList())
  }

  @MigratedFrom("spock.util.JvmSpec#can check for Java 8 version compatibility (old scheme)")
  fun `can check for Java 8 version compatibility (old scheme)`(minor: Int) {
    System.setProperty("java.specification.version", "1.$minor")
    val jvm = Jvm.getCurrent()

    expect
    jvm.isJava8Compatible == (minor >= 8)

    where
    variable(minor).from((7..8).toList())
  }

  @MigratedFrom("spock.util.JvmSpec#can check for Java 8 version compatibility (old scheme) (given major version)")
  fun `can check for Java 8 version compatibility (old scheme) (given major version)`(minor: Int) {
    System.setProperty("java.specification.version", "1.$minor")
    val jvm = Jvm.getCurrent()

    expect
    jvm.isJavaVersionCompatible(minor) == (minor >= 8)

    where
    variable(minor).from((7..8).toList())
  }

  @MigratedFrom("spock.util.JvmSpec#can check for Java version")
  fun `can check for Java version`(major: Int) {
    System.setProperty("java.specification.version", "$major")
    val jvm = Jvm.getCurrent()

    expect
    for (i in 8..23) {
      assert(flag(jvm, "isJava$i") == (i == major))
    }

    where
    variable(major).from((9..23).toList())
  }

  @MigratedFrom("spock.util.JvmSpec#can check for Java version compatibility")
  fun `can check for Java version compatibility`(major: Int) {
    System.setProperty("java.specification.version", "$major")
    val jvm = Jvm.getCurrent()

    expect
    for (i in 8..23) {
      assert(flag(jvm, "isJava${i}Compatible") == (i <= major))
    }

    where
    variable(major).from((9..23).toList())
  }

  @MigratedFrom("spock.util.JvmSpec#can check for Java version (given major version)")
  fun `can check for Java version (given major version)`(major: Int) {
    System.setProperty("java.specification.version", "$major")
    val jvm = Jvm.getCurrent()

    expect
    for (i in 8..23) {
      assert(jvm.isJavaVersion(i) == (i == major))
    }

    where
    variable(major).from((9..23).toList())
  }

  @MigratedFrom("spock.util.JvmSpec#can check for Java version compatibility (given major version)")
  fun `can check for Java version compatibility (given major version)`(major: Int) {
    System.setProperty("java.specification.version", "$major")
    val jvm = Jvm.getCurrent()

    expect
    for (i in 8..23) {
      assert(jvm.isJavaVersionCompatible(i) == (i <= major))
    }

    where
    variable(major).from((9..23).toList())
  }

  private fun flag(jvm: Jvm, getter: String): Boolean = Jvm::class.java.getMethod(getter).invoke(jvm) as Boolean
}
