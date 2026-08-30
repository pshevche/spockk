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

package io.github.pshevche.spockk.lang

import groovy.lang.Closure
import org.spockframework.mock.IMockInvocation

/**
 * Adapts a Kotlin lambda to a real `groovy.lang.Closure`, for generated code behind `capture` to
 * hand to Spock's own `InteractionBuilder.addCodeArg`. `doCall` must be `vararg`: a fixed zero-arity
 * `doCall()` throws Groovy's `MissingMethodException` the moment Spock invokes it with a real
 * argument (confirmed empirically against Spock's dispatch).
 */
internal class SpockkClosure<R>(
  private val action: (Array<out Any?>) -> R
) : Closure<R>(null, null) {

  @Suppress("unused") // invoked reflectively by Groovy's Closure.call() dispatch, matched by arity alone
  fun doCall(vararg args: Any?): R = action(args)
}

/**
 * Adapts a Kotlin lambda to a `groovy.lang.Closure` for `InteractionBuilder.addCodeResponse` -
 * `doCall`'s single, non-vararg `IMockInvocation` parameter is what `CodeResponseGenerator` reflects
 * on to decide to pass the real invocation object rather than its raw `Object[]` arguments as one
 * value (confirmed empirically: a vararg `doCall` received that array as a single wrapped element).
 */
internal class SpockkResponseClosure<R>(
  private val action: (IMockInvocation) -> R
) : Closure<R>(null, null) {

  @Suppress("unused") // invoked reflectively by Groovy's Closure.call() dispatch
  fun doCall(invocation: IMockInvocation): R = action(invocation)
}

/** Adapts a `does`/`did` block to the `Closure` [org.spockframework.mock.runtime.InteractionBuilder.addCodeResponse] expects. */
internal fun <R> responseClosure(block: (List<Any?>) -> R): Closure<Any?> = SpockkResponseClosure { invocation ->
  block(invocation.arguments)
}

/**
 * Adapts `capture(slot)` to the `Closure` [org.spockframework.mock.runtime.InteractionBuilder.addCodeArg]
 * expects: records the matched argument and always matches (Spock's `CodeArgumentConstraint` only
 * checks whether the closure throws, never its return value).
 */
internal fun <T> captureClosure(slot: CapturedArg<T>): Closure<Any?> = SpockkClosure { args ->
  @Suppress("UNCHECKED_CAST")
  slot.set(args[0] as T)
  null
}
