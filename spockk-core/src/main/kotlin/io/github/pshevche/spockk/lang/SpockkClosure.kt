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
 * `capture` to hand to Spock's own `InteractionBuilder.addCodeArg`/`addCodeResponse`
 * (`org.spockframework.mock.constraint.CodeArgumentConstraint`/
 * `org.spockframework.mock.response.CodeResponseGenerator`), which invoke the closure via
 * `Closure.call(Object...)`.
 *
 * `doCall` is declared with a Kotlin `vararg` - confirmed empirically (Spock's own source only
 * documents the outer contract, not Groovy's closure-dispatch mechanics) to be the one shape
 * Groovy's `Closure.call()` dispatch accepts regardless of how many arguments the real call site
 * supplies. A `Closure` subclass with a truly zero-arg `doCall()` throws Groovy's own
 * `MissingMethodException` the instant Spock invokes it with a real argument list (which
 * `CodeResponseGenerator`/`CodeArgumentConstraint` both do for any stubbed method that takes
 * arguments); a `vararg doCall` accepts 0, 1, or N arguments uniformly, because Groovy's method
 * lookup matches it as a real Java varargs method. One consequence: `getParameterTypes()` (which
 * `CodeResponseGenerator` reflects on to decide whether to pass the raw `IMockInvocation` instead of
 * the invocation's argument list) reports a single `Object[]` parameter here, never `IMockInvocation`
 * - fine, since this class only ever backs `does`/`did` (Kotlin lambdas with no `IMockInvocation`
 * parameter to receive) and `capture` (a per-argument predicate that only needs the one matched
 * argument value).
 */
internal class SpockkClosure<R>(
  private val action: (Array<out Any?>) -> R
) : Closure<R>(null, null) {

  @Suppress("unused") // invoked reflectively by Groovy's Closure.call() dispatch, matched by arity alone
  fun doCall(vararg args: Any?): R = action(args)
}

/**
 * Adapts a `does`/`did` block (a Kotlin `() -> Unit` lambda, already a real function value by the
 * time the compiler plugin sees it - no lambda synthesis needed on the IR side) to the `Closure`
 * [org.spockframework.mock.runtime.InteractionBuilder.addCodeResponse] expects.
 */
internal fun responseClosure(block: () -> Unit): Closure<Any?> = SpockkClosure {
  block()
  null
}

/**
 * Adapts `capture(slot)` to the `Closure` [org.spockframework.mock.runtime.InteractionBuilder.addCodeArg]
 * expects: records the single matched argument onto [slot] and always matches (Spock's own
 * `CodeArgumentConstraint.isSatisfiedBy` only looks at whether the closure throws, never at its
 * return value - confirmed against source - so there's nothing to signal beyond not throwing).
 */
internal fun <T> captureClosure(slot: CapturedArg<T>): Closure<Any?> = SpockkClosure { args ->
  @Suppress("UNCHECKED_CAST")
  slot.set(args[0] as T)
  null
}
