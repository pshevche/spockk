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

package io.github.pshevche.spockk.fixtures.runtime.samples.condition

import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification
import java.io.IOException

class BareThrownWithoutTypeSpec : Specification() {
  fun `feature`() {
    `when`
    true

    then
    thrown<Exception>()
  }
}

class BareThrownNullSpec : Specification() {
  fun `feature`() {
    `when`
    true

    then
    thrown<Exception>(null)
  }
}

class AssignedThrownNullSpec : Specification() {
  fun `feature`() {
    `when`
    true

    then
    val e = thrown<Exception>(null)
  }
}

class ExplicitAndInferredTypeSpec : Specification() {
  fun `feature`() {
    `when`
    throw Exception()

    then
    val e: Exception = thrown(IOException::class.java)
  }
}

class FieldAssignmentThrownSpec : Specification() {
  private var ioException: IOException? = null

  fun `feature`() {
    `when`
    throw RuntimeException()

    then
    ioException = thrown()
  }
}

class VariableAssignmentThrownSpec : Specification() {
  fun `feature`() {
    var ioException: IOException?

    `when`
    throw RuntimeException()

    then
    ioException = thrown()
  }
}
