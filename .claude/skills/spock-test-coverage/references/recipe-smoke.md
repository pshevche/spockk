# Recipe: smoke

**Detection:** plain `Specification`, no embedded compiler, no snapshotter, no `ConditionRenderingSpec` base.
**Target:** `spockk-specs/src/test/kotlin/.../smoke/`, a real Spockk spec run by the Spock engine.

This is the simplest recipe: the upstream feature exercises Spockk's own runtime behavior directly, so the port is
a normal Kotlin test written in Spockk's own block-label syntax. No embedded runner, no dynamic compilation.

## Before (upstream, `org.spockframework.smoke.condition.SatisfiedConditions`)

```groovy
class SatisfiedConditions extends Specification {
  def "boolean"() {
    expect: true
  }

  def "number"() {
    expect: 1
  }
  // ...
}
```

## After (`spockk-specs/.../smoke/condition/TopLevelImplicitConditionSmokeTest.kt`)

```kotlin
class TopLevelImplicitConditionSmokeTest : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.SatisfiedConditions#boolean")
  fun `boolean literal condition`() {
    expect
    true
  }
}
```

Notes on this specific port:

- Upstream's `"number"` feature (`expect: 1`) and `"object"`/`"collection"` features rely on Groovy truth (a
  non-zero number, a non-empty collection, any non-null object are all truthy). Kotlin's `expect` block requires an
  actual `Boolean`, so these are not straightforward ports: either write the Kotlin-native equivalent (an explicit
  boolean expression) as a *new* feature, or record the Groovy-truth-specific behavior as `not-applicable` in
  `exclusions.toml`. Do not force `1` or `[1]` through as if it type-checked; it will not compile.
- Only `"boolean"` ported directly here since it needed no semantic translation. This is normal: a single upstream
  class can split across a real port, a Kotlin-native rewrite, and a not-applicable exclusion, feature by feature.
