---
layout: page
title: Examples
---

<div class="page-container vp-doc">

# Examples

A few short specs showing what Spockk actually adds on top of plain Kotlin tests.

<CodeCarousel>

<CarouselSlide title="Specifications as documentation" description="Block descriptions turn a feature into a story anyone can read, engineer or not, and Spock keeps them around for its reports.">

<CodeWindow title="BridgeOperationsSpec.kt">

```kotlin
class BridgeOperationsSpec : Specification() {
  fun `raising shields under attack`() {
    given("the Enterprise cruising at impulse")
    val enterprise = Enterprise()

    `when`("a Klingon bird-of-prey decloaks nearby")
    enterprise.raiseShields()

    then("shields are at full strength")
    assert(enterprise.shieldStrength == 100)
  }
}
```

</CodeWindow>

</CarouselSlide>

<CarouselSlide title="Natural assertions" description="A bare boolean is enough: no assert() required, and a failure still renders Spock's own value diagram.">

<CodeWindow title="ShieldsSpec.kt">

```kotlin
fun `shields hold against a single torpedo hit`() {
  given
  val enterprise = Enterprise(shieldStrength = 100)

  `when`
  enterprise.absorbHit(30)

  then
  enterprise.shieldStrength == 70
}
```

</CodeWindow>

<CodeWindow title="test output">

```text
Condition not satisfied:

enterprise.shieldStrength == 70
|          |              |
40         40             false
```

</CodeWindow>

</CarouselSlide>

<CarouselSlide title="Data-driven features" description="A where table runs the feature once per row, iterations and all, exactly like Spock does in Groovy.">

<CodeWindow title="WarpFactorSpec.kt">

```kotlin
fun `warp factor #factor is within safety limits`(factor: Int, safe: Boolean) {
  expect
  enterprise.isWarpSafe(factor) == safe

  where
  factor ; safe
  1      ; true
  6      ; true
  10     ; false
}
```

</CodeWindow>

</CarouselSlide>

<CarouselSlide title="Test lifecycle fixtures" description="setup()/cleanup() need no annotations: Spockk recognizes them by name, same as Spock does in Groovy.">

<CodeWindow title="BridgeSystemsSpec.kt">

```kotlin
class BridgeSystemsSpec : Specification() {
  fun setup() {
    enterprise.resetDiagnostics()
  }

  fun `bridge and engineering report ready`() {
    `when`
    enterprise.runDiagnostics("bridge", "engineering")

    then
    enterprise.diagnostics.size == 2

    cleanup
    enterprise.resetDiagnostics()
  }
}
```

</CodeWindow>

</CarouselSlide>

<CarouselSlide title="Experimental extension support" description="Spock's own extensions, like @FailsWith for documenting a known bug, work as experimental support in Spockk too.">

<CodeWindow title="WarpCoreSpec.kt">

```kotlin
class WarpCoreSpec : Specification() {
  @FailsWith(WarpCoreBreachException::class)
  fun `exceeding maximum warp causes a core breach`() {
    expect
    enterprise.engageWarp(10)
  }
}
```

</CodeWindow>

</CarouselSlide>

</CodeCarousel>

<div class="page-nav">
  <a class="page-nav-next" href="/limitations">
    <span class="page-nav-label">Next</span>
    <span class="page-nav-title">Limitations →</span>
  </a>
</div>

</div>
