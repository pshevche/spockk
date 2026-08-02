# Spock Condition Rewriting — Test Coverage Documentation

> **Source repository:** `/Users/pshevche/dev/spockframework/spock`  
> **Date generated:** 2026-07-03  
> **Purpose:** Document the test coverage for explicit and implicit condition rewriting to guide the implementation of a comparable feature in Spockk.

---

## 1. Architecture of Condition Rewriting

### 1.1 Entry Points

Two entry methods on `ConditionRewriter` (`spock-core/.../compiler/ConditionRewriter.java`):

| Method | Trigger | Parameter |
|---|---|---|
| `rewriteExplicitCondition(AssertStatement, IRewriteResources)` | Groovy `assert` statement | `AssertStatement`, resources |
| `rewriteImplicitCondition(ExpressionStatement, IRewriteResources)` | Boolean expression in `then`/`expect` block | `ExpressionStatement`, resources |

### 1.2 Orchestration via DeepBlockRewriter

`DeepBlockRewriter` (`spock-core/.../compiler/DeepBlockRewriter.java`) decides which path to take:

- **Line 64–69:** `visitAssertStatement()` → calls `ConditionRewriter.rewriteExplicitCondition()` on every `AssertStatement` AST node.
- **Line 202–237:** `handleImplicitCondition()` → calls `ConditionRewriter.rewriteImplicitCondition()` on `ExpressionStatement` nodes when:
  - The statement is a top-level statement in a `then`, `expect`, or `filter` block, OR
  - The statement is inside a `with {}`, `verifyAll {}`, `verifyEach {}` condition method, OR
  - The statement is inside a closure of an interaction with `interactionClosureDepth == 1`.

### 1.3 ConditionRewriter Internals

The `ConditionRewriter` converts Groovy AST expressions into calls to `SpockRuntime` methods:

- **`rewriteOtherCondition()`** — for non-method-call expressions (binary, unary, ternary, etc.). Each sub-expression gets wrapped in `valueRecorder.record(valueRecorder.startRecordingValue(id), expr)`, then passed to `SpockRuntime.verifyCondition()`.
- **`rewriteMethodCondition()`** — for `MethodCallExpression` conditions. Arguments are unwrapped, and the call is dispatched through `SpockRuntime.verifyMethodCondition()`.
- **`rewriteStaticMethodCondition()`** — for `StaticMethodCallExpression` conditions.
- **`isSpecialCollectionCondition()`** — detects `=~` (match) and `==~` (find) operators on collections (non-string operands), rewrites to `SpockRuntime.matchCollectionsAsSet()` / `SpockRuntime.matchCollectionsInAnyOrder()`.
- **Opt-out:** `!!` prefix on implicit conditions bypasses rewriting (`isOptOutExpression()`). On explicit conditions with `!!`, sub-expressions are not recorded but the assertion is still wrapped in try/catch.

### 1.4 Implicit Conditions Detection

`ImplicitConditionsUtils` (`spock-core/.../compiler/condition/ImplicitConditionsUtils.java`):

- **`isImplicitCondition(Statement)`** — returns `true` when the statement is an `ExpressionStatement` whose expression is **not** a `DeclarationExpression`.
- **`checkIsValidImplicitCondition(Statement, ErrorReporter)`** — warns if the expression is a `BinaryExpression` with an assignment operator (`=`, `+=`, `-=`), suggesting `==` was intended.

### 1.5 Runtime Rendering

On condition failure:
1. Recorded sub-expression values are collected into a `Condition` object.
2. `ExpressionInfoRenderer` assembles the hierarchical rendering with values placed under their source positions.
3. Diff renderers (`runtime/condition/`) produce structured diffs for strings, arrays, collections, maps, sets, and beans.

---

## 2. Test Coverage Map

### 2.1 Smoke Tests (`spock-specs/.../smoke/condition/`)

These are **end-to-end runtime tests** — real Spock specs executed via the Spock test engine.

#### 2.1.1 Explicit Conditions

| Test File | Test Scenarios Covered | Lines |
|---|---|---|
| `ExplicitConditionsInFeatureMethods` | `assert` in: anonymous block, setup, expect, when, then, cleanup blocks; nested variants (inside `if`, `each` closure); method-call expression with message | 1–100 |
| `ExplicitConditionsInFixtureMethods` | `assert` in: `setupSpec()`, `cleanupSpec()`, `setup()`, `cleanup()` | 1–50 |
| `ExplicitConditionsInFields` | `assert` inside closures stored in instance and `@Shared` fields | 1–39 |
| `ExplicitConditionsInHelperMethods` | `assert` at top level, nested (`if`), deeply nested (closure inside `if`) of helper methods | 1–55 |
| `ExplicitConditionsInNestedPositions` | `assert` in: `for` loop, closure (`each`), implicit condition context (`every`), void method in expect block, deeply nested (`every` → `if` → `any` → `assert`) | 1–56 |
| `ExplicitConditionsWithMessage` | `assert` with: string message, GString message, object message, `null` message, indirect `null` message, colon-syntax message (`: "msg"`), method condition with message, static method condition with message; verified via `isRendered()` | 1–157 |

#### 2.1.2 Implicit Conditions (Evaluation)

| Test File | Test Scenarios Covered | Lines |
|---|---|---|
| `ConditionsAndGroovyTruth` (`SatisfiedConditions` / `UnsatisfiedConditions`) | Groovy truth: `true`/`false`, numbers (`1`/`0`), objects, collections (`[1]`/`[]`), `++`/`--` operators, multi-operator expressions. All tests use **implicit** conditions in `expect:` block. | 1–79 |
| `ConditionEvaluation` | Multi-line conditions; `MethodCallExpression`, spread-dot, safe operator (`?.`), named args (both syntaxes); `StaticMethodCallExpression`; `ConstructorCallExpression` (regular, named args, named args as map); `TernaryExpression`, `ShortTernaryExpression`; `BinaryExpression` (arithmetic, comparison, subscription); `PrefixExpression`, `PostfixExpression`; `BooleanExpression`; `ClosureExpression`; `TupleExpression`; `MapExpression`; `ListExpression`; `RangeExpression`; `PropertyExpression`; `AttributeExpression`; `MethodPointerExpression`; `ConstantExpression`; `ClassExpression`; `RegexExpression`; `GStringExpression`; `ArrayExpression`; `SpreadExpression`; `SpreadMapExpression`; `NotExpression`; `UnaryMinusExpression`; `UnaryPlusExpression`; `BitwiseNegationExpression`; `CastExpression`; `ArgumentListExpression`; statically imported fields/enums; block conditions; collection conditions (`=~`, `==~`) with various types and `null`s; Groovy regex conditions still work; implicit condition with custom exception; **`!!` opt-out** (4 tests for bypassing wrapping, falsy values, static methods, outermost-only) | 1–561 |
| `MethodConditionEvaluation` | Method conditions dispatched through `SpockRuntime` — verifies arguments are passed correctly (regular, null, list elements) | 1–63 |
| `PartialConditionEvaluation` | Short-circuit boolean operators (`&&`, `||`, `^`) where some sub-expressions are never evaluated | 1–31 |
| `ExceptionsInConditions` | NPE in regular/method condition; exception while invoking condition; Spock assertion errors propagated as-is; Spock exceptions propagated as-is; deep nesting exception rendering | 1–163 |
| `ConditionNotSatisfiedErrors` | Each condition gets its own value set (no aliasing across invocations) | 1–24 |

#### 2.1.3 Implicit Conditions (Rendering)

| Test File | Test Scenarios Covered | Lines |
|---|---|---|
| `ConditionRendering` | Renders every Groovy AST expression type: simple, multi-line, `MethodCallExpression` (implicit/explicit target, GString method, static, spread-dot, safe, top-level, named args), `StaticMethodCallExpression`, `ConstructorCallExpression` (regular, non-static inner class, named args), `TernaryExpression`, `ShortTernaryExpression`, `BinaryExpression`, `PrefixExpression`, `PostfixExpression`, `BooleanExpression`, `ClosureExpression`, `TupleExpression`, `MapExpression`, `ListExpression`, `RangeExpression`, `PropertyExpression`, `AttributeExpression`, `MethodPointerExpression`, `ConstantExpression`, `ClassExpression` (with comments, same-name classes), `instanceof`, `VariableExpression`, `GStringExpression`, `ArrayExpression`, `SpreadExpression`, `SpreadMapExpression`, `NotExpression`, `UnaryMinusExpression`, `UnaryPlusExpression`, `BitwiseNegationExpression`, `CastExpression`, `ArgumentListExpression`, statically imported field/enum, explicit closure call, backslash escaping, nested condition isolation | 1–905 |
| `ImplicitClosureCallRendering` | Implicit closure calls: local variable, method argument, field, property, qualified property, method call (nested), qualified method call (nested). Verifies rendering correctly substitutes implicit `.call()` with direct invocation syntax. | 1–130 |
| `ConditionRenderingSpec` (base class) | `isRendered(String, Closure)` — catches `ConditionNotSatisfiedError` and compares rendering. `isRendered(String, Condition)` — direct comparison. `renderedConditionContains(Closure, String...)` — substring matching. | 1–57 |
| `IsRenderedExtension` | Spock extension implementing `@IsRendered` annotation for declarative rendering assertions in feature methods. | 1–49 |

#### 2.1.4 Value Rendering

| Test File | Test Scenarios Covered | Lines |
|---|---|---|
| `ValueRendering` | `null`, `char`, string, empty string, multi-line string, list, map, single-line `toString()`, multi-line `toString()`, `null` `toString()`, empty `toString()`, exception-throwing `toString()`, enum literal, statically imported enum, enum with custom `toString()`, variable with enum value, default `toString()` (fallback to `dump()`) | 1–303 |

#### 2.1.5 Diff Rendering (Equality Comparisons)

| Test File | Test Scenarios Covered | Lines |
|---|---|---|
| `EqualityComparisonRendering` | Values with different representations, same-repr/same-type, same-repr/different-type, same-repr/different-array-type, same-repr/different-anonymous-type, same-rendered-and-literal-repr, same-literal-repr (List vs Set), null values, type hints in nested equality, type hints suppressed when values equal, no type hints for `.equals()` | 1–209 |
| `StringComparisonRendering` | String diff with differences, `null` comparison, subexpression diff, subexpression equal strings, **large strings** (difference at start/middle/end, multiple differences, complete difference, context overflow, integer overflow prevention), line break diffs, newline escape diffs, interpolated string diffs, long string with line breaks/newline escapes | 1–339 |
| `ArrayComparisonRendering` | Primitive array, object array, arrays with default `toString()`, primitive 2D array, object 2D array, multidimensional arrays with higher cardinality | 1–93 |
| `SetComparisonRendering` | Missing and extra elements, only missing, large set with small differences, large set with large differences (too many to render), contained/containing sets | 1–111 |
| `CollectionConditionRendering` | Nested lenient matching, nested regex finding, nested regex complex finding, lenient matching (variable-variable, variable-literal, literal-variable, literal-literal), strict matching, nested strict matching, nested regex matching, indirect regex find/match, regex find/match with non-String types | 1–284 |
| `DiffedObjectRendering` | Null values, bean property rendering, bean sub-class, bean with interface, bean sub-class with interface, class rendering (Bootstrap class loader), class with Groovy class loader | 1–212 |
| `MatcherConditionRendering` | Short syntax (`x equalTo(43)`), long syntax (`that x, equalTo(43)`), explicit condition (`assert that(x, equalTo(43))`), custom message | 1–87 |

#### 2.1.6 Invalid/Error Conditions

| Test File | Test Scenarios Covered | Lines |
|---|---|---|
| `InvalidConditions` | Assignment operators (`=`, `+=`, `-=`) forbidden in `expect` and `then` blocks; assignment in explicit `assert`; assignments allowed in variable declarations. Tests compile behavior (compilation errors expected). | 1–125 |
| `ExceptionConditions` | `thrown()` basic usage; may occur after another condition; may occur in `and`'ed/chained blocks; when-block with var defs; catching `Exception`, `RuntimeException`, `Error`, `Throwable`, base type; exception in first/second block of when-group; multiple exceptions; only once per then-block; must be top-level; undeterminable type; explicit type `null` with inferred type; explicit + inferred type mismatch; must be `Throwable` subtype; observe failing explicit condition; observe failing interaction (known limitation); field/variable assignment; reuse exception variable; multi-assignments in when-block; method reference preservation | 1–340 |
| `MatcherConditions` | Work in expect/then blocks; alternative `that`/`expect` syntax; explicit conditions with `that()`; custom message; custom matcher implementations; fail when matcher doesn't match; can only be used where condition expected; numeric literal as actual; string/method/property expression restrictions (needs `that`); hamcrest matchers in interactions | 1–207 |

---

### 2.2 AST Transformation Tests (`spock-specs/.../smoke/ast/condition/`)

These are **compile-time tests** using `compiler.transpileFeatureBody()` / `compiler.transpile()` / `compiler.transpileSpecBody()`, comparing output to snapshots.

| Test File | Test Scenarios Covered | Lines |
|---|---|---|
| `ConditionMethodsAstSpec` | GDK method (`null.with{}`) as implicit/explicit condition; condition methods (`with`, `verifyAll`, `verifyEach`) nested within themselves (3 combinations), with exception (3 combos), with only exception (3 combos); standalone condition methods (3 methods), with exception (3 methods), with only exception (3 methods) | 1–180 |
| `CollectionConditionAstSpec` | `matchCollectionsAsSet` (`=~`), `matchCollectionsInAnyOrder` (`==~`), regex find (`=~`), regex match (`==~`) — transformation snapshots | 1–89 |
| `ExceptionConditionsAstSpec` | `thrown()` rewrite preserves method reference when variable shadows method name; same for multi-assignments | 1–75 |
| `BaseVerifyMethodsAstSpec` | Shared base for `@Verify`/`@VerifyAll`: transforms conditions in spec methods; transforms in non-spec classes; private methods; static methods; explicit conditions; methods without conditions stay unchanged; interactions are illegal; ignores non-annotated methods; transforms at all nesting levels; checks for invalid conditions (assignment = condition); ignores overridden methods; fails on non-void return type (in spec and outside) | 1–310 |
| `VerifyMethodsAstSpec` | Concrete: `@Verify` annotation-specific tests (delegates to `BaseVerifyMethodsAstSpec`) | 1–14 |
| `VerifyAllMethodsAstSpec` | Concrete: `@VerifyAll` annotation-specific tests (delegates to `BaseVerifyMethodsAstSpec`) | 1–14 |

---

### 2.3 Runtime Unit Tests (`spock-specs/.../runtime/condition/`)

| Test File | Test Scenarios Covered | Lines |
|---|---|---|
| `EditDistanceSpec` | Levenshtein distance matrix for: "sitting"/"kitten", "Sunday"/"Saturday", "levenshtein"/"meilenstein"; path calculation (edit operations); distance computation (12 test cases); Monte Carlo verification (100 random strings vs edited variants) | 1–173 |
| `EditPathRendererSpec` | Edit path rendering (diff visualization) | — |

---

### 2.4 Snapshot Resources

Snapshot files for AST transformation tests:
```
spock-specs/src/test/resources/snapshots/org/spockframework/smoke/ast/condition/
```

Snapshot files for runtime rendering tests:
```
spock-specs/src/test/resources/snapshots/org/spockframework/smoke/condition/
```

---

## 3. Test Coverage Grid: Explicit vs Implicit

### 3.1 Explicit Conditions (`assert`)

| Coverage Dimension | Covered By |
|---|---|
| **Block placement** | `ExplicitConditionsInFeatureMethods` (all blocks: setup, expect, when, then, cleanup, anonymous) |
| **Fixture methods** | `ExplicitConditionsInFixtureMethods` (setupSpec, cleanupSpec, setup, cleanup) |
| **Field initializers** | `ExplicitConditionsInFields` (instance field, shared field — via closure deferral) |
| **Helper methods** | `ExplicitConditionsInHelperMethods` (top-level, nested, deeply nested) |
| **Nested positions** | `ExplicitConditionsInNestedPositions` (for loop, closure, every, each, deep nesting) |
| **Message rendering** | `ExplicitConditionsWithMessage` (7 scenarios: string, GString, object, null, indirect null, colon syntax, method/static method condition with message) |
| **AST transformation (assert parsed)** | `ConditionMethodsAstSpec` (GDK method as explicit condition) |
| **Opt-out (`!!` + assert)** | `ConditionRewriter.java` (lines 632–638: `!!` prefix on explicit conditions still records values) — **No dedicated runtime test found** |
| **Runtime rendering** | All `ConditionRendering` / `*Rendering` tests use `assert` |

### 3.2 Implicit Conditions (bare expressions in then/expect)

| Coverage Dimension | Covered By |
|---|---|
| **Groovy truth** | `ConditionsAndGroovyTruth` (boolean, number, object, collection, ++/--, multi-operator) |
| **Expression types** | `ConditionEvaluation` (30+ Groovy AST expression types) — comprehensive |
| **Method conditions** | `MethodConditionEvaluation` (argument passing correctness) |
| **Short-circuit** | `PartialConditionEvaluation` (boolean operators with unevaluated sub-expressions) |
| **Exceptions in conditions** | `ExceptionsInConditions` (NPE, exception while invoking, Spock errors propagated, deep nesting) |
| **No aliasing** | `ConditionNotSatisfiedErrors` (independent value sets) |
| **Rendering** | `ConditionRendering` (30+ expression types), `ImplicitClosureCallRendering` (6 scenarios) |
| **Value rendering** | `ValueRendering` (15+ value types and edge cases) |
| **Opt-out (`!!` prefix)** | `ConditionEvaluation` (4 tests: bypass wrapping, falsy values, static methods, outermost-only) |
| **Invalid conditions** | `InvalidConditions` (assignment operators forbidden, declaration allowed) |
| **Exception conditions** | `ExceptionConditions` (25+ scenarios for `thrown()`/`notThrown()`) |
| **Matcher conditions** | `MatcherConditions` (10+ scenarios for Hamcrest integration) |

---

## 4. Critical Test Scenarios for Spockk Implementation

When implementing implicit condition rewriting in Spockk, these are the scenarios that tests **must** cover:

### 4.1 Opt-out Mechanism (`!!` prefix)

Spock supports `!!expr` to bypass implicit condition rewriting:
- `!!customContains("foo", "bar")` → thrown exception propagates as-is (no `ConditionNotSatisfiedError` wrapping)
- `!!aList.each { ... }` → `.each()` returns collection, not boolean; `!!` prevents false-negative
- `!!min(0, 0)` → static method returning falsy value must not fail
- `!!false && false` → opt-out only works on **outermost** expression

**Spockk coverage gap:** This is not yet implemented.

### 4.2 Assignment Detection

`InvalidConditions` verifies that assignments (`=`, `+=`, `-=`) in `then`/`expect` blocks produce compile errors with clear messages. Variable declarations (`def x = 42`) must be allowed.

### 4.3 Collection Condition Operators

- `=~` (lenient matching) rewrites to `matchCollectionsAsSet()` for non-string operands
- `==~` (strict matching) rewrites to `matchCollectionsInAnyOrder()` for non-string operands
- String operands keep Groovy's regex semantics
- `null` operands handled correctly

### 4.4 Method Condition Dispatch

When a top-level expression is a method call (e.g., `x.isEmpty()`), Spock dispatches through `SpockRuntime.verifyMethodCondition()` instead of evaluating directly. This preserves argument values for rendering.

### 4.5 Exception Handling in Conditions

- Exceptions during sub-expression evaluation are caught and rendered inline (e.g., NPE with stacktrace shown as a value)
- Spock's own exceptions (`ConditionNotSatisfiedError`, `SpockException`) propagate unwrapped
- Deep nesting exceptions are properly rendered

### 4.6 Hierarchical Value Recording

Each sub-expression gets a monotonically increasing `recordCount`. The `ValueRecorder` at runtime pairs these IDs with actual values. Rendering walks the AST in parallel with recorded values to produce the hierarchical output. Short-circuited sub-expressions leave gaps (N/A values).

### 4.7 @Verify / @VerifyAll Helper Methods

- `@Verify` — fail-fast behavior (first failure stops execution)
- `@VerifyAll` — collect-all behavior (all conditions evaluated, all errors reported)
- Both methods: must have `void` or dynamic return type; can be in spec classes or standalone; implicit conditions in method body are rewritten; interactions are forbidden inside.
- Nested `with {}`, `verifyAll {}`, `verifyEach {}` within these methods also get rewritten.

---

## 5. Summary Statistics

| Category | File Count | Approximate Test Scenarios |
|---|---|---|
| Explicit condition smoke tests | 6 files | ~42 scenarios |
| Implicit condition evaluation | 6 files | ~80 scenarios |
| Condition rendering | 10 files | ~85 scenarios |
| Diff rendering | 7 files | ~55 scenarios |
| Invalid/error conditions | 3 files | ~45 scenarios |
| AST transformation snapshot tests | 7 files | ~40 scenarios |
| Runtime unit tests | 2 files | ~120 assertions |
| **Total** | **41 files** | **~470 scenarios** |

---

## 6. Key Source Files (for implementation reference)

| File | Path (from spock-core) |
|---|---|
| ConditionRewriter | `src/main/java/org/spockframework/compiler/ConditionRewriter.java` |
| DeepBlockRewriter | `src/main/java/org/spockframework/compiler/DeepBlockRewriter.java` |
| ImplicitConditionsUtils | `src/main/java/org/spockframework/compiler/condition/ImplicitConditionsUtils.java` |
| BaseVerifyMethodRewriter | `src/main/java/org/spockframework/compiler/condition/BaseVerifyMethodRewriter.java` |
| Condition (runtime) | `src/main/java/org/spockframework/runtime/Condition.java` |
| ValueRecorder | `src/main/java/org/spockframework/runtime/ValueRecorder.java` |
| ExpressionInfoRenderer | `src/main/java/org/spockframework/runtime/ExpressionInfoRenderer.java` |
