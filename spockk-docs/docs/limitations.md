---
layout: page
title: Limitations
---

<script setup>
const has = [
  'given/when/then/expect/and/cleanup block labels, with natural-language descriptions',
  'Explicit and implicit conditions, with Spock-style failure diagrams',
  'Data tables and data pipes',
  'Fixture methods (setup/cleanup/setupSpec/cleanupSpec)',
  "Runs on Spock's own JUnit Platform engine, with full IDE/report integration"
]

const lacks = [
  'No interaction-based testing syntax (<code>1 * mock.method()</code>)',
  'Spock extensions (<code>@IgnoreIf</code>, <code>@Requires</code>, ...) are only experimentally supported',
  "More than 10 years of features waiting to be ported to Kotlin 😁"
]
</script>

<div class="page-container vp-doc">

# Limitations

Spockk is in active development, and it falls short of writing a specification directly in Spock for Groovy.

<ComparisonPanels
  has-title="Spockk today"
  :has="has"
  lacks-title="Still Groovy-only"
  :lacks="lacks"
/>

These and other improvements are on the roadmap. Stay tuned for future releases!

<div class="page-nav">
  <a class="page-nav-next" href="changelog">
    <span class="page-nav-label">Next</span>
    <span class="page-nav-title">Changelog →</span>
  </a>
</div>

</div>
