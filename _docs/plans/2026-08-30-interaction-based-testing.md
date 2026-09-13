# Interaction-Based Testing (Preview) Implementation Plan

**Goal:** Make Spock's mock/stub interaction verification and stubbing work in Spockk with a Kotlin-idiomatic
syntax: `N * obj.method(args)` cardinality, `any()`/`anyMethod()` matchers, `does`/`did`/`returns`/`returned`
response infixes, `capture`/`slot` argument capture, `noMoreInteractions(...)` sugar, and a `Mock`/`Stub` trailing
builder-block for eager stub configuration. Ships enabled by default, no feature flag.

**Architecture:** New marker declarations in `spockk-core` (throw if unrewritten, same convention as
`given`/`when`/`then`). A new `InteractionStatementsRewriter` recognizes the marker call shapes and builds real
`org.spockframework.mock.runtime.InteractionBuilder`/`MockController` calls (both already shaded unmodified into
`spockk-core`) - no matching/verification logic reimplemented. `ConditionStatementsRewriter` routes interaction
statements to it instead of treating them as boolean conditions. `FeatureRewriter` gets a WHEN/THEN lookahead
(mirroring the existing exception-condition one) that brackets a `when:` block with `enterScope()` +
moved-interaction-registration when its paired `then:` block declares interactions, and inserts `leaveScope()` at
the start of that `then:` block. `MockingApiTransformer` gets a second, 2-arg `Mock`/`Stub` overload for the
builder-block syntax, registering its interactions eagerly (no scope wrapping).

See design doc: `_docs/specs/2026-08-30-interaction-based-testing-design.md` - read it in full before starting,
it has the exact confirmed Spock runtime API surface (method signatures, `Wildcard` sentinel, `moveInteractions`
shape) this plan builds against.

**Tech Stack:** Kotlin IR, Spock runtime (shaded `InteractionBuilder`, `MockController`, `SpecificationContext`,
`Wildcard`, `groovy.lang.Closure`)

---

### Task 1: `spockk-core` - marker declarations and real runtime pieces

**Files:**
- Create: `spockk-core/src/main/kotlin/io/github/pshevche/spockk/lang/Interactions.kt`
- Create: `spockk-core/src/main/kotlin/io/github/pshevche/spockk/lang/CapturedArg.kt`

- [ ] **Step 1:** In `Interactions.kt`, following `Blocks.kt`'s `throwIllegalLabelUsageException`-style convention
  (add a parallel `throwIllegalInteractionUsageException(label: String): Nothing`), declare the pure markers:
  `fun <T> any(): T`, `fun <T> T.anyMethod(): Nothing`, `operator fun <T> Int.times(call: T): T`,
  `operator fun <T> IntRange.times(call: T): T`, `infix fun <T> T.does(block: () -> Unit): T`,
  `infix fun <T> T.did(block: () -> Unit): T`, `infix fun <T> T.returns(value: T): T`,
  `infix fun <T> T.returned(value: T): T`, `fun <T> capture(slot: CapturedArg<T>): T`,
  `fun noMoreInteractions(vararg mocks: Any?)` - every body throws.
- [ ] **Step 2:** In `CapturedArg.kt`: `class CapturedArg<T>` with an internal mutable backing field, a public
  `val captured: T` getter that throws `IllegalStateException("No value captured yet")` if nothing was recorded,
  and an internal `fun set(value: T)` the generated `addCodeArg` closure will call. Plus `fun <T> slot(): CapturedArg<T> = CapturedArg()`
  (a real constructor call, not a marker - `slot()` itself needs no rewriting, only `capture(slot)` does).
- [ ] **Step 3:** Add the `groovy.lang.Closure` adapter (name TBD during implementation, e.g. `internal fun <R> closureOf(paramType: Class<*>?, action: (Any?) -> R): Closure<R>`) used internally by generated code for `does`/`did`/`capture`. Before writing this, fetch and read `CodeResponseGenerator`/`CodeArgumentConstraint` source in full (`org.spockframework.mock.response`/`org.spockframework.mock.constraint`) to confirm exactly how Groovy's `Closure.call()` dispatches to a subclass (`doCall` by convention - verify signature/arity requirements) and what `CodeArgumentConstraint` passes for a match test (confirmed for responses: `IMockInvocation` if the closure's sole declared parameter is that type, else `invocation.getArguments()` - argument matching is presumably symmetric but unconfirmed, verify it directly). Write a throwaway Groovy/Kotlin smoke check if needed to nail this before wiring it into IR generation.
- [ ] **Step 4:** Run `./gradlew :spockk-core:compileKotlin`, commit.

### Task 2: IR identifiers and detectors

**Files:**
- Modify: `spockk-compiler-plugin/.../compilation/ir/IrIdentifiers.kt`
- Create: `spockk-compiler-plugin/.../compilation/transformer/interaction/InteractionStatementShape.kt` (or similar - detection helpers)

- [ ] **Step 1:** Add FQNs under `IrIdentifiers.Spockk` for `any`, `anyMethod`, `does`, `did`, `returns`, `returned`,
  `capture`, `noMoreInteractions`, and under `IrIdentifiers.Spock` (new `MOCK_PKG_FQN = FqName("org.spockframework.mock")`,
  `MOCK_RUNTIME_PKG_FQN = MOCK_PKG_FQN.child("runtime")`, `LANG_PKG_FQN2 = FqName("org.spockframework.lang")`)
  add `INTERACTION_BUILDER_FQN`, `MOCK_CONTROLLER_FQN`, `WILDCARD_FQN` (Spock's own, distinct from Spockk's existing
  `Specification._`-based `WILDCARD_FQN` already in the file - rename/namespace carefully to avoid collision, check
  the existing `WILDCARD_FQN` isn't already used for something this feature needs to keep working). `Int.times`/
  `IntRange.times` don't need their own FQN lookup if detection is done structurally (see Step 2), but note Kotlin's
  *built-in* `Int.times(Int)` FQN too, so the detector can positively distinguish the Spockk marker overload from an
  ordinary integer multiplication that happens to appear as a bare statement (extremely unlikely in test code, but
  don't rely on "no real `Int*Int` statement ever appears bare in a then-block" - check the resolved callee's FQN,
  not just the operator shape).
- [ ] **Step 2:** Add detection helpers (mirrors `isExceptionConditionCall()`/`asImplicitAssertionHelperCall()`):
  `IrStatement.asInteractionStatement(): InteractionStatement?` that recognizes, at the top of a statement: (a) a
  call to the Spockk `times` marker (cardinality present) wrapping a call chain that may itself be wrapped in
  `does`/`did`/`returns`/`returned`, or (b) a bare call (no cardinality) wrapped in `does`/`did`/`returns`/`returned`
  directly (the `given:`/`Stub{}`-block shape) or a bare mock method call with no wrapper at all (still a valid
  interaction with no response, e.g. `1 * obj.setName(any())` with nothing after it). Unwrap layer by layer down to
  the innermost `IrCall` (the actual `obj.method(args)` shape) and to the cardinality expression, response
  expression, and response kind, returning a data structure the rewriter (Task 4) can consume without needing to
  re-parse the IR shape.
- [ ] **Step 3:** Add `fun List<IrStatement>.hasInteractionStatement(): Boolean`, used by Task 5's when/then
  lookahead.
- [ ] **Step 4:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit.

### Task 3: `IrInteractionBuilder`/`IrMockController` runtime wrappers

**Files:**
- Create: `spockk-compiler-plugin/.../compilation/transformer/ir/IrInteractionBuilder.kt`
- Create: `spockk-compiler-plugin/.../compilation/transformer/ir/IrMockController.kt`

- [ ] **Step 1:** `IrInteractionBuilder`, mirroring `IrSpecInternals`'s one-wrapper-per-Spock-class convention:
  resolve `INTERACTION_BUILDER_FQN` via `findRequiredClassSymbol`, expose one `irXxx` method per
  `InteractionBuilder` fluent method needed (`irNew` for the `(line, column, text)` constructor - check what
  `ConditionRewriter`/`ExpressionInfoBuilder` already does for capturing a condition's source line/column/text,
  reuse that same source-position/text-extraction utility rather than re-deriving it - grep `sourceTextCache`/
  `SourceTextCache` usage in `IrSpockRuntime`/`ConditionRewriter` first), `irSetFixedCount`, `irSetRangeCount`,
  `irAddEqualTarget`, `irAddEqualMethodName`, `irAddEqualArg`, `irAddCodeArg`, `irAddConstantResponse`,
  `irAddCodeResponse`, `irBuild`. Each returns the fluent chain's `IrCall` so the rewriter (Task 4) can nest them.
- [ ] **Step 2:** `IrMockController`: resolve `MOCK_CONTROLLER_FQN` (or the `IMockController` interface FQN -
  confirm during implementation whether `addInteraction`/`enterScope`/`leaveScope` are declared on the interface
  `SpecificationContext.getMockController()` returns, or only on the concrete class requiring an `irAs` cast the
  same way `IrSpecificationContext.setThrownException` needed one for a package-private concrete-class method).
  Expose `irGetMockController(builder, specAccessor)` (via `IrSpecificationContext`-style
  `getSpecificationContext().getMockController()` chain - consider adding this as a new method on the existing
  `IrSpecificationContext` instead of a new class, if `getMockController()` genuinely lives there; decide during
  implementation based on Step 1's interface-vs-class finding), `irAddInteraction(builder, controllerExpr, interactionExpr)`,
  `irEnterScope(builder, controllerExpr)`, `irLeaveScope(builder, controllerExpr)`.
- [ ] **Step 3:** Wire both into `SpockkIrRewriterContext` alongside `spockRuntime`/`specInternals`.
- [ ] **Step 4:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit.

### Task 4: `InteractionStatementsRewriter`

**Files:**
- Create: `spockk-compiler-plugin/.../compilation/transformer/interaction/InteractionStatementsRewriter.kt`

- [ ] **Step 1:** `rewrite(statement: InteractionStatement, builder, specAccessor): IrStatement` - given the parsed
  shape from Task 2 Step 2, build the fluent `InteractionBuilder` chain: `new InteractionBuilder(line, col, text)`,
  `.setFixedCount(N)` or `.setRangeCount(from, to, inclusive)`, `.addEqualTarget(<the call's dispatch receiver
  expression>)`, `.addEqualMethodName(<literal name, or Wildcard sentinel for anyMethod()>)`, then for each
  argument in the original call: if the argument expression is a call to the `any()` marker -> `.addEqualArg(Wildcard.INSTANCE)`;
  if it's a call to `capture(slot)` -> `.addCodeArg(<closure adapter wrapping a lambda that calls slot.set(arg) and returns true>)`;
  otherwise -> `.addEqualArg(<the argument expression itself, unchanged - literal equality>)`. Then, if a response
  was present: `does`/`did` -> `.addCodeResponse(<closure adapter wrapping the block>)`; `returns`/`returned` ->
  `.addConstantResponse(<the value expression>)`. Finally `.build()`, then
  `mockController.addInteraction(<built interaction>)`.
- [ ] **Step 2:** Handle `noMoreInteractions(mock1, mock2, ...)` as sugar: for each vararg argument, synthesize the
  equivalent of `0 * mock.anyMethod()` and run it through the same Step 1 logic (`setFixedCount(0)`,
  `addEqualTarget(mock)`, `addEqualMethodName(Wildcard sentinel)`, no args/response).
  the same "given-block: eager, no scope" path as the `Mock`/`Stub` builder block (Task 6) - `noMoreInteractions`
  is not itself scope-wrapped, it registers a real interaction the *existing* active scope's verification will
  check against.
- [ ] **Step 3:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit.

### Task 5: Wire into `ConditionStatementsRewriter` and `FeatureRewriter` (when/then scoping)

**Files:**
- Modify: `spockk-compiler-plugin/.../compilation/transformer/condition/ConditionStatementsRewriter.kt`
- Modify: `spockk-compiler-plugin/.../compilation/transformer/FeatureRewriter.kt`
- Create: `spockk-compiler-plugin/.../compilation/transformer/interaction/InteractionScopeRewriter.kt` (the
  when-block `enterScope()`/moved-statements wrapper, and the then-block `leaveScope()` insertion - mirrors
  `WhenBlockRewriter`'s shape but note it also needs to *move* the extracted interaction-building statements out
  of the `then:` block and into the position right before the `when:` block's own statements, per Spock's own
  `moveInteractions` - re-read `WhenBlockRewriter.kt` and `ExceptionConditionRewriter.kt` in full before writing
  this, the shape is close but not identical: exception conditions rewrite *in place*, interactions *move*)

- [ ] **Step 1:** Re-read `rewriteBehaviorStatements` in `FeatureRewriter.kt` in full first (confirm it hasn't
  changed further since the exception-conditions work).
- [ ] **Step 2:** In `ConditionStatementsRewriter.rewrite`, add a branch: `statement.asInteractionStatement() != null`
  (from Task 2 Step 2) -> delegate to `InteractionStatementsRewriter` (Task 4) instead of treating it as a
  condition. Ordering matters versus the existing `isConditionStatement` check - an interaction statement must
  never fall through to condition treatment.
- [ ] **Step 3:** Extend `FeatureRewriter`'s existing WHEN/THEN pre-scan (added for exception conditions) with a
  parallel `hasInteractionStatement()` check on `behaviorBlocks[i+1]`. A WHEN block needing *either* treatment (or
  both - an exception condition and an interaction can coexist in the same `then:`) needs both wrappers composed:
  confirm the correct nesting order (interaction scope entry/exit around the *outside*, exception try/catch around
  the `when:` block's own statements *inside* that - i.e. `enterScope(); try { ...when-statements... } catch { ... }; `
  with `leaveScope()` at the start of `then:`, after the exception-handling machinery already there) - write this
  out explicitly before coding it, get it reviewed against Spock's own combined behavior if both features are
  simultaneously exercised in one `when`/`then` pair (a real test case, not just a mental check - add it to Task 7).
- [ ] **Step 4:** For a `THEN` block that has interactions: extract the interaction statements (leaving any
  remaining plain conditions/exception-condition calls in place, in original order, matching Spock's own
  `block.getAst().removeIf(interactions::contains)` behavior), run each through `InteractionStatementsRewriter`,
  insert the built statements *before* the paired `when:` block's own statements (after the `enterScope()` call),
  and insert `mockController.leaveScope()` as the *first* statement of the `then:` block's rewritten output.
- [ ] **Step 5:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit.

### Task 6: `Mock`/`Stub` trailing-lambda builder block

**Files:**
- Modify: `spockk-core/.../lang/Interactions.kt` (or a new small file) - add the 2-arg marker overloads
- Modify: `spockk-compiler-plugin/.../compilation/transformer/mock/MockingApiTransformer.kt`

- [ ] **Step 1:** Add `fun <T> Mock(type: Class<T>, block: T.() -> Unit): T` and
  `fun <T> Stub(type: Class<T>, block: T.() -> Unit): T` to `spockk-core`, throwing bodies.
- [ ] **Step 2:** In `MockingApiTransformer`, detect this 2-arg shape (distinct from the existing 0/1-arg
  `Mock`/`Stub`/`Spy` detection already there - re-read the file in full first) at a `visitVariable`/`visitCall`
  site. Rewrite to the existing `MockImpl`/`StubImpl` construction unchanged, then process the trailing lambda's
  statements: each is an interaction statement (per Task 2's detector, no cardinality prefix expected/required
  here per the design doc - a bare `does`/`returns`-wrapped call, or a bare call with no response at all), run
  each through `InteractionStatementsRewriter` (Task 4), and emit the built `addInteraction` calls directly after
  the mock's construction - no `enterScope`/`leaveScope`.
- [ ] **Step 3:** Run `./gradlew :spockk-compiler-plugin:compileKotlin`, commit.

### Task 7: Compilation snapshot tests

**Files:**
- Create: `spockk-specs/src/test/resources/samples/compilation/source/interaction/{BasicCardinality,AnyMatcher,ReturnsResponse,DoesResponse,CaptureArgument,AnyMethodWildcard,NoMoreInteractions,StubBuilderBlock,InteractionWithExceptionCondition}.kt`
- Create: matching golden files under `.../transformed/interaction/`
- Follow the existing compilation-test harness convention (check `spockk-specs/.../compilation/` test classes for
  the exact `TransformationSample`/`BaseCompilationTest` usage pattern first)

- [ ] **Step 1-9:** One pair per scenario above, confirming the generated IR shape - especially the when/then
  bracketing (Task 5) and the interaction-move-not-copy behavior.
- [ ] **Step 10:** Run `./gradlew :spockk-specs:test --rerun`, fix failures, commit.

### Task 8: Smoke tests (real runtime behavior)

**Files:**
- Create: `spockk-specs/src/test/kotlin/io/github/pshevche/spockk/smoke/mock/InteractionsSmokeTest.kt`

- [ ] **Step 1:** Cardinality satisfied/unsatisfied (too few/too many invocations - confirm the real
  `TooFewInvocationsError`/`TooManyInvocationsError` (or whatever Spock's `verifyInteractions()` actually throws -
  confirm exact type) surfaces correctly through JUnit Platform, same "real Spock error type, not reimplemented"
  principle as `WrongExceptionThrownError` in the exception-conditions work.
- [ ] **Step 2:** `any()` matches any value of the right type; a literal-equality arg only matches that exact value.
- [ ] **Step 3:** `returns`/`returned` produce the stubbed value; `does`/`did` run their side effect and (if the
  method has a non-Unit return type) still return correctly (confirm what a `does {}` with no explicit response
  returns for a non-Unit method - likely Spock's own default-value-for-unstubbed-response behavior, confirm and
  document).
- [ ] **Step 4:** `capture`/`slot` records the actual invocation argument, readable via `.captured` after the
  `when:` block runs.
- [ ] **Step 5:** `anyMethod()` matches any method name; `noMoreInteractions(obj)` fails when an un-declared call
  happened, passes when it didn't.
- [ ] **Step 6:** `Stub(Type::class.java) { ... }`/`Mock(Type::class.java) { ... }` builder-block interactions are
  active for the whole feature (not scope-limited to one `when:`/`then:` pair).
- [ ] **Step 7:** A `then:` block combining an interaction *and* a plain boolean condition *and* (separately) a
  case combining an interaction with `thrown()` in the same block, confirming Task 5 Step 3's nesting is correct.
- [ ] **Step 8:** Run `./gradlew :spockk-specs:test`, fix failures, commit.

### Task 9: Update issue and final verification

- [ ] **Step 1:** Check off completed acceptance criteria on [#204](https://github.com/pshevche/spockk/issues/204);
  note the explicitly out-of-scope items from the design doc as documented limitations, not silent gaps.
- [ ] **Step 2:** Run `./gradlew build` (full build: compile, test, spotless, detekt) at the repo root.
- [ ] **Step 3:** Two rounds of self code review (diff-level - correctness, reuse/simplification, efficiency) before
  handing off; fix findings between rounds.
- [ ] **Step 4:** Fix any remaining failures, commit, push to `claude/issue-204-syntax-review-r1r7gm`.
