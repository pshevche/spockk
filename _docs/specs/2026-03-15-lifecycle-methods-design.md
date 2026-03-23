# Lifecycle Methods: Setup and Cleanup

**Date:** 2026-03-15
**Issue:** TBD

## Context

Spockk currently supports feature methods with block labels (`given`, `when`, `then`, `expect`, `where`) but lacks support for Spock's lifecycle fixture methods (`setup()`, `cleanup()`, `setupSpec()`, `cleanupSpec()`) and the `setup` and `cleanup` blocks inside feature methods.

Lifecycle methods let users define setup and teardown logic that runs before/after each feature (instance-scoped) or before/after the entire spec (class-scoped). The `setup` and `cleanup` blocks let users define setup and cleanup logic inline within a feature method — `setup` is an alias for `given`, and `cleanup` guarantees its statements run even if the feature throws an exception.

## Functional Design

### How Spock Does It

#### Fixture Method Names

Spock recognizes four fixture method names (case-sensitive), defined in [`Identifiers.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/util/Identifiers.java):

- `setup()` — runs before each feature iteration (instance-scoped)
- `cleanup()` — runs after each feature iteration (instance-scoped)
- `setupSpec()` — runs once before all features (class-scoped, uses shared instance)
- `cleanupSpec()` — runs once after all features (class-scoped, uses shared instance)

#### What the developer writes

```groovy
class MySpec extends Specification {
  def x = 42

  def setupSpec() { println("setupSpec") }
  def setup() { println("setup") }
  def cleanup() { println("cleanup") }
  def cleanupSpec() { println("cleanupSpec") }

  def "my feature"() {
    expect:
    x == 42
  }
}
```

#### Compile-Time AST Transformations

The transformation pipeline is orchestrated by [`SpockTransform.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpockTransform.java):

1. **`SpecParser`** — Detects fixture methods by name matching against `Identifiers.FIXTURE_METHODS`. Validates they are not static and have correct capitalization. Wraps each in a `FixtureMethod` model object containing the method body (unlike feature methods, fixture methods don't have named blocks like `when:`/`then:` — the entire body is treated as a single unnamed block). Stores them on the `Spec` model (`setSetupMethod`, `setCleanupMethod`, etc.).

2. **`SpecRewriter`** — Applies two transformations to fixture methods:
   - **De-virtualization**: Makes fixture methods `private` (changes visibility from default/public to `ACC_PRIVATE`). Without this, if both `Base` and `Derived` define `setup()`, calling `setup()` on a `Derived` instance would only invoke `Derived.setup()` due to polymorphism. Making them private lets Spock's runtime invoke each class's `setup()` independently via reflection.
   - **Field access validation**: For `setupSpec()` and `cleanupSpec()`, validates that they do not access instance fields — only `@Shared` and static fields are permitted (since they run on the shared instance). This is done by `InstanceFieldAccessChecker`.

3. **`SpecAnnotator`** — Does NOT add any special metadata annotation to fixture methods (unlike feature methods which get `@FeatureMetadata`). Fixture methods can receive repeated extension annotations if the user annotates them.

After transformation, fixture methods keep their original names (`setup`, `cleanup`, `setupSpec`, `cleanupSpec`) but become `private`.

#### Setup and Cleanup Block Transformation

##### Setup Block

The `setup:` block label inside a feature method is an alias for `given:`. In Spock's `BlockParseInfo` enum, `GIVEN.addNewBlock()` directly delegates to `SETUP.addNewBlock()`, creating a `SetupBlock` in both cases. Both map to `BlockKind.SETUP` at runtime. They are fully interchangeable and have identical semantics — the choice between `setup:` and `given:` is purely stylistic.

Spockk already supports `given` as a block label. Adding `setup` means registering it as an additional label that behaves identically to `given` — same valid successors, same `BlockKind`, same transformation.

##### Cleanup Block

The `cleanup:` block inside a feature method is a separate concept from the `cleanup()` lifecycle method. When a feature contains a `cleanup:` block, `SpecRewriter.visitCleanupBlock()` wraps the feature body in a try-catch-finally structure that emulates Java 7 try-with-resources exception handling:

```groovy
// Before (what the developer writes):
def "my feature"() {
  setup:
  def resource = acquireResource()

  expect:
  resource.isValid()

  cleanup:
  resource.release()
}

// After (what Spock transforms it into):
def "$spock_feature_0_0"() {
  Throwable $spock_feature_throwable = null
  try {
    // feature statements (setup, expect blocks)
    def resource = acquireResource()
    assert resource.isValid()
  } catch (Throwable $spock_tmp_throwable) {
    $spock_feature_throwable = $spock_tmp_throwable
    throw $spock_tmp_throwable
  } finally {
    try {
      // cleanup block statements
      resource.release()
    } catch (Throwable $spock_tmp_throwable) {
      if ($spock_feature_throwable != null) {
        $spock_feature_throwable.addSuppressed($spock_tmp_throwable)
      } else {
        throw $spock_tmp_throwable
      }
    }
  }
}
```

Key aspects:

- If the feature body throws, the exception is captured in `$spock_feature_throwable`, then re-thrown
- The cleanup block runs in the `finally`
- If the cleanup block also throws, its exception is added as suppressed if a feature exception already exists; otherwise the cleanup exception propagates normally
- Variable declarations from blocks before `cleanup` are hoisted so they're accessible in the cleanup block's scope

#### Runtime Discovery and Execution

At runtime, fixture methods are discovered by [`SpecInfoBuilder`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/SpecInfoBuilder.java), which uses reflection to look for private methods named `setup`, `cleanup`, `setupSpec`, and `cleanupSpec` on each spec class in the hierarchy. Each found method is wrapped in a `MethodInfo` with the appropriate `MethodKind` (`SETUP`, `CLEANUP`, `SETUP_SPEC`, `CLEANUP_SPEC`) and added to the `SpecInfo`.

Execution is handled by [`PlatformSpecRunner`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/PlatformSpecRunner.java):

##### Execution Order

For a spec hierarchy `Base extends Specification`, `Derived extends Base`:

1. `Base.setupSpec()` -> `Derived.setupSpec()` (top-down)
2. Per feature iteration:
   1. `Base.setup()` -> `Derived.setup()` (top-down)
   2. Feature method
   3. `Derived.cleanup()` -> `Base.cleanup()` (bottom-up)
3. `Derived.cleanupSpec()` -> `Base.cleanupSpec()` (bottom-up)

Setup methods run **parent-first** (top-down), cleanup methods run **child-first** (bottom-up). This is enforced by recursive methods in `PlatformSpecRunner`:

- `doRunSetupSpec()` calls `runSetupSpec(superSpec)` BEFORE invoking current spec's methods
- `doRunCleanupSpec()` invokes current spec's methods BEFORE calling `runCleanupSpec(superSpec)`
- Same pattern for `doRunSetup()` and `doRunCleanup()`

##### Error Handling

- **Setup fails**: `cleanup()` still runs. `cleanupSpec()` also always runs (it's in the JUnit engine's spec-level `after` lifecycle, independent of per-iteration errors)
- **Feature fails**: `cleanup()` still runs
- **Cleanup fails**: The error is reported via `supervisor.error()`. If a feature error already exists, cleanup errors are collected alongside it
- **Field initializer fails**: `cleanup()` does NOT run (the instance wasn't fully constructed)

##### Instance Management

- `setupSpec()` and `cleanupSpec()` run on the **shared instance** (`context.getSharedInstance()`)
- `setup()` and `cleanup()` run on the **current instance** (fresh per iteration)

#### Spock Source References

##### Source files

- [`SpockTransform.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpockTransform.java) — Orchestrates the transformation pipeline
- [`SpecParser.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecParser.java) — Detects and parses fixture methods (lines 94-151)
- [`SpecRewriter.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecRewriter.java) — De-virtualizes fixture methods (lines 309-316), validates field access (lines 319-323), cleanup block try-finally wrapping (lines 540-625)
- [`FixtureMethod.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/model/FixtureMethod.java) — Compile-time model for fixture methods
- [`Identifiers.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/util/Identifiers.java) — Fixture method name constants
- [`SpecInfo.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/model/SpecInfo.java) — Runtime model storing fixture method lists (lines 67-70)
- [`MethodKind.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/model/MethodKind.java) — Enum defining fixture method kinds
- [`PlatformSpecRunner.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/PlatformSpecRunner.java) — Runtime execution of fixture methods (lines 127-181, 293-386)
- [`SpecInfoBuilder.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/SpecInfoBuilder.java) — Runtime discovery of fixture methods via reflection (lines 226-236)

##### Test files

- [`FixtureMethods.groovy`](https://github.com/spockframework/spock/blob/master/spock-specs/src/test/groovy/org/spockframework/smoke/FixtureMethods.groovy) — Tests execution order, inheritance behavior, field access restrictions, cleanup-on-failure
- [`SetupBlocks.groovy`](https://github.com/spockframework/spock/blob/master/spock-specs/src/test/groovy/org/spockframework/smoke/SetupBlocks.groovy) — Tests setup block behavior, verifies `given` is an alias for `setup`
- [`CleanupBlocks.groovy`](https://github.com/spockframework/spock/blob/master/spock-specs/src/test/groovy/org/spockframework/smoke/CleanupBlocks.groovy) — Tests cleanup block behavior within features
- [`CleanupBlocksAstSpec.groovy`](https://github.com/spockframework/spock/blob/master/spock-specs/src/test/groovy/org/spockframework/smoke/ast/CleanupBlocksAstSpec.groovy) — AST snapshot tests for cleanup block transformation

### Gap to Spockk

Spockk has no concept of fixture methods. The collector (`SpockkTransformationContextCollector`) silently skips methods without block labels, so `setup()` / `cleanup()` / `setupSpec()` / `cleanupSpec()` are treated as ordinary Kotlin functions — never made private, never discovered by the Spock runtime.

The `setup` and `cleanup` block labels are also unknown to Spockk: they are not registered in `FeatureBlockLabel`, so using `setup:` causes a compilation error, and `cleanup:` does not exist at all.

### Proposed Approach

The implementation spans four modules:

- **`spockk-core`**: Register `setup` and `cleanup` as block label objects/functions in `Blocks.kt`
- **`spockk-compiler-plugin`**: Add fixture method detection (collection phase) and de-virtualization (transformation phase); register `setup`/`cleanup` block FQNs and update the state machine; implement cleanup block try-catch-finally IR generation
- **`spockk-intellij-plugin`**: Suppress "unused expression" warnings for `setup` and `cleanup` block references
- **`spockk-specs`**: Snapshot tests, error scenario tests, smoke tests, and runtime engine tests

## Implementation Plan

1. Add setup and cleanup block labels — core library + compiler plugin registration + block validation state machine
2. Add fixture method detection — collection phase: detect, validate, and track fixture methods
3. Implement fixture method IR transformation — de-virtualization and field access validation
4. Implement cleanup block IR transformation — try-catch-finally IR generation
5. Add smoke and runtime tests — end-to-end and engine-level test coverage
6. Update IntelliJ plugin — suppress warnings for new block label FQNs
