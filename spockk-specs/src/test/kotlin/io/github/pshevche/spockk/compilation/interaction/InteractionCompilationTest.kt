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

package io.github.pshevche.spockk.compilation.interaction

import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import spock.lang.Specification

/**
 * Covers the when/then interaction scoping shape
 * ([io.github.pshevche.spockk.compilation.transformer.interaction.InteractionScopeRewriter]) - the
 * "trickiest architectural piece" of the feature, since it *moves* interaction-building statements
 * out of the `then` block rather than rewriting in place. Asserts structurally (ordering of the real
 * calls in the IR dump) rather than via exact-dump comparison
 * ([io.github.pshevche.spockk.compilation.BaseCompilationTest]) - `getMockController()`'s reused,
 * already implicit-notnull-wrapped dispatch-receiver sub-expressions (carried over from the original
 * `then`-block statement, not something the rewriter itself adds) can't be reproduced byte-for-byte
 * by independently hand-written Kotlin, the same reason
 * [io.github.pshevche.spockk.compilation.condition.ExceptionConditionsCompilationTest] skips exact-dump
 * testing for `thrown(Type)`'s own rewrite. Response/capture markers (`responseClosure`/
 * `captureClosure`, both `internal` to `spockk-core`) aren't reachable from this test module's own
 * hand-written source at all; those, together with runtime pass/fail behavior, are covered by
 * [io.github.pshevche.spockk.smoke.mock.InteractionsSmokeTest] instead.
 */
@OptIn(ExperimentalCompilerApi::class)
class InteractionCompilationTest : Specification() {

  fun `when block is bracketed with enterScope-leaveScope around a paired interaction`() {
    given
    val source = kotlin(
      "BasicCardinality.kt",
      """
      import io.github.pshevche.spockk.lang.times

      interface Greeter {
        fun greet(name: String)
      }

      class BasicCardinality : spock.lang.Specification() {
        fun `some feature`() {
          io.github.pshevche.spockk.lang.given
          val obj = Mock(Greeter::class.java)

          io.github.pshevche.spockk.lang.`when`
          obj.greet("Alice")

          io.github.pshevche.spockk.lang.then
          1 * obj.greet("Alice")
        }
      }
      """.trimIndent()
    )

    `when`
    val result = transform(source)
    val dump = result.irDump

    then
    verifyAll {
      result.isSuccess()
      dump.contains("fun setFixedCount")
      dump.contains("fun addEqualTarget")
      dump.contains("fun addEqualMethodName")
      dump.contains("value=\"greet\"")
      dump.contains("fun addEqualArg")
      dump.contains("value=\"Alice\"")
    }
    val enterScopeIdx = dump.indexOf("fun enterScope")
    val addInteractionIdx = dump.indexOf("fun addInteraction")
    val realGreetCallIdx = dump.indexOf("fun greet (name: kotlin.String): kotlin.Unit declared in <root>.Greeter")
    val leaveScopeIdx = dump.indexOf("fun leaveScope")

    verifyAll {
      enterScopeIdx > 0
      addInteractionIdx > enterScopeIdx
      realGreetCallIdx > addInteractionIdx
      leaveScopeIdx > realGreetCallIdx
    }
  }

  fun `interaction scope is the outer bracket around a nested exception condition`() {
    given
    val source = kotlin(
      "InteractionWithExceptionCondition.kt",
      """
      import io.github.pshevche.spockk.lang.times

      interface Greeter {
        fun greet(name: String)
      }

      class InteractionWithExceptionCondition : spock.lang.Specification() {
        fun `some feature`() {
          io.github.pshevche.spockk.lang.given
          val obj = Mock(Greeter::class.java)

          io.github.pshevche.spockk.lang.`when`
          obj.greet("Alice")

          io.github.pshevche.spockk.lang.then
          1 * obj.greet("Alice")
          thrown(IllegalStateException::class.java)
        }
      }
      """.trimIndent()
    )

    `when`
    val result = transform(source)
    val dump = result.irDump

    then
    result.isSuccess()
    val setThrownExceptionNullIdx = dump.indexOf("fun setThrownException")
    val enterScopeIdx = dump.indexOf("fun enterScope")
    val addInteractionIdx = dump.indexOf("fun addInteraction")
    val tryIdx = dump.indexOf("TRY")
    val realGreetCallIdx = dump.indexOf("fun greet (name: kotlin.String): kotlin.Unit declared in <root>.Greeter")
    val leaveScopeIdx = dump.indexOf("fun leaveScope")
    val checkExceptionThrownIdx = dump.indexOf("fun checkExceptionThrown")

    verifyAll {
      // interaction scope is the outer bracket: setThrownException(null)/enterScope/the built
      // addInteraction statement all precede the try{} wrapping the when block's own statements.
      setThrownExceptionNullIdx > 0
      enterScopeIdx > setThrownExceptionNullIdx
      addInteractionIdx > enterScopeIdx
      tryIdx > addInteractionIdx
      realGreetCallIdx > tryIdx
      // leaveScope() is the first statement of the rewritten then block, before thrown(Type)'s own
      // rewritten check.
      leaveScopeIdx > realGreetCallIdx
      checkExceptionThrownIdx > leaveScopeIdx
    }
  }
}
