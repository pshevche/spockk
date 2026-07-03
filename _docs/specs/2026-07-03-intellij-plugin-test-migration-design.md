# Migrate IntelliJ Plugin Tests from JUnit 4 to Spockk

**Issue:** #203
**Date:** 2026-07-03
**Status:** Draft

## Summary

Migrate the `spockk-intellij-plugin` module's 6 test classes off JUnit 4 onto JUnit 5 (Phase 1), then investigate migrating to Spockk specs (Phase 2). Tests stay in `spockk-intellij-plugin` module.

## Current State

- 6 test classes in `spockk-intellij-plugin/src/test/kotlin/` using JUnit 4
- Base class: `BaseSpockkIntelliJPluginTestCase` extends `LightJavaCodeInsightFixtureTestCase` (JUnit 3/4)
- Test methods: `fun testXxx()` (JUnit 3 naming convention)
- Lifecycle: `override fun setUp()` (JUnit 3 style)
- Dependencies: `junit:junit:4.13.2`, `TestFrameworkType.Platform` already configured
- No JUnit 5 Jupiter dependencies in version catalog

## Phase 1: JUnit 4 → JUnit 5

### Build Configuration

The project already uses JUnit Platform 6.1.1. JUnit Jupiter 6.x is version-aligned with Platform 6.x.

- Add `junit-jupiter` to `gradle/libs.versions.toml`:
  ```toml
  [versions]
  junit-jupiter = "6.1.1"

  [libraries]
  junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
  ```
- Replace `testImplementation(libs.junit4)` with `testImplementation(libs.junit-jupiter)` in `spockk-intellij-plugin/build.gradle.kts`
- Remove `testImplementation(libs.opentest4j)` — JUnit Jupiter bundles opentest4j transitively
- Keep `TestFrameworkType.Platform` — this provides JUnit Platform infrastructure

### Base Class Migration

Replace `LightJavaCodeInsightFixtureTestCase` with `com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5` (confirmed available in IntelliJ Platform 2025.3).

`LightJavaCodeInsightFixtureTestCase5` uses the same `myFixture` field, `getTestDataPath()`, and other API as its JUnit 4 counterpart, but with JUnit 5 lifecycle (uses `@Test` annotations instead of naming conventions).

### BaseSpockkIntelliJPluginTestCase (`BaseSpockkIntelliJPluginTestCase.kt`)

Changes:
- Extends `LightJavaCodeInsightFixtureTestCase5` instead of `LightJavaCodeInsightFixtureTestCase`
- No other changes needed — `getTestDataPath()`, `configureFromDefaultFile()`, and `findRequiredElementByTextAndType()` are independent of the test framework

### BaseSpockkUnusedExpressionInspectionSuppressorTest (`BaseSpockkUnusedExpressionInspectionSuppressorTest.kt`)

Changes:
- Add `@BeforeEach` annotation to `override fun setUp()`

### Concrete Test Classes (4 files)

All test methods gain `@Test` annotation:
- `SpockkUnreachableCodeSuppressorTest` — 3 methods
- `SpockkUnusedBlockLabelSuppressorTest` — 3 methods
- `SpockkUnusedDataTableStatementSuppressorTest` — 2 methods
- `SpockkDataTableFormattingModelBuilderTest` — 5 methods

Assertion imports change from `org.junit.Assert` to `org.junit.jupiter.api.Assertions`.

### Test Data

No changes — resource files in `src/test/resources/<TestClassName>/` remain as-is.

### Risk

Low. IntelliJ Platform has supported JUnit 5 since 2021.x. The Gradle Plugin's `TestFrameworkType.Platform` already brings JUnit Platform. The `LightJavaCodeInsightFixtureTestCase5` class needs to be verified on IntelliJ 2025.3.

## Phase 2: JUnit 5 → Spockk Investigation

### Challenge

IntelliJ Platform test base classes (`LightJavaCodeInsightFixtureTestCase5`) are class-inheritance-based, but Spockk specs need to extend `spockk.lang.Specification`. Cannot inherit from both.

### Approach Options

Two composition approaches are possible:

**A. Manual fixture creation** — Use `IdeaTestFixtureFactory` in Spockk's `setup()`/`cleanup()`:

```kotlin
class SuppressorSpec : SpockkSpecification() {
  lateinit var myFixture: CodeInsightTestFixture

  def setup() {
    val projectFixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder().fixture
    myFixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createCodeInsightTestFixture(projectFixture)
    myFixture.setUp()
  }

  def cleanup() {
    myFixture.tearDown()
  }
}
```

**B. IntelliJ's `@TestFixtures` extension system** — The Platform now provides JUnit 5 extensions (`TestFixtureInitializer`, `@TestFixtures`) for framework-agnostic fixture management. A Spockk spec could use these extensions directly via `@RegisterExtension` or `@TestFixtures`.

### Validation

1. Convert one simple test (e.g., `SpockkUnreachableCodeSuppressorTest`) to a Spockk spec
2. Run via `TestFrameworkType.Platform` + Spock engine
3. If successful: migrate remaining 5 test files
4. If blocked: document blockers, keep JUnit 5 as stable state

## Postmortem: Phase 2 Spockk Migration Attempt

Phase 2 was explored but ultimately abandoned. Here's what happened and why.

### Attempted Approach

Converted `SpockkUnreachableCodeSuppressorTest` to a Spockk spec by extending `spock.lang.Specification` and manually creating the `CodeInsightTestFixture` via `IdeaTestFixtureFactory` in `setup()`/`cleanup()`.

### What Worked

- **Spockk spec syntax**: The compiler plugin correctly transforms `fun \`feature name\`()` and imported block labels (`expect`, `cleanup`, etc.). The spec compiled successfully.
- **Spockk compiler plugin application**: The `spockk.compiler-plugin-consumer` convention plugin correctly applies the `-Xplugin` argument to `compileTestKotlin` (tested with a `SmokeSpockkSpec`).

### What Blocked

**1. IntelliJ Platform test sandbox causes classpath conflict with Spock**

The IntelliJ Platform Gradle Plugin creates a test sandbox that copies all test runtime dependencies into `.intellijPlatform/sandbox/.../plugins-test/.../lib/`. When `spockk-core` (shadow jar) is added as a test dependency, its transitive `spock-core` jar gets copied to the sandbox. At runtime, both the original `spock-core` jar (from Gradle's classpath) and the sandbox copy are loaded, causing Spock's `ExtensionClassesLoader` to detect duplicate extension declarations and fail with:

```
ExtensionException: Duplicated Extension declaration for [...]
```

The error is thrown by `ExtensionClassesLoader.loadClasses()`, which strictly rejects any duplicate `IGlobalExtension` implementations — there is no opt-out flag.

**2. No clean way to prevent the sandbox duplication**

- Excluding `spock-core` as a transitive dependency breaks compilation because composite builds (used during development with `projects.spockkCore`) don't consume the shadow jar; they use the project's raw class files, which don't include spock-core.
- Preventively deleting the jar from the sandbox in a task `doLast` block is fragile and paths are version-dependent.
- Changing `spockk-core`'s dependency from `api` to `implementation` would break `spockk-specs`, which relies on the transitive `spock-core` in composite mode.

### Resolution

Phase 2 is abandoned. The existing JUnit 5 migration (Phase 1) is sufficient: 13 tests pass, all JUnit 4 usage eliminated. The Spockk spec files have been removed from the branch.

## Non-Goals

- Moving tests to `spockk-specs` module (out of scope for this issue)
- Changing IntelliJ Platform version or test infrastructure
- Refactoring production code in `spockk-intellij-plugin`

## Acceptance Criteria

- [ ] `spockk-intellij-plugin` tests are no longer using JUnit 4
- [ ] All 6 test classes use JUnit 5 (Phase 1)
- [ ] Build passes with `./gradlew build`
- [ ] Optionally: 1+ test classes use Spockk syntax (Phase 2)
