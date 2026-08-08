---
layout: page
title: How It Works
---

<div class="page-container vp-doc">

# How Does It Work?

Spockk is a Kotlin compiler plugin, not a runtime library with clever tricks. During compilation, it walks your specification's IR (Kotlin's intermediate representation) and rewrites block labels into the exact metadata Spock's own Groovy AST transform would have generated: `@FeatureMetadata`, `@BlockMetadata`, calls into `SpockRuntime` for block tracking, generated data-provider methods, all of it. By the time the JVM sees your class, it's indistinguishable from a spec Spock compiled from Groovy.

Drag through the carousel below to see what a few common constructs look like before and after.

<TransformCarousel>

<TransformSlide title="Block labels become Spock metadata" description="given/when/then descriptions aren't comments: they're compiled into Spock's own block metadata, so the IDE and Spock's reports show exactly what you wrote.">

**What you write**

```kotlin
class WalletSpec : Specification() {
  fun `topping up a wallet`() {
    given("an empty wallet")
    val wallet = Wallet()

    `when`("$25 is added")
    wallet.topUp(25)

    then("the balance is $25")
    assert(wallet.balance == 25)
  }
}
```

**What Spock runs**

```kotlin
@SpecMetadata(filename = "WalletSpec.kt", line = 1)
class WalletSpec : Specification() {
  @FeatureMetadata(
    name = "topping up a wallet",
    ordinal = 0,
    blocks = [
      BlockMetadata(BlockKind.SETUP, ["an empty wallet"]),
      BlockMetadata(BlockKind.WHEN, ["$25 is added"]),
      BlockMetadata(BlockKind.THEN, ["the balance is $25"])
    ]
  )
  fun `$spock_feature_0_0`() {
    SpockRuntime.callBlockEntered(this, 0)
    val wallet = Wallet()
    SpockRuntime.callBlockExited(this, 0)
    SpockRuntime.callBlockEntered(this, 1)
    wallet.topUp(25)
    SpockRuntime.callBlockExited(this, 1)
    SpockRuntime.callBlockEntered(this, 2)
    assert(wallet.balance == 25)
    SpockRuntime.callBlockExited(this, 2)
  }
}
```

</TransformSlide>

<TransformSlide title="Fixture methods are recognized by name" description="setup()/cleanup()/setupSpec()/cleanupSpec() need no annotations: Spockk recognizes them the same way Spock recognizes them in Groovy, and wires them into the feature lifecycle.">

**What you write**

```kotlin
class WalletSpec : Specification() {
  fun setup() {
    println("preparing a fresh wallet")
  }

  fun `topping up a wallet`() {
    expect
    // ...
  }
}
```

**What Spock runs**

```kotlin
@SpecMetadata(filename = "WalletSpec.kt", line = 1)
class WalletSpec : Specification() {
  private fun setup() {
    println("preparing a fresh wallet")
  }

  @FeatureMetadata(
    name = "topping up a wallet",
    ordinal = 0,
    blocks = [BlockMetadata(BlockKind.EXPECT, [""])]
  )
  fun `$spock_feature_0_0`() {
    SpockRuntime.callBlockEntered(this, 0)
    // ...
    SpockRuntime.callBlockExited(this, 0)
  }
}
```

</TransformSlide>

<TransformSlide title="Data tables become real data providers" description="Every column of a where table is compiled into its own generated data-provider method, exactly like Spock generates for a table defined in Groovy.">

**What you write**

```kotlin
class WalletSpec : Specification() {
  fun `rejects an oversized withdrawal`(balance: Int, withdrawal: Int) {
    expect
    // ...

    where
    balance ; withdrawal
    10      ; 20
    0       ; 1
  }
}
```

**What Spock runs**

```kotlin
@FeatureMetadata(
  name = "rejects an oversized withdrawal",
  parameterNames = ["balance", "withdrawal"],
  blocks = [
    BlockMetadata(BlockKind.EXPECT, [""]),
    BlockMetadata(BlockKind.WHERE, [""])
  ]
)
fun `$spock_feature_0_0`(balance: Int, withdrawal: Int) {
  // ...
}

@DataProviderMetadata(dataVariables = ["balance"])
fun `$spock_feature_0_0prov0`() =
  listOf(10, 0)

@DataProviderMetadata(dataVariables = ["withdrawal"])
fun `$spock_feature_0_0prov1`() =
  listOf(20, 1)

@DataProcessorMetadata(
  dataVariables = ["balance", "withdrawal"]
)
fun `$spock_feature_0_0proc`(
  p0: Any,
  p1: Any
) = arrayOf(p0 as Int, p1 as Int)
```

</TransformSlide>

<TransformSlide title="Bare booleans become Spock-style diagnostics" description="No assert() required: a boolean expression on its own line is an implicit condition, and a failure renders the same value diagram Spock builds for Groovy.">

**What you write**

```kotlin
class WalletSpec : Specification() {
  fun `topping up a wallet`() {
    given
    val wallet = Wallet()

    `when`
    wallet.topUp(25)

    then
    wallet.balance == 25
  }
}
```

**What Spock runs**

```kotlin
fun `$spock_feature_0_0`() {
  val recorder = ValueRecorder()
  val collector = ErrorRethrower.INSTANCE
  // ...given and when blocks...
  SpockRuntime.callBlockEntered(this, 2)
  try {
    SpockRuntime.verifyCondition(
      collector, recorder.reset(),
      "wallet.balance == 25", 9, 5, null,
      recorder.record(0, wallet.balance == 25) as Boolean
    )
  } catch (t: Throwable) {
    SpockRuntime.conditionFailedWithException(
      collector, recorder,
      "wallet.balance == 25", 9, 5, null, t
    )
  }
  SpockRuntime.callBlockExited(this, 2)
}
```

</TransformSlide>

</TransformCarousel>

Because this all happens at compile time via a Kotlin IR compiler plugin, there's no reflection, no runtime proxies, and no separate spec format to learn: your specs show up correctly in IntelliJ's test tree, in Spock's HTML reports, and to any Spock extension that inspects feature metadata.

## Grouping conditions

Sometimes you want to check several conditions against the same object, or collect every failure instead of stopping at the first one. Spockk provides `verify`, `verifyAll`, and `verifyEach` for this, usable directly inside a `then`/`expect` block:

<RevealCode>

```kotlin
then
verifyAll(wallet) {
  balance == 25
  transactions.size == 1
}
```

</RevealCode>

`verify(target) { ... }` scopes conditions to `target` and fails fast on the first unsatisfied one, just like a plain sequence of conditions. `verifyAll` behaves the same way but collects every failing condition and reports them together. `verifyEach(items) { ... }` runs the block once per element, aggregating failures across the whole collection instead of stopping at the first one. These mirror Spock's `with`, `verifyAll`, and `verifyEach` (Spockk calls the first one `verify` since `with` already means something else in Kotlin's standard library).

They also work inside an ordinary helper method of your specification, not just directly in a `then`/`expect` block. The one place they don't get this treatment is a top-level Kotlin extension function: the call still runs, just without the implicit-condition rewriting inside its block.

<div class="page-nav">
  <a class="page-nav-next" href="/limitations">
    <span class="page-nav-label">Next</span>
    <span class="page-nav-title">Limitations →</span>
  </a>
</div>

</div>
