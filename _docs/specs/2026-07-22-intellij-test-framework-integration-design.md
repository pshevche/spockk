# IntelliJ Test Framework Integration for Spockk

## Problem

In IntelliJ 2026.2, the built-in Spock plugin was rewritten and no longer provides test framework support for Kotlin-based Spockk specs. Spockk spec classes and feature methods are not recognized as test elements — gutter run icons are missing, and tests cannot be launched from the IDE.

## Approach

Register Spockk as a proper IntelliJ test framework via three extension points:
1. `com.intellij.testFramework` — `SpockkTestFramework` detects Spockk specs and features
2. `org.jetbrains.kotlin.idea.testFrameworkProvider` — `SpockkTestFrameworkProvider` bridges to Kotlin test infrastructure
3. `com.intellij.runConfigurationProducer` — Gradle-based run configuration producers

Tests must execute via Gradle (the Spockk compiler plugin is applied through the Gradle plugin). Gutter icons are auto-provided by the IntelliJ test framework infrastructure once the framework is registered; if not, a `RunLineMarkerContributor` for Kotlin will be added.

## Architecture

### 1. Test Framework — `SpockkTestFramework`

**Extension points:** `com.intellij.testFramework`, `org.jetbrains.kotlin.idea.testFrameworkProvider`

**Class hierarchy:**
- Implements `TestFramework` directly (does NOT extend `JavaTestFramework` — Spockk uses superclass-based detection, not annotation-based marker classes)
- Implements `KotlinPsiBasedTestFramework` directly (does NOT extend `AbstractKotlinPsiBasedTestFramework` — Spockk's superclass-based detection doesn't fit the annotation-oriented base class)

**Spec class detection** (`isTestClass()` / `checkTestClass()`):
A Kotlin class is a Spockk spec if it (or any superclass) extends `spock.lang.Specification`. Uses PSI resolution (`KtSuperTypeListEntry.typeReference?.resolveToClass()`) to walk the real superclass hierarchy. Results are cached via `CachedValuesManager` with `PsiModificationTracker.MODIFICATION_COUNT` dependency (same pattern as existing `Psi.kt`).

**Scope:** We do NOT restrict to test source roots (`isUnderTestSources()`). Spockk specs are always in test sources by convention (they depend on `spock.lang.Specification` which is a test dependency), but checking this adds complexity with no practical benefit. A class in `src/main/` that extends `Specification` would get gutter icons, which is correct behavior if someone puts a spec there.

**Feature method detection** (`isTestMethod()`):
A Kotlin function inside a spec class is a feature if:
- Its body contains at least one Spockk block label reference (`given`, `setup`, `expect`, `when`, `then`, `and`, `where`, `cleanup`)
- It is NOT a fixture method (`setup`, `cleanup`, `setupSpec`, `cleanupSpec`)

Uses `PsiTreeUtil.collectElementsOfType(function, KtNameReferenceExpression::class.java)` to find all reference expressions in the method body, then filters each candidate through `isSpockkBlock()` from `Psi.kt`. This covers reference expressions at any nesting depth (including inside lambdas) but does NOT descend into nested class or object declarations within the method body — those are separate classes, not features.

**Edge cases:**
- **Abstract spec classes:** Detected as test classes. IntelliJ automatically provides a dropdown of concrete subclasses to run.
- **Fixture methods:** NOT detected as feature methods.
- **Nested/inner spec classes:** NOT detected (no gutter icons).
- **Kotlin `object` specs:** NOT detected (no gutter icons). `object MySpec : Specification()` is valid but excluded because spec classes are typically `class` declarations. `object` specs are uncommon and the `KotlinPsiBasedTestFramework.checkTestClass()` base logic has different code paths for `object` vs `class` — excluding `object` avoids edge cases. This is a deliberate scope reduction; `object` specs can be run by adding a run config manually or by converting to a `class`.

**Icon:** Standard test run icon (`AllIcons.RunConfigurations.TestState.Run`).

### 2. `KotlinTestFrameworkProvider` — `SpockkTestFrameworkProvider`

**Extension point:** `org.jetbrains.kotlin.idea.testFrameworkProvider`

**Why needed:** The Kotlin plugin has its own test detection layer that runs alongside IntelliJ's Java `TestFrameworks` service. Without a registered `KotlinTestFrameworkProvider`:

- **`isProducedByKotlin()` / `isProducedByJava()`** — The Kotlin plugin uses these to decide configuration precedence. Missing them means Spockk run configs produced by Kotlin-aware producers could be silently replaced by generic Java config producers (e.g., the JUnit runner).
- **`getJavaTestEntity()`** — The Gradle run configuration base classes need a Java `PsiClass`/`PsiMethod` to build `--tests` filters. Without a provider that extracts these from Kotlin PSI, method-level filtering won't work.
- **`isTestFrameworkAvailable()`** — Used by the Kotlin plugin's test UI to decide whether to offer Spockk gutter icons. Missing this means no gutter icons when the Kotlin plugin is active.
- **`isTestJavaClass()` / `isTestJavaMethod()`** — These bridge our Kotlin PSI detection to the Java PSI layer, ensuring that when the Kotlin plugin's `PsiClass` facade asks "is this a test?", it gets the right answer.

Implements `KotlinTestFrameworkProvider`:
- `getCanRunJvmTests()` — returns `true`
- `isTestJavaClass()` / `isTestJavaMethod()` — delegates to `SpockkTestFramework`
- `isTestFrameworkAvailable()` — checks if `spock.lang.Specification` is on the module's classpath
- `getJavaTestEntity()` / `getJavaEntity()` — provides Java PSI equivalents for Kotlin PSI elements

### 3. PSI Utilities — `Psi.kt` additions

- `PsiElement.isSpockkSpec()` — checks if an enclosing `KtClass` extends `spock.lang.Specification`
- `PsiElement.isSpockkFeature()` — checks if an enclosing `KtFunction` is inside a spec class and contains block labels (excluding fixture methods)

These are shared by the test framework and run configuration producer.

### 4. Gutter Icons

**Implementation approach:**
1. Register `TestFramework` — if the existing `TestRunLineMarkerProvider` (registered for `JAVA` language) or Kotlin plugin's equivalent automatically picks up our framework, no custom provider is needed.
2. A test verifies gutter icon presence. If absent, add `RunLineMarkerContributor` for `kotlin` language.

**Spec class level:** Green run icon next to `class` keyword. Left-click offers "Run Spockk Spec" / "Debug Spockk Spec". Runs all features in the spec class.

**Feature method level:** Green run icon next to `fun` keyword. Left-click offers "Run Spockk Feature" / "Debug Spockk Feature". Runs a single feature using display name filtering.

### 5. Gradle Run Configuration Producers

**Extension point:** `com.intellij.runConfigurationProducer`

**Class-level producer:** `SpockkTestClassGradleConfigurationProducer` extends `AbstractKotlinTestClassGradleConfigurationProducer`

**Method-level producer:** `SpockkTestMethodGradleConfigurationProducer` extends `AbstractKotlinTestMethodGradleConfigurationProducer`

These follow the same pattern as `KotlinJvmTestClassGradleConfigurationProducer` / `KotlinJvmTestMethodGradleConfigurationProducer`. The base classes handle:
- Detecting available Gradle test tasks (all `Test`-type tasks in the module) — no custom logic needed
- Creating `GradleRunConfiguration` instances
- Setting `--tests` filter

**Method-level filtering:** Uses the feature's original name as display name. `--tests "pkg.SpecClass.some feature"` sends a `MethodSelector` to JUnit Platform, which the Spock engine resolves via `@FeatureMetadata.name`. This is confirmed working.

**Filter format details:**
- Feature names are Kotlin identifiers (e.g., `` fun `some feature`() ``). The backticks are Kotlin syntax, not part of the identifier. The PSI element name is `"some feature"` (no backticks), and that's what goes in the `--tests` filter.
- Spaces in the display name are passed through — the `--tests` filter value is double-quoted and handled by Gradle's argument parser, not the shell. Gradle passes it as a single argument to the JVM.
- Characters like `$`, `#`, `!` in display names may cause issues with Gradle's `--tests` filter parsing. These are unusual in feature names but could appear. If a filter fails to match, Gradle runs no tests and exits successfully — no error. We document this as a known limitation.

## Files

### New files
| File | Purpose |
|------|---------|
| `SpockkTestFramework.kt` | TestFramework + KotlinPsiBasedTestFramework implementation |
| `SpockkTestFrameworkProvider.kt` | KotlinTestFrameworkProvider implementation |
| `SpockkTestClassGradleConfigurationProducer.kt` | Class-level Gradle run config producer |
| `SpockkTestMethodGradleConfigurationProducer.kt` | Method-level Gradle run config producer |
| (Conditional) `SpockkRunLineMarkerContributor.kt` | Gutter icons, only if TestFramework doesn't auto-provide them |

### Modified files
| File | Changes |
|------|---------|
| `Psi.kt` | Add `isSpockkSpec()`, `isSpockkFeature()` |
| `plugin.xml` | Register testFramework, testFrameworkProvider, runConfigurationProducer extensions |

**Build changes:** Add `libs.spock` (spock-core) as a `testImplementation` dependency of `spockk-intellij-plugin` — required for PSI resolution of `spock.lang.Specification` in tests.

### Test files
| File | What it tests |
|------|---------------|
| `SpockkTestFrameworkTest.kt` | Spec class detection, feature method detection, fixture method exclusion, abstract class handling |
| `SpockkTestFrameworkIconTest.kt` | Gutter icon presence on spec classes and feature methods |
| `SpockkTestRunConfigurationProducerTest.kt` | Run config created with correct --tests filter, Gradle task selection |

## Verification

- Test that spec classes extending `spock.lang.Specification` are detected
- Test that feature methods with block labels are detected
- Test that fixture methods (`setup`, `cleanup`, `setupSpec`, `cleanupSpec`) are NOT detected as features
- Test that regular methods (no block labels) in spec classes are NOT detected
- Test that abstract spec classes are detected and produce runnable configurations
- Test that nested classes and non-spec classes are NOT detected
- Test that gutter icons appear on spec classes and feature methods
- Test that clicking the gutter icon produces a valid Gradle run configuration with `--tests` filter
- Test that the run configuration correctly targets the module's Gradle test tasks

## Dependencies on Kotlin Plugin API

The Kotlin plugin classes referenced are internal API (`org.jetbrains.kotlin.idea.gradleJava.run`). This is acceptable because:
- `sinceBuild = "262"` pins to IntelliJ 2026.2 with a specific Kotlin plugin version
- The pattern follows established convention used by Kotlin's own JUnit/TestNG integration
- If the Kotlin plugin API changes in a future IntelliJ version, the plugin can be updated accordingly

**Fallback plan:** If `AbstractKotlinTestClassGradleConfigurationProducer` is not accessible (package visibility), fall back to extending `TestClassGradleConfigurationProducer` (public Gradle Java API) directly. The `TestFrameworks.isTestClass()` mechanism still works via our `TestFramework` registration.

## Build Changes

Add `libs.spock` (spock-core) to `spockk-intellij-plugin` as `testImplementation` dependency — required for PSI resolution of `spock.lang.Specification` in test fixtures.

**Test classpath verification:** `LightJavaCodeInsightFixtureTestCase5` resolves test fixture code against the test module's classpath. Adding `libs.spock` as `testImplementation` should be sufficient — the IntelliJ test framework includes the test runtime classpath. If resolution fails, verify that:
1. `spock.lang.Specification` appears in the test module's resolved dependency graph
2. The test project's module root manager includes the spock library
