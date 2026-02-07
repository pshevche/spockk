# CLAUDE.md

## Project Overview

Spockk is a Kotlin compiler plugin that brings Spock-style BDD testing syntax to Kotlin. It transforms concise specification syntax (block labels like `given`, `when`, `then`, `expect`, `where`) into executable JUnit Platform tests.

## Modules

- **spockk-core** — Runtime library with block labels and data variable APIs
- **spockk-compiler-plugin** — Kotlin IR transformations for test syntax
- **spockk-gradle-plugin** — Gradle plugin applying the compiler plugin to Kotlin tasks
- **spockk-intellij-plugin** — IntelliJ IDEA integration (warning suppression, data table formatting)
- **spockk-specs** — Framework test suite (dogfooding: tests written using Spockk itself)
- **spockk-docs** — User guide in AsciiDoc, published to GitHub Pages

## Tech Stack

- Kotlin 2.3.10, JDK 21 (toolchain)
- Gradle 9.3.1 with version catalog (`gradle/libs.versions.toml`)
- Convention plugins in `gradle/plugins/`
- JUnit Platform 6.0.2, Spock 2.4-groovy-5.0

## Build Commands

```bash
./gradlew build                  # Full build (compile + test + code quality checks)
./gradlew test                   # Run all tests
./gradlew :spockk-specs:test     # Run framework tests only
./gradlew spotlessApply          # Auto-format code
./gradlew spotlessCheck          # Check formatting
./gradlew detekt                 # Static analysis
./gradlew :spockk-docs:asciidoctor  # Generate documentation
```

## Code Style

- **Formatter**: Spotless with ktlint (IntelliJ IDEA style)
- **Static analysis**: Detekt (config: `gradle/config/detekt.yml`)
- **Indent**: 2 spaces for Kotlin/Kts files
- **License headers**: Apache 2.0 on all source files (`gradle/config/licenseHeader.txt`)
- **Line endings**: LF, with final newline

## Testing

- Tests live in `spockk-specs/src/test/kotlin/`, organized by type: `compilation/`, `e2e/`, `runtime/`, `smoke/`
- Test fixtures in `spockk-specs/src/testFixtures/`
- Uses Kotlin power-assert for enhanced assertion messages
- Parallel execution enabled (half available processors)

## Publishing

- **Maven Central**: spockk-core, spockk-compiler-plugin
- **Gradle Plugin Portal**: spockk-gradle-plugin (ID: `io.github.pshevche.spockk`)
- **JetBrains Marketplace**: spockk-intellij-plugin
- **GitHub Pages**: spockk-docs
- Version defined in `gradle.properties` (currently `0.2.0`)

## CI

- GitHub Actions: `verify.yml` (push/PR to main), `publish.yml` (manual dispatch)
- PGP signing enabled in CI for non-fork builds
- Renovate configured for automated dependency updates
