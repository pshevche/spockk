# Recipe: condition-rendering

**Detection:** extends `ConditionRenderingSpec`.
**Target:** condition rendering assertions against the shared fixture specs in
`spockk-specs/src/testFixtures/kotlin/.../fixtures/runtime/samples/condition/ConditionRenderingSpecs.kt`, asserted
from `spockk-specs/src/test/kotlin/.../runtime/ConditionRenderingTest.kt`.

Upstream's `ConditionRenderingSpec` base class provides an `isRendered(expectedDiagram, closure)` DSL: run the
closure, catch the condition failure, and compare its rendered diagram against the expected ASCII art inline.
Spockk has no equivalent DSL; instead, the failing condition lives in a small dedicated fixture spec class, and the
test executes it through the engine and compares the resulting failure message directly.

**This is not a divergence in behavior.** Spockk rewrites conditions through Spock's own condition rewriter and
renders failures with the shaded Spock runtime (`SpockRuntime`, `ValueRecorder`, `Condition`,
`ExpressionInfoBuilder`), unmodified. A ported condition should produce the *same* diagram as its upstream
original, character for character. A difference is a bug to report against the compiler plugin, not a Kotlin
formatting quirk to work around. This holds for `spockk-specs`' own assertions too, since the suite dogfoods
Spockk end to end.

## Before (upstream, `org.spockframework.smoke.condition.EqualityComparisonRendering`)

```groovy
class EqualityComparisonRendering extends ConditionRenderingSpec {
  def "values with different representations"() {
    expect:
    isRendered """
x == y
| |  |
1 |  2
  false
    """, {
      def x = 1
      def y = 2
      assert x == y
    }
  }
}
```

## After (the same assertion, from `ConditionRenderingTest` and its fixture)

```kotlin
// spockk-specs/src/testFixtures/kotlin/.../fixtures/runtime/samples/condition/ConditionRenderingSpecs.kt
class IntegerEqualitySpec : Specification() {
  fun `test`() {
    expect
    1 == 2
  }
}
```

```kotlin
// spockk-specs/src/test/kotlin/.../runtime/ConditionRenderingTest.kt
class ConditionRenderingTest : Specification() {

  @MigratedFrom("org.spockframework.smoke.condition.EqualityComparisonRendering#values with different representations")
  fun `integer equality`() {
    expect
    failureMessage(IntegerEqualitySpec::class) ==
      """
      |Condition not satisfied:
      |
      |1 == 2
      |  |
      |  false
      |
      """.trimMargin()
  }
}
```

`failureMessage(specClass)` (defined in `ConditionRenderingTest.kt`) runs the given fixture spec's single feature
through `EngineTestKitUtils.execute()` and extracts the rendered failure message from the resulting
`TestExecutionResult`.

## Porting notes

- Add one small fixture spec class per rendering scenario to `ConditionRenderingSpecs.kt` (named for what it
  exercises, e.g. `IntegerEqualitySpec`), then one assertion method in `ConditionRenderingTest.kt` comparing its
  rendered failure message to the expected diagram, written as a Kotlin raw string with `trimMargin()`.
- Reindent/reformat the expected diagram as needed for `trimMargin()`, but do not alter the diagram's actual
  content: the values, the alignment markers (`|`), and the `false`/`true` line must match what Spock's real
  runtime renders. If they do not match, that is a genuine rendering bug worth reporting, not something to paper
  over by editing the expected string until it passes.
- Do not reach for Kotlin's `power-assert` compiler plugin here or anywhere else in this codebase: it is not part
  of the build, and condition rendering is handled entirely by the shaded Spock runtime.
