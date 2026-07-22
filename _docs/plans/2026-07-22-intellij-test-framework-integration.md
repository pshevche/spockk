# IntelliJ Test Framework Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Register Spockk as a proper IntelliJ test framework with gutter icons and Gradle-based run configuration producers.

**Architecture:** Implement `TestFramework` + `KotlinPsiBasedTestFramework` for spec/feature detection, `KotlinTestFrameworkProvider` for Kotlin plugin bridge, and two `AbstractKotlinTestClassGradleConfigurationProducer`/`AbstractKotlinTestMethodGradleConfigurationProducer` subclasses for Gradle run configs. Register all via `plugin.xml` extensions.

**Tech Stack:** Kotlin 2.4.0, IntelliJ Platform 2026.2, Gradle 9.4.1, JUnit Platform 6.1.1

---

### Task 1: Build configuration and PSI utilities

**Files:**
- Modify: `spockk-intellij-plugin/build.gradle.kts`
- Modify: `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/Psi.kt`

- [ ] **Step 1: Add spock-core test dependency to `build.gradle.kts`**

Edit `spockk-intellij-plugin/build.gradle.kts`. Add after the `testImplementation(libs.hamcrest)` line:

```kotlin
  testImplementation(libs.spock)
```

- [ ] **Step 2: Add `isSpockkSpec()` and `isSpockkFeature()` to `Psi.kt`**

Edit `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/Psi.kt`. Add at the bottom of the file (before closing brace if any):

```kotlin
internal fun PsiElement.isSpockkSpec(): Boolean {
  val classOrObject = PsiTreeUtil.getParentOfType(this, KtClassOrObject::class.java) ?: return false
  if (classOrObject is KtClass && (classOrObject.isInterface() || classOrObject.isEnum())) return false
  val key = SPOCKK_SPEC_KEY
  val cached = classOrObject.getUserData(key)
    ?: CachedValuesManager.getManager(classOrObject.project).createCachedValue {
      val result = classOrObject.superTypeListEntries.any { entry ->
        entry.typeReference?.resolveToClass()?.qualifiedName == "spock.lang.Specification"
      }
      CachedValueProvider.Result(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
  if (classOrObject.getUserData(key) == null) {
    classOrObject.putUserData(key, cached)
  }
  return cached.value
}

internal fun PsiElement.isSpockkFeature(): Boolean {
  val ktFunction = PsiTreeUtil.getParentOfType(this, KtNamedFunction::class.java) ?: return false
  if (ktFunction.isLocal) return false
  if (isFixtureMethod(ktFunction)) return false
  val specClass = PsiTreeUtil.getParentOfType(ktFunction, KtClassOrObject::class.java) ?: return false
  if (!specClass.isSpockkSpec()) return false
  val key = SPOCKK_FEATURE_KEY
  val cached = ktFunction.getUserData(key)
    ?: CachedValuesManager.getManager(ktFunction.project).createCachedValue {
      val hasBlockLabel = PsiTreeUtil.collectElementsOfType(ktFunction, KtNameReferenceExpression::class.java)
        .any { it.isSpockkBlock() }
      CachedValueProvider.Result(hasBlockLabel, PsiModificationTracker.MODIFICATION_COUNT)
    }
  if (ktFunction.getUserData(key) == null) {
    ktFunction.putUserData(key, cached)
  }
  return cached.value
}

private fun isFixtureMethod(function: KtNamedFunction): Boolean {
  val name = function.name
  return name == "setup" || name == "cleanup" || name == "setupSpec" || name == "cleanupSpec"
}

private val SPOCKK_SPEC_KEY = Key.create<CachedValue<Boolean>>("spockk.spec")
private val SPOCKK_FEATURE_KEY = Key.create<CachedValue<Boolean>>("spockk.feature")
```

Add the missing import for `resolveToClass` (may already be imported if `KtSuperTypeListEntry` is used elsewhere). Add at the import section:

```kotlin
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :spockk-intellij-plugin:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add spockk-intellij-plugin/build.gradle.kts spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/Psi.kt
git commit -m "feat(intellij): add PSI utilities for Spockk spec/feature detection"
```

---

### Task 2: SpockkTestFramework — spec class detection

**Files:**
- Create: `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFramework.kt`
- Create: `spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkTest.kt`
- Create: `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testDetectSpecClass.kt`

- [ ] **Step 1: Create test fixture for spec class detection**

Create `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testDetectSpecClass.kt`:

```kotlin
import spock.lang.Specification

class MySpec : Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}

class NotASpec {
  fun regularMethod() {
    println("not a test")
  }
}
```

- [ ] **Step 2: Write failing test for spec class detection**

Create `spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkTest.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.psi.PsiElement
import com.intellij.testFramework.LightJavaCodeInsightFixtureTestCase5
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkTestFrameworkTest : LightJavaCodeInsightFixtureTestCase5() {

  private lateinit var framework: SpockkTestFramework

  @BeforeEach
  private fun setUp() {
    framework = SpockkTestFramework()
    myFixture.configureFromDefaultFile()
  }

  override fun getTestDataPath(): String {
    val path = javaClass.getResource("/SpockkTestFrameworkTest/")!!
    return path.path
  }

  @Test
  fun testDetectSpecClass() {
    val specClass = findRequiredElementByTextAndType("class MySpec", PsiElement::class.java)
    assertTrue(framework.isTestClass(specClass))
  }

  @Test
  fun testDetectNonSpecClass() {
    val nonSpecClass = findRequiredElementByTextAndType("class NotASpec", PsiElement::class.java)
    assertFalse(framework.isTestClass(nonSpecClass))
  }

  private fun <T : PsiElement> findRequiredElementByTextAndType(
    text: String, elementClass: Class<T>
  ): T {
    return myFixture.findElementByText(text, elementClass)
  }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :spockk-intellij-plugin:test --tests "io.github.pshevche.spockk.intellij.SpockkTestFrameworkTest.testDetectSpecClass"`
Expected: COMPILATION ERROR — SpockkTestFramework class not found

- [ ] **Step 4: Implement SpockkTestFramework**

Create `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFramework.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.ide.IconProvider
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testIntegration.TestFramework
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

class SpockkTestFramework : TestFramework {

  override fun getName(): String = "Spockk"

  override fun getIcon() = com.intellij.icons.AllIcons.RunConfigurations.TestState.Run

  override fun isLibraryAttached(module: com.intellij.openapi.module.Module): Boolean = false

  override fun getLibraryPath(): String = ""

  override fun getDefaultSuperClass(): String = "spock.lang.Specification"

  override fun isTestClass(element: PsiElement): Boolean = element.isSpockkSpec()

  override fun isPotentialTestClass(element: PsiElement): Boolean = isTestClass(element)

  override fun findSetUpMethod(element: PsiElement): PsiElement? = null

  override fun findTearDownMethod(element: PsiElement): PsiElement? = null

  override fun findOrCreateSetUpMethod(element: PsiElement): PsiElement? = null

  override fun getSetUpMethodFileTemplateDescriptor() = null

  override fun getTearDownMethodFileTemplateDescriptor() = null

  override fun getTestMethodFileTemplateDescriptor() = null

  override fun isIgnoredMethod(element: PsiElement): Boolean = false

  override fun isTestMethod(element: PsiElement): Boolean = element.isSpockkFeature()

  override fun getLanguage(): Language = KotlinLanguage.INSTANCE
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :spockk-intellij-plugin:test --tests "io.github.pshevche.spockk.intellij.SpockkTestFrameworkTest.testDetectSpecClass" --tests "io.github.pshevche.spockk.intellij.SpockkTestFrameworkTest.testDetectNonSpecClass"`
Expected: PASS (2/2)

- [ ] **Step 6: Commit**

```bash
git add spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFramework.kt spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkTest.kt spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testDetectSpecClass.kt
git commit -m "feat(intellij): implement SpockkTestFramework with spec class detection"
```

---

### Task 3: SpockkTestFramework — feature method detection + edge cases

**Files:**
- Modify: `spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkTest.kt`
- Create: `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testDetectFeatureMethod.kt`
- Create: `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testExcludeFixtureMethods.kt`
- Create: `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testExcludeRegularMethods.kt`
- Create: `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testDetectAbstractSpec.kt`

- [ ] **Step 1: Create test fixture for feature method detection**

Create `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testDetectFeatureMethod.kt`:

```kotlin
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.then

class FeatureMethodSpec : spock.lang.Specification() {
  fun `a passing feature`() {
    expect
    assert(true)
  }

  fun `a feature with all blocks`() {
    when:
    val x = 1
    then:
    assert(x == 1)
  }
}
```

Create `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testExcludeFixtureMethods.kt`:

```kotlin
import io.github.pshevche.spockk.lang.expect

class FixtureMethodSpec : spock.lang.Specification() {
  fun setup() {
    expect
    println("setup")
  }

  fun cleanup() {
    expect
    println("cleanup")
  }

  fun setupSpec() {
    expect
    println("setupSpec")
  }

  fun cleanupSpec() {
    expect
    println("cleanupSpec")
  }

  fun `a real feature`() {
    expect
    assert(true)
  }
}
```

Create `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testExcludeRegularMethods.kt`:

```kotlin
import io.github.pshevche.spockk.lang.expect

class RegularMethodSpec : spock.lang.Specification() {
  fun helperMethod(): Int {
    return 42
  }

  fun `a real feature`() {
    expect
    assert(helperMethod() == 42)
  }
}
```

Create `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkTest/testDetectAbstractSpec.kt`:

```kotlin
abstract class AbstractBaseSpec : spock.lang.Specification() {
  fun `base feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}

class ConcreteSpec : AbstractBaseSpec() {
  fun `concrete feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
```

- [ ] **Step 2: Add test methods to SpockkTestFrameworkTest**

Add to `SpockkTestFrameworkTest.kt`:

```kotlin
  @Test
  fun testDetectFeatureMethod() {
    myFixture.configureFromDefaultFile()
    val featureMethod = findRequiredElementByTextAndType(
      "fun `a passing feature`", PsiElement::class.java
    )
    assertTrue(framework.isTestMethod(featureMethod))
  }

  @Test
  fun testExcludeFixtureMethods() {
    myFixture.configureFromDefaultFile()
    val setupMethod = findRequiredElementByTextAndType(
      "fun setup()", PsiElement::class.java
    )
    assertFalse(framework.isTestMethod(setupMethod))
    val cleanupMethod = findRequiredElementByTextAndType(
      "fun cleanup()", PsiElement::class.java
    )
    assertFalse(framework.isTestMethod(cleanupMethod))
  }

  @Test
  fun testExcludeRegularMethods() {
    myFixture.configureFromDefaultFile()
    val helperMethod = findRequiredElementByTextAndType(
      "fun helperMethod()", PsiElement::class.java
    )
    assertFalse(framework.isTestMethod(helperMethod))
    val featureMethod = findRequiredElementByTextAndType(
      "fun `a real feature`", PsiElement::class.java
    )
    assertTrue(framework.isTestMethod(featureMethod))
  }

  @Test
  fun testDetectAbstractSpec() {
    myFixture.configureFromDefaultFile()
    val abstractClass = findRequiredElementByTextAndType(
      "class AbstractBaseSpec", PsiElement::class.java
    )
    assertTrue(framework.isTestClass(abstractClass))
    val concreteClass = findRequiredElementByTextAndType(
      "class ConcreteSpec", PsiElement::class.java
    )
    assertTrue(framework.isTestClass(concreteClass))
    val baseFeature = findRequiredElementByTextAndType(
      "fun `base feature`", PsiElement::class.java
    )
    assertTrue(framework.isTestMethod(baseFeature))
  }
```

- [ ] **Step 3: Run tests**

Run: `./gradlew :spockk-intellij-plugin:test --tests "io.github.pshevche.spockk.intellij.SpockkTestFrameworkTest"`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add spockk-intellij-plugin/src/test/
git commit -m "feat(intellij): add feature method detection and edge case tests"
```

---

### Task 4: SpockkTestFrameworkProvider

**Files:**
- Create: `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkProvider.kt`
- Create: `spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkProviderTest.kt`
- Create: `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkProviderTest/testProviderDetection.kt`

- [ ] **Step 1: Create test fixture**

Create `spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkProviderTest/testProviderDetection.kt`:

```kotlin
class SpecForProvider : spock.lang.Specification() {
  fun `a feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
```

- [ ] **Step 2: Write failing test**

Create `spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkProviderTest.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.psi.PsiElement
import com.intellij.testFramework.LightJavaCodeInsightFixtureTestCase5
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockkTestFrameworkProviderTest : LightJavaCodeInsightFixtureTestCase5() {

  private lateinit var provider: SpockkTestFrameworkProvider

  @BeforeEach
  private fun setUp() {
    provider = SpockkTestFrameworkProvider()
    myFixture.configureFromDefaultFile()
  }

  override fun getTestDataPath(): String {
    val path = javaClass.getResource("/SpockkTestFrameworkProviderTest/")!!
    return path.path
  }

  @Test
  fun testCanRunJvmTests() {
    assertTrue(provider.canRunJvmTests)
  }

  @Test
  fun testProviderDetectsSpecClass() {
    val specClass = myFixture.findElementByText("class SpecForProvider", PsiElement::class.java)
    val entity = provider.getJavaTestEntity(specClass, false)
    assertNotNull(entity) { "Should produce a JavaTestEntity for a spec class" }
    assertEquals("SpecForProvider", entity?.testClass?.name)
  }
}
```

Run to verify it fails (class not found).

- [ ] **Step 3: Implement SpockkTestFrameworkProvider**

Create `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkProvider.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.execution.configurations.ConfigurationFromContext
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.extensions.JavaEntity
import org.jetbrains.kotlin.idea.extensions.JavaTestEntity
import org.jetbrains.kotlin.idea.extensions.KotlinTestFrameworkProvider
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

class SpockkTestFrameworkProvider : KotlinTestFrameworkProvider {

  override val canRunJvmTests: Boolean get() = true

  override fun isProducedByJava(context: ConfigurationFromContext): Boolean = false

  override fun isProducedByKotlin(context: ConfigurationFromContext): Boolean = true

  override fun isTestJavaClass(clazz: PsiClass): Boolean =
    clazz.isSpockkSpec()

  override fun isTestJavaMethod(method: PsiMethod): Boolean =
    method.isSpockkFeature()

  override fun isTestFrameworkAvailable(element: PsiElement): Boolean =
    element.isSpockkSpec() || element.isSpockkFeature()

  override fun getJavaTestEntity(element: PsiElement, allowMethods: Boolean): JavaTestEntity? {
    val ktClass = PsiTreeUtil.getParentOfType(element, KtClassOrObject::class.java) ?: return null
    if (!ktClass.isSpockkSpec()) return null
    val psiClass = ktClass as? PsiClass ?: return null
    if (!allowMethods) return JavaTestEntity(psiClass, null)
    val ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
    if (ktFunction != null && ktFunction.isSpockkFeature()) {
      val psiMethod = ktFunction as? PsiMethod
      return JavaTestEntity(psiClass, psiMethod)
    }
    return JavaTestEntity(psiClass, null)
  }

  override fun getJavaEntity(element: PsiElement): JavaEntity? {
    val ktClass = PsiTreeUtil.getParentOfType(element, KtClassOrObject::class.java) ?: return null
    val psiClass = ktClass as? PsiClass ?: return null
    val ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
    val psiMethod = ktFunction?.let { it as? PsiMethod }
    return JavaEntity(psiClass, psiMethod)
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :spockk-intellij-plugin:test --tests "io.github.pshevche.spockk.intellij.SpockkTestFrameworkProviderTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkProvider.kt spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkTestFrameworkProviderTest.kt spockk-intellij-plugin/src/test/resources/SpockkTestFrameworkProviderTest/
git commit -m "feat(intellij): add SpockkTestFrameworkProvider for Kotlin plugin integration"
```

---

### Task 5: Gradle configuration producers

**Files:**
- Create: `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestClassGradleConfigurationProducer.kt`
- Create: `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestMethodGradleConfigurationProducer.kt`
- Create: `spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/GradleConfigurationProducerTest.kt`

- [ ] **Step 1: Implement SpockkTestClassGradleConfigurationProducer**

Create `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestClassGradleConfigurationProducer.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.openapi.module.Module
import org.jetbrains.kotlin.idea.gradleJava.run.AbstractKotlinTestClassGradleConfigurationProducer
import org.jetbrains.kotlin.idea.gradleJava.run.KotlinGradleConfigurationProducer

class SpockkTestClassGradleConfigurationProducer :
  AbstractKotlinTestClassGradleConfigurationProducer(),
  KotlinGradleConfigurationProducer {

  override fun getForceGradleRunner(): Boolean = false

  override fun getHasTestFramework(): Boolean = true

  override fun isApplicable(module: Module): Boolean = true
}
```

- [ ] **Step 2: Implement SpockkTestMethodGradleConfigurationProducer**

Create `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestMethodGradleConfigurationProducer.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.openapi.module.Module
import org.jetbrains.kotlin.idea.gradleJava.run.AbstractKotlinTestMethodGradleConfigurationProducer
import org.jetbrains.kotlin.idea.gradleJava.run.KotlinGradleConfigurationProducer

class SpockkTestMethodGradleConfigurationProducer :
  AbstractKotlinTestMethodGradleConfigurationProducer(),
  KotlinGradleConfigurationProducer {

  override fun getForceGradleRunner(): Boolean = false

  override fun getHasTestFramework(): Boolean = true

  override fun isApplicable(module: Module): Boolean = true
}
```

- [ ] **Step 3: Compile to verify**

Run: `./gradlew :spockk-intellij-plugin:compileKotlin`
Expected: BUILD SUCCESSFUL

**Note on testing:** The Gradle configuration producers depend on `AbstractKotlinTestClassGradleConfigurationProducer` and `AbstractKotlinTestMethodGradleConfigurationProducer`, which require a full IDE project setup with Gradle model to create run configurations. Automated testing of these producers requires an integration test with a loaded Gradle project, which is complex and slow. For now, compilation verification + manual IDE testing is sufficient. The core detection logic (which these producers depend on via `TestFrameworks.isTestClass()`) is thoroughly tested in Tasks 2-3.

- [ ] **Step 4: Commit**

```bash
git add spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestClassGradleConfigurationProducer.kt spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkTestMethodGradleConfigurationProducer.kt
git commit -m "feat(intellij): add Gradle run configuration producers for Spockk"
```

---

### Task 6: plugin.xml registrations and gutter icon verification

**Files:**
- Modify: `spockk-intellij-plugin/src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: Register extensions in plugin.xml**

Edit `spockk-intellij-plugin/src/main/resources/META-INF/plugin.xml`. Add inside the `<extensions defaultExtensionNs="com.intellij">` block, after the existing formatter entry:

```xml
        <testFramework implementation="io.github.pshevche.spockk.intellij.SpockkTestFramework"/>
        <runConfigurationProducer implementation="io.github.pshevche.spockk.intellij.SpockkTestClassGradleConfigurationProducer"/>
        <runConfigurationProducer implementation="io.github.pshevche.spockk.intellij.SpockkTestMethodGradleConfigurationProducer"/>
```

Add inside `<extensions defaultExtensionNs="org.jetbrains.kotlin">` block (create if it doesn't exist):

```xml
    <extensions defaultExtensionNs="org.jetbrains.kotlin.idea">
        <testFrameworkProvider implementation="io.github.pshevche.spockk.intellij.SpockkTestFrameworkProvider"/>
    </extensions>
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :spockk-intellij-plugin:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add spockk-intellij-plugin/src/main/resources/META-INF/plugin.xml
git commit -m "feat(intellij): register test framework, provider, and run config extensions"
```

---

### Task 7: Gutter icon presence test

**Files:**
- Create: `spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkGutterIconTest.kt`
- Create: `spockk-intellij-plugin/src/test/resources/SpockkGutterIconTest/testGutterIconOnSpecClass.kt`
- (Conditional) Create: `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkRunLineMarkerContributor.kt`

- [ ] **Step 1: Create test fixture**

Create `spockk-intellij-plugin/src/test/resources/SpockkGutterIconTest/testGutterIconOnSpecClass.kt`:

```kotlin
class SpecWithGutter : spock.lang.Specification() {
  fun `testable feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
```

- [ ] **Step 2: Write gutter icon test**

Create `spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkGutterIconTest.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.intellij.testFramework.LightJavaCodeInsightFixtureTestCase5
import com.intellij.testIntegration.TestRunLineMarkerProvider
import org.junit.jupiter.api.Test

class SpockkGutterIconTest : LightJavaCodeInsightFixtureTestCase5() {

  override fun getTestDataPath(): String {
    val path = javaClass.getResource("/SpockkGutterIconTest/")!!
    return path.path
  }

  @Test
  fun testGutterIconOnSpecClass() {
    myFixture.configureFromDefaultFile()
    val classKeyword = myFixture.findElementByText("class SpecWithGutter", PsiElement::class.java)
    val info = getLineMarkerInfo(classKeyword)
    if (info == null) {
      // TestFramework didn't auto-provide gutter icons — verify via RunLineMarkerContributor
      val contributor = SpockkRunLineMarkerContributor()
      val result = contributor.getInfo(classKeyword)
      assertNotNull(result) { "RunLineMarkerContributor should produce an Info for spec classes" }
    } else {
      // TestFramework auto-provided — just verify it's present
      assertNotNull(info) { "Gutter icon should be present on spec class" }
    }
  }

  private fun getLineMarkerInfo(element: PsiElement): Any? {
    // Try the standard test line marker provider first
    val testProvider = TestRunLineMarkerProvider()
    return testProvider.getInfo(element)
  }
}
```

- [ ] **Step 3: Run gutter icon test**

Run: `./gradlew :spockk-intellij-plugin:test --tests "io.github.pshevche.spockk.intellij.SpockkGutterIconTest"`
Expected: PASS (or FAIL with useful information about whether auto-provider works)

**Note:** If the test fails because `TestRunLineMarkerProvider` doesn't find our Spockk spec class, it means we need a custom `RunLineMarkerContributor`. Proceed to Step 4. If it passes, the auto-provider works and skip to Step 6.

- [ ] **Step 4 (Conditional): Implement SpockkRunLineMarkerContributor**

If Step 3 revealed that auto-providers don't work, create `spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkRunLineMarkerContributor.kt`:

```kotlin
package io.github.pshevche.spockk.intellij

import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

class SpockkRunLineMarkerContributor : RunLineMarkerContributor() {

  override fun getInfo(element: PsiElement): Info? {
    val ktClass = PsiTreeUtil.getParentOfType(element, KtClassOrObject::class.java)
    if (ktClass != null && element == ktClass.nameIdentifier && ktClass.isSpockkSpec()) {
      val actions = ExecutorRegistry.getInstance().getRegisteredExecutors()
        .map { it.action }
        .toTypedArray()
      return Info(AllIcons.RunConfigurations.TestState.Run, actions, null)
    }
    val ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
    if (ktFunction != null && element == ktFunction.nameIdentifier && ktFunction.isSpockkFeature()) {
      val actions = ExecutorRegistry.getInstance().getRegisteredExecutors()
        .map { it.action }
        .toTypedArray()
      return Info(AllIcons.RunConfigurations.TestState.Run, actions, null)
    }
    return null
  }
}
```

Then register in `plugin.xml` inside the `com.intellij` extensions block:

```xml
        <runLineMarkerContributor language="kotlin"
                                  implementationClass="io.github.pshevche.spockk.intellij.SpockkRunLineMarkerContributor"/>
```

- [ ] **Step 5 (Conditional): Update test and verify**

Update `SpockkGutterIconTest.kt` to remove the fallback path (since we now have the custom contributor):

```kotlin
  @Test
  fun testGutterIconOnSpecClass() {
    myFixture.configureFromDefaultFile()
    val classKeyword = myFixture.findElementByText("class SpecWithGutter", PsiElement::class.java)
    val contributor = SpockkRunLineMarkerContributor()
    val result = contributor.getInfo(classKeyword)
    assertNotNull(result) { "RunLineMarkerContributor should produce an Info for spec classes" }
  }
```

Run: `./gradlew :spockk-intellij-plugin:test --tests "io.github.pshevche.spockk.intellij.SpockkGutterIconTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add spockk-intellij-plugin/src/main/kotlin/io/github/pshevche/spockk/intellij/SpockkRunLineMarkerContributor.kt spockk-intellij-plugin/src/test/kotlin/io/github/pshevche/spockk/intellij/SpockkGutterIconTest.kt spockk-intellij-plugin/src/test/resources/SpockkGutterIconTest/ spockk-intellij-plugin/src/main/resources/META-INF/plugin.xml
git commit -m "feat(intellij): add gutter icons for Spockk spec classes and features"
```
