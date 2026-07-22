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
A Kotlin class is a Spockk spec if it (or any superclass) extends `spock.lang.Specification`. Traverse the PSI supertype list to check for a reference resolvable to this FQN.

**Feature method detection** (`isTestMethod()`):
A Kotlin function inside a spec class is a feature if:
- Its body contains at least one Spockk block label reference (`given`, `setup`, `expect`, `when`, `then`, `and`, `where`, `cleanup`)
- It is NOT a fixture method (`setup`, `cleanup`, `setupSpec`, `cleanupSpec`)

Reuses `isSpockkBlock()` from `Psi.kt` with a body-scoped PSI visitor.

**Edge cases:**
- **Abstract spec classes:** Detected as test classes. IntelliJ automatically provides a dropdown of concrete subclasses to run.
- **Fixture methods:** NOT detected as feature methods.
- **Nested/inner spec classes:** NOT detected (no gutter icons).
- **Kotlin `object` specs:** NOT detected (no gutter icons).

**Icon:** Standard test run icon (`AllIcons.RunConfigurations.TestState.Run`).

### 2. `KotlinTestFrameworkProvider` — `SpockkTestFrameworkProvider`

**Extension point:** `org.jetbrains.kotlin.idea.testFrameworkProvider`

Implements `KotlinTestFrameworkProvider` to bridge Spockk detection with the Kotlin plugin's JVM test infrastructure:
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
