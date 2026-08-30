# Interaction-Based Testing (Preview): Mock/Stub Verification and Stubbing Syntax

**Date:** 2026-08-30
**Issue:** [#204](https://github.com/pshevche/spockk/issues/204)

## Context

Spockk already lets users create Spock mocks/stubs/spies (`Mock(Type::class.java)`, `Stub(...)`, `Spy(...)`) via
`MockingApiTransformer`, which rewrites the inherited Groovy `MockingApi.Mock/Stub/Spy(Class)` calls to
`SpecInternals.MockImpl/StubImpl/SpyImpl(...)`, inferring the mock's name and type from the enclosing `val`
declaration. This works today, unconditionally enabled, and is unaffected by this feature.

What's missing is Spock's **interaction-based testing**: declaring how many times a mocked method should be called
(`1 * obj.setName(_)`), and what a stubbed method should do when called (`obj.getUsername() >> "Name"`). Spock's
Groovy compiler recognizes these as a special AST shape *before* type checking - the call is never actually
resolved or invoked, just used as a template (method name, arg matchers, response). Spockk's compiler plugin runs on
**already type-checked K2 IR**, so this exact syntax can't carry over: `_` can't adapt its static type per call
site, and `>>` isn't defined for arbitrary receiver/response type pairs. This document scopes a Kotlin-idiomatic
syntax that stays as close as possible to Spock's shape (for two reasons: familiarity for anyone coming from Spock,
and the issue's own secondary goal of a proof-of-value to share with the Spock team) while being real, type-checked
Kotlin that the existing IR-marker convention (`given`/`when`/`then`/`verify`/... - see `spockk-core/.../lang/Blocks.kt`,
`Conditions.kt`) already establishes: source-level marker functions that throw `UnsupportedOperationException` if
the compiler plugin doesn't rewrite them, so nothing silently no-ops.

Per explicit product decision (this issue): **ships enabled by default, no feature flag** - the issue's own
"disabled by default" and "depends on #201" acceptance criteria are dropped for this iteration.

## Runtime API (confirmed against Spock source, `spock-core` master branch, shaded unmodified into `spockk-core`)

Everything below is real, already-shaded Spock runtime - no new matching/verification logic gets reimplemented,
following the same principle the condition-rendering feature already established (see `AGENTS.md`,
"Condition Rendering Internals").

**`org.spockframework.mock.runtime.InteractionBuilder`** (constructor `(int line, int column, String text)`,
fluent, `build()` returns `IMockInteraction`):
```java
setFixedCount(Object count)
setRangeCount(Object minCount, Object maxCount, boolean inclusive)
addEqualTarget(Object target)
addEqualMethodName(String name)     // special-cases name.equals(Wildcard.INSTANCE.toString()) -> WildcardMethodNameConstraint
addEqualArg(Object arg)             // special-cases a Wildcard instance -> WildcardArgumentConstraint (matches anything)
addCodeArg(Closure closure)         // custom per-argument predicate, used for capture()
addConstantResponse(Object constant)
addCodeResponse(Closure closure)    // computed per-invocation response/side effect
build()
```
`org.spockframework.lang.Wildcard.INSTANCE` is the sentinel Spock's own `_` compiles to; `addEqualArg`/
`addEqualMethodName` both special-case it internally, so Spockk doesn't need to reimplement wildcard matching - it
only needs to pass the same sentinel.

**`org.spockframework.mock.runtime.MockController`** (implements `IMockController`; one instance per spec
iteration, already created and wired by Spock's own runtime the same way it already backs every `Mock()`/`Stub()`
call today - Spockk does not create or manage its lifecycle):
```java
void addInteraction(IMockInteraction interaction)   // pushes onto the currently active scope
void enterScope()   // pushes a new interaction scope
void leaveScope()   // pops the scope and calls scope.verifyInteractions() - THIS is where "too few/many invocations" throws
```
Obtained via `SpecificationContext.getMockController(): IMockController` (already-declared, package
`org.spockframework.runtime`, same class `IrSpecificationContext` already wraps for `setThrownException`).

**Compiled shape Spock's own Groovy `SpecRewriter.moveInteractions` produces** (confirmed from source, `spock-core`
`org.spockframework.compiler.SpecRewriter`), for a `when`/`then` pair where the `then` block declares interactions:
```
<statements before the when block>
mockController.enterScope()
<interaction-building statements, moved here from the then block>
<when block statements, unchanged>
mockController.leaveScope()          // inserted at the START of the then block
<then block's remaining (non-interaction) statements>
```
This is the exact shape Spockk's `ExceptionConditionRewriter`/`WhenBlockRewriter` pairing (see
`_docs/specs/2026-08-09-exception-conditions-design.md`) already established for `thrown`/`notThrown` - a `then`
block's content determines whether its paired `when` block needs special wrapping, pre-scanned via
`FeatureBody.behaviorBlocks[i+1]`. Interactions declared in a `given:`-time `Stub`/`Mock` builder block (see below)
are **not** scope-wrapped at all - they're registered once, directly, right after the mock is constructed, and live
for the rest of the iteration in whatever scope is active at that point (Spock's own default/outer scope).

## Proposed Syntax

```kotlin
given
val obj = Mock(Person::class.java)

when
obj.setName("Alice")

then
1 * obj.setName(any())              // cardinality * call; any() replaces Spock's `_`

when
val name = obj.getUsername()

then
1 * obj.getUsername() returned "Name"    // response infix in a then/expect block: past tense
```

```kotlin
given
val nameSlot = slot<String>()
val obj = Stub(Person::class.java) {
  setUsername(capture(nameSlot)) does {           // response infix in a given/Stub-block: present tense
    println("setUsername called with ${nameSlot.captured}")
  }
  getUsername() returns "Name"
}
```

### Element-by-element design

| Spock | Kotlin (this design) | Why |
|---|---|---|
| `_` (any arg) | `any()` / `any<T>()` | `T` inferred from the parameter, reified generic. `_` can't adapt its static type per call site in real Kotlin; `any()` is also the established idiom in MockK/Mockito-Kotlin, so it's not a foreign spelling. |
| `N * target.method(args)` | `N * target.method(args)` (kept verbatim) | `operator fun <T> Int.times(call: T): T` type-checks legitimately - the call's real resolved return type flows straight through the marker. The IR rewriter deletes the whole statement and rebuilds it from the extracted `IrCall`, so `method` is never actually invoked - same "throws if left unrewritten" contract as `given`/`then`. Ranges (`1..3 * ...`) use the `IntRange` overload -> `setRangeCount`. |
| `>> value` / `>> { closure }` | `returns value` / `does { }` (given/Stub-block context) and `returned value` / `did { }` (then/expect context) | Infix, not operator, so the response's type is checked against the call's real return type at compile time - something Spock's own runtime-only Groovy check can't do. Two tense-matched name pairs rather than one: `does`/`returns` read naturally when *configuring* a stub ahead of time (`given:`), `did`/`returned` read naturally when *asserting what already happened* (`then:`, past tense, after the `when:` stimulus ran). All four compile to the identical `addCodeResponse`/`addConstantResponse` call - purely a naming/readability layer, not a semantic split - so nothing stops using either pair in either block; the names just aim at the block where each reads best. |
| (Groovy `>> { closure }` doubling as both side-effect-only and computed-value) | kept as one family (`does`/`did`) rather than split into a separate "computed value" flavor | Per product decision - not introducing a third `answers`-style name for this preview; `does { ... }`'s lambda body can still be the last expression influencing the mock's return value the same way Spock's own closure-as-response does. |
| `_._` (wildcard target) | *(out of scope - see Wildcards below)* | No literal Kotlin receiver to call `.method()` on without a real target value. |
| `obj._(...)` (wildcard method name on a known target) | `obj.anyMethod()` | `fun <T> T.anyMethod(): Nothing`, a generic extension callable on any mock regardless of its real interface - type-checks trivially. The rewriter recognizes this specific symbol and emits `addEqualMethodName(Wildcard.INSTANCE.toString())` instead of a literal name. Usable standalone (`1 * obj.anyMethod()`, "exactly one call to obj total, any method") or via the `noMoreInteractions` sugar below. |
| `0 * _._` used per-mock ("no other interactions on this mock") | `noMoreInteractions(obj, ...)` | Sugar recognized by the rewriter as exactly `0 * obj.anyMethod()` per argument - matches the idiom already familiar from Mockito's `verifyNoMoreInteractions`/MockK's `confirmVerified`, without needing a real "wildcard target" primitive at all (every real-world use of `_._` this preview needs to support is per-mock, not truly cross-mock). |
| `obj.method(*_)` (any number of args, regardless of arity) | **not supported** | Genuinely infeasible: Kotlin resolves `obj.method(...)` against one specific overload at compile time, fixing arity the moment the call type-checks. Spock's own mechanism for this is Groovy's spread operator matched against a `SpreadWildcard` sentinel - a dynamic-dispatch trick with no Kotlin equivalent. `obj.anyMethod()` (any method, args irrelevant since it isn't a real call to the real method at all) covers the practical "catch-all" use case instead. |
| regex method/arg names | **not supported** | Explicitly out of scope per product decision - keeps method/property matching to equality + the one `anyMethod()` wildcard. |
| argument capture (Spock: read `IMockInvocation.arguments` inside a `>> { }` closure) | `slot<T>()` + `capture(slot)` | Spock has no dedicated capture primitive of its own (closures just read raw invocation args); this is a Spockk-only convenience, mirroring MockK's `slot`/Mockito's `ArgumentCaptor`. Maps to `addCodeArg(closure)` where the closure both matches (returns `true`, i.e. equivalent to `any()`) and records the observed value onto the given `CapturedArg<T>`. |

### Runtime pieces needed in `spockk-core`

Two categories, matching the existing convention split in `Blocks.kt`/`Conditions.kt`:

**Pure compile-time markers** (throw `UnsupportedOperationException` if reached - the compiler plugin always
replaces the whole enclosing statement, so these bodies only run if the plugin didn't):
`any<T>()`, `anyMethod<T>()` (extension), `operator fun <T> Int.times(call: T): T`, `operator fun <T> ClosedRange<Int>.times(call: T): T`,
`infix fun <T> T.does(block: () -> Unit): T`, `infix fun <T> T.did(block: () -> Unit): T`,
`infix fun <T> T.returns(value: T): T`, `infix fun <T> T.returned(value: T): T`,
`fun <T> capture(slot: CapturedArg<T>): T`, `fun noMoreInteractions(vararg mocks: Any?)`.

**Real runtime pieces** (these genuinely execute later, when a stubbed mock method is actually invoked - not
rewritten away, because they're not statements the plugin recognizes as *interaction declarations*, they're the
generated *replacement* code the rewriter emits, and the value-capture/lambda-adapter logic behind them has to
really run):
- `class CapturedArg<T>` with a `val captured: T` (throws `IllegalStateException` if read before any invocation
  captured a value) and an internal setter the generated `addCodeArg` closure calls.
- A small `groovy.lang.Closure` adapter wrapping a Kotlin lambda, used internally by the generated code for
  `does`/`did`/`capture` (`does { }`'s lambda, `capture(slot)`'s match-and-record predicate). `groovy.lang.Closure`
  is already a required transitive runtime dependency (see `AGENTS.md`, "Condition Rendering Internals" - Spock's
  own `Specification`/`MockingApi` already expose it in their bytecode), so this isn't a new dependency, just a new
  small adapter class. Groovy's closure-dispatch mechanics (how `CodeResponseGenerator`/`CodeArgumentConstraint`
  decide what to pass the closure - confirmed for responses: reflects on `closure.getParameterTypes()`, passes the
  `IMockInvocation` if the sole declared parameter is that type, otherwise `invocation.getArguments()`) need to be
  matched precisely; exact subclassing mechanics (what method Groovy's `Closure.call()` dispatches to - `doCall`,
  by Groovy convention) get confirmed empirically during implementation, the same way condition-rendering's
  slot-packing algorithm was verified against real `Condition.getRendering()` output rather than derived by hand.

### `Mock`/`Stub`/`Spy` builder-block syntax (given-block, eager registration)

Spock's real `MockingApi` has a `Stub(Class, Closure)`/`Spy(Class, Closure)` overload for exactly this Groovy
pattern, but a Kotlin lambda doesn't SAM-convert to `groovy.lang.Closure` (that conversion only applies to Java
functional interfaces), so it can't be passed directly. Instead, three new **Spockk-only** marker overloads, in
the same `lang` package as the existing block labels, resolved by Kotlin's normal overload resolution (they don't
collide with the inherited 1-arg Groovy `Mock(Class)`/`Stub(Class)`/`Spy(Class)` members, or with Spy's other 2-arg
overloads - different parameter types, no ambiguity):
```kotlin
fun <T> Mock(type: Class<T>, block: T.() -> Unit): T = throw UnsupportedOperationException(...)
fun <T> Stub(type: Class<T>, block: T.() -> Unit): T = throw UnsupportedOperationException(...)
fun <T> Spy(type: Class<T>, block: T.() -> Unit): T = throw UnsupportedOperationException(...)
```
`MockingApiTransformer` (already unconditionally enabled, untouched otherwise) is extended to recognize this
2-arg shape: rewrite to the existing `MockImpl`/`StubImpl`/`SpyImpl` construction (unchanged), then run the new interaction
rewriter over `block`'s statements, registering each one directly via `mockController.addInteraction(...)` - no
`enterScope`/`leaveScope` wrapping, since these interactions aren't verifying a call count against a `when:`
stimulus, they're just configuring stub behavior for the rest of the iteration (Spock's own semantics for
`given:`-scoped stubs).

### Where the rewriter plugs into the existing pipeline

- **New `InteractionStatementsRewriter`** (`compilation/transformer/interaction/`), parallel to the existing
  `ConditionStatementsRewriter`: recognizes the `Int.times(...)`/`ClosedRange<Int>.times(...)` marker call shape (or
  a bare `does`/`did`/`returns`/`returned`-wrapped call with no cardinality, for the `given:`-block case) at the top
  of a statement, walks the wrapped `IrCall` to extract target/method/args/response, and builds the real
  `InteractionBuilder` chain + `MockController.addInteraction(...)` call using the real Spock runtime classes above
  (mirrors how condition rendering reuses `SpockRuntime`/`Condition` instead of reimplementing anything).
- **`ConditionStatementsRewriter`** gains an `isInteractionStatement()` check alongside its existing
  `isConditionStatement()`/helper-call checks, so a `then`/`expect` block routes an interaction statement to the
  new rewriter instead of treating it as a boolean condition. (`given:`/`Stub{}`-block interactions go through a
  separate, simpler, non-recursive path from `MockingApiTransformer` - see above - since they're never mixed with
  plain conditions.)
- **`FeatureRewriter`**/**new `InteractionScopeRewriter`** (or an extension of the existing `WhenBlockRewriter`):
  extends the existing WHEN/THEN lookahead (already used for `hasExceptionCondition()`) with a parallel
  `hasInteractionStatement()` check, and when a `then:` block has interactions, brackets the preceding `when:`
  block with `enterScope()`/moved-interaction-registration-statements before, and inserts `leaveScope()` at the
  start of the `then:` block - the exact shape Spock's own `SpecRewriter.moveInteractions` produces (see above).
  Chained `then:` blocks after one `when:` (Spock's `addBarrier()` case) are out of scope for this preview, same as
  they're already out of scope for exception conditions - `BlockOrderValidatingFeatureStatementsCollector` already
  rejects a `then:` immediately following another `then:` as a compile error upstream, so this isn't reachable.

### Scope (v1 preview)

**In scope:** `Mock`/`Stub`/`Spy` construction, including the `Spy(Type::class.java) { ... }` builder-block form
(`SpecInternals.SpyImpl`'s first 8 overloads have the identical `(Specification, name, Type, [Map/Class], [Closure])`
shape as `MockImpl`/`StubImpl` - the same generic `findMockImplMethod` resolution already used for `Mock`/`Stub`
applies unchanged; Spy's two additional "wrap an existing instance" overloads are simply never matched by a
`Spy(Type::class.java)`-shaped call, so nothing extra was needed for that case). Interactions on a `Spy` were
initially scoped out on the assumption that Spock "discourages" the combination - checked against Spock's own docs
during implementation: `1 * spy.method(_)` verification and stubbing are fully supported and documented
(`interaction_based_testing.html`, including a dedicated `callRealMethod()` helper for "stub and still delegate to
the real method"); the only actual caveat is a style note ("think twice before using this feature") aimed at Spy's
general partial-mocking pattern, not a technical restriction Spockk needs to encode. Cardinality as exact count
(`N`) or range (`a..b`); `any()`/`any<T>()` argument matcher; literal argument equality (any other literal value
passed directly, no wrapper needed); `does`/`did`/`returns`/`returned` response specification; `capture`/`slot`
argument capture; `anyMethod()` wildcard method matching; `noMoreInteractions(...)` sugar; when/then interaction
scoping; given-block/`Stub{}`/`Spy{}`-block eager stub registration.

**Out of scope, explicitly documented (not silent gaps):** regex method/property names; any-arity/wildcard
argument-count matching (`(*_)`); true cross-mock wildcard target (`_._` beyond the per-mock `noMoreInteractions`
sugar); `throws` as a dedicated response (expressible as `does { throw ... }`/`did { throw ... }`); chained
`then:` blocks after one `when:`; ordering constraints between interactions (Spock's `then:`/`then:` chaining,
already unreachable per the grammar above); iterable/sequential responses (`>> [1, 2, 3]`, `addIterableResponse`);
`callRealMethod()` (needs an argument-aware response lambda shape `does`/`did` don't have - Spy interactions
otherwise work, only this one delegate-and-compute pattern is deferred); `Spy(existingInstance) { ... }` (wrapping
an already-constructed object rather than a `Class` token - the plain, non-builder-block `Spy(instance)` this
overload sugars over was already out of this session's reach to verify and is left for a follow-up).

### Spock Source References

- [`InteractionBuilder.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/mock/runtime/InteractionBuilder.java) - full fluent API used to build `IMockInteraction`
- [`MockController.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/mock/runtime/MockController.java) - `addInteraction`/`enterScope`/`leaveScope` (scope stack, `leaveScope` calls `verifyInteractions()`)
- [`SpecificationContext.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/SpecificationContext.java) - `getMockController(): IMockController`
- [`SpecRewriter.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecRewriter.java) - `moveInteractions` (the exact when/then bracketing shape this design mirrors)
- [`InteractionRewriter.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/InteractionRewriter.java) - Groovy's own call-chain construction, wildcard target/method detection
- [`CodeResponseGenerator.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/mock/response/CodeResponseGenerator.java) - confirms closure-parameter-type reflection for responses (`IMockInvocation` vs raw args array)
- `org.spockframework.lang.Wildcard` - the `_`/`anyMethod()`/`any()` sentinel, shaded unmodified into `spockk-core`

### Existing Spockk Files Referenced

- `spockk-compiler-plugin/.../compilation/transformer/mock/MockingApiTransformer.kt` - existing `Mock`/`Stub`/`Spy`
  construction rewriting, extended (not replaced) for the new 2-arg builder-block overloads
- `spockk-compiler-plugin/.../compilation/transformer/condition/ConditionStatementsRewriter.kt` - gains
  `isInteractionStatement()` routing
- `spockk-compiler-plugin/.../compilation/transformer/condition/WhenBlockRewriter.kt`,
  `spockk-compiler-plugin/.../compilation/transformer/FeatureRewriter.kt` - lookahead pattern this design's
  when/then interaction scoping directly reuses (see `_docs/specs/2026-08-09-exception-conditions-design.md`)
- `spockk-compiler-plugin/.../compilation/transformer/ir/IrSpecInternals.kt`,
  `.../transformer/ir/IrSpecificationContext.kt` - the one-wrapper-class-per-Spock-class convention this design's
  new `IrInteractionBuilder`/`IrMockController` wrappers follow
- `spockk-core/.../lang/Blocks.kt`, `.../lang/Conditions.kt` - the marker-function/`throw`-if-unrewritten
  convention this design's new markers follow
