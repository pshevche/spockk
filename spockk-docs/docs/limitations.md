---
layout: page
title: Limitations
---

<script setup>
const has = [
  'given/when/then/expect/and/cleanup block labels, with natural-language descriptions',
  'Data tables and data pipes for data-driven features',
  'Fixture methods: setup/cleanup/setupSpec/cleanupSpec',
  "Runs on Spock's own JUnit Platform engine — real feature/block metadata, full IDE and report integration",
  'Works with Kotlin assertions, AssertJ, Hamcrest, and Spock\'s Mock()/Stub()/Spy() APIs'
]

const lacks = [
  'No interaction-based testing syntax (<code>1 * mock.method()</code>) — use Mockito\'s <code>verify()</code> instead',
  'No power-assert-style condition rewriting — write explicit <code>assert()</code>, AssertJ, or Hamcrest checks',
  'Spock extensions (<code>@IgnoreIf</code>, <code>@Requires</code>, …) are only experimentally supported',
  'IDE tooling is limited to JetBrains IDEs, via the Spockk IntelliJ plugin',
  "Not standalone — requires Spock, its JUnit Platform engine, and Spockk's Gradle and compiler plugins"
]
</script>

<div class="page-container vp-doc">

# Limitations

Spockk is still in active development. Here's an honest side-by-side of what it already gives you in Kotlin, and where it still falls short of writing a specification directly in Spock for Groovy.

<ComparisonPanels
  has-title="Spockk today"
  :has="has"
  lacks-title="Still Groovy-only"
  :lacks="lacks"
/>

None of the gaps above are architectural dead ends — they're on the [roadmap](https://github.com/users/pshevche/projects/4/views/1). Stay tuned for future releases!

<div class="page-nav">
  <a class="page-nav-next" href="/changelog">
    <span class="page-nav-label">Next</span>
    <span class="page-nav-title">Changelog →</span>
  </a>
</div>

</div>
