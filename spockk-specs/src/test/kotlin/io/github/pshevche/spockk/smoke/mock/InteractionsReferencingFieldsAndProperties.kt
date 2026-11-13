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
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

class InteractionsReferencingFieldsAndProperties : Specification() {
  private val aField = 1
  val aProperty = "abc"

  @MigratedFrom("org.spockframework.smoke.mock.InteractionsReferencingFieldsAndProperties#interactions can reference fields")
  fun `interactions can reference fields`() {
    val oracle = Mock(Oracle::class.java)

    `when`
    val result = oracle.askQuestion(aField)

    then
    oracle.askQuestion(aField) returns aField
    result == aField
  }

  @MigratedFrom(
    "org.spockframework.smoke.mock.InteractionsReferencingFieldsAndProperties#interactions can reference properties"
  )
  fun `interactions can reference properties`() {
    val oracle = Mock(Oracle::class.java)

    `when`
    val result = oracle.askQuestion(aProperty)

    then
    oracle.askQuestion(aProperty) returns aProperty
    result == aProperty
  }
}

interface Oracle {
  fun askQuestion(q: Any?): Any?
}
