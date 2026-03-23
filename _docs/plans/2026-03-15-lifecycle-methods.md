# Lifecycle Methods Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development
> (recommended) or superpowers:executing-plans to implement this plan task-by-task.
> Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add support for Spock's lifecycle fixture methods (`setup()`, `cleanup()`, `setupSpec()`, `cleanupSpec()`) and `setup`/`cleanup` block labels to Spockk.

**Architecture:** Extends the compiler plugin's collection and transformation phases. Block labels are registered in spockk-core, detected/validated in the collector, and transformed (de-virtualized, try-catch-finally wrapped) in the rewriter pipeline.

**Tech Stack:** Kotlin IR, JUnit Platform, Spock runtime

---

### Task 1: Add setup and cleanup block labels

Register `setup` and `cleanup` as first-class block labels in `spockk-core` and `spockk-compiler-plugin`. `setup` is an alias for `given` (same semantics, different name). `cleanup` is a new terminal block that can follow `then` or `expect`. Update the block validation state machine to accept both.

**Files:**
- Modify: `spockk-core/.../lang/Blocks.kt`
- Modify: `spockk-compiler-plugin/.../ir/IrIdentifiers.kt`
- Modify: `spockk-compiler-plugin/.../common/FeatureBlockLabel.kt`
- Modify: `spockk-compiler-plugin/.../common/FeatureBlockLabelIrElement.kt`
- Modify: `spockk-compiler-plugin/.../collector/ValidatingFeatureBlockCollector.kt`
- Test: `spockk-specs/.../compilation/FeatureBlockStructureValidationTest.kt`
- Test: `spockk-specs/.../smoke/SetupBlockSmokeTest.kt`

- [ ] **Step 1:** Add `setup` and `cleanup` label objects and functions to `Blocks.kt`
- [ ] **Step 2:** Add FQN constants `SETUP_BLOCK_FQN` and `CLEANUP_BLOCK_FQN` to `IrIdentifiers.kt`
- [ ] **Step 3:** Add `SETUP` and `CLEANUP` entries to `FeatureBlockLabel`
- [ ] **Step 4:** Add `Setup` and `Cleanup` data classes to `FeatureBlockLabelIrElement` and wire into `fromElement()`
- [ ] **Step 5:** Update `ValidatingFeatureBlockCollector` state machine — accept `Setup` in `INIT`, add `CLEANUP` state, add cleanup transitions from `EXPECTATION_EXPECT` and `EXPECTATION_THEN`
- [ ] **Step 6:** Write error scenario tests: `cleanup` before `then`/`expect`, `cleanup` followed by non-`and`, consecutive `setup` blocks
- [ ] **Step 7:** Write `SetupBlockSmokeTest` verifying `setup:` executes equivalently to `given:`
- [ ] **Step 8:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit

### Task 2: Add fixture method detection

Extend the collection phase to recognize and track fixture methods (`setup`, `cleanup`, `setupSpec`, `cleanupSpec`). Add compile-time validation (no duplicates, no block labels inside, no `super.` calls).

**Files:**
- Modify: `spockk-compiler-plugin/.../common/SpockkTransformationContext.kt`
- Modify: `spockk-compiler-plugin/.../common/MutableSpockkTransformationContext.kt`
- Modify: `spockk-compiler-plugin/.../collector/SpockkTransformationContextCollector.kt`
- Test: `spockk-specs/.../compilation/FixtureMethodValidationTest.kt`

- [ ] **Step 1:** Add `FixtureMethodKind` enum and `fixtureMethods` field to `SpecContext`
- [ ] **Step 2:** Add mutable fixture method tracking to `MutableSpecContext`
- [ ] **Step 3:** Modify `visitFunctionNew()` to detect fixture methods by name, call `addFixtureMethod()`, and return early
- [ ] **Step 4:** Add validation: duplicate fixture method, block labels inside fixture method, `super.` calls
- [ ] **Step 5:** Write `FixtureMethodValidationTest` covering all three error scenarios
- [ ] **Step 6:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit

### Task 3: Implement fixture method IR transformation

Transform detected fixture methods: make them `private` (de-virtualization) and validate that `setupSpec()`/`cleanupSpec()` do not access instance fields.

**Files:**
- Modify: `spockk-compiler-plugin/.../compilation/ir/IrFunction.kt`
- Create: `spockk-compiler-plugin/.../transformer/fixture/FixtureMethodRewriter.kt`
- Modify: `spockk-compiler-plugin/.../transformer/SpecRewriter.kt`
- Test: `spockk-specs/.../compilation/` (snapshot pairs for SetupMethod, CleanupMethod, SetupSpecMethod, CleanupSpecMethod)
- Test: `spockk-specs/.../compilation/FieldAccessCompilationTest.kt`
- Test: `spockk-specs/.../smoke/FixtureMethodsSmokeTest.kt`
- Test: `spockk-specs/.../smoke/FixtureMethodsInheritanceSmokeTest.kt`
- Test: `spockk-specs/.../runtime/FixtureMethodsEngineTest.kt`

- [ ] **Step 1:** Add `makePrivate()` extension to `IrFunction.kt`
- [ ] **Step 2:** Create `FixtureMethodRewriter` implementing `SpockkIrRewriter` with de-virtualization and field access validation
- [ ] **Step 3:** Wire `rewriteFixtureMethods()` into `SpecRewriter.rewrite()` after `rewriteWhereBlocks`
- [ ] **Step 4:** Write compilation snapshot tests for all four fixture method types
- [ ] **Step 5:** Write field access error scenario tests (`setupSpec`/`cleanupSpec` accessing instance fields)
- [ ] **Step 6:** Write `FixtureMethodsSmokeTest` and `FixtureMethodsInheritanceSmokeTest`
- [ ] **Step 7:** Write `FixtureMethodsEngineTest` for discovery and failure reporting
- [ ] **Step 8:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit

### Task 4: Implement cleanup block IR transformation

Transform features with `cleanup:` blocks by wrapping the feature body in a try-catch-finally structure with exception suppression semantics.

**Files:**
- Modify: `spockk-compiler-plugin/.../common/SpockkTransformationContext.kt`
- Create: `spockk-compiler-plugin/.../transformer/cleanup/CleanupBlockRewriter.kt`
- Modify: `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt`
- Modify: `spockk-compiler-plugin/.../compilation/ir/DeclarationIrBuilder.kt` (add `irTryCatchFinally`, `irCatch` helpers)
- Test: `spockk-specs/.../compilation/` (snapshot pairs for CleanupBlock, CleanupBlockWithWhere)
- Test: `spockk-specs/.../smoke/CleanupBlockSmokeTest.kt`
- Test: `spockk-specs/.../smoke/CleanupBlockExceptionHandlingSmokeTest.kt`

- [ ] **Step 1:** Extend `FeatureContext` to partition blocks into `featureBlocks`, `cleanupBlocks`, and `dataProviderBlocks`
- [ ] **Step 2:** Add IR builder helpers (`irTryCatchFinally`, `irCatch`) to `DeclarationIrBuilder.kt`
- [ ] **Step 3:** Create `CleanupBlockRewriter` generating the full try-catch-finally IR structure
- [ ] **Step 4:** Update `FeatureRewriter.rewriteFeatureStatements()` to delegate to `CleanupBlockRewriter` when cleanup blocks are present
- [ ] **Step 5:** Write compilation snapshot tests for cleanup block and cleanup+where
- [ ] **Step 6:** Write `CleanupBlockSmokeTest` and `CleanupBlockExceptionHandlingSmokeTest`
- [ ] **Step 7:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit

### Task 5: Add smoke and runtime tests

Complete the end-to-end and engine-level test suite for lifecycle methods, ensuring coverage matches Spock's own test suite.

**Files:**
- Create/Modify: `spockk-specs/.../smoke/FixtureMethodsSmokeTest.kt`
- Create/Modify: `spockk-specs/.../smoke/FixtureMethodsInheritanceSmokeTest.kt`
- Create/Modify: `spockk-specs/.../smoke/CleanupBlockSmokeTest.kt`
- Create/Modify: `spockk-specs/.../smoke/CleanupBlockExceptionHandlingSmokeTest.kt`
- Create/Modify: `spockk-specs/.../runtime/FixtureMethodsEngineTest.kt`

- [ ] **Step 1:** Ensure `FixtureMethodsSmokeTest` covers: execution order, cleanup-on-setup-failure, no-cleanup-on-field-init-failure
- [ ] **Step 2:** Ensure `FixtureMethodsInheritanceSmokeTest` covers parent/child ordering in both directions
- [ ] **Step 3:** Ensure `CleanupBlockSmokeTest` covers: runs on success, runs on failure, variable accessibility
- [ ] **Step 4:** Ensure `CleanupBlockExceptionHandlingSmokeTest` covers exception suppression semantics
- [ ] **Step 5:** Ensure `FixtureMethodsEngineTest` covers discovery, setupSpec failure, per-iteration independence
- [ ] **Step 6:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit

### Task 6: Update IntelliJ plugin

Suppress "unused expression" warnings for `setup` and `cleanup` block label references.

**Files:**
- Modify: `spockk-intellij-plugin/.../Psi.kt`

- [ ] **Step 1:** Add `io.github.pshevche.spockk.lang.setup` and `io.github.pshevche.spockk.lang.cleanup` to `SPOCKK_BLOCKS_FQN`
- [ ] **Step 2:** Manually verify in IntelliJ IDEA: no warnings for `setup:` and `cleanup:` blocks, existing blocks still suppressed
- [ ] **Step 3:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit
