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

/**
 * Adapts a Kotlin lambda to a real `groovy.lang.Closure`, for generated code behind `does`/`did`/
 * `capture` to hand to Spock's own `InteractionBuilder.addCodeArg`/`addCodeResponse`. `doCall` must
 * be `vararg`: a fixed zero-arity `doCall()` throws Groovy's `MissingMethodException` the moment
 * Spock invokes it with real arguments (confirmed empirically against Spock's dispatch).
 */
internal class SpockkClosure<R>(
  private val action: (Array<out Any?>) -> R
) : Closure<R>(null, null) {

  @Suppress("unused") // invoked reflectively by Groovy's Closure.call() dispatch, matched by arity alone
  fun doCall(vararg args: Any?): R = action(args)
}

/** Adapts a `does`/`did` block to the `Closure` [org.spockframework.mock.runtime.InteractionBuilder.addCodeResponse] expects. */
internal fun responseClosure(block: () -> Unit): Closure<Any?> = SpockkClosure {
  block()
  null
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
