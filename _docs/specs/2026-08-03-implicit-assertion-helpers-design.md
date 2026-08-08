# Implicit Assertion Helpers: verify, verifyAll, verifyEach

**Date:** 2026-08-03
**Issue:** TBD

## Context

Spockk recently added support for top-level implicit conditions: inside `expect`/`then` blocks, a bare boolean
expression statement (or an explicit `assert(...)` call) is automatically treated as an assertion, without needing
Groovy's `assert` keyword. This mirrors Spock's own implicit-condition sugar.

Spock also lets developers get the same sugar *inside a nested closure*, via three built-in helper methods:

- `with(target) { ... }` — scopes conditions to an implicit target object (avoids repeating `target.` on every line)
  and fails fast on the first failing condition, like a normal sequence of assertions.
- `verifyAll { ... }` / `verifyAll(target) { ... }` — same implicit-condition sugar, but collects *all* failing
  conditions in the block and reports them together instead of stopping at the first one ("soft assertions").
- `verifyEach(things) { ... }` — runs the block once per element of an `Iterable`, with the current element as the
  implicit target, collecting failures across all elements (not just the first failing element) and reporting which
  element(s) failed.

This document scopes the equivalent Spockk API — named `verify`, `verifyAll`, `verifyEach` (Spockk's `verify`
replaces Spock's `with`, since `with` collides with Kotlin's own stdlib `with` scope function) — and how to
implement it on top of Spockk's existing IR-based condition rewriting.

## Functional Design

### How Spock Does It

#### Runtime API

[`Specification.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/spock/lang/Specification.java)
declares:

```java
<U> void with(U target, Closure<?> closure)
<U> void with(Object target, Class<U> type, Closure closure)   // type-checked variant, for IDE completion

void verifyAll(Closure closure)
<U> void verifyAll(U target, Closure<?> closure)
<U> void verifyAll(Object target, Class<U> type, Closure closure)

<U> void verifyEach(Iterable<U> things, Closure<?> closure)
<U> void verifyEach(Iterable<U> things, Function<? super U, ?> namer, Closure<?> closure)
```

Their runtime bodies are trivial — `with`/`verifyAll` just bind the closure's delegate to the target with
`DELEGATE_FIRST` resolution and invoke it:

```java
closure.setDelegate(target);
closure.setResolveStrategy(Closure.DELEGATE_FIRST);
GroovyRuntimeUtil.invokeClosure(closure, target);
```

All of the "real" behavior — implicit conditions, error collection, soft-assertion reporting — is injected into the
closure's *body* at compile time. The runtime methods themselves know nothing special about conditions.

`verifyEach` is the one exception: its per-item loop, failure wrapping, and aggregation is real runtime logic, in
`SpockRuntime.verifyEach`. Content below is verified against the `2.4-groovy-5.0` release sources (this project's
pinned Spock version, where the method sits at roughly lines 183-215); `master`'s line numbers drift over time, so
follow the method name rather than a line link:

```java
public static <T> void verifyEach(
    Iterable<T> things,
    Function<? super T, ?> namer,
    @ClosureParams(value = FromString.class, options = {"T", "T, int"})
    @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST)
    Closure<?> closure
) {
  List<InternalItemFailure<T>> failures = new ArrayList<>();
  int index = -1;
  for (T thing : things) {
    index++;
    try {
      closure.setDelegate(thing);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      if (closure.getMaximumNumberOfParameters() == 1) {
        GroovyRuntimeUtil.invokeClosure(closure, thing);
      } else {
        GroovyRuntimeUtil.invokeClosure(closure, thing, index);
      }
    } catch (Throwable throwable) {
      failures.add(new InternalItemFailure<>(thing, index, throwable));
    }
  }

  if (failures.size() == 1) {
    throw getAssertionFailedError(namer, failures.get(0));
  } else if (!failures.isEmpty()) {
    List<SpockAssertionError> processedFailures = failures.stream()
        .map(failure -> getAssertionFailedError(namer, failure))
        .collect(toList());
    throw new MultipleFailuresError("", processedFailures);
  }
}

private static <T> SpockAssertionError getAssertionFailedError(Function<? super T, ?> namer, InternalItemFailure<T> failure) {
  SpockAssertionError error = new SpockAssertionError(
      String.format(Locale.ROOT, "Assertions failed for item[%d] %s:\n%s",
          failure.index, namer.apply(failure.item), failure.throwable.toString()));
  error.setStackTrace(failure.throwable.getStackTrace());
  return error;
}
```

Each item's block runs with fail-fast condition semantics internally (first failing condition for *that* item
throws immediately), the surrounding loop catches it, wraps it with the item's index/name, and continues to the next
item. A single failure is thrown directly; multiple failures are aggregated into `org.opentest4j.MultipleFailuresError`.

`ErrorCollector`/`ErrorRethrower` ([`ErrorCollector.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/ErrorCollector.java))
is the same collect-or-rethrow abstraction Spockk already uses for top-level conditions:

- `collectOrThrow(Throwable)` — either appends to an internal list (collecting mode) or throws immediately
  (`ErrorRethrower`, a stateless singleton used for fail-fast scopes).
- `validateCollectedErrors()` — no errors: no-op; one error: throws it directly; more than one: throws a
  `SpockMultipleFailuresError` wrapping all of them.

#### Compile-Time AST Transformation

The special treatment is driven by
[`org.spockframework.compiler.SpecialMethodCall`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecialMethodCall.java)
and [`DeepBlockRewriter`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/DeepBlockRewriter.java):

- `Identifiers.CONDITION_METHODS = {"with", "verifyAll", "verifyEach"}`. A statement is recognized as a call to one
  of these (or to a user method annotated `@org.spockframework.lang.ConditionBlock`) when it's an unqualified method
  call (`this`/`super` receiver) whose trailing argument is a **literal** closure — non-literal closures (stored in
  a variable, passed indirectly) get no special treatment.
- `isConditionMethodCall()` = name is `with` or `verifyEach` (fail-fast: reuse the ambient error collector).
  `isGroupConditionBlock()` = name is `verifyAll` (soft: swap in a fresh collecting `ErrorCollector`).
- `DeepBlockRewriter.doVisitClosureExpression` recurses into the closure body with the *same* statement-rewriting
  visitor used for `then`/`expect` blocks, then injects a value recorder (and, for `verifyAll`, a fresh error
  collector) local at the top of that closure and — for `verifyAll` — calls `validateCollectedErrors()` at the end.
- Crucially, `handleImplicitCondition`'s gate is:
  ```java
  if (!(stat == currTopLevelStat && isThenOrExpectOrFilterBlock()
      || currSpecialMethodCall.isConditionMethodCall()
      || currSpecialMethodCall.isConditionBlock()
      || currSpecialMethodCall.isGroupConditionBlock()
      || (insideInteraction && interactionClosureDepth == 1))) {
    return false;
  }
  ```
  A bare boolean statement is an implicit condition only if it's a **direct top-level statement of a
  then/expect/filter block**, or it's **directly inside a `with`/`verifyAll`/`verifyEach`/`@ConditionBlock`
  closure** — arbitrary deeper nesting (a `for`, an `if`, a plain `.each {}` lambda) does not. Only literal-closure
  calls to these three methods get this treatment, and — per Spock's own known limitation
  ([spockframework/spock#1276](https://github.com/spockframework/spock/issues/1276)) — nesting inside a **custom
  helper method** is buggy in real Spock: the AST transform injects fixed-name locals
  (`$spock_errorCollector`/`$spock_valueRecorder`) and can redeclare them when a helper method itself is visited a
  second time in a nested call, a purely textual-AST scoping bug that doesn't have an equivalent in Kotlin IR's
  symbol-based locals.

### Gap to Spockk

Spockk's existing implicit-condition machinery
([`ConditionRewriter.kt`](https://github.com/pshevche/spockk/blob/main/spockk-compiler-plugin/src/main/kotlin/io/github/pshevche/spockk/compilation/transformer/condition/ConditionRewriter.kt),
[`FeatureRewriter.kt`](https://github.com/pshevche/spockk/blob/main/spockk-compiler-plugin/src/main/kotlin/io/github/pshevche/spockk/compilation/transformer/FeatureRewriter.kt))
only ever looks at the **flat top-level statement list of a `then`/`expect` block** inside a feature method:

- `FeatureRewriter.rewriteBehaviorStatements` declares one shared `ValueRecorder`/`ErrorCollector` pair per feature
  (a deliberate simplification vs. Spock's per-closure-depth recorders — safe because the recorder is reset
  per-condition regardless of nesting) and routes `EXPECT`/`THEN` blocks through `ConditionRewriter`.
- `ConditionRewriter.rewrite` iterates that block's statements once, wraps each condition statement
  (`isConditionStatement`: an `assert(...)` call, or a bare `Boolean`-typed expression statement) in a
  try/catch calling `SpockRuntime.verifyCondition`/`conditionFailedWithException`, and bookends the whole thing
  with `callBlockEntered`/`callBlockExited`.
- There is no concept of descending into a nested lambda body. `verify`/`verifyAll`/`verifyEach` don't exist yet
  anywhere in the codebase (confirmed: no matches for `with(`, `verifyAll`, `verifyEach` in `spockk-core` or the
  compiler plugin).
- Plain (non-feature) helper methods in a `Specification` subclass are invisible to the plugin entirely —
  `SpockkTransformationContextCollector.visitBlockBody` only registers a method via `context.addFeature(...)` if its
  statement list contains block-label markers; a method with none is silently skipped.

### Proposed Approach

**Scope:** `verify`/`verifyAll`/`verifyEach` get implicit-condition treatment in exactly two places:

1. As a direct statement inside a `then`/`expect` block (matches where bare implicit conditions already work).
2. As a direct statement anywhere inside an ordinary (non-feature, non-fixture) method of a `Specification`
   subclass — i.e. a user-defined **helper method** — regardless of whether that method is itself called from a
   `then`/`expect` block. This is the one deliberate expansion beyond "then/expect only": it's what makes it
   possible to factor assertions out into a reusable helper, which real Spock supports too (modulo the nesting bug
   noted above, which Kotlin IR's symbol-based locals don't reproduce).

Bare boolean statements at the *top level of a helper method* are **not** implicit conditions — only calls to
`verify`/`verifyAll`/`verifyEach` are recognized there. Nesting (`verifyAll { verify(x) { ... } }`, a helper method
called from inside a `verifyAll` block, etc.) is supported by construction, since the rewriter recurses uniformly
regardless of where it started.

**Explicitly out of scope: top-level/extension-function helpers.** "Method of a `Specification` subclass" means a
*member* method. A top-level Kotlin extension function such as `fun Specification.checkPc(pc: PC) = verify(pc) {
vendor == "Sunny" }` cannot be reached by the plugin's class-scoped traversal the way a member helper can —
`SpockkIrTransformer.visitFunctionNew` already null-guards on `maybeCurrentIrClass` for exactly this reason
(extension functions have no enclosing `IrClass` on the visitor's class stack; see the existing comment at
`SpockkIrTransformer.kt:40`). A `verify`/`verifyAll`/`verifyEach` call inside such a function is not specially
rewritten — it still compiles and runs as an ordinary function call, just without implicit-condition sugar (bare
booleans inside stay plain, unchecked expression statements). This is a deliberate scope decision, not an oversight:
no compile-time diagnostic is planned for this iteration. Member-method helpers, the documented and intended usage,
are unaffected.

**Inherited/overridden helper methods need no special handling.** Feature methods need the `addPotentialFeature`/
fake-override machinery in `MutableSpockkTransformationContext` because Spockk materializes each inherited feature
as its own distinct JUnit dynamic test per subclass. A plain helper method has no such requirement: it's rewritten
once, in the class where its real `IrBlockBody` lives, and ordinary JVM virtual dispatch makes it work correctly
however it's called (directly or transitively, from any subclass). `HelperMethodContext` does not need a parallel
fake-override mechanism.

**Runtime API** (`spockk-core`, new `Conditions.kt`, real functioning Kotlin — not compile-erased markers like
`Blocks.kt`, since unlike block labels these wrap genuine lambda-invocation/iteration behavior that must still work
correctly even where the compiler plugin doesn't apply special treatment, e.g. a non-literal lambda argument):

```kotlin
fun <T> verify(target: T, block: T.() -> Unit) = target.block()
fun verifyAll(block: () -> Unit) = block()
fun <T> verifyAll(target: T, block: T.() -> Unit) = target.block()
fun <T> verifyEach(things: Iterable<T>, block: T.() -> Unit) = verifyEach(things, { it.toString() }, block)
fun <T> verifyEach(things: Iterable<T>, namer: (T) -> String, block: T.() -> Unit) { /* ported algorithm below */ }
```

`verify`/`verifyAll` use Kotlin's native extension-lambda receiver (`T.() -> Unit`) for target binding — no
delegate/resolve-strategy machinery needed, since Kotlin resolves unqualified member access against the receiver
statically.

**Non-inline, deliberately.** All five functions are ordinary (non-`inline`) functions. The alternative — `inline`,
matching Kotlin's own `with`/`apply`/`run` — would allow a bare non-local `return` inside the block (closer to
Groovy closure ergonomics), but adds unverified risk around cross-module inline-body serialization interacting with
the compiler plugin's own IR rewriting of the block's contents, for a marginal ergonomic gain. Decision: stay
non-inline; a `return` inside a `verify`/`verifyAll`/`verifyEach` block uses the standard Kotlin labeled form
(`return@verify`, `return@verifyAll`, `return@verifyEach`) like any other non-inline lambda parameter — this is
ordinary, well-understood Kotlin behavior, not a special case introduced by this feature.

**`verifyEach`'s index-parameter form is deferred.** Real Spock's `verifyEach` also accepts a two-arg closure
(`{ item, index -> ... }`) for accessing the loop index directly. Spockk ships only the `namer`-based overload in
this iteration (covers the main use case — readable failure messages — via `Conditions.kt`'s
`verifyEach(things, namer, block)`); an index-parameter overload is a pure additive API change and can be added
later without breaking anything shipped now.

`verifyEach` **does not use Groovy at all** (`spockk-core` already depends on Groovy transitively for
`spock.lang.Specification`/mocking, but this call path avoids it entirely per explicit instruction). It's a direct
Kotlin port of `SpockRuntime.verifyEach`'s algorithm: loop with index, try/catch per item, wrap each failure via the
same `"Assertions failed for item[%d] %s:\n%s"` format into a `SpockAssertionError` (already shaded, unmodified,
into `spockk-core`; confirmed public constructors `()`, `(String)`, `(Throwable)`, `(String, Throwable)` in the
`2.4-groovy-5.0` sources) with the original stack trace, then throw the single failure directly or aggregate
multiple into `org.opentest4j.MultipleFailuresError` — reusing Spock's exact message format and error types without
going through `Closure`. Dependency chain confirmed by inspecting the resolved build: `opentest4j` is not shaded
into `spockk-core` (`shadowJar` only includes `io/github/pshevche/spockk/**`, `org/spockframework/**`, `spock/**`),
but resolves transitively and correctly regardless — `spock-core`'s Gradle module metadata declares
`junit-platform-engine` as an `apiElements` (compile-scope) dependency, and `junit-platform-engine` in turn declares
`org.opentest4j:opentest4j` as its own `apiElements` dependency; since `spockk-core` declares `api(libs.spock)`, this
propagates all the way through. No new dependency declaration needed. (Real Spock's own `SpockMultipleFailuresError`,
thrown by `ErrorCollector.validateCollectedErrors()` on the IR-driven `verifyAll` path, itself extends
`org.opentest4j.MultipleFailuresError` — so the two aggregation paths throw type-compatible errors.)

**Compile-time rewriting** (`spockk-compiler-plugin`):

- New FQNs in `IrIdentifiers.Spockk`: `VERIFY_FQN`, `VERIFY_ALL_FQN`, `VERIFY_EACH_FQN`. Matching by FqName alone
  (ignoring overload) is sufficient and already precedented: `IrCall.fqName()` collapses to package+name with no
  signature encoding, and `isAssertCall()` already uses exactly this technique to match both of Kotlin's `assert`
  overloads (`IrStatement.kt:100-103`) against one `ASSERT_FQN` constant.
- `ConditionRewriter`'s per-statement loop (currently private, bookended by `callBlockEntered`/`callBlockExited` in
  `rewrite()`) is factored into a reusable core,
  `rewriteConditionStatements(statements, enclosingFunction, valueRecorderVar, errorCollectorVar, builder,
  treatBareBooleansAsConditions)`, that:
  - Detects a statement that's a call to `verify`/`verifyAll`/`verifyEach` **with a literal trailing lambda
    argument** (`IrFunctionExpression`) — non-literal arguments are left as ordinary calls, matching Spock's own
    restriction.
  - Recurses into that lambda's `IrBlockBody`, applying this same function to it in place, **always passing
    `treatBareBooleansAsConditions = true` for the recursive call** — regardless of what was passed in for the
    outer statement list. This is what makes `verify`/`verifyAll`/`verifyEach` bodies get implicit-condition
    treatment even when reached from a helper method's non-condition top level (see below), and what makes nesting
    (`verifyAll { verify(x) { ... } }`) compose for free.
  - Otherwise (not a helper call): treats the statement as a condition — wraps it in the existing
    try/catch/`verifyCondition` scaffolding — only if `treatBareBooleansAsConditions` is true *for this call* (an
    `assert(...)` call always counts, matching today's `isConditionStatement`; a bare boolean only counts when the
    flag is true). Otherwise the statement is left untouched.
  - For `verify`/`verifyEach`: reuses whichever `(ValueRecorder, ErrorCollector)` pair is already ambient at that
    point (the feature-shared pair inside a `then`/`expect` block, or a pair declared once at the top of the
    enclosing helper method) — fail-fast, matching Spock's `isConditionMethodCall()`.
  - For `verifyAll`: declares a **fresh** local `ErrorCollector` as the first statement of the lambda body — a real
    instance built via its constructor (confirmed: `ErrorCollector` declares no explicit constructor, so Java/Kotlin
    supplies an implicit public no-arg one, unlike `ErrorRethrower`'s explicitly `private` constructor), using the
    same constructor-call pattern `FeatureRewriter.initializeValueRecorderStatement` already uses for
    `ValueRecorder` (`findRequiredClassSymbol(...).constructors.first()` + `builder.irCallConstructor(...)`),
    applied to `ERROR_COLLECTOR_FQN` instead of `VALUE_RECORDER_FQN`. (Note: this is *not* the same pattern as
    `FeatureRewriter.initializeErrorCollectorStatement`, which reads the `ErrorRethrower.INSTANCE` singleton field
    rather than constructing anything — that method builds the fail-fast case, not the fresh-collector case.) The
    fresh collector is threaded through the recursive rewrite of that body, and
    `errorCollector.validateCollectedErrors()` is appended as the lambda's last statement.
  - The shared feature-level `ValueRecorder` is reused at every nesting depth (consistent with Spockk's existing
    per-feature simplification).
- `FeatureRewriter` is updated to route `then`/`expect` block statements through this recursive core (unchanged
  externally — `callBlockEntered`/`callBlockExited` still bookend only the top-level block, not nested lambdas) with
  `treatBareBooleansAsConditions = true`. **This is not purely a routing change**: `rewriteBehaviorStatements`'s
  `hasConditions` check (which gates whether the shared `ValueRecorder`/`ErrorCollector` locals are declared at all)
  currently only looks at `isConditionStatement` — an `assert(...)` call or a bare boolean. It must also recognize a
  literal-lambda `verify`/`verifyAll`/`verifyEach` call as "this block has conditions", reusing the same detection
  the recursive core uses. Without this, a `then`/`expect` block whose *only* statement is `verify(x) { ... }` (a
  common, not edge-case, shape) computes `hasConditions = false`, leaves both vars `null`, and the existing
  `valueRecorderVar!!`/`errorCollectorVar!!` non-null assertions in the rewrite path crash.
- `SpockkTransformationContextCollector.visitBlockBody` gains a second detection path: for a method that is neither
  a feature (no block labels) nor a fixture method, scan its statements (recursively, through literal-lambda
  `verify`/`verifyAll`/`verifyEach` calls only — not through arbitrary control flow) for any occurrence of these
  three calls. If found, register the method in a new `HelperMethodContext` (parallel to `FeatureContext`) so
  `SpockkIrTransformer.visitFunctionNew` can dispatch a `HelperMethodRewriter`, which declares a fresh
  `(ValueRecorder, ErrorRethrower.INSTANCE-backed ErrorCollector)` pair scoped to that method (mirroring
  `FeatureRewriter`'s once-per-feature declaration) before calling the same recursive core over the method's
  top-level statements with `treatBareBooleansAsConditions = false` (bare booleans at a helper method's own top
  level are ordinary statements; only the helper-call detection inside `rewriteConditionStatements` applies there —
  see above for how recursion into a matched call flips the flag back to `true` for that nested body).
- `ConditionValueRecordingTransformer` needs no algorithmic changes. Its implicit-`<this>`-skipping logic in
  `visitGetValue` (skips any `IrValueParameter` named `"<this>"`) should already correctly skip a nested lambda's
  own implicit extension-receiver parameter for free: Kotlin IR conventionally names both dispatch and extension
  receiver parameters `"<this>"`, and this codebase already relies on that same blanket convention elsewhere
  (`IrFunction.assignableParameters()` filters out parameters named `"<this>"` generically, not just for the
  top-level feature's receiver). So e.g. `verify(pc) { vendor == "Sunny" }` should already render identically to
  Spock's `with(pc) { vendor == "Sunny" }` with no code change — this is validated empirically (a rendering test
  cross-checked against real Spock's `Condition(values, text, position, null, null, null).getRendering()`, per the
  project's established practice), with `visitGetValue` extended only if that empirical check turns up a mismatch.

### Spock Source References

- [`Specification.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/spock/lang/Specification.java) — `with`/`verifyAll`/`verifyEach` public API and their trivial runtime bodies
- [`SpockRuntime.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/SpockRuntime.java) — `verifyEach` (lines ~304-330), `getAssertionFailedError`, `verifyCondition` (the same method Spockk already calls)
- [`ErrorCollector.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/ErrorCollector.java) — `collectOrThrow`/`validateCollectedErrors`, already shaded and used by Spockk
- [`SpecialMethodCall.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecialMethodCall.java) — detection of `with`/`verifyAll`/`verifyEach`/`@ConditionBlock` calls, literal-closure restriction
- [`DeepBlockRewriter.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/DeepBlockRewriter.java) — `isThenOrExpectOrFilterBlock`, `handleImplicitCondition` gating, per-closure-depth recorder/collector injection
- [`ConditionBlock.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/spock/lang/ConditionBlock.java) — the user-extensibility annotation (not adopted by Spockk in this iteration; noted for future reference)
- [`VerifyAllSpecification.groovy`](https://github.com/spockframework/spock/blob/master/spock-specs/src/test/groovy/org/spockframework/verifyall/VerifyAllSpecification.groovy) — real Spock's test coverage for `verifyAll` (multi-failure aggregation, nesting, void methods, target type checks)
- [spockframework/spock#1276](https://github.com/spockframework/spock/issues/1276) — known Spock limitation with nested `verifyAll` inside helper methods, informing why Spockk's IR-symbol-based scoping doesn't need the same workaround

### Gap to Spockk (Existing Spockk Files Touched)

- `spockk-core/.../lang/Blocks.kt` — untouched (verify/verifyAll/verifyEach are not block labels)
- `spockk-core/.../lang/Verification.kt` — new, real runtime API
- `spockk-compiler-plugin/.../ir/IrIdentifiers.kt` — new FQNs
- `spockk-compiler-plugin/.../transformer/condition/ConditionRewriter.kt` — factor out reusable recursive core, add helper-call detection
- `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt` — use the refactored core (behavior-preserving for existing then/expect handling)
- `spockk-compiler-plugin/.../collector/SpockkTransformationContextCollector.kt` — detect helper methods containing verify/verifyAll/verifyEach calls
- `spockk-compiler-plugin/.../shared/SpockkTransformationContext.kt` / `MutableSpockkTransformationContext.kt` — new `HelperMethodContext`
- `spockk-compiler-plugin/.../transformer/SpockkIrTransformer.kt` — dispatch new `HelperMethodRewriter`
- `spockk-compiler-plugin/.../transformer/ir/ConditionValueRecordingTransformer.kt` — verify (and extend if needed) implicit-receiver skipping for nested lambdas
