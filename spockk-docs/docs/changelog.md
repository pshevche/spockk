---
layout: page
title: Changelog
---

<div class="page-container vp-doc">

# Changelog

## v0.5.1

- [Core] Support nullable data provider arguments
- Regular dependency management

## v0.5.0

- [Core] Added support for implicit conditions in expectation blocks
- [Core] Added `verify`/`verifyAll`/`verifyEach` implicit-assertion helpers
- [Core] Implemented Spock-style assertion failure rendering
- [IntelliJ Plugin] Added syntax support for implicit condition statements
- Regular dependency management

## v0.4.1

- [IntelliJ Plugin] Added support for IntelliJ 2026.2
- Regular dependency management

## v0.4.0

- [Core] Added support for Kotlin 2.4.x
- [Core] Bundle Spock API to avoid declaring Spock as a separate dependency
- Regular dependency management

## v0.3.2

- [Core] Allow accessing `@Shared` fields from `setupSpec` and `cleanupSpec` methods

## v0.3.1

- [Core] Do not fail the build if the source set has no specifications to transform

## v0.3.0

- [Core] Added support for fixture methods (`setup()`, `cleanup()`, `setupSpec()`, `cleanupSpec()`)
- [Core] Added support for `setup` and `cleanup` block labels in feature methods
- [Core] Fixed field behavior in fixture methods and data providers to match Spock semantics
- [Core] Enabled reliable block information access for Spock extensions
- [Core] Added support for Kotlin 2.3.20
- [IntelliJ Plugin] Added syntax support for `setup` and `cleanup` block labels
- [IntelliJ Plugin] Suppressed false-positive unreachable code warnings in `cleanup` and `where` blocks

## v0.2.1

- [Core] Fixed the transformation of extension functions in test fixtures
- [IntelliJ Plugin] Maintenance release with dependency upgrades

## v0.2.0

- [Core] Added support for data-driven features
- [Core] Introduced experimental support for Spock extensions
- [Core] Added support for Kotlin 2.3.0
- [IntelliJ Plugin] Added syntax support for Spock's data-driven features
- [IntelliJ Plugin] Integrated with Spock's native IntelliJ plugin

## v0.1.0

- [Core] Implement rich BDD-style syntax for defining test features
- [IntelliJ plugin] Detect Spockk syntax in test sources
- [IntelliJ plugin] Execute Spockk tests with Gradle from IDE

</div>
