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
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import org.spockframework.mock.runtime.MockMakerConfiguration
import org.spockframework.runtime.RunContext
import org.spockframework.util.IThrowableFunction
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties
import java.io.File

class RunContextSpec : Specification() {

  @MigratedFrom("org.spockframework.runtime.RunContextSpec#initial run context is named 'default'")
  fun `initial run context is named 'default'`() {
    expect
    RunContext.get().name == "default"
  }

  @RestoreSystemProperties
  @MigratedFrom(
    "org.spockframework.runtime.RunContextSpec#Spock user home of initial run context can be configured via system property or environment variable"
  )
  fun `Spock user home of initial run context can be configured via system property or environment variable`() {
    given
    val dir = File("foo", "bar")
    System.setProperty("spock.user.home", dir.path)

    expect
    createBottomContext().spockUserHome == dir
  }

  @MigratedFrom("org.spockframework.runtime.RunContextSpec#child context has its own name and Spock user home")
  fun `child context has its own name and Spock user home`() {
    given
    val dir = File("new", "home")

    expect
    RunContext.withNewContext<Boolean, RuntimeException>(
      "new name",
      dir,
      null,
      emptyList(),
      listOf(MockMakerConfiguration::class.java),
      false,
      IThrowableFunction {
        val context = RunContext.get()
        context.name == "new name" && context.spockUserHome == dir
      }
    )

    and
    RunContext.get().name == "default"
    RunContext.get().spockUserHome != dir
  }

  // createBottomContext is package-private; Groovy ignores that, Kotlin needs reflection.
  private fun createBottomContext(): RunContext =
    RunContext::class.java.getDeclaredMethod("createBottomContext")
      .apply { isAccessible = true }
      .invoke(null) as RunContext
}
