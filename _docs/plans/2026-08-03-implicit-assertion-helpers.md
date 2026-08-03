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

- [ ] **Step 1:** Add `fun <T> verify(target: T, block: T.() -> Unit)`
- [ ] **Step 2:** Add `fun verifyAll(block: () -> Unit)` and `fun <T> verifyAll(target: T, block: T.() -> Unit)`
- [ ] **Step 3:** Add `fun <T> verifyEach(things: Iterable<T>, block: T.() -> Unit)` delegating to the 3-arg overload with a default `toString()` namer
- [ ] **Step 4:** Add `fun <T> verifyEach(things: Iterable<T>, namer: (T) -> String, block: T.() -> Unit)` — port `SpockRuntime.verifyEach`'s loop: index-tracked iteration, try/catch per item, wrap failures via shaded `org.spockframework.runtime.SpockAssertionError` with the `"Assertions failed for item[%d] %s:\n%s"` format and the original stack trace, throw single failure directly or `org.opentest4j.MultipleFailuresError` for multiple
- [ ] **Step 5:** Confirm `SpockAssertionError` and `MultipleFailuresError` resolve from `spockk-core`'s existing dependencies (shaded `org.spockframework.**`, transitive via `libs.spock`) — no new dependency needed
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
- Create: `spockk-compiler-plugin/.../transformer/condition/ImplicitAssertionHelperCall.kt` (or similar) — detects a statement that is a literal-lambda call to `verify`/`verifyAll`/`verifyEach` and extracts the lambda's `IrFunctionExpression`/`IrSimpleFunction`/`IrBlockBody`
- Modify: `spockk-compiler-plugin/.../transformer/ir/IrSpockRuntime.kt` if a new `ErrorCollector` construction helper is needed (mirror `FeatureRewriter.initializeErrorCollectorStatement`, generalized to build inside an arbitrary enclosing function/builder rather than only the feature)

- [ ] **Step 1:** Extract `rewriteConditionStatements(statements, enclosingFunction, valueRecorderVar, errorCollectorVar, builder): List<IrStatement>` from `ConditionRewriter.rewrite()` — same per-statement try/catch/`verifyCondition` wrapping, no block-entered/exited calls
- [ ] **Step 2:** Add literal-trailing-lambda detection for calls matching `VERIFY_FQN`/`VERIFY_ALL_FQN`/`VERIFY_EACH_FQN` (match by callee FqName regardless of overload; locate the last value argument that is an `IrFunctionExpression`)
- [ ] **Step 3:** In `rewriteConditionStatements`, when a statement is such a call: for `verify`/`verifyEach`, recurse into the lambda body reusing the same `valueRecorderVar`/`errorCollectorVar` (rebuilding a `DeclarationIrBuilder` scoped to the nested `IrSimpleFunction`); leave the call statement itself structurally unchanged (only its lambda body's statements are replaced)
- [ ] **Step 4:** For `verifyAll`, same recursion but first prepend a freshly constructed local `ErrorCollector` declaration to the lambda body (real instance via constructor, not `ErrorRethrower.INSTANCE`) and append `errorCollector.validateCollectedErrors()` as the lambda's last statement; thread this fresh var as the collector for the recursive rewrite of that body only
- [ ] **Step 5:** Confirm nesting composes for free (a `verify`/`verifyEach` lambda body containing another `verify`/`verifyAll`/`verifyEach` call is rewritten correctly by the same recursive step)
- [ ] **Step 6:** `ConditionRewriter.rewrite()` (block-level entry point) now calls `rewriteConditionStatements` internally, bookended by `callBlockEntered`/`callBlockExited` as before — no behavior change for existing then/expect handling
- [ ] **Step 7:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 4: Wire then/expect blocks through the new recursive core

Confirm `FeatureRewriter` picks up the refactor transparently and verify/verifyAll/verifyEach work when called
directly inside `then`/`expect`.

**Files:**
- Modify (if needed): `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt`
- Test: compilation snapshot pairs (see Task 6)

- [ ] **Step 1:** Confirm `FeatureRewriter.rewriteBehaviorStatements`'s `hasConditions` check also fires when a then/expect block contains only a `verify`/`verifyAll`/`verifyEach` call and no bare top-level condition (the value recorder/error collector must still be declared)
- [ ] **Step 2:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 5: Add helper-method detection and rewriting

Extend the collector to notice plain (non-feature, non-fixture) methods of a `Specification` subclass that contain
`verify`/`verifyAll`/`verifyEach` calls, and rewrite them with their own freshly declared, method-scoped
`(ValueRecorder, ErrorCollector)` pair.

**Files:**
- Modify: `spockk-compiler-plugin/.../shared/SpockkTransformationContext.kt` — add `HelperMethodContext`
- Modify: `spockk-compiler-plugin/.../shared/MutableSpockkTransformationContext.kt` — track helper methods with conditions
- Modify: `spockk-compiler-plugin/.../collector/SpockkTransformationContextCollector.kt` — detect such methods in `visitBlockBody`'s else-branch (today's `bodyParser.getFeatureBody() == null` path silently drops everything; add the new scan there)
- Create: `spockk-compiler-plugin/.../transformer/condition/HelperMethodRewriter.kt`
- Modify: `spockk-compiler-plugin/.../transformer/SpockkIrTransformer.kt` — dispatch `HelperMethodRewriter` for registered helper methods

- [ ] **Step 1:** Add a statement-list scan (reusing the literal-lambda detection from Task 3) that answers "does this method contain a verify/verifyAll/verifyEach call anywhere reachable via direct statements and recursion into their lambda bodies" without treating bare booleans at the method's own top level as conditions
- [ ] **Step 2:** Register matching methods in a new context bucket (`HelperMethodContext`), parallel to `FeatureContext`/`fixtureMethods`
- [ ] **Step 3:** Create `HelperMethodRewriter`: declares a fresh `ValueRecorder` + `ErrorRethrower.INSTANCE`-backed `ErrorCollector` pair once at the top of the method (mirroring `FeatureRewriter.initializeValueRecorderStatement`/`initializeErrorCollectorStatement`, generalized), then calls the same `rewriteConditionStatements` core from Task 3 over the method's top-level statements with `treatBareBooleansAsConditions = false`
- [ ] **Step 4:** Wire dispatch into `SpockkIrTransformer.visitFunctionNew` alongside the existing `featureContext` lookup
- [ ] **Step 5:** Confirm a helper method with no verify/verifyAll/verifyEach calls is untouched (no spurious recorder/collector declarations)
- [ ] **Step 6:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 6: Validate condition rendering inside nested lambdas

**Files:**
- Modify (if needed): `spockk-compiler-plugin/.../transformer/ir/ConditionValueRecordingTransformer.kt`
- Test: extend `spockk-specs/.../runtime/ConditionRenderingTest.kt`

- [ ] **Step 1:** Write a rendering test for `verify(pc) { vendor == "Sunny" }` and compare against real Spock's `Condition(values, text, position, null, null, null).getRendering()` for the equivalent `with(pc) { vendor == "Sunny" }`
- [ ] **Step 2:** If the lambda's implicit extension-receiver parameter gets spuriously recorded (shifting indices), extend `visitGetValue`'s `<this>`-skip check to also skip it
- [ ] **Step 3:** Repeat for `verifyAll`/`verifyEach` (element as implicit receiver) and for a nested case (`verify` inside `verifyAll`)
- [ ] **Step 4:** Run `./gradlew :spockk-specs:test --rerun`, fix failures, commit

### Task 7: Add compilation snapshot tests

**Files:**
- Create: `spockk-specs/src/test/resources/samples/compilation/source/condition/{VerifyBasic,VerifyAllBasic,VerifyAllNoTarget,VerifyEachBasic,NestedVerifyInVerifyAll,VerifyInHelperMethod,VerifyWithNonLiteralLambda}.kt`
- Create: matching golden files under `spockk-specs/src/test/resources/samples/compilation/transformed/condition/`
- Modify: `spockk-specs/src/test/kotlin/.../compilation/condition/ImplicitConditionsCompilationTest.kt` (or a new `VerificationHelpersCompilationTest.kt`)

- [ ] **Step 1:** `verify(target) { ... }` inside a `then` block — golden shows ambient error collector reused, no fresh collector declared
- [ ] **Step 2:** `verifyAll(target) { ... }` and `verifyAll { ... }` — golden shows a fresh `ErrorCollector` declared inside the lambda and `validateCollectedErrors()` appended
- [ ] **Step 3:** `verifyEach(things) { ... }` — golden shows the lambda body rewritten with the ambient collector, no loop synthesized (the loop lives in the runtime function, not IR)
- [ ] **Step 4:** Nested `verify` inside `verifyAll` — golden shows the inner lambda using the fresh `verifyAll` collector
- [ ] **Step 5:** `verify`/`verifyAll` call inside a plain helper method (not a feature) — golden shows a method-scoped recorder/collector pair declared, bare booleans outside the lambda untouched
- [ ] **Step 6:** A `verify` call with a non-literal lambda argument (stored in a `val` first) — golden shows no special rewriting, ordinary call
- [ ] **Step 7:** Run `./gradlew :spockk-specs:test --rerun`, fix failures, commit

### Task 8: Add smoke tests for runtime pass/fail semantics

**Files:**
- Create: `spockk-specs/src/test/kotlin/.../smoke/condition/{VerifySmokeTest,VerifyAllSmokeTest,VerifyEachSmokeTest,VerifyInHelperMethodSmokeTest}.kt`

- [ ] **Step 1:** `VerifySmokeTest` — fails fast on first bad condition (`@FailsWith(ConditionNotSatisfiedError::class)`), passes when all conditions hold, target's members resolve without qualification
- [ ] **Step 2:** `VerifyAllSmokeTest` — single failing condition throws directly; multiple failing conditions aggregate into a multi-failure error listing all of them; all-passing block succeeds
- [ ] **Step 3:** `VerifyEachSmokeTest` — passes when all elements satisfy the block; a single failing element throws directly with item context in the message; multiple failing elements aggregate, and elements after a failure are still checked (not skipped); custom `namer` overload changes the reported name
- [ ] **Step 4:** `VerifyInHelperMethodSmokeTest` — a helper method using `verify`/`verifyAll`/`verifyEach`, called from a `then` block, reports failures the same way as if written inline
- [ ] **Step 5:** Run `./gradlew :spockk-specs:test`, fix failures, commit

### Task 9: Update spockk-docs

**Files:**
- Modify: relevant AsciiDoc page(s) under `spockk-docs` alongside the existing implicit-conditions documentation

- [ ] **Step 1:** Document `verify`/`verifyAll`/`verifyEach` usage, the then/expect + helper-method scope restriction, and the naming rationale (`verify` instead of `with`, to avoid colliding with Kotlin's stdlib `with`)
- [ ] **Step 2:** Run `./gradlew :spockk-docs:asciidoctor`, verify output, commit

### Task 10: Final verification

- [ ] **Step 1:** Run `./gradlew build` (full build: compile, test, spotless, detekt) at the repo root
- [ ] **Step 2:** Fix any remaining failures, commit
