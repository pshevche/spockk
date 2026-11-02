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
import io.github.pshevche.spockk.fixtures.runtime.EngineTestKitUtils.execute
import io.github.pshevche.spockk.fixtures.runtime.samples.mock.TooManyInvocationsSpecs
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import spock.lang.Specification

class TooManyInvocations : Specification() {

  @MigratedFrom("org.spockframework.smoke.mock.TooManyInvocations#shows matched invocations, ordered by last occurrence")
  fun `shows matched invocations, ordered by last occurrence`() {
    `when`
    val message = failureMessageOf("shows matched invocations, ordered by last occurrence")

    then
    message == normalize(
      """
      Too many invocations for:

      3 * list.add(any())   (4 invocations)

      Matching invocations (ordered by last occurrence):

      2 * list.add(2)   <-- this triggered the error
      1 * list.add(4)
      1 * list.add(1)
      """
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooManyInvocations#scope-level unmatched invocations are not shown without unsatisfied interactions")
  fun `scope-level unmatched invocations are not shown without unsatisfied interactions`() {
    `when`
    val message = failureMessageOf("scope-level unmatched invocations are not shown without unsatisfied interactions")

    then
    message == normalize(
      """
      Too many invocations for:

      0 * list.add(1)   (1 invocation)

      Matching invocations (ordered by last occurrence):

      1 * list.add(1)   <-- this triggered the error
      """
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooManyInvocations#unmatched invocations on different methods are excluded")
  fun `unmatched invocations on different methods are excluded`() {
    `when`
    val message = failureMessageOf("unmatched invocations on different methods are excluded")

    then
    message == normalize(
      """
      Too many invocations for:

      0 * list.add(1)   (1 invocation)

      Matching invocations (ordered by last occurrence):

      1 * list.add(1)   <-- this triggered the error
      """
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooManyInvocations#both unmatched invocations and unsatisfied interactions are shown together")
  fun `both unmatched invocations and unsatisfied interactions are shown together`() {
    `when`
    val message = failureMessageOf("both unmatched invocations and unsatisfied interactions are shown together")

    then
    message == normalize(
      """
      Too many invocations for:

      0 * list.add(1)   (1 invocation)

      Matching invocations (ordered by last occurrence):

      1 * list.add(1)   <-- this triggered the error

      Unmatched invocations (ordered by similarity):

      1 * list.add(42)   (0 invocations)
      One or more arguments(s) didn't match:
      0: argument == expected
         |        |  |
         1        |  42
                  false
      """
    )
  }

  @MigratedFrom("org.spockframework.smoke.mock.TooManyInvocations#no unmatched section when no relevant context exists")
  fun `no unmatched section when no relevant context exists`() {
    `when`
    val message = failureMessageOf("no unmatched section when no relevant context exists")

    then
    message == normalize(
      """
      Too many invocations for:

      0 * list.add(1)   (1 invocation)

      Matching invocations (ordered by last occurrence):

      1 * list.add(1)   <-- this triggered the error
      """
    )
  }

  private fun failureMessageOf(feature: String): String {
    val failure = execute(selectMethod(TooManyInvocationsSpecs::class.java, feature))
      .failed().list().single()
      .getRequiredPayload(TestExecutionResult::class.java)
      .throwable.orElseThrow()
    return normalize(failure.message ?: "")
  }

  private fun normalize(text: String): String =
    text.trimIndent().trim().replace(Regex("(?m)^ +$"), "")
}
