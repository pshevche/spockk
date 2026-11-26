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
import spock.lang.Specification
import spock.util.Exceptions
import java.io.FileNotFoundException

class ExceptionsSpec : Specification() {

  private val exception = RuntimeException()
  private val cause = IllegalStateException()
  private val cause2 = FileNotFoundException()

  fun setup() {
    exception.initCause(cause)
    cause.initCause(cause2)
  }

  @MigratedFrom("spock.util.ExceptionsSpec#get root cause")
  fun `get root cause`() {
    expect
    Exceptions.getRootCause(exception) == cause2
    Exceptions.getRootCause(cause) == cause2
    Exceptions.getRootCause(cause2) == cause2
  }

  @MigratedFrom("spock.util.ExceptionsSpec#get cause chain")
  fun `get cause chain`() {
    expect
    Exceptions.getCauseChain(exception) == listOf(exception, cause, cause2)
    Exceptions.getCauseChain(cause) == listOf(cause, cause2)
    Exceptions.getCauseChain(cause2) == listOf(cause2)
  }
}
