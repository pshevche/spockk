# Exception Conditions Implementation Plan

**Goal:** Make Spock's `thrown(Type)`, `notThrown(Type)`, `noExceptionThrown()` exception-condition helpers work in
Spockk. A `when` block immediately followed by a `then` block containing one of these calls gets its statements
wrapped in a try/catch that records the thrown exception (if any) on `SpecificationContext`; `thrown(Type::class.java)`
is rewritten to call Spock's own already-shaded `SpecInternals.checkExceptionThrown`; `notThrown`/`noExceptionThrown`
need no call-site rewriting at all since their real inherited bodies already read the recorded exception correctly.

**Architecture:** New `WhenBlockRewriter` (mirrors `CleanupBlockRewriter`'s try/catch-wrapping pattern - no variable
hoisting needed, Kotlin IR resolves locals by symbol not lexical nesting) wraps a `when` block's statements when its
paired `then` block needs it. A new, separate, non-recursive `ExceptionConditionRewriter` scans a `then` block's own
top-level statements for exception-condition calls, rewrites `thrown(Type)` calls, and validates at-most-one -
deliberately kept out of the existing shared `ConditionStatementsRewriter` (used for `then`/`expect`/`verify`/
`verifyAll`/`verifyEach`/helper methods) to avoid complicating that recursive abstraction with a then-block-only,
non-recursive concern. `FeatureRewriter` pre-scans `featureBody.behaviorBlocks` once to find WHEN/THEN pairs that
need this treatment (a WHEN block in `behaviorBlocks` is always immediately followed by exactly one THEN block, per
`BlockOrderValidatingFeatureStatementsCollector`'s state machine).

See design doc: `_docs/specs/2026-08-09-exception-conditions-design.md`

**Tech Stack:** Kotlin IR, Spock runtime (shaded `SpecInternals`, `SpecificationContext`, `WrongExceptionThrownError`,
`UnallowedExceptionThrownError`, `InvalidSpecException`)

---

### Task 1: Add IR identifiers and detectors

**Files:**
- Modify: `spockk-compiler-plugin/src/main/kotlin/io/github/pshevche/spockk/compilation/ir/IrIdentifiers.kt`
- Modify: `spockk-compiler-plugin/src/main/kotlin/io/github/pshevche/spockk/compilation/ir/IrStatement.kt` (or wherever `isAssertCall()` currently lives - re-locate it first, it may have moved since last read)

- [ ] **Step 1:** Add `THROWN_FQN`, `NOT_THROWN_FQN`, `NO_EXCEPTION_THROWN_FQN` to `IrIdentifiers.Spock`, as children of the existing `SPECIFICATION_FQN` (matching how `WILDCARD_FQN`/`SHARED_ANNOTATION_FQN` are namespaced)
- [ ] **Step 2:** Add `isExceptionConditionCall(): Boolean` next to `isAssertCall()` - same FqName-match-ignoring-overload technique (`(this as? IrCall)?.symbol?.owner?.fqNameWhenAvailable` against the three new FQNs), matching any of `thrown`/`notThrown`/`noExceptionThrown` regardless of arity
- [ ] **Step 3:** Add a narrower `isThrownCall(): Boolean` (FqName == `THROWN_FQN` only) for the rewrite step (Task 3) to distinguish "detected, needs rewriting" from "detected, no rewrite needed"
- [ ] **Step 4:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 2: Add runtime call-building wrappers

**Files:**
- Modify: `spockk-compiler-plugin/.../transformer/ir/IrSpecificationContext.kt`
- Create: `spockk-compiler-plugin/.../transformer/ir/IrSpecInternals.kt`

- [ ] **Step 1:** In `IrSpecificationContext`, add `irSetThrownException(builder, specAccessor, throwableExprOrNull): IrExpression` - same `getSpecificationContextInstance()` + `specificationContextClass.functionByName("setThrownException")` pattern already used for `irSetCurrentBlock`
- [ ] **Step 2:** Create `IrSpecInternals`, mirroring `IrSpockRuntime`'s one-wrapper-class-per-Spock-class convention: resolve `SPEC_INTERNALS_FQN` (already declared in `IrIdentifiers.kt`, currently unused) once via `findRequiredClassSymbol`, expose `irCheckExceptionThrown(builder, specAccessor, exceptionTypeClassExpr): IrCall` building a static call to `SpecInternals.checkExceptionThrown(Specification, Class<? extends Throwable>)` (`functionByName("checkExceptionThrown")`, `arguments[0] = irGet(specAccessor)`, `arguments[1] = exceptionTypeClassExpr`)
- [ ] **Step 3:** Wire `IrSpecInternals` into `SpockkIrRewriterContext` (same place `spockRuntime`/`IrSpockRuntime` is constructed), so it's reachable the same way (`rewriterContext.specInternals.irCheckExceptionThrown(...)`)
- [ ] **Step 4:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 3: Add `ExceptionConditionRewriter` (detection, `thrown` rewrite, validation)

**Files:**
- Create: `spockk-compiler-plugin/.../transformer/condition/ExceptionConditionRewriter.kt`
- Create: `spockk-compiler-plugin/.../transformer/condition/InvalidExceptionConditionExceptionFactory.kt` (mirrors `InvalidParametrizationExceptionFactory`)

- [ ] **Step 1:** Add a scan helper: given a `THEN` block's statement list, find all top-level exception-condition calls - checking both bare `IrExpression` statements (`isExceptionConditionCall()`) and `IrVariable.initializer` expressions (a `val e = thrown(...)` shape) - returning enough info to know count (for validation) and, for `thrown` specifically, where to substitute
- [ ] **Step 2:** `InvalidExceptionConditionExceptionFactory(file, irElement)` - one factory method, `multipleExceptionConditionsException()`, message text adapted from Spock's ("A 'then' block may only have a single exception condition"), throwing `CompilationException` (same `org.jetbrains.kotlin.backend.common.CompilationException(message, file, irElement)` constructor `InvalidParametrizationExceptionFactory` uses)
- [ ] **Step 3:** `ExceptionConditionRewriter.rewrite(statements: List<IrStatement>): List<IrStatement>` - if more than one exception-condition call found, throw via the new factory; otherwise, for the single `thrown(...)` match (if any - `notThrown`/`noExceptionThrown` need no rewrite per the design doc), substitute in place: for a bare statement, replace it with `irAs(specInternals.irCheckExceptionThrown(builder, feature.requiredThisParameter(), originalCallArg), originalCall.type)`; for a `val`/`var` declaration, mutate `IrVariable.initializer` the same way (matching the existing in-place-mutation style `ConditionStatementsRewriter.rewriteHelperCallLambdaBody` already uses on `lambdaStatements`)
- [ ] **Step 4:** Add a `fun List<IrStatement>.hasExceptionCondition(): Boolean` used by Task 5's when-block-pairing pre-scan (same detection as Step 1, boolean form)
- [ ] **Step 5:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 4: Add `WhenBlockRewriter`

**Files:**
- Create: `spockk-compiler-plugin/.../transformer/condition/WhenBlockRewriter.kt`

- [ ] **Step 1:** `WhenBlockRewriter(rewriterContext, feature, whenBlock: FeatureBlock).rewrite(): List<IrStatement>` - mirrors `CleanupBlockRewriter`'s shape: `specificationContext.irSetThrownException(builder, specAccessor, irNull())`, then `callBlockEntered`, then `irTry(tryExpressions = whenBlock.statements, catchExpressions = [irCatch(catchVar, specificationContext.irSetThrownException(builder, specAccessor, irGet(catchVar)))], finallyExpressions = [])` (no rethrow - the exception is consumed), then `callBlockExited`. No variable hoisting (see design doc's simplification #3 - `CleanupBlockRewriter` is direct existing proof this is safe)
- [ ] **Step 2:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 5: Wire into `FeatureRewriter`

**Files:**
- Modify: `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt`

- [ ] **Step 1:** Re-read `rewriteBehaviorStatements` in full first (confirm it hasn't changed further since this session's research) before editing
- [ ] **Step 2:** Before the main loop, pre-scan `featureBody.behaviorBlocks`: for each `WHEN` block at index `i`, check whether `behaviorBlocks[i+1]` (guaranteed to be the paired `THEN` block) has an exception condition (`hasExceptionCondition()` from Task 3); collect the set of WHEN-block ordinals (or indices) needing wrapping
- [ ] **Step 3:** Change the main loop from `forEach` to an indexed iteration (`withIndex()`). For a `WHEN` block in the needs-wrapping set: route through `WhenBlockRewriter` instead of the generic `else` branch. For a `THEN` block whose *previous* block was a wrapped `WHEN`: run `ExceptionConditionRewriter.rewrite(it.statements)` first, then pass the result into the existing (unmodified) `ConditionRewriter(...).rewrite(...)` exactly as today. Every other block (`SETUP`/`GIVEN`/unpaired `WHEN`/`EXPECT`/unpaired `THEN`) is unchanged
- [ ] **Step 4:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit

### Task 6: Compilation snapshot tests

**Files:**
- Create: `spockk-specs/src/test/resources/samples/compilation/source/condition/{ThrownBasic,ThrownWithVariable,NotThrown,NoExceptionThrown,WhenBlockWithVariableDefs,MultipleWhenThenPairs,NoExceptionConditionNoWrapping}.kt`
- Create: matching golden files under `spockk-specs/src/test/resources/samples/compilation/transformed/condition/`
- Modify/Create: a compilation test class following the existing convention (check `ImplicitConditionsCompilationTest.kt` or equivalent for the exact harness)

- [ ] **Step 1:** `thrown(Type::class.java)` as a bare `then` statement - golden shows when-block wrapped, `thrown` call replaced with the `SpecInternals.checkExceptionThrown` + cast
- [ ] **Step 2:** `val e = thrown(Type::class.java)` - golden shows the variable's initializer rewritten, not the statement itself
- [ ] **Step 3:** `notThrown(Type::class.java)` / `noExceptionThrown()` - golden shows when-block wrapped, call itself unchanged
- [ ] **Step 4:** `when` block with variable declarations later read in `then` - golden confirms no hoisting occurs and the variable remains correctly referenced
- [ ] **Step 5:** Multiple `when`/`then` pairs in one feature, only one containing an exception condition - golden confirms only the paired `when` gets wrapped
- [ ] **Step 6:** A `when`/`then` pair with no exception-condition call - golden confirms it's untouched (still the plain `blockEntered`/statements/`blockExited` shape)
- [ ] **Step 7:** Run `./gradlew :spockk-specs:test --rerun`, fix failures, commit

### Task 7: Update smoke tests

**Files:**
- Modify: `spockk-specs/src/test/kotlin/io/github/pshevche/spockk/smoke/condition/ExceptionConditionsSmokeTest.kt`

- [ ] **Step 1:** Flip the existing `@FailsWith`-pinned-broken-behavior scenarios to their real, correct outcomes (basic `thrown()` catches; `noExceptionThrown()`/`notThrown()` correctly reject an actual exception with `UnallowedExceptionThrownError`)
- [ ] **Step 2:** Add scenarios mirroring Spock's own `ExceptionConditions.groovy`: catching `Exception`/`RuntimeException`/`Error`/`Throwable`; wrong exception type -> `WrongExceptionThrownError`; `thrown(null)` -> `InvalidSpecException`; multiple `when`/`then` pairs each with their own exception condition; at-most-one violation -> compile error (likely needs a separate inline-spec-based test per this project's "Error scenarios -> inline specs" convention, using `TestDataFactory.specWithFeatureBody()`/`transform()`, not a smoke test)
- [ ] **Step 3:** Confirm `expect`-block usage still throws the fallback `InvalidSpecException` unchanged (already-correct case, regression guard)
- [ ] **Step 4:** Update the class doc comment - it currently describes the gap as unimplemented; correct it to describe what's now supported and what's deferred (linking the design doc)
- [ ] **Step 5:** Run `./gradlew :spockk-specs:test`, fix failures, commit

### Task 8: Update issue and final verification

- [ ] **Step 1:** Check off completed acceptance criteria on [#271](https://github.com/pshevche/spockk/issues/271); leave deferred items noted as follow-ups, don't close the issue over them
- [ ] **Step 2:** Run `./gradlew build` (full build: compile, test, spotless, detekt) at the repo root
- [ ] **Step 3:** Fix any remaining failures, commit, push to `claude/spockk-exception-assertions-88u7de`
