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

package io.github.pshevche.spockk.compilation.condition

import io.github.pshevche.spockk.compilation.BaseCompilationTest
import io.github.pshevche.spockk.compilation.TestDataFactory.specWithFeatureBody
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class ExceptionConditionValidationTest : BaseCompilationTest() {

  fun `rejects a then block with more than one exception condition`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
          io.github.pshevche.spockk.lang.`when`
          "".substring(5)

          io.github.pshevche.spockk.lang.then
          thrown(StringIndexOutOfBoundsException::class.java)
          noExceptionThrown()
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("A 'then' block may only have a single exception condition")
  }

  fun `rejects a captured thrown() alongside a different bare notThrown()`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
          io.github.pshevche.spockk.lang.`when`
          "".substring(5)

          io.github.pshevche.spockk.lang.then
          val e = thrown(StringIndexOutOfBoundsException::class.java)
          notThrown(IllegalArgumentException::class.java)
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("A 'then' block may only have a single exception condition")
  }

  fun `rejects the same thrown() call appearing twice`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
          io.github.pshevche.spockk.lang.`when`
          "".substring(5)

          io.github.pshevche.spockk.lang.then
          thrown(StringIndexOutOfBoundsException::class.java)
          thrown(StringIndexOutOfBoundsException::class.java)
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("A 'then' block may only have a single exception condition")
  }

  fun `accepts a single exception condition alongside an ordinary condition`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
          io.github.pshevche.spockk.lang.`when`
          "".substring(5)

          io.github.pshevche.spockk.lang.then
          val e: StringIndexOutOfBoundsException = thrown(StringIndexOutOfBoundsException::class.java)
          e.message != null
          """
            .trimIndent()
        )
      )

    then
    result.isSuccess()
  }
}
