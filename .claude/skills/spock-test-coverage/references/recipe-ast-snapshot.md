# Recipe: ast-snapshot

**Detection:** `@Snapshot` + `SpockSnapshotter` + `transpile*`.
**Target:** a source/transformed pair under `spockk-specs/src/test/resources/samples/compilation/{source,transformed}`,
via `assertTransformation()`.

Upstream snapshots the AST/source Groovy's own `ConditionRewriter` produces for a given input, comparing against a
checked-in snapshot file. Spockk's equivalent compares two full Kotlin source files (source and its expected
IR-transformed shape) by compiling both and diffing their IR dumps, via `assertTransformation()` in
`BaseCompilationTest`.

## Before (upstream, `org.spockframework.smoke.ast.BlocksAst`)

```groovy
class BlocksAst extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  def "all observable blocks with empty labels"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    given: ''
    expect: ''
    when: ''
    then: ''
    cleanup: ''
    where: ''
    combined: ''
    filter: ''
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
```

## After (the same pattern, from `SpockkAnnotationCompilationTest` and its resource pair)

```kotlin
// spockk-specs/src/test/kotlin/.../compilation/SpockkAnnotationCompilationTest.kt
class SpockkAnnotationCompilationTest : BaseCompilationTest() {

  @MigratedFrom("org.spockframework.smoke.ast.BlocksAst#all observable blocks with empty labels")
  fun `annotates classes with feature methods with @SpecMetadata`() {
    expect
    assertTransformation(sampleFromResource("SingleFeatureSpec"))
  }
}
```

```kotlin
// spockk-specs/src/test/resources/samples/compilation/source/SingleFeatureSpec.kt
class SingleFeatureSpec : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
```

```kotlin
// spockk-specs/src/test/resources/samples/compilation/transformed/SingleFeatureSpec.kt
@org.spockframework.runtime.model.SpecMetadata(filename = "SingleFeatureSpec.kt", line = 1)
class SingleFeatureSpec : spock.lang.Specification() {
  @org.spockframework.runtime.model.FeatureMetadata(/* ... */)
  fun `$spock_feature_0_0`() {
    /* ... the full expected IR shape ... */
  }
}
```

## Porting notes

- Write the `source/<Name>.kt` file as plain, untransformed Kotlin using Spockk's block-label syntax; write
  `transformed/<Name>.kt` as the exact IR the compiler plugin should produce for it. `assertTransformation()`
  compiles both and asserts the IR dumps match.
- **The `@MigratedFrom` annotation goes on the test method in `src/test/kotlin` that calls
  `assertTransformation()`, never inside the resource files.** The `source`/`transformed` `.kt` files under
  `src/test/resources` are not compiled as part of the annotated test class and cannot carry annotations; the
  scanner only looks at real Kotlin sources under `src/test` and `src/testFixtures`.
- Reuse an existing sample pair (`sampleFromResource("SingleFeatureSpec")`) when the upstream feature is really
  checking the same transformation shape under a different name; only add a new source/transformed pair when the
  upstream feature exercises a genuinely different transformation.
