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

package io.github.pshevche.spockk.mock

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.setup
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import spock.lang.Specification
import spock.mock.MockMakerId

class MockMakerIdSpec : Specification() {

  @MigratedFrom("spock.mock.MockMakerIdSpec#Valid creation")
  fun `Valid creation`() {
    expect
    MockMakerId("valid").toString() == "valid"
  }

  @MigratedFrom("spock.mock.MockMakerIdSpec#Invalid creation")
  fun `Invalid creation`(input: String?, expectedErrorMessage: String) {
    `when`
    MockMakerId(input)

    then
    val ex: IllegalArgumentException = thrown()
    ex.message == expectedErrorMessage

    where
    input          ; expectedErrorMessage
    null           ; "The ID is null."
    ""             ; "The ID '' is invalid. A valid ID must comply with the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
    "with space"   ; "The ID 'with space' is invalid. A valid ID must comply with the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
    "UpperCase"    ; "The ID 'UpperCase' is invalid. A valid ID must comply with the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
    "0numberStart" ; "The ID '0numberStart' is invalid. A valid ID must comply with the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
    "half-kebab-"  ; "The ID 'half-kebab-' is invalid. A valid ID must comply with the pattern: ^[a-z][a-z0-9-]*+(?<!-)\$"
  }

  @MigratedFrom("spock.mock.MockMakerIdSpec#Equals/hashcode")
  fun `Equals-hashcode`() {
    setup
    val id1 = MockMakerId("id1")
    val id1Copy = MockMakerId("id1")
    val id2 = MockMakerId("id2")

    expect
    id1.hashCode() == id1Copy.hashCode()
    id1.hashCode() != id2.hashCode()
    id1 == id1
    id1 == id1Copy
    id1 != id2
  }

  @MigratedFrom("spock.mock.MockMakerIdSpec#compareTo")
  fun compareTo() {
    setup
    val id1 = MockMakerId("id1")
    val id1Copy = MockMakerId("id1")
    val id2 = MockMakerId("id2")

    expect
    id1.compareTo(id1) == 0
    id1.compareTo(id1Copy) == 0
    id1.compareTo(id2) == -1
    id2.compareTo(id1) == 1
  }
}
