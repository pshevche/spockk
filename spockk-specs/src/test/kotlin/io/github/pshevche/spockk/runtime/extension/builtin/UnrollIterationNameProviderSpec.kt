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

package io.github.pshevche.spockk.runtime.extension.builtin

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.Stub
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.returns
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.where
import org.spockframework.runtime.SpockAssertionError
import org.spockframework.runtime.extension.builtin.UnrollIterationNameProvider
import org.spockframework.runtime.model.IterationInfo
import spock.lang.Issue
import spock.lang.Specification

class UnrollIterationNameProviderSpec : Specification() {

  @Issue("https://github.com/spockframework/spock/issues/237")
  @MigratedFrom(
    "org.spockframework.runtime.extension.builtin.UnrollIterationNameProviderSpec#regex-like data values are substituted correctly (i.e. literally)"
  )
  fun `regex-like data values are substituted correctly (i_e_ literally)`(value: Any?, name: String) {
    given
    val nameGenerator = UnrollIterationNameProvider(null, "foo #dataVar bar", false)
    val iterationInfo = Stub(IterationInfo::class.java) {
      getIterationIndex() returns 0
      getDataVariables() returns mapOf("dataVar" to value)
    }

    expect
    nameGenerator.getName(iterationInfo) == name

    where
    value                                ; name
    "$"                                  ; "foo $ bar"
    "#([a-zA-Z_\\$][\\w\\$]*)"            ; "foo #([a-zA-Z_\\$][\\w\\$]*) bar"
  }

  @MigratedFrom(
    "org.spockframework.runtime.extension.builtin.UnrollIterationNameProviderSpec#data values are converted to strings in Groovy style"
  )
  fun `data values are converted to strings in Groovy style`(value: Any?, name: String) {
    given
    val nameGenerator = UnrollIterationNameProvider(null, "foo #dataVar bar", false)
    val iterationInfo = Stub(IterationInfo::class.java) {
      getIterationIndex() returns 0
      getDataVariables() returns mapOf("dataVar" to value)
    }

    expect
    nameGenerator.getName(iterationInfo) == name

    where
    value                      ; name
    listOf(1, 2, 3)            ; "foo [1, 2, 3] bar"
    linkedMapOf("a" to 1, "b" to 2) ; "foo [a:1, b:2] bar"
  }

  @MigratedFrom(
    "org.spockframework.runtime.extension.builtin.UnrollIterationNameProviderSpec#missing variables are rendered as #Error:dataVars"
  )
  fun `missing variables are rendered as #Error_dataVars`() {
    given
    val nameGenerator = UnrollIterationNameProvider(null, "foo #dataVars bar", false)
    val iterationInfo = Stub(IterationInfo::class.java) {
      getIterationIndex() returns 0
      getDataVariables() returns mapOf("dataVar" to "1")
    }

    expect
    nameGenerator.getName(iterationInfo) == "foo #Error:dataVars bar"
  }

  @MigratedFrom(
    "org.spockframework.runtime.extension.builtin.UnrollIterationNameProviderSpec#exceptions during variable eval are rendered as #Error:dataVars"
  )
  fun `exceptions during variable eval are rendered as #Error_dataVars`() {
    given
    val nameGenerator = UnrollIterationNameProvider(null, "foo #dataVar.foo bar", false)
    val iterationInfo = Stub(IterationInfo::class.java) {
      getIterationIndex() returns 0
      getDataVariables() returns mapOf("dataVar" to "1")
    }

    expect
    nameGenerator.getName(iterationInfo) == "foo #Error:dataVar.foo bar"
  }

  @Issue("https://github.com/spockframework/spock/issues/767")
  @MigratedFrom(
    "org.spockframework.runtime.extension.builtin.UnrollIterationNameProviderSpec#missing variables throw an exception if validateExpressions is set to true"
  )
  fun `missing variables throw an exception if validateExpressions is set to true`() {
    given
    val nameGenerator = UnrollIterationNameProvider(null, "foo #dataVars bar", true)

    `when`
    nameGenerator.getName(Stub(IterationInfo::class.java))

    then
    val e = thrown(SpockAssertionError::class.java)
    e.message == "Error in @Unroll, could not find matching variable for expression: dataVars"
    e.cause == null
  }

  @Issue("https://github.com/spockframework/spock/issues/767")
  @MigratedFrom(
    "org.spockframework.runtime.extension.builtin.UnrollIterationNameProviderSpec#exceptions during variable eval throw an exception if validateExpressions is set to true"
  )
  fun `exceptions during variable eval throw an exception if validateExpressions is set to true`() {
    given
    val nameGenerator = UnrollIterationNameProvider(null, "foo #dataVar.foo bar", true)
    val iterationInfo = Stub(IterationInfo::class.java) {
      getIterationIndex() returns 0
      getDataVariables() returns mapOf("dataVar" to "1")
    }

    `when`
    nameGenerator.getName(iterationInfo)

    then
    val e = thrown(SpockAssertionError::class.java)
    e.message == "Error in @Unroll expression: dataVar.foo"
    e.cause!!.message == "No such property: foo for class: java.lang.String"
  }
}
