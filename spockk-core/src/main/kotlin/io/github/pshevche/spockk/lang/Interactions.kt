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

@file:Suppress("unused", "UnusedReceiverParameter", "UNUSED_PARAMETER", "FunctionNaming")

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
 *
 * Returns `Unit`, not `Nothing`: `1 * obj.anyMethod()` needs `Int.times<T>(call: T): T` to infer a
 * concrete `T` - a `Nothing`-typed argument is assignable to every built-in numeric `Int.times`
 * overload equally (`Nothing` is a subtype of `Int`, `Long`, etc.), which makes that call ambiguous
 * between this marker and those built-ins; `Unit` isn't assignable to any of them, so only this
 * marker's overload ever applies.
 */
fun <T> T.anyMethod() {
  throwIllegalInteractionUsageException("anyMethod")
}

/**
 * Cardinality prefix for an interaction: `N * target.method(args)`. Kept verbatim from Spock's own
 * syntax - the call's real resolved return type flows straight through this marker, so it type-checks
 * legitimately. The compiler plugin deletes the whole statement and rebuilds it from the wrapped call.
 */
operator fun <T> Int.times(call: T): T = throwIllegalInteractionUsageException("*")

/** Range cardinality prefix: `(a..b) * target.method(args)`. */
operator fun <T> IntRange.times(call: T): T = throwIllegalInteractionUsageException("*")

/**
 * Configures a stub's behavior, present tense - reads naturally in a `given:`/`Stub{}` block.
 * [block] receives the real invocation's arguments, positionally, and its result becomes the mock's
 * response - so it doubles as a side effect (ignore the argument, return `Unit`) or a response
 * computed from the argument (e.g. `setName(any()) does { args -> greeting = "Hi, ${args[0]}" }`).
 * Semantically identical to [did]; the two names exist purely to read naturally in the block each
 * targets.
 */
infix fun <T> T.does(block: (List<Any?>) -> T): T = throwIllegalInteractionUsageException("does")

/**
 * Asserts a mock's behavior already ran, past tense - reads naturally in a `then:`/`expect:` block.
 * Semantically identical to [does].
 */
infix fun <T> T.did(block: (List<Any?>) -> T): T = throwIllegalInteractionUsageException("did")

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
 * Sugar for asserting no interaction happened on [mocks] beyond the ones already declared earlier in
 * the same scope - mirrors Mockito's `verifyNoMoreInteractions`/MockK's `confirmVerified`. Equivalent
 * to `0 * mock.anyMethod()` for each mock.
 */
fun noMoreInteractions(vararg mocks: Any?): Unit = throwIllegalInteractionUsageException("noMoreInteractions")

/**
 * `Mock`/`Stub` with a trailing builder block for eager stub/mock configuration - Spockk-only
 * overloads (different arity from the inherited, real `MockingApi.Mock(Class)`/`Stub(Class)`, so no
 * ambiguity), since a Kotlin lambda can't be passed where the real Groovy `Mock(Class, Closure)`
 * overload expects a `groovy.lang.Closure`. Each statement in [block] is itself an interaction
 * statement (`does`/`did`/`returns`/`returned`-wrapped, or bare) - registered directly, once, right
 * after the mock is constructed, not scope-wrapped like a `then:` block's interactions.
 */
fun <T> Mock(type: Class<T>, block: T.() -> Unit): T = throwIllegalInteractionUsageException("Mock")

/** @see Mock */
fun <T> Stub(type: Class<T>, block: T.() -> Unit): T = throwIllegalInteractionUsageException("Stub")

/**
 * Same as [Mock], but for spying on [instance] - an already-constructed object rather than a `Class`
 * token, since a real `Spy`, unlike `Mock`/`Stub`, delegates to a real object instead of a bytecode
 * proxy Spock builds from scratch. A single overload taking the instance directly (no `Class<T>`
 * builder-block sibling): `Spy(GreeterImpl::class.java) { ... }` and `Spy(instance: T, block)` would
 * be ambiguous at the Kotlin source level if both existed - confirmed empirically, `T` here is
 * unconstrained enough that a `Class<Foo>` argument satisfies it too, and unlike the plain 1-arg
 * `Spy(Class<T>)`/`Spy(T)` pair (where Kotlin's overload resolution does pick the more specific
 * `Class<T>` candidate), adding the second, identically-shaped `block: T.() -> Unit` parameter makes
 * the two candidates incomparable, so Kotlin reports an outright ambiguity error rather than picking
 * one. Construct the instance directly instead: `Spy(GreeterImpl()) { ... }`.
 */
fun <T> Spy(instance: T, block: T.() -> Unit): T = throwIllegalInteractionUsageException("Spy")
