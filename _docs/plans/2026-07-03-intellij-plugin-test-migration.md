# IntelliJ Plugin Test Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `spockk-intellij-plugin` tests from JUnit 4 to JUnit 5 (Phase 1), then investigate Spockk migration (Phase 2).

**Architecture:** Replace `LightJavaCodeInsightFixtureTestCase` (JUnit 4) with `LightJavaCodeInsightFixtureTestCase5` (JUnit 5) across 2 base classes and 4 concrete test files. Add `junit-jupiter:6.1.1` dependency. Then attempt converting one test to a Spockk spec using manual `CodeInsightTestFixture` composition.

**Tech Stack:** Kotlin 2.4.0, IntelliJ Platform 2025.3.4, JUnit 5 (Jupiter 6.1.1), JUnit Platform 6.1.1

---

### Task 1: Update version catalog and build configuration

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `spockk-intellij-plugin/build.gradle.kts`

- [ ] **Step 1: Add `junit-jupiter` to version catalog**

Edit `gradle/libs.versions.toml`:
- Add `junit-jupiter = "6.1.1"` under `[versions]`
- Add `junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }` under `[libraries]`

Result:
```toml
[versions]
auto-service = "1.1.1"
junit-platform = "6.1.1"
junit-jupiter = "6.1.1"
kotlin = "2.4.0"
```

```toml
[libraries]
google-autoservice = { module = "com.google.auto.service:auto-service", version.ref = "auto-service" }
google-autoservice-annotations = { module = "com.google.auto.service:auto-service-annotations", version.ref = "auto-service" }
hamcrest = { module = "org.hamcrest:hamcrest", version = "3.0" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junit-platform" }
junit-platform-testkit = { module = "org.junit.platform:junit-platform-testkit", version.ref = "junit-platform" }
junit4 = { module = "junit:junit", version = "4.13.2" }
opentest4j = { module = "org.opentest4j:opentest4j", version = "1.3.0" }
```

- [ ] **Step 2: Update `spockk-intellij-plugin/build.gradle.kts`**

Replace:
```kotlin
  testImplementation(libs.junit4)
  testImplementation(libs.opentest4j)
```

With:
```kotlin
  testImplementation(libs.junit.jupiter)
```

Wait — the version catalog name will have a hyphen, so the accessor would be `libs.junit.jupiter`. But in TOML, `junit-jupiter` becomes `libs.junit.jupiter`. Let me verify this.

Actually, in Gradle version catalogs, `junit-jupiter` becomes `libs.junit.jupiter`. The hyphen is converted to a dot. This is correct.

Final dependencies block:
```kotlin
dependencies {
  intellijPlatform {
    intellijIdea("2025.3.4")

    bundledPlugin("org.jetbrains.kotlin")
    bundledPlugin("org.jetbrains.plugins.gradle")

    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }

  testImplementation(libs.hamcrest)
  testImplementation(libs.junit.jupiter)
}
```

- [ ] **Step 3: Run build to verify compilation**

Run: `./gradlew :spockk-intellij-plugin:compileTestKotlin`
Expected: BUILD SUCCESSFUL (tests won't pass yet — they still reference JUnit 4 base classes)

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml spockk-intellij-plugin/build.gradle.kts
git commit -m "build: add JUnit 5 dependency for IntelliJ plugin tests (#203)"
```

---

### Task 2: Migrate BaseSpockkIntelliJPluginTestCase to JUnit 5

**Files:**
- Modify: `spockk-intellij-plugin/src/test/kotlin/.../BaseSpockkIntelliJPluginTestCase.kt`

- [ ] **Step 1: Change import and base class**

In `BaseSpockkIntelliJPluginTestCase.kt`, replace:
```kotlin
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

abstract class BaseSpockkIntelliJPluginTestCase : LightJavaCodeInsightFixtureTestCase() {
```

With:
```kotlin
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5

abstract class BaseSpockkIntelliJPluginTestCase : LightJavaCodeInsightFixtureTestCase5() {
```

- [ ] **Step 2: Run tests to verify**

Run: `./gradlew :spockk-intellij-plugin:compileTestKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add spockk-intellij-plugin/src/test/kotlin/
git commit -m "refactor: migrate BaseSpockkIntelliJPluginTestCase to JUnit 5 (#203)"
```

---

### Task 3: Migrate BaseSpockkUnusedExpressionInspectionSuppressorTest to JUnit 5

**Files:**
- Modify: `spockk-intellij-plugin/src/test/kotlin/.../BaseSpockkUnusedExpressionInspectionSuppressorTest.kt`

- [ ] **Step 1: Add `@BeforeEach` annotation**

In `BaseSpockkUnusedExpressionInspectionSuppressorTest.kt`, add import and annotation:

```kotlin
import org.junit.jupiter.api.BeforeEach

abstract class BaseSpockkUnusedExpressionInspectionSuppressorTest : BaseSpockkIntelliJPluginTestCase() {

  protected lateinit var suppressor: SpockkUnusedExpressionInspectionSuppressor

  @BeforeEach
  override fun setUp() {
    super.setUp()
    suppressor = SpockkUnusedExpressionInspectionSuppressor()
  }
```

- [ ] **Step 2: Run tests to verify**

Run: `./gradlew :spockk-intellij-plugin:compileTestKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/BaseSpockkUnusedExpressionInspectionSuppressorTest.kt
git commit -m "refactor: migrate BaseSpockkUnusedExpressionInspectionSuppressorTest to JUnit 5 (#203)"
```

---

### Task 4: Migrate SpockkUnreachableCodeSuppressorTest to JUnit 5

**Files:**
- Modify: `spockk-intellij-plugin/src/test/kotlin/.../SpockkUnreachableCodeSuppressorTest.kt`

- [ ] **Step 1: Add `@Test` annotations and `@BeforeEach`, fix imports**

File becomes:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.psi.PsiElement
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkUnreachableCodeSuppressorTest : BaseSpockkIntelliJPluginTestCase() {

  private lateinit var suppressor: SpockkUnusedExpressionInspectionSuppressor

  @BeforeEach
  override fun setUp() {
    super.setUp()
    suppressor = SpockkUnusedExpressionInspectionSuppressor()
    myFixture.configureFromDefaultFile()
  }

  private fun isSuppressedFor(elementText: String): Boolean =
    suppressor.isSuppressedFor(
      findRequiredElementByTextAndType(elementText, PsiElement::class.java),
      "KotlinUnreachableCode"
    )

  @Test
  fun testSuppressUnreachableCodeWarningsForWhereBlockStatements() {
    assertTrue(isSuppressedFor("val11"))
    assertTrue(isSuppressedFor("val21"))
  }

  @Test
  fun testSuppressUnreachableCodeWarningsForCleanupBlockStatements() {
    assertTrue(isSuppressedFor("println"))
  }

  @Test
  fun testDoesNotSuppressUnreachableCodeOutsideSpecialBlocks() {
    assertFalse(isSuppressedFor("regularStatement"))
  }
}
```

- [ ] **Step 2: Run the specific test class**

Run: `./gradlew :spockk-intellij-plugin:test --tests "*SpockkUnreachableCodeSuppressorTest*"`
Expected: All 3 tests PASS

- [ ] **Step 3: Commit**

```bash
git add spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkUnreachableCodeSuppressorTest.kt
git commit -m "refactor: migrate SpockkUnreachableCodeSuppressorTest to JUnit 5 (#203)"
```

---

### Task 5: Migrate SpockkUnusedBlockLabelSuppressorTest to JUnit 5

**Files:**
- Modify: `spockk-intellij-plugin/src/test/kotlin/.../SpockkUnusedBlockLabelSuppressorTest.kt`

- [ ] **Step 1: Add `@Test` annotations and `@BeforeEach`, fix imports**

File becomes:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.psi.PsiElement
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkUnusedBlockLabelSuppressorTest : BaseSpockkUnusedExpressionInspectionSuppressorTest() {

  @BeforeEach
  override fun setUp() {
    super.setUp()
    myFixture.configureFromDefaultFile()
  }

  @Test
  fun testSuppressUnusedWarningsForSpockkBlockObjectReferences() {
    assertTrue(isSuppressedFor("expect"))
  }

  @Test
  fun testWarnsAboutSpockkObjectReferencesForOtherInspections() {
    assertFalse(
      suppressor.isSuppressedFor(
        myFixture.findElementByText("expect", PsiElement::class.java),
        "UnusedDeclaration"
      )
    )
  }

  @Test
  fun testWarnsAboutUnusedNonSpockkObjectReferences() {
    assertFalse(isSuppressedFor("expect"))
  }
}
```

- [ ] **Step 2: Run the specific test class**

Run: `./gradlew :spockk-intellij-plugin:test --tests "*SpockkUnusedBlockLabelSuppressorTest*"`
Expected: All 3 tests PASS

- [ ] **Step 3: Commit**

```bash
git add spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkUnusedBlockLabelSuppressorTest.kt
git commit -m "refactor: migrate SpockkUnusedBlockLabelSuppressorTest to JUnit 5 (#203)"
```

---

### Task 6: Migrate SpockkUnusedDataTableStatementSuppressorTest to JUnit 5

**Files:**
- Modify: `spockk-intellij-plugin/src/test/kotlin/.../SpockkUnusedDataTableStatementSuppressorTest.kt`

- [ ] **Step 1: Add `@Test` annotations and `@BeforeEach`, fix imports**

File becomes:

```kotlin
package io.github.pshevche.spockk.intellij

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class SpockkUnusedDataTableStatementSuppressorTest : BaseSpockkUnusedExpressionInspectionSuppressorTest() {

  @BeforeEach
  override fun setUp() {
    super.setUp()
    myFixture.configureFromDefaultFile()
  }

  @Test
  fun testSuppressUnusedWarningsForDataTableStatements() {
    listOf("variable1", "variable2").forEach {
      assertTrue(isSuppressedFor(it, KtNameReferenceExpression::class.java))
    }

    listOf("val11", "val21", "val12", "val22").forEach {
      assertTrue(isSuppressedFor(it, KtLiteralStringTemplateEntry::class.java))
    }
  }

  @Test
  fun testWarnsAboutDataTableStatementsInNonFeatures() {
    listOf("variable1", "variable2").forEach {
      assertFalse(isSuppressedFor(it, KtNameReferenceExpression::class.java))
    }

    listOf("val11", "val21", "val12", "val22").forEach {
      assertFalse(isSuppressedFor(it, KtLiteralStringTemplateEntry::class.java))
    }
  }
}
```

- [ ] **Step 2: Run the specific test class**

Run: `./gradlew :spockk-intellij-plugin:test --tests "*SpockkUnusedDataTableStatementSuppressorTest*"`
Expected: Both tests PASS

- [ ] **Step 3: Commit**

```bash
git add spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkUnusedDataTableStatementSuppressorTest.kt
git commit -m "refactor: migrate SpockkUnusedDataTableStatementSuppressorTest to JUnit 5 (#203)"
```

---

### Task 7: Migrate SpockkDataTableFormattingModelBuilderTest to JUnit 5

**Files:**
- Modify: `spockk-intellij-plugin/src/test/kotlin/.../SpockkDataTableFormattingModelBuilderTest.kt`

- [ ] **Step 1: Add `@Test` annotations**

File becomes:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import org.junit.jupiter.api.Test

class SpockkDataTableFormattingModelBuilderTest : BaseSpockkIntelliJPluginTestCase() {

  @Test
  fun testApplyKotlinFormatterByDefault() {
    checkFormattingBeforeAndAfter()
  }

  @Test
  fun testApplyDefaultFormatterToTablesOutsideOfWhereBlock() {
    checkFormattingBeforeAndAfter()
  }

  @Test
  fun testSpockkTableBasicUsage() {
    checkFormattingBeforeAndAfter()
  }

  @Test
  fun testSpockkTableWithComments() {
    checkFormattingBeforeAndAfter()
  }

  @Test
  fun testSpockkTableWithBlockDescription() {
    checkFormattingBeforeAndAfter()
  }

  private fun checkFormattingBeforeAndAfter() {
    myFixture.configureByFile("/$name/before.kt")

    WriteCommandAction.runWriteCommandAction(project) {
      CodeStyleManager.getInstance(project).reformat(myFixture.file)
    }

    myFixture.checkResultByFile("/$name/after.kt")
  }
}
```

- [ ] **Step 2: Run the specific test class**

Run: `./gradlew :spockk-intellij-plugin:test --tests "*SpockkDataTableFormattingModelBuilderTest*"`
Expected: All 5 tests PASS

- [ ] **Step 3: Commit**

```bash
git add spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkDataTableFormattingModelBuilderTest.kt
git commit -m "refactor: migrate SpockkDataTableFormattingModelBuilderTest to JUnit 5 (#203)"
```

---

### Task 8: Format, full build, and finalize

**Files:**
- No source changes — run formatting and full build

- [ ] **Step 1: Run spotlessCheck**

Run: `./gradlew spotlessCheck`
Expected: PASS (no formatting violations)

If it fails, run: `./gradlew spotlessApply`

- [ ] **Step 2: Run full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Remove JUnit 4 dependency from version catalog** (if no other module uses it)

Check: `grep -r "libs.junit4" --include="*.kts" --include="*.toml" -l`
If only `gradle/libs.versions.toml` references it, remove the `junit4` and `opentest4j` entries from the catalog.

- [ ] **Step 4: Commit final cleanup**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: remove unused JUnit 4 dependencies (#203)"
```

---

### Task 9: Phase 2 — Explore Spockk migration (experimental)

**Files:**
- Create: `spockk-intellij-plugin/src/test/kotlin/.../SpockkUnreachableCodeSuppressorSpockkSpec.kt`
- Modify: `spockk-intellij-plugin/build.gradle.kts` (add Spockk runtime dependency)

- [ ] **Step 1: Add Spockk dependency to build.gradle.kts**

```kotlin
dependencies {
  // ...existing deps...
  testImplementation(projects.spockkCore)
}
```

- [ ] **Step 2: Convert SpockkUnreachableCodeSuppressorTest to a Spockk spec**

Create `SpockkUnreachableCodeSuppressorSpockkSpec.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.psi.PsiElement
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import spockk.lang.Specification

class SpockkUnreachableCodeSuppressorSpockkSpec : Specification() {

  lateinit var myFixture: CodeInsightTestFixture
  lateinit var suppressor: SpockkUnusedExpressionInspectionSuppressor

  def setup() {
    val fixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR)
    val projectFixture = fixtureBuilder.fixture
    myFixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createCodeInsightTestFixture(projectFixture)
    myFixture.setTestDataPath("./src/test/resources/SpockkUnreachableCodeSuppressorTest/")
    myFixture.setUp()

    suppressor = SpockkUnusedExpressionInspectionSuppressor()
    myFixture.configureByFile("/SpockkUnreachableCodeSuppressorTest.kt")
  }

  def cleanup() {
    myFixture.tearDown()
  }

  def "suppress unreachable code warnings for where block statements"() {
    expect:
    isSuppressedFor("val11")
    isSuppressedFor("val21")
  }

  def "suppress unreachable code warnings for cleanup block statements"() {
    expect:
    isSuppressedFor("println")
  }

  def "does not suppress unreachable code outside special blocks"() {
    expect:
    !isSuppressedFor("regularStatement")
  }

  private fun isSuppressedFor(elementText: String): Boolean {
    val document = com.intellij.psi.PsiDocumentManager.getInstance(myFixture.project).getDocument(myFixture.file)
    val index = document!!.text.indexOf(elementText)
    val element = com.intellij.psi.util.PsiTreeUtil.getParentOfType<PsiElement>(
      myFixture.file.findElementAt(index), PsiElement::class.java
    )
    return suppressor.isSuppressedFor(element!!, "KotlinUnreachableCode")
  }
}
```

- [ ] **Step 3: Run the Spockk spec**

Run: `./gradlew :spockk-intellij-plugin:test --tests "*SpockkUnreachableCodeSuppressorSpockkSpec*"`
Expected: Either all tests PASS, or see error indicating incompatibility

- [ ] **Step 4: Document findings**

If Spockk works: update remaining 3 test files and commit.
If Spockk fails: note the blocker in `_docs/specs/2026-07-03-intellij-plugin-test-migration-design.md` under Phase 2.
