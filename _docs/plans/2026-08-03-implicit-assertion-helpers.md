# Implicit Assertion Helpers Implementation Plan

**Goal:** Add `verify`, `verifyAll`, and `verifyEach` helper methods to Spockk — Kotlin equivalents of Spock's
`with`/`verifyAll`/`verifyEach` — that support implicit (bare-boolean) assertions inside their lambda bodies. Usable
as a direct statement inside `then`/`expect` blocks, and as a direct statement anywhere inside ordinary (non-feature)
helper methods of a `Specification` subclass, with arbitrary nesting between them.

**Architecture:** Extends the compiler plugin's existing implicit-condition machinery. `ConditionRewriter`'s
per-statement rewriting loop is factored into a reusable recursive core that both `FeatureRewriter` (then/expect
blocks) and a new `HelperMethodRewriter` (plain helper methods) drive. `verify`/`verifyEach` reuse whichever
`(ValueRecorder, ErrorCollector)` pair is ambient in their scope (fail-fast); `verifyAll` declares its own fresh
collecting `ErrorCollector` and validates it at the end of its lambda. `verifyEach`'s runtime function is a direct,
Groovy-free Kotlin port of `SpockRuntime.verifyEach`'s loop/wrap/aggregate algorithm.

See design doc: `_docs/specs/2026-08-03-implicit-assertion-helpers-design.md`

**Tech Stack:** Kotlin IR, JUnit Platform, Spock runtime (shaded `ErrorCollector`, `ErrorRethrower`,
`SpockAssertionError`), `org.opentest4j.MultipleFailuresError`

---

### Task 1: Add the runtime API to spockk-core

Add `verify`, `verifyAll` (two overloads), and `verifyEach` (two overloads) as real, functioning Kotlin functions —
not compile-erased markers. `verifyEach` ports `SpockRuntime.verifyEach`'s exact algorithm without touching Groovy.

**Files:**
- Create: `spockk-core/src/main/kotlin/io/github/pshevche/spockk/lang/Verification.kt`
- Test: `spockk-core` unit test (or `spockk-specs` smoke test) exercising the runtime functions directly, without the compiler plugin, to confirm graceful fallback behavior (block still runs; bare booleans just aren't asserted)

- [ ] **Step 1:** Add `fun <T> verify(target: T, block: T.() -> Unit)` — non-inline (deliberate; see design doc). A `return` inside the block uses the standard labeled form (`return@verify`), same as any other non-inline lambda parameter
- [ ] **Step 2:** Add `fun verifyAll(block: () -> Unit)` and `fun <T> verifyAll(target: T, block: T.() -> Unit)` — non-inline, same rationale
- [ ] **Step 3:** Add `fun <T> verifyEach(things: Iterable<T>, block: T.() -> Unit)` delegating to the 3-arg overload with a default `toString()` namer. No `(item, index)` two-arg overload in this iteration — deferred, pure additive API change if added later
- [ ] **Step 4:** Add `fun <T> verifyEach(things: Iterable<T>, namer: (T) -> String, block: T.() -> Unit)` — port `SpockRuntime.verifyEach`'s loop: index-tracked iteration, try/catch per item, wrap failures via shaded `org.spockframework.runtime.SpockAssertionError` with the `"Assertions failed for item[%d] %s:\n%s"` format and the original stack trace, throw single failure directly or `org.opentest4j.MultipleFailuresError` for multiple
- [ ] **Step 5:** `SpockAssertionError` and `MultipleFailuresError` are confirmed to resolve from `spockk-core`'s existing dependencies with no new dependency needed — `SpockAssertionError` is shaded (`org/spockframework/**`); `opentest4j` isn't shaded but resolves transitively via `spock-core` → `junit-platform-engine` → `opentest4j`, all `apiElements`/compile-scope, propagated through `spockk-core`'s `api(libs.spock)`. Just wire up the imports
- [ ] **Step 6:** Run `./gradlew :spockk-core:test spotlessApply`, fix failures, commit

### Task 2: Add IR identifiers

**Files:**
- Modify: `spockk-compiler-plugin/src/main/kotlin/io/github/pshevche/spockk/compilation/ir/IrIdentifiers.kt`

- [ ] **Step 1:** Add `VERIFY_FQN`, `VERIFY_ALL_FQN`, `VERIFY_EACH_FQN` to `IrIdentifiers.Spockk`, pointing at `io.github.pshevche.spockk.lang.verify`/`verifyAll`/`verifyEach`
- [ ] **Step 2:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 3: Factor ConditionRewriter into a reusable recursive core

Extract the statement-rewriting loop out of `ConditionRewriter.rewrite()` (currently private, bookended by
`callBlockEntered`/`callBlockExited`) so it can be driven both at block level and recursively inside a
`verify`/`verifyAll`/`verifyEach` lambda body, without re-triggering block-entered/exited calls for nested lambdas.

**Files:**
- Modify: `spockk-compiler-plugin/.../transformer/condition/ConditionRewriter.kt`
- Create: `spockk-compiler-plugin/.../transformer/condition/ImplicitAssertionHelperCall.kt` (or similar) — detects a statement that is a literal-lambda call to `verify`/`verifyAll`/`verifyEach`, extracts the lambda's `IrFunctionExpression`/`IrSimpleFunction`/`IrBlockBody`, and exposes a `containsImplicitAssertionHelperCall(statements): Boolean` used by both this task's `hasConditions` fix and Task 5's helper-method detection
- Modify: `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt` — extend the `hasConditions` predicate (see Step 7 below; this is a real code change, not a follow-up confirmation)

- [ ] **Step 1:** Extract `rewriteConditionStatements(statements, enclosingFunction, valueRecorderVar, errorCollectorVar, builder, treatBareBooleansAsConditions: Boolean): List<IrStatement>` from `ConditionRewriter.rewrite()` — same per-statement try/catch/`verifyCondition` wrapping, no block-entered/exited calls. An `assert(...)` call is always treated as a condition; a bare boolean statement is only treated as one when `treatBareBooleansAsConditions` is true for *this* statement list
- [ ] **Step 2:** Add literal-trailing-lambda detection for calls matching `VERIFY_FQN`/`VERIFY_ALL_FQN`/`VERIFY_EACH_FQN` (match by callee FqName regardless of overload — precedented by `isAssertCall()`'s identical technique; locate the last value argument that is an `IrFunctionExpression`)
- [ ] **Step 3:** In `rewriteConditionStatements`, when a statement is such a call: for `verify`/`verifyEach`, recurse into the lambda body reusing the same `valueRecorderVar`/`errorCollectorVar` and **always** `treatBareBooleansAsConditions = true` for the recursive call, regardless of the flag's value at the outer level (rebuilding a `DeclarationIrBuilder` scoped to the nested `IrSimpleFunction`); leave the call statement itself structurally unchanged (only its lambda body's statements are replaced)
- [ ] **Step 4:** For `verifyAll`, same recursion (also always `treatBareBooleansAsConditions = true` inside) but first prepend a freshly constructed local `ErrorCollector` declaration to the lambda body — a real instance via its constructor (`ErrorCollector` has no explicit constructor, so an implicit public no-arg one is available — confirmed via the `2.4-groovy-5.0` sources), using the same constructor-call pattern `FeatureRewriter.initializeValueRecorderStatement` uses for `ValueRecorder` (`findRequiredClassSymbol(...).constructors.first()` + `builder.irCallConstructor(...)`), **not** the `ErrorRethrower.INSTANCE` field-read pattern `initializeErrorCollectorStatement` uses (that builds the fail-fast case, a different thing) — and append `errorCollector.validateCollectedErrors()` as the lambda's last statement; thread this fresh var as the collector for the recursive rewrite of that body only
- [ ] **Step 5:** Confirm nesting composes for free (a `verify`/`verifyEach` lambda body containing another `verify`/`verifyAll`/`verifyEach` call is rewritten correctly by the same recursive step)
- [ ] **Step 6:** `ConditionRewriter.rewrite()` (block-level entry point) now calls `rewriteConditionStatements(..., treatBareBooleansAsConditions = true)` internally, bookended by `callBlockEntered`/`callBlockExited` as before — no behavior change for existing then/expect handling
- [ ] **Step 7:** Fix the real gap this refactor exposes: `FeatureRewriter.rewriteBehaviorStatements`'s `hasConditions` check currently only tests `isConditionStatement` (an `assert(...)` call or bare boolean) per statement. Extend it to also treat a block as having conditions when any of its statements is a literal-lambda `verify`/`verifyAll`/`verifyEach` call (reuse `containsImplicitAssertionHelperCall` from this task). Without this, a `then`/`expect` block whose only statement is `verify(x) { ... }` computes `hasConditions = false`, leaves `valueRecorderVar`/`errorCollectorVar` `null`, and the existing `!!` assertions in the rewrite path throw — this is the common case, not an edge case, so this step is required before Task 4 can be verified
- [ ] **Step 8:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 4: Verify then/expect blocks work end-to-end through the new recursive core

With Task 3's `hasConditions` fix in place, confirm `FeatureRewriter` routes `then`/`expect` statements through the
recursive core correctly for every shape: bare conditions only, helper calls only, and a mix of both in one block.

This task needs its own executable checkpoint — `:spockk-compiler-plugin:compileKotlin` only compiles the plugin's
own sources and cannot exercise the rewriter against sample code. Use the project's existing quick-check pattern
(`TestDataFactory.specWithFeatureBody()` + `transform()`, asserting `result.isSuccess()`) rather than deferring
verification to Task 7's formal golden files, so Task 3's refactor is validated before Task 5 builds on it.

**Files:**
- Test: throwaway/inline check via `TestDataFactory.specWithFeatureBody()` (superseded by Task 7's formal snapshot pairs — this task's own test does not need to survive as a permanent file)

- [ ] **Step 1:** Using `TestDataFactory.specWithFeatureBody()` + `transform()`, compile a `then` block containing only a `verify(x) { ... }` call (no bare top-level condition) and assert `result.isSuccess()` — confirms the value recorder/error collector are declared and no `!!`-related crash occurs
- [ ] **Step 2:** Same pattern, a `then` block mixing a bare condition and a `verify`/`verifyAll`/`verifyEach` call — assert `result.isSuccess()`
- [ ] **Step 3:** Run `./gradlew :spockk-compiler-plugin:compileKotlin :spockk-specs:test --tests "*ConditionRewriter*"` (or equivalent for wherever the check lands), commit

### Task 5: Add helper-method detection and rewriting

Extend the collector to notice plain (non-feature, non-fixture) **member** methods of a `Specification` subclass
that contain `verify`/`verifyAll`/`verifyEach` calls, and rewrite them with their own freshly declared,
method-scoped `(ValueRecorder, ErrorCollector)` pair.

**Explicitly out of scope:** top-level Kotlin extension functions on `Specification` (as opposed to member methods)
are not detected — `SpockkIrTransformer.visitFunctionNew` already null-guards on `maybeCurrentIrClass` for this
exact reason (no enclosing `IrClass` on the visitor's class stack; see the existing comment at
`SpockkIrTransformer.kt:40`). A `verify`/`verifyAll`/`verifyEach` call there must silently run as an ordinary call
with no implicit-condition sugar — **not crash the compiler**. No compile diagnostic for this case in this
iteration — documented as a known limitation (Task 9), not implemented as an error.

**Correctness-critical:** `SpockkTransformationContextCollector` visits the whole module, including top-level
declarations outside any class (`SpockkCompilerPlugin.kt:51`), so `maybeCurrentIrClass` is genuinely `null` for a
top-level extension function — this is exactly the scenario the paragraph above needs to degrade gracefully on.
`visitBlockBody`'s existing else-branch (where the new scan is added, per the Files note below) currently does
`context.addFeature(currentIrClass, function, it)`, where `currentIrClass = maybeCurrentIrClass!!` — an *unguarded*
assertion (`BaseSpockkIrElementVisitor.kt`). The new scan must **not** follow that pattern. Follow the *safe*
precedent instead, used two lines away in the same file's `visitFunctionNew` for fixture methods:
`maybeCurrentIrClass?.let { spec -> context.addFixtureMethod(spec, declaration) }`. Registering (and therefore
rewriting) a helper method must only happen when `maybeCurrentIrClass` is non-null; when it's null, skip
registration entirely and let the call through unrewritten, per the paragraph above.

**Files:**
- Modify: `spockk-compiler-plugin/.../shared/SpockkTransformationContext.kt` — add `HelperMethodContext`
- Modify: `spockk-compiler-plugin/.../shared/MutableSpockkTransformationContext.kt` — track helper methods with conditions
- Modify: `spockk-compiler-plugin/.../collector/SpockkTransformationContextCollector.kt` — detect such methods in `visitBlockBody`'s else-branch (today's `bodyParser.getFeatureBody() == null` path silently drops everything; add the new scan there)
- Create: `spockk-compiler-plugin/.../transformer/condition/HelperMethodRewriter.kt`
- Modify: `spockk-compiler-plugin/.../transformer/SpockkIrTransformer.kt` — dispatch `HelperMethodRewriter` for registered helper methods

- [ ] **Step 1:** Add a statement-list scan (reusing the literal-lambda detection from Task 3) that answers "does this method contain a verify/verifyAll/verifyEach call anywhere reachable via direct statements and recursion into their lambda bodies" without treating bare booleans at the method's own top level as conditions
- [ ] **Step 2:** Register matching methods in a new context bucket (`HelperMethodContext`), parallel to `FeatureContext`/`fixtureMethods` — gated by `maybeCurrentIrClass?.let { ... }`, never the unguarded `currentIrClass`/`!!` form (see correctness note above)
- [ ] **Step 3:** Create `HelperMethodRewriter`: declares a fresh `ValueRecorder` + `ErrorRethrower.INSTANCE`-backed `ErrorCollector` pair once at the top of the method (mirroring `FeatureRewriter.initializeValueRecorderStatement`/`initializeErrorCollectorStatement`, generalized to an arbitrary enclosing function), then calls `rewriteConditionStatements` from Task 3 over the method's top-level statements with `treatBareBooleansAsConditions = false` (the flag governs only this top-level call — recursion into any matched helper call's lambda body flips it back to `true` per Task 3 Step 3)
- [ ] **Step 4:** Wire dispatch into `SpockkIrTransformer.visitFunctionNew` alongside the existing `featureContext` lookup
- [ ] **Step 5:** Confirm a helper method with no verify/verifyAll/verifyEach calls is untouched (no spurious recorder/collector declarations)
- [ ] **Step 6:** Add a case for a **top-level Kotlin extension function** on `Specification` containing a `verify`/`verifyAll`/`verifyEach` call (e.g. `fun Specification.checkPc(pc: PC) = verify(pc) { vendor == "Sunny" }`) — assert it compiles successfully with no special rewriting (this is the scenario Step 2's guard exists for; it must not crash the compiler)
- [ ] **Step 7:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 6: Validate condition rendering inside nested lambdas

Depends on Task 3/4 (the recursive rewriter and its `hasConditions` fix) being in place.

**Files:**
- Modify (if needed): `spockk-compiler-plugin/.../transformer/ir/ConditionValueRecordingTransformer.kt`
- Modify: `spockk-specs/src/testFixtures/kotlin/io/github/pshevche/spockk/fixtures/runtime/samples/condition/ConditionRenderingSpecs.kt` — add fixture specs for `verify`/`verifyAll`/`verifyEach` (and a nested case), following the existing convention where every case in `ConditionRenderingTest.kt` drives against a compiled spec class from this shared file
- Test: extend `spockk-specs/.../runtime/ConditionRenderingTest.kt`

- [ ] **Step 1:** Write a rendering test for `verify(pc) { vendor == "Sunny" }` and compare against real Spock's `Condition(values, text, position, null, null, null).getRendering()` for the equivalent `with(pc) { vendor == "Sunny" }`
- [ ] **Step 2:** If the lambda's implicit extension-receiver parameter gets spuriously recorded (shifting indices), extend `visitGetValue`'s `<this>`-skip check to also skip it
- [ ] **Step 3:** Repeat for `verifyAll`/`verifyEach` (element as implicit receiver) and for a nested case (`verify` inside `verifyAll`)
- [ ] **Step 4:** Run `./gradlew :spockk-specs:test --rerun`, fix failures, commit

### Task 7: Add compilation snapshot tests

**Files:**
- Create: `spockk-specs/src/test/resources/samples/compilation/source/condition/{VerifyBasic,VerifyAllBasic,VerifyAllNoTarget,VerifyEachBasic,NestedVerifyInVerifyAll,VerifyInHelperMethod,VerifyWithNonLiteralLambda,VerifyInExtensionFunction}.kt`
- Create: matching golden files under `spockk-specs/src/test/resources/samples/compilation/transformed/condition/`
- Modify: `spockk-specs/src/test/kotlin/.../compilation/condition/ImplicitConditionsCompilationTest.kt` (or a new `VerificationHelpersCompilationTest.kt`)

- [ ] **Step 1:** `verify(target) { ... }` inside a `then` block — golden shows ambient error collector reused, no fresh collector declared
- [ ] **Step 2:** `verifyAll(target) { ... }` and `verifyAll { ... }` — golden shows a fresh `ErrorCollector` declared inside the lambda and `validateCollectedErrors()` appended
- [ ] **Step 3:** `verifyEach(things) { ... }` — golden shows the lambda body rewritten with the ambient collector, no loop synthesized (the loop lives in the runtime function, not IR)
- [ ] **Step 4:** Nested `verify` inside `verifyAll` — golden shows the inner lambda using the fresh `verifyAll` collector
- [ ] **Step 5:** `verify`/`verifyAll` call inside a plain helper method (not a feature) — golden shows a method-scoped recorder/collector pair declared, bare booleans outside the lambda untouched
- [ ] **Step 6:** A `verify` call with a non-literal lambda argument (stored in a `val` first) — golden shows no special rewriting, ordinary call
- [ ] **Step 7:** A `verify`/`verifyAll`/`verifyEach` call inside a top-level Kotlin extension function on `Specification` — golden shows no special rewriting (permanent version of Task 5 Step 6's check)
- [ ] **Step 8:** Run `./gradlew :spockk-specs:test --rerun`, fix failures, commit

### Task 8: Add smoke tests for runtime pass/fail semantics

**Files:**
- Create: `spockk-specs/src/test/kotlin/.../smoke/condition/{VerifySmokeTest,VerifyAllSmokeTest,VerifyEachSmokeTest,VerifyInHelperMethodSmokeTest}.kt`

- [ ] **Step 1:** `VerifySmokeTest` — fails fast on first bad condition (`@FailsWith(ConditionNotSatisfiedError::class)`), passes when all conditions hold, target's members resolve without qualification
- [ ] **Step 2:** `VerifyAllSmokeTest` — single failing condition throws directly; multiple failing conditions aggregate into a multi-failure error listing all of them; all-passing block succeeds
- [ ] **Step 3:** `VerifyEachSmokeTest` — passes when all elements satisfy the block; a single failing element throws directly with item context in the message; multiple failing elements aggregate, and elements after a failure are still checked (not skipped); custom `namer` overload changes the reported name
- [ ] **Step 4:** `VerifyInHelperMethodSmokeTest` — a helper method using `verify`/`verifyAll`/`verifyEach`, called from a `then` block, reports failures the same way as if written inline
- [ ] **Step 5:** Run `./gradlew :spockk-specs:test`, fix failures, commit

### Task 9: Update spockk-docs

**There is no existing implicit-conditions doc page to extend** — `spockk-docs` currently has no coverage of the
already-shipped implicit-condition feature at all (`writing_tests.adoc` only shows the explicit `assert(...)` form).
Worse, `spockk-docs/docs/limitations.adoc:5` currently reads *"it does not yet provide built-in fixtures for
defining conditions (assertions)"* — stale even before this feature, since implicit conditions already shipped.
This task documents both the pre-existing implicit-conditions feature and the new helpers together, and corrects
the stale limitation.

**Files:**
- Modify: `spockk-docs/.../writing_tests.adoc` — add implicit-conditions coverage (bare boolean / `assert(...)` in then/expect) alongside the new helpers, since neither is documented today
- Modify: `spockk-docs/docs/limitations.adoc` — remove/correct the stale "no built-in fixtures for conditions" claim

- [ ] **Step 1:** Document implicit conditions (bare boolean statements in `then`/`expect`, already shipped but undocumented) as a prerequisite for the rest of this section
- [ ] **Step 2:** Document `verify`/`verifyAll`/`verifyEach` usage, the then/expect + member-helper-method scope restriction, and the naming rationale (`verify` instead of `with`, to avoid colliding with Kotlin's stdlib `with`)
- [ ] **Step 3:** Document known limitations: extension-function (non-member) helpers on `Specification` aren't specially rewritten; `verifyEach`'s `(item, index)` two-arg closure form isn't available yet (use the `namer` overload for per-item message context); `return` inside a block uses the labeled form (`return@verify` etc.)
- [ ] **Step 4:** Correct `limitations.adoc:5`'s stale claim
- [ ] **Step 5:** Run `./gradlew :spockk-docs:asciidoctor`, verify output, commit

### Task 10: Final verification

- [ ] **Step 1:** Run `./gradlew build` (full build: compile, test, spotless, detekt) at the repo root
- [ ] **Step 2:** Fix any remaining failures, commit
