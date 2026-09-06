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
import io.github.pshevche.spockk.compilation.BaseCompilationTest
import io.github.pshevche.spockk.compilation.TransformationSample.Companion.sampleFromResource
import io.github.pshevche.spockk.fixtures.compilation.CompilationUtils.transform
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.verifyAll
import io.github.pshevche.spockk.lang.`when`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * Covers the when/then interaction scoping shape
 * ([io.github.pshevche.spockk.compilation.transformer.interaction.InteractionScopeRewriter]), which
 * *moves* interaction-building statements out of the `then` block rather than rewriting in place.
 * Runtime pass/fail behavior is covered separately by
 * [io.github.pshevche.spockk.smoke.mock.InteractionsSmokeTest].
 */
@OptIn(ExperimentalCompilerApi::class)
class InteractionCompilationTest : BaseCompilationTest() {

  fun `when block is bracketed with enterScope-leaveScope around a paired interaction`() {
    expect
    assertTransformation(sampleFromResource("interaction/BasicCardinality"))
  }

  /**
   * Asserted structurally (call ordering) rather than via exact-dump comparison, unlike the sibling
   * test above: `thrown(Type)`'s own call-site rewrite carries a Java `@FlexibleNullability`
   * platform-type marker that ordinary hand-written Kotlin source can't reproduce byte-for-byte -
   * the same reason [io.github.pshevche.spockk.compilation.condition.ExceptionConditionsCompilationTest]
   * doesn't snapshot-test `thrown(Type)` either. This test's own concern (interaction scope nesting
   * *outside* the exception try/catch) is unaffected by that and unrelated to interactions themselves.
   */
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
