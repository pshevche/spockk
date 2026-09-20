# Recipe: engine-runtime

**Detection:** extends `EmbeddedSpecification`, uses `runner.run*`.
**Target:** the test class's package and name mirror upstream exactly (see `SKILL.md` Step 3), via
`EngineTestKitUtils.execute()` against a fixture spec added to `src/testFixtures/`.

This is the recipe where Groovy and Kotlin diverge the most mechanically. Upstream compiles a spec body given as a
*string* at runtime (`runner.runFeatureBody(...)`, `runner.runSpecBody(...)`) and asserts on the result. Kotlin has
no equivalent to dynamic Groovy compilation available here, so the string-embedded spec becomes a real, ahead-of-
time-compiled fixture class under `src/testFixtures/kotlin/.../fixtures/runtime/samples/...`, executed through the
JUnit Platform TestKit (`EngineTestKitUtils.execute()`, a thin wrapper already in `testFixtures`) and asserted on
via its `Events` API.

## Before (upstream, `org.spockframework.runtime.RunContextEmbeddedSpec`)

```groovy
class RunContextEmbeddedSpec extends EmbeddedSpecification {
  def "initial run context is named 'default'"() {
    given:
    runner.addClassImport(RunContext)
    expect:
    RunContext.get().name == "default"

    when:
    runner.runFeatureBody """
        expect:
        RunContext.get().name == "default/EmbeddedSpecRunner"
    """

    then:
    RunContext.get().name == "default"
  }
}
```

## After (the same transformation pattern, shown via the existing `SpockkTestEngineSmokeTest`)

The embedded spec body becomes a fixture class, written once and reused across tests that need it:

```kotlin
// spockk-specs/src/testFixtures/kotlin/.../fixtures/runtime/samples/smoke/SimpleSpec.kt
class SimpleSpec : Specification() {
  fun `successful feature`() {
    expect
    assert(true)
  }

  fun `failing feature`() {
    expect
    assert(false)
  }
}
```

The test then executes it through the real JUnit Platform engine and asserts on the emitted events, not on strings:

```kotlin
// spockk-specs/src/test/kotlin/.../runtime/SpockkTestEngineSmokeTest.kt
class SpockkTestEngineSmokeTest : Specification() {
  fun `discovers test class by class name`() {
    `when`
    val events = execute(selectClass(SimpleSpec::class.java))

    then
    events.assertStatistics { it.started(2).succeeded(1).failed(1) }
  }
}
```

`EngineTestKitUtils.execute(selector)` (in `testFixtures`) wraps
`EngineTestKit.engine("spock").selectors(selector).execute().testEvents().debug()`.

## What to actually port

Do not try to preserve the "spec body as a string" shape; that is a Groovy implementation detail of how upstream
tests the engine, not the behavior under test. Instead:

1. Identify what the upstream feature is really asserting (discovery, ordering, listener callbacks, run context
   scoping, etc).
2. Write or reuse a minimal fixture class in `src/testFixtures/.../samples/` that exercises the same shape.
3. Assert on `Events`/`TestExecutionResult` from `EngineTestKitUtils.execute()`, matching what the upstream
   `result.*` assertions were checking (event counts, ordering, exceptions, displayNames).

Put `@MigratedFrom` on the test method that runs the assertion (in `src/test/`), never on the fixture class itself:
the fixture is reusable scaffolding, not the thing being traced.
