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

@file:Suppress("unused", "UnusedReceiverParameter", "UNUSED_PARAMETER")

package io.github.pshevche.spockk.lang

private fun throwIllegalInteractionUsageException(label: String): Nothing = throw UnsupportedOperationException(
  "'$label' should only be used inside a 'then'/'expect' block interaction, or a 'Mock'/'Stub' builder block"
)

/**
 * Matches any argument value, mirroring Spock's `_`. `T` is inferred from the parameter it's passed
 * for; a reified generic isn't needed here since the compiler plugin never actually calls this body.
 */
fun <T> any(): T = throwIllegalInteractionUsageException("any")

/**
 * Matches any method call on the receiving mock, regardless of name or arguments - mirrors Spock's
 * `obj._(...)` wildcard method name. Standalone (`1 * obj.anyMethod()`) or via [noMoreInteractions].
 */
fun <T> T.anyMethod(): Nothing = throwIllegalInteractionUsageException("anyMethod")

/**
 * Cardinality prefix for an interaction: `N * target.method(args)`. Kept verbatim from Spock's own
 * syntax - the call's real resolved return type flows straight through this marker, so it type-checks
 * legitimately. The compiler plugin deletes the whole statement and rebuilds it from the wrapped call.
 */
operator fun <T> Int.times(call: T): T = throwIllegalInteractionUsageException("*")

/** Range cardinality prefix: `(a..b) * target.method(args)`. */
operator fun <T> IntRange.times(call: T): T = throwIllegalInteractionUsageException("*")

/**
 * Configures a stub's side effect, present tense - reads naturally in a `given:`/`Stub{}` block.
 * Semantically identical to [did]; the two names exist purely to read naturally in the block each
 * targets.
 */
infix fun <T> T.does(block: () -> Unit): T = throwIllegalInteractionUsageException("does")

/**
 * Asserts a mock's side effect already ran, past tense - reads naturally in a `then:`/`expect:`
 * block. Semantically identical to [does].
 */
infix fun <T> T.did(block: () -> Unit): T = throwIllegalInteractionUsageException("did")

/**
 * Configures a stub's return value, present tense - reads naturally in a `given:`/`Stub{}` block.
 * Semantically identical to [returned].
 */
infix fun <T> T.returns(value: T): T = throwIllegalInteractionUsageException("returns")

/**
 * Asserts the value a mock already returned, past tense - reads naturally in a `then:`/`expect:`
 * block. Semantically identical to [returns].
 */
infix fun <T> T.returned(value: T): T = throwIllegalInteractionUsageException("returned")

/**
 * Matches any argument value (like [any]) while also recording it onto [slot], readable via
 * [CapturedArg.captured] once the interaction's invocation has happened.
 */
fun <T> capture(slot: CapturedArg<T>): T = throwIllegalInteractionUsageException("capture")

/**
 * Sugar for asserting no interaction happened on [mocks] beyond the ones already declared earlier in
 * the same scope - mirrors Mockito's `verifyNoMoreInteractions`/MockK's `confirmVerified`. Equivalent
 * to `0 * mock.anyMethod()` for each mock.
 */
fun noMoreInteractions(vararg mocks: Any?): Unit = throwIllegalInteractionUsageException("noMoreInteractions")
