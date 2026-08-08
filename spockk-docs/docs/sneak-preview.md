---
layout: page
title: Sneak Preview
---

<div class="page-container vp-doc">

# Sneak Preview

No slides, no theory: here's an actual Spockk specification, in full.

<RevealCode>

```kotlin
import io.github.pshevche.spockk.lang.and
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`
import spock.lang.Specification

class WalletSpec : Specification() {
  fun `topping up a wallet`() {
    given("an empty wallet")
    val wallet = Wallet()

    `when`("$25 is added")
    wallet.topUp(25)

    then("the balance is $25")
    wallet.balance == 25

    and("a transaction is recorded")
    wallet.transactions.size == 1
  }
}
```

</RevealCode>

That's a complete Kotlin class: `Specification` is Spock's own base class, `given`/`when`/`then`/`and` are ordinary Kotlin declarations, and the string in parentheses is a natural-language description of the block. Notice there's no `assert()` either: a bare boolean expression is enough, and Spockk still knows exactly which line failed and why (more on that below).

Data-driven features read just as naturally:

<RevealCode>

```kotlin
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.given
import io.github.pshevche.spockk.lang.where
import spock.lang.Specification
import kotlin.test.assertFailsWith

class WalletSpec : Specification() {
  fun `withdrawing more than the balance fails`(balance: Int, withdrawal: Int) {
    given("a wallet with some balance")
    val wallet = Wallet(balance)

    expect("the withdrawal to be rejected")
    assertFailsWith<InsufficientFundsException> { wallet.withdraw(withdrawal) }

    where
    balance ; withdrawal
    10      ; 20
    0       ; 1
    5       ; 100
  }
}
```

</RevealCode>

Three rows, three iterations, one readable table: the same data-driven testing model Spock offers in Groovy, expressed as plain Kotlin values.

When a condition like `wallet.balance == 25` fails, Spockk doesn't just print `false`. It renders the same value diagram Spock builds for a failing Groovy condition, showing exactly what each part of the expression evaluated to:

```kotlin
then
wallet.balance == 100
```

<TerminalWindow title="test output">

```text
Condition not satisfied:

wallet.balance == 100
|      |       |
25     25      false
```

</TerminalWindow>

Curious what the compiler actually does with this? Head over to [How It Works](/how-it-works) to see the transformation. Or, if you'd rather try it yourself first, jump to [Getting Started](/getting-started).

<div class="page-nav">
  <a class="page-nav-next" href="/getting-started">
    <span class="page-nav-label">Next</span>
    <span class="page-nav-title">Getting Started →</span>
  </a>
</div>

</div>
