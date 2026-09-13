---
layout: page
title: Examples
---

<div class="page-container vp-doc">

# Examples

A few short specs showing what Spockk actually adds on top of plain Kotlin tests.

<CodeCarousel>

<CarouselSlide title="Specifications as documentation" description="Block descriptions turn a feature into a story anyone can read, engineer or not.">

<CodeWindow title="BridgeOperationsSpec.kt">

```kotlin
class BridgeOperationsSpec : Specification() {

  fun `raising shields under attack`() {
    given("the Enterprise cruising at impulse")
    val enterprise = Enterprise()

    `when`("a Klingon bird-of-prey decloaks nearby")
    enterprise.raiseShields()

    then("shields are at full strength")
    enterprise.shieldStrength == 100
  }

}
```

</CodeWindow>

</CarouselSlide>

<CarouselSlide title="Natural assertions" description="A bare condition is enough: no assertion framework required, and a failure renders a rich value diagram.">

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
|          40             false
Enterprise(shieldStrength=40)
```

</CodeWindow>

</CarouselSlide>

<CarouselSlide title="Data-driven features" description="A data table declares feature iterations, exactly like Spock does in Groovy.">

<CodeWindow title="WarpFactorSpec.kt">

```kotlin
fun `warp factor #factor is within safety limits`(
  factor: Int,
  safe: Boolean
) {
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

<CarouselSlide title="Test lifecycle fixtures" description="No annotations needed: Spockk recognizes them by name, same as Spock does in Groovy.">

<CodeWindow title="BridgeSystemsSpec.kt">

```kotlin
class BridgeSystemsSpec : Specification() {

  fun setupSpec() {
    enterprise.initializeDiagnostics()
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

<CarouselSlide title="Interaction-based testing" description="Verify a collaborator was called, and stub its response - a preview of Spock's mocking syntax, natively in Kotlin.">

<CodeWindow title="BridgeOperationsSpec.kt">

```kotlin
fun `raising shields draws power from the deflector grid`() {
  given
  val powerGrid = Mock(PowerGrid::class.java)
  val enterprise = Enterprise(powerGrid)

  `when`
  enterprise.raiseShields()

  then
  1 * powerGrid.divertPower(200)
}
```

</CodeWindow>

</CarouselSlide>

<CarouselSlide title="Experimental extension support" description="Spock's own extensions work in Spockk too.">

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
  <a class="page-nav-next" href="limitations">
    <span class="page-nav-label">Next</span>
    <span class="page-nav-title">Limitations →</span>
  </a>
</div>

</div>
