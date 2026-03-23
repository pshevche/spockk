# Align IR Transformations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development
> (recommended) or superpowers:executing-plans to implement this plan task-by-task.
> Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close gaps between Spockk's IR output and what Spock's runtime expects — fix block metadata, add block entry/exit calls, add mock scope cleanup, and complete the cleanup block transformation.

**Architecture:** All changes are in `spockk-compiler-plugin` transformers (`FeatureRewriter`, `CleanupBlockRewriter`) and `spockk-specs` compilation snapshots.

**Tech Stack:** Kotlin IR, Spock runtime (`SpockRuntime`, `SpecificationContext`, `MockController`)

---

### Task 1: Fix `given:` and `and:` BlockKind mapping

Fix two block label mapping issues so that `given:` blocks appear in `@FeatureMetadata.blocks` as `BlockKind.SETUP`, and `and:` blocks merge their descriptions into the preceding block's `texts` array (matching Spock's `SpecParser` behaviour).

Correct block metadata is a prerequisite for block entry/exit calls (Task 2) and cleanup block context restoration (Task 3).

**Files:**
- Modify: `spockk-compiler-plugin/.../common/FeatureBlockLabel.kt`
- Modify: `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt`
- Test: Compilation snapshot `FeatureWithMultipleBlocksAndDescriptions.kt`
- Test: All other snapshots with `given:` blocks

- [ ] **Step 1:** Change `GIVEN` blockKind from `null` to `"SETUP"` in `FeatureBlockLabel.kt`
- [ ] **Step 2:** Add `doMergeBlocks()` method to `FeatureRewriter` that merges `and:` descriptions into the predecessor block
- [ ] **Step 3:** Update `FeatureWithMultipleBlocksAndDescriptions` snapshot to expect `SETUP` BlockKind and merged `and:` descriptions
- [ ] **Step 4:** Update all other affected compilation snapshots
- [ ] **Step 5:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit

### Task 2: Insert block entry/exit notification calls and `MockController.leaveScope()`

Add `SpockRuntime.callBlockEntered(this, blockIndex)` and `callBlockExited(this, blockIndex)` around each block's statements in feature methods, and append `MockController.leaveScope()` at the end of every feature method.

Depends on Task 1 (correct block metadata) so that block indices match the `@FeatureMetadata.blocks` array order.

**Files:**
- Modify: `spockk-compiler-plugin/.../ir/IrIdentifiers.kt`
- Modify: `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt`
- Test: Compilation snapshots `SingleFeatureSpec.kt`, `MultiFeatureSpec.kt`, `FeatureWithMultipleBlocksAndDescriptions.kt`
- Test: All fixture, field, parametrization, and cleanup snapshots

- [ ] **Step 1:** Add FQN constants for `SpockRuntime`, `SpecificationContext`, and `MockController` to `IrIdentifiers.kt`
- [ ] **Step 2:** Add `irCallBlockEntered()`, `irCallBlockExited()`, and `irMockControllerLeaveScope()` private helpers to `FeatureRewriter`
- [ ] **Step 3:** Update `rewriteFeatureStatements()` to wrap each merged block with entry/exit calls and append `leaveScope()`
- [ ] **Step 4:** Update `SingleFeatureSpec` snapshot to expect `callBlockEntered(0)` / `callBlockExited(0)` + `leaveScope()`
- [ ] **Step 5:** Update `MultiFeatureSpec`, `FeatureWithMultipleBlocksAndDescriptions`, and all other affected snapshots
- [ ] **Step 6:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit

### Task 3: Fix cleanup block `$spock_failedBlock` / `setCurrentBlock()` pattern

Update the cleanup block try-catch-finally structure to match Spock's full output, including block context preservation via `$spock_failedBlock`. This ensures Spock extensions can correctly identify which block failed even after the cleanup block transitions the current block.

Depends on Task 2 (block entry/exit calls) because `callBlockEntered/Exited` for the cleanup block are emitted inside the `finally`, and `getCurrentBlock()` is only meaningful after block entry calls are in place.

**Files:**
- Modify: `spockk-compiler-plugin/.../ir/IrIdentifiers.kt`
- Modify: `spockk-compiler-plugin/.../transformer/fixture/CleanupBlockRewriter.kt`
- Modify: `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt`
- Test: Compilation snapshots `cleanup/CleanupBlock.kt`, `cleanup/CleanupBlockWithWhere.kt`

- [ ] **Step 1:** Add `BlockInfo` FQN constant to `IrIdentifiers.kt`
- [ ] **Step 2:** Refactor `CleanupBlockRewriter` to accept pre-built feature statements, cleanup block index, and a `BlockCallBuilder` interface
- [ ] **Step 3:** Implement the full `$spock_failedBlock` pattern: `getCurrentBlock()` capture, `setCurrentBlock()` restoration in inner finally
- [ ] **Step 4:** Update `FeatureRewriter` cleanup path to compute cleanup block index and pass pre-built statements
- [ ] **Step 5:** Update `CleanupBlock` and `CleanupBlockWithWhere` compilation snapshots
- [ ] **Step 6:** Run `./gradlew spotlessApply && ./gradlew build`, fix failures, commit
