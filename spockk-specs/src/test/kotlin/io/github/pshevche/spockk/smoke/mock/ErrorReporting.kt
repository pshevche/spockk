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

package io.github.pshevche.spockk.smoke.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Mock
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.times
import io.github.pshevche.spockk.lang.`when`
import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.mock.TooManyInvocationsError
import spock.lang.FailsWith
import spock.lang.Specification

class ErrorReporting : Specification() {

  val collaborator: ErrorReportingCollaborator = Mock(ErrorReportingCollaborator::class.java)
  val swallower = ErrorReportingSwallower(collaborator)

  @FailsWith(TooFewInvocationsError::class)
  @MigratedFrom(
    "org.spockframework.smoke.mock.ErrorReporting#error 'too few invocations' is reported even if object under test swallows exception"
  )
  fun `error 'too few invocations' is reported even if object under test swallows exception`() {
    `when`
    swallower.swallow()

    then
    3 * collaborator.work()
  }

  @FailsWith(TooManyInvocationsError::class)
  @MigratedFrom(
    "org.spockframework.smoke.mock.ErrorReporting#error 'too many invocations' is reported even if object under test swallows exception"
  )
  fun `error 'too many invocations' is reported even if object under test swallows exception`() {
    `when`
    swallower.swallow()

    then
    1 * collaborator.work()
  }
}

class ErrorReportingSwallower(private val collaborator: ErrorReportingCollaborator) {
  @Suppress("TooGenericExceptionCaught", "SwallowedException")
  fun swallow() {
    try {
      collaborator.welcome()
      collaborator.work()
      collaborator.work()
    } catch (ignored: Throwable) {
      // The collaborator's invocation errors must still surface through the mock.
    }
  }
}

interface ErrorReportingCollaborator {
  fun welcome()

  fun work()
}
