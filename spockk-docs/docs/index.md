---
layout: page
title: Spockk
description: Spock's expressive specification syntax, natively in Kotlin.
---

<div class="page-container page-container-hero">
<section class="spockk-hero">
  <div>
    <h1 class="spockk-hero-title">Spock specs, <span class="gradient-text">written the way Kotlin wants them</span>.</h1>
    <p class="spockk-hero-tagline">
      Spockk is a compiler plugin that brings Spock's <code>given</code>/<code>when</code>/<code>then</code>
      specification syntax natively into Kotlin: no Groovy, no string DSLs, just real Kotlin code that compiles
      down to genuine Spock specifications.
    </p>
    <div class="spockk-hero-actions">
      <a class="spockk-btn spockk-btn-brand" href="/examples">See it in action</a>
      <a class="spockk-btn spockk-btn-alt" href="/getting-started">Get Started</a>
      <a class="spockk-btn spockk-btn-alt" href="https://github.com/pshevche/spockk">GitHub ↗</a>
    </div>
  </div>
  <div class="spockk-hero-visual">

<CodeWindow title="WelcomeSpec.kt">

```kotlin
class WelcomeSpec : Specification() {
  fun `welcome to Spockk`(
    visitor: Being,
    greeting: String
  ) {
    given
    val spock = Vulcan("Spock")

    expect
    spock.greet(visitor) == greeting

    where
    visitor         ; greeting
    Vulcan("Sarek") ; "Live long and prosper!"
    Human("Kirk")   ; "Hello!"
  }
}
```

</CodeWindow>

  </div>
</section>
</div>

<div class="page-container vp-doc">

## What is Spockk?

[Spock](https://spockframework.org/) is one of the most expressive testing frameworks around, but it's written for Groovy, and speaks with a Groovy accent: dynamic typing, closures-as-blocks, and an AST transform that rewrites your test class before Kotlin ever gets a say.

**Spockk teaches Kotlin to speak Spock.** It's a Kotlin compiler plugin that recognizes Spock's block-label vocabulary (`given`, `when`, `then`, `expect`, `and`, `where`, `cleanup`), written as plain, statically-typed Kotlin, and rewrites it at compile time into genuine Spock feature methods. The result runs on Spock's own JUnit Platform engine, right alongside your Groovy specs.

Spockk is **not** a standalone testing framework, and it doesn't try to replace Spock's documentation: [Spock's reference docs](https://spockframework.org/spock/docs/2.4/index.html) already do a great job of explaining specifications, fixtures, and data-driven testing in general. This guide focuses on what's actually new: how that vocabulary maps onto idiomatic Kotlin, and what that buys you.

<div class="feature-grid">
  <div class="feature-card">
    <div class="feature-icon">🧬</div>
    <h3>Native Kotlin syntax</h3>
    <p>Block labels are real Kotlin declarations, type-checked by the compiler: not comments, not annotations, not a Groovy interop shim.</p>
  </div>
  <div class="feature-card">
    <div class="feature-icon">⚙️</div>
    <h3>Real Spock underneath</h3>
    <p>Your specs compile down to genuine Spock feature and block metadata, so you keep Spock's reporting, IDE test trees, and extensions.</p>
  </div>
  <div class="feature-card">
    <div class="feature-icon">🧩</div>
    <h3>Fits your toolchain</h3>
    <p>Runs on Spock's JUnit Platform engine, so it works with the build tools, CI pipelines, and IDEs you already use for Kotlin.</p>
  </div>
</div>

<div class="page-nav">
  <a class="page-nav-next" href="/getting-started">
    <span class="page-nav-label">Next</span>
    <span class="page-nav-title">Getting Started →</span>
  </a>
</div>

</div>
