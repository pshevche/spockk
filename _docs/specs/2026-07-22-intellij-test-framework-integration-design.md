# IntelliJ Test Framework Integration for Spockk

## Problem

In IntelliJ 2026.2, the built-in Spock plugin was rewritten and no longer provides test framework support for Kotlin-based Spockk specs. Spockk spec classes and feature methods are not recognized as test elements — gutter run icons are missing, and tests cannot be launched from the IDE.

## Approach

Register Spockk as a proper IntelliJ test framework via the `testFramework` extension point. Provide a `TestRunConfigurationProducer` that creates Gradle-based run configurations (tests must execute via Gradle with the Spockk compiler plugin applied). Gutter icons are provided either automatically by the test framework registration or via a dedicated `LineMarkerProvider`.

## Architecture

### 1. Test Framework — `SpockkTestFramework`

**Extension point:** `com.intellij.testFramework`

Implements `TestFramework` (or extends `JavaTestFramework`) to recognize Spockk test elements at the PSI level.

**Spec class detection:**
A Kotlin class (`KtClass`) is a Spockk spec if it (or any superclass) extends `spock.lang.Specification`. Traverse the PSI supertype list to check for a reference resolvable to this FQN.

**Feature method detection:**
A Kotlin function (`KtFunction`) inside a spec class is a feature if:
- Its body contains at least one Spockk block label reference (`given`, `setup`, `expect`, `when`, `then`, `and`, `where`, `cleanup`)
- It is NOT a fixture method (`setup`, `cleanup`, `setupSpec`, `cleanupSpec`)

Reuses `isSpockkBlock()` from `Psi.kt` with a body-scoped PSI visitor.

### 2. PSI Utilities — `Psi.kt` additions

- `PsiElement.isSpockkSpec()` — checks if an enclosing `KtClass` extends `spock.lang.Specification`
- `PsiElement.isSpockkFeature()` — checks if an enclosing `KtFunction` is inside a spec class and contains block labels

These are shared by the test framework and line marker provider.

### 3. Gutter Icons — Conditional

If the registered `TestFramework` provides automatic line markers in the IDE, no custom provider is needed. Otherwise, a `LineMarkerProvider` extension is added for Kotlin language.

**Spec class level:** Green run icon next to `class` keyword. Offers "Run Spockk Spec" / "Debug Spockk Spec".

**Feature method level:** Green run icon next to `fun` keyword. Offers "Run Spockk Feature" / "Debug Spockk Feature".

### 4. Run Configuration Producer — `SpockkTestRunConfigurationProducer`

**Extension point:** `com.intellij.configurationProducer`

Creates `GradleRunConfiguration` instances when clicking gutter icons.

**Class-level:** Gradle run config with `--tests "pkg.SpecClassName"` filter.

**Method-level:** Gradle run config with `--tests "pkg.SpecClassName.featureName"` filter.

**Gradle task detection:**
- Queries the IntelliJ Gradle project model for the module
- Finds all `Test`-type tasks (e.g., `test`, `integrationTest`, `customTest`)
- Auto-selects the `test` task by default
- User can change the task from the run configuration dropdown

**Implementation:**
- Uses `GradleConfigurationType` to create `GradleRunConfiguration` instances
- Sets `taskNames` to the selected Gradle task (e.g., `:module:test`)
- Sets `scriptParameters` with `--tests` filter
- Working directory is the project root

Test results appear automatically via IntelliJ's Gradle integration (XML test report parsing).

## Files

### New files
- `src/main/kotlin/.../SpockkTestFramework.kt`
- `src/main/kotlin/.../SpockkTestRunConfigurationProducer.kt`
- (Conditional) `src/main/kotlin/.../SpockkLineMarkerProvider.kt`

### Modified files
- `src/main/kotlin/.../Psi.kt` — add `isSpockkSpec()`, `isSpockkFeature()`
- `src/main/resources/META-INF/plugin.xml` — register new extensions

### Test files
- `src/test/kotlin/.../SpockkTestFrameworkTest.kt` — tests spec/feature detection and gutter icon presence

## Considerations

**Method-level `--tests` filtering:** Spockk feature names are Kotlin identifiers (e.g., `` `some feature` ``). The `@FeatureMetadata` annotation stores the original name. For Gradle `--tests` filtering, class-level (`--tests "pkg.SpecClass"`) maps to JUnit Platform `ClassSelector` which works reliably. Method-level filtering may need refinement during implementation — alternatives include display name filters or unique ID selectors.

## Verification

- Test that spec classes extending `spock.lang.Specification` are detected
- Test that feature methods with block labels are detected
- Test that regular methods (no block labels) and fixture methods are NOT detected
- Test that gutter icons appear on spec classes and feature methods
- Test that clicking the gutter icon produces a valid Gradle run configuration with `--tests` filter
