# Exception Conditions: thrown, notThrown, noExceptionThrown

**Date:** 2026-08-09
**Issue:** [#271](https://github.com/pshevche/spockk/issues/271)

## Context

Spock's exception-condition helpers - `thrown(Type)`, `notThrown(Type)`, `noExceptionThrown()` - let a `then` block
assert on an exception thrown by the immediately preceding `when` block. Spockk specs extend the real
`spock.lang.Specification` class directly, so these are inherited Java methods, not Spockk-authored API - but they
don't work: Spockk's `when` blocks are never wrapped in a try/catch that records the exception anywhere, so
`thrown(Type)` always throws its own unconditional-fallback `InvalidSpecException`, and `notThrown`/`noExceptionThrown`
silently no-op. This is pinned as a regression baseline in
`spockk-specs/src/test/kotlin/io/github/pshevche/spockk/smoke/condition/ExceptionConditionsSmokeTest.kt`. This
document scopes the real implementation.

## Functional Design

### How Spock Does It

**Runtime API** ([`Specification.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/spock/lang/Specification.java)):

```java
public <T extends Throwable> T thrown() { throw new InvalidSpecException("..."); }               // line 86-89
public <T extends Throwable> T thrown(Class<T> type) { throw new InvalidSpecException("..."); }   // line 109-112
public void notThrown(Class<? extends Throwable> type) { ... }                                    // line 120-127, real body
public void noExceptionThrown() { ... }                                                            // line 133-137, real body
```

`thrown()`/`thrown(Class)` are **unconditional-throw fallback bodies** - real, callable JVM methods that only fire
when the compiler's AST transform didn't rewrite the call site (used outside a `then` block, or not top-level).
`notThrown`/`noExceptionThrown` have **real working bodies** that read
`getSpecificationContext().getThrownException()` directly and are **never** AST-rewritten.

**When-block wrapping** ([`SpecRewriter.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecRewriter.java)):
`visitThenBlock` (lines 500-518) calls `DeepBlockRewriter` to find a top-level exception-condition call
(`Identifiers.EXCEPTION_CONDITION_METHODS = {thrown, notThrown, noExceptionThrown}`, `.../util/Identifiers.java:95-113`)
in the `then` block; if found, `rewriteWhenBlockForExceptionCondition(block.getPrevious(WhenBlock.class))`
(lines 770-800) wraps the **entire** preceding `when` block's statements in:

```
specificationContext.setThrownException(null)
try { <when-block statements, unchanged> }
catch (Throwable t) { specificationContext.setThrownException(t) }   // no rethrow
```

The `null` reset clears stale state from a previous data-driven iteration or an earlier `when`/`then` pair in the
same method. Variable declarations inside the `when` block are hoisted to the enclosing method
(`moveVariableDeclarations`) so they stay in scope for `then` - a Groovy-source-scoping concern (see below for why
this doesn't apply to Kotlin IR).

**`thrown(Type)` rewriting** ([`SpecialMethodCall.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecialMethodCall.java) lines 174-220,
[`DeepBlockRewriter.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/DeepBlockRewriter.java) lines 259-280):
a `thrown(...)` call is rewritten to `SpecInternals.thrownImpl(this, inferredName, inferredType, <original args>)`,
landing on ([`SpecInternals.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/SpecInternals.java) lines 74-98):

```java
public static Throwable checkExceptionThrown(Specification specification, Class<? extends Throwable> exceptionType) {
  if (exceptionType == null) throw new InvalidSpecException("Thrown exception type cannot be inferred automatically. ...");
  if (!Throwable.class.isAssignableFrom(exceptionType)) throw new InvalidSpecException("...");
  Throwable actual = ((SpecificationContext) specification.getSpecificationContext()).getThrownException();
  if (exceptionType.isInstance(actual)) return actual;
  throw new WrongExceptionThrownError(exceptionType, actual);
}
```

`notThrown`/`noExceptionThrown` are never rewritten to go through `SpecInternals` - their own bodies on
`Specification` already do the equivalent check and throw `UnallowedExceptionThrownError`.

**Validation**: at most one exception condition per `then`-block chain (`InvalidSpecCompileException` otherwise);
must be a top-level statement.

### Gap to Spockk

- `FeatureRewriter.rewriteBehaviorStatements` (`spockk-compiler-plugin/src/main/kotlin/io/github/pshevche/spockk/compilation/transformer/FeatureRewriter.kt:143-165`)
  handles `WHEN`/`SETUP`/`GIVEN` blocks identically - `irCallBlockEntered` / raw statements / `irCallBlockExited`,
  no try/catch, no exception ever recorded anywhere.
- `IrIdentifiers.kt` already declares `SPECIFICATION_FQN`, `SPECIFICATION_CONTEXT_FQN`, and `SPEC_INTERNALS_FQN` (the
  last one currently unused anywhere), but no FQNs for `thrown`/`notThrown`/`noExceptionThrown` themselves, and
  `IrSpecificationContext.kt` has no `setThrownException` wrapper.
- Confirmed empirically (this session, `ExceptionConditionsSmokeTest.kt`): `thrown(Type::class.java)` always throws
  `InvalidSpecException` (its fallback body); `when`-block exceptions always propagate raw, crashing the test before
  the `then` block runs; `notThrown`/`noExceptionThrown` pass vacuously since `getThrownException()` is never
  populated.

### Proposed Approach

**Kotlin simplification #1 - no AST type-inference needed for `thrown(Type)`.** `Specification.thrown(Class<T> type)`
is an ordinary Java generic method; Kotlin infers `T` from the `Class<T>` argument on its own (ordinary generic
inference, not Groovy's declaration-scanning trick). So the only rewrite needed is: replace the `thrown(Type::class.java)`
call with `SpecInternals.checkExceptionThrown(this, Type::class.java)` - same argument expression, different (static)
callee, plus a cast back from `Throwable` to the call's own already-resolved type via `irAs` (the exact pattern
`IrSpecificationContext.getSpecificationContextInstance` already uses for a same-shaped cast, see
`spockk-compiler-plugin/.../transformer/ir/IrSpecificationContext.kt`). This single substitution rule applies
identically whether `thrown(...)` is a bare top-level statement or a `val e = thrown(...)` initializer - no
special-casing between the two shapes, since both already carry the same resolved type before rewriting.

`SpecInternals.checkExceptionThrown` is a **public static method already shaded unmodified into `spockk-core`** -
zero new checking logic needs to be written; this also means edge cases (`thrown(null)` -> `InvalidSpecException`,
non-`Throwable` class -> `InvalidSpecException`) are handled correctly for free, by construction.

**Kotlin simplification #2 - `notThrown`/`noExceptionThrown` need no call-site rewriting at all.** Once the paired
`when` block correctly populates `getThrownException()`, their real inherited bodies just work as-is.

**Kotlin simplification #3 - no variable hoisting needed.** Spock's `moveVariableDeclarations` exists because Groovy
source-level `try` blocks lexically scope their local variable declarations. Kotlin IR does not have this constraint:
`IrVariable` references resolve by symbol identity, not lexical nesting, and this codebase already relies on exactly
that fact - `CleanupBlockRewriter.rewrite()` (`spockk-compiler-plugin/.../transformer/fixture/CleanupBlockRewriter.kt:60-68`)
wraps the **entire** feature body's statements (`behaviorStatements`, which routinely includes `when`-block variable
declarations later read in `then`/`cleanup`) directly inside a `try` with zero hoisting step, and this already works
correctly today. The new when-block wrapper follows the identical no-hoisting pattern.

**When-block wrapping** (new `WhenBlockRewriter`, mirrors `CleanupBlockRewriter`'s use of the existing
`irTry`/`irCatch`/`irCatchParameter` helpers from `spockk-compiler-plugin/.../compilation/ir/DeclarationIrBuilder.kt`):

```
specificationContext.setThrownException(null)     // new IrSpecificationContext.irSetThrownException
callBlockEntered(...)
try { <when-block statements, unchanged, no hoisting> }
catch (t: Throwable) { specificationContext.setThrownException(t) }   // no rethrow - exception is consumed
callBlockExited(...)
```

Gated on: the immediately-following `then` block contains a top-level exception-condition call. `FeatureBody.behaviorBlocks`
is a flat, ordered `List<FeatureBlock>`, and `BlockOrderValidatingFeatureStatementsCollector`'s state machine
guarantees a `WHEN` block is always immediately followed by exactly one `THEN` block (never `EXPECT` - `ACTION`
state only accepts `AND`/`THEN`) - so `FeatureRewriter` can safely pre-scan `behaviorBlocks[i+1]` before its main
loop, without restructuring the loop itself into anything more complex than an indexed pass.

**`thrown(Type)` rewriting + validation** happens as a **new, separate, non-recursive pass** over a `THEN` block's
own top-level statements - deliberately *not* folded into the existing `ConditionStatementsRewriter`
(`spockk-compiler-plugin/.../transformer/condition/ConditionStatementsRewriter.kt`), which is shared/recursive
machinery reused for `then`/`expect` blocks, `verify`/`verifyAll`/`verifyEach` lambda bodies, and helper methods -
mixing in a then-block-only, non-recursive concern there would complicate an abstraction that today has no notion of
"which block label is this" at all. Instead, `FeatureRewriter` runs the new pass on a `THEN` block's statements
*before* handing the (now `thrown`-free) result to the existing, unmodified `ConditionRewriter`/`ConditionStatementsRewriter`
pipeline - zero risk to the already-shipped `verify`/`verifyAll`/`verifyEach` feature. This pass:

- Detects a top-level `thrown`/`notThrown`/`noExceptionThrown` call, as a bare statement *or* as a `val`/`var`
  declaration's initializer (mirrors `ImplicitAssertionHelperCall.kt`'s FqName-matching technique, extended to also
  look at `IrVariable.initializer`).
- Rewrites a matched `thrown(Type::class.java)` call in place (substituting the statement, or the variable's
  initializer) to `irAs(IrSpecInternals.irCheckExceptionThrown(...), originalCall.type)`. A zero-arg `thrown()`
  assigned to a `val`/`var` (`val e: IOException = thrown()`) is rewritten the same way, synthesizing the missing
  type argument as `DeclaredType::class.java` from the variable's own declared type - the same `KClass<T>.java`
  idiom `MockingApiTransformer.inferMockType` already uses to infer `Mock()`/`Stub()`/`Spy()`'s type from a variable
  declaration. A zero-arg `thrown()` used as a bare statement has no declared type to infer from and is left
  unrewritten (see deferred scope below).
- Leaves `notThrown`/`noExceptionThrown` calls untouched (no rewrite needed, per simplification #2) - detection only
  drives when-block-wrapping and validation.
- Validates at most one exception-condition call per `then` block: `CompilationException` otherwise, via a new
  `InvalidExceptionConditionExceptionFactory` mirroring the existing
  `InvalidParametrizationExceptionFactory` (`spockk-compiler-plugin/.../transformer/parametrization/InvalidParametrizationExceptionFactory.kt`)
  pattern (`CompilationException(message, file, irElement)`).

A zero-arg `thrown()`/bare `notThrown` used in an `expect` block (or anywhere not the `then` half of a `when`/`then`
pair) is **not** specially detected by this pass - it already falls through today to the real fallback body, which
already throws the correct `InvalidSpecException` ("...only allowed in 'then' blocks..."). No new code needed for
that case.

### Scope (v1)

**In scope:** when-block wrapping; `thrown(Type::class.java)` rewriting; zero-arg `thrown()` type inference from a
`val`/`var` declaration's declared type; `notThrown(Type::class.java)`/`noExceptionThrown()` working via wrapping
alone; at-most-one-per-then-block validation; `expect`-block usage unchanged (already correct).

**Deferred, documented as known limitations (not silent gaps):**

- **Zero-arg `thrown()` used as a bare statement** (no `val`/`var` declaration to infer a type from, e.g.
  `then { thrown() }`). Kotlin's explicit `thrown(Type::class.java)` is the idiomatic, fully-supported spelling for
  this case. A bare `thrown()` still compiles (inherited real method, generic return type inferred by Kotlin as
  `Nothing`/`Throwable`) and still triggers when-block-wrapping detection (harmless - the wrap just isn't exercised
  by a rewritten call), but the call itself isn't rewritten - falls through to today's `InvalidSpecException`
  fallback, unchanged from current behavior. (Assigning the result to a `val`/`var` first and using that, as in
  `val e: IOException = thrown()`, is not affected by this limitation - see above.)
- **Fully general "must be top-level" diagnostics** (e.g. `thrown()` nested inside an `if`, or passed as a function
  argument) - not specially detected; falls through to today's fallback-throw behavior (a bare `thrown()` throws
  `InvalidSpecException`; the preceding `when` block, having no top-level exception condition, isn't wrapped either,
  so the underlying exception - if any - propagates raw). Less precise error message than Spock's dedicated
  diagnostic, not a functional regression: this degrades no worse than the pre-existing baseline.

Chained `then` blocks after one `when` (`when: ...` `then: ...` `then: ...`, without an intervening `and`) were
initially scoped as a deferred limitation, but turn out not to be reachable at all: Spockk's pre-existing
`BlockOrderValidatingFeatureStatementsCollector` grammar already rejects a `then` block immediately following another
`then` block as a compile error, unrelated to this feature - so `behaviorBlocks[i+1]` always being the correct paired
`then` block for a `when` is not just an assumption, it's enforced upstream.

### Spock Source References

- [`Specification.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/spock/lang/Specification.java) - `thrown`/`notThrown`/`noExceptionThrown` public API and fallback/real bodies
- [`SpecInternals.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/SpecInternals.java) - `thrownImpl`/`checkExceptionThrown`, already shaded into `spockk-core`
- [`SpecificationContext.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/SpecificationContext.java) - `getThrownException`/`setThrownException` (the latter not on `ISpecificationContext`, requires a cast to the concrete class - same pattern `IrSpecificationContext.kt` already uses for `setCurrentBlock`)
- [`SpecRewriter.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecRewriter.java) - `rewriteWhenBlockForExceptionCondition`, its `visitThenBlock` call site
- [`SpecialMethodCall.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/SpecialMethodCall.java) / [`DeepBlockRewriter.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/compiler/DeepBlockRewriter.java) - exception-condition detection and `thrown` rewriting
- [`WrongExceptionThrownError.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/WrongExceptionThrownError.java) / [`UnallowedExceptionThrownError.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/UnallowedExceptionThrownError.java) / [`InvalidSpecException.java`](https://github.com/spockframework/spock/blob/master/spock-core/src/main/java/org/spockframework/runtime/InvalidSpecException.java) - all `SpockAssertionError`/`SpockException` subclasses, already shaded
- [`ExceptionConditions.groovy`](https://github.com/spockframework/spock/blob/master/spock-specs/src/test/groovy/org/spockframework/smoke/condition/ExceptionConditions.groovy) - Spock's own test coverage (basic usage, wrong/inferred type, multiple exceptions, must-be-top-level, once-per-block)

### Gap to Spockk (Existing Spockk Files Touched)

- `spockk-compiler-plugin/.../ir/IrIdentifiers.kt` - new FQNs for `thrown`/`notThrown`/`noExceptionThrown`
- `spockk-compiler-plugin/.../ir/IrStatement.kt` - `isExceptionConditionCall()` detector, mirroring `isAssertCall()`
- `spockk-compiler-plugin/.../transformer/ir/IrSpecificationContext.kt` - new `irSetThrownException`
- `spockk-compiler-plugin/.../transformer/ir/IrSpecInternals.kt` - new, wraps `SpecInternals.checkExceptionThrown`
- `spockk-compiler-plugin/.../transformer/condition/WhenBlockRewriter.kt` - new, the when-block try/catch wrapper
- `spockk-compiler-plugin/.../transformer/condition/ExceptionConditionRewriter.kt` - new, the then-block-scoped detection/rewrite/validation pass, including zero-arg `thrown()` type inference for `val`/`var` declarations via the `KClass<T>.java` idiom shared with `MockingApiTransformer`
- `spockk-compiler-plugin/.../transformer/condition/InvalidExceptionConditionExceptionFactory.kt` - new, mirrors `InvalidParametrizationExceptionFactory`
- `spockk-compiler-plugin/.../transformer/FeatureRewriter.kt` - pre-scan for WHEN/THEN pairing, route matching WHEN blocks through `WhenBlockRewriter`, run `ExceptionConditionRewriter` on matching THEN blocks before the existing `ConditionRewriter`
- `spockk-specs/.../smoke/condition/ExceptionConditionsSmokeTest.kt` - flip pinned-broken-behavior assertions to real expected outcomes
- New compilation snapshot test pairs under `spockk-specs/src/test/resources/samples/compilation/{source,transformed}/condition/`
