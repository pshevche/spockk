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

## IR Transformations

When writing or modifying Kotlin IR transformations in `spockk-compiler-plugin`:

- **Prefer `DeclarationIrBuilder` extension functions** over raw `IrFactory` or `Ir*Impl` constructors. The project's `DeclarationIrBuilder.kt` (`spockk-compiler-plugin/.../compilation/ir/DeclarationIrBuilder.kt`) provides helpers like `irAnnotation()`, `irListOf()`, `irArrayOf()`, `irVar()`, `irVal()`, `irEnumValue()`, etc. Use these instead of manually constructing IR nodes.
- **Implement the `SpockkIrRewriter` interface** for new rewriters. It provides `context: IrGeneratorContext` and `irBuilder(owner)` factory method.
- **Extend `BaseSpockkIrElementTransformer`** when writing visitor-based transformers that need `currentIrClass`/`currentIrFunction` access.
- **Follow the composition pattern**: `SpockkIrTransformer` delegates to `SpecRewriter`/`FeatureRewriter`, which in turn delegate to specialized rewriters like `WhereBlockRewriter`. Prefer composing rewriters over deep inheritance.

## Testing

- Tests live in `spockk-specs/src/test/kotlin/`, organized by type: `compilation/`, `e2e/`, `runtime/`, `smoke/`
- Test fixtures in `spockk-specs/src/testFixtures/`
- Uses Kotlin power-assert for enhanced assertion messages
- Parallel execution enabled (half available processors)

### Testing conventions

- **Transformation correctness → snapshot tests**: Create a source/transformed file pair under `spockk-specs/src/test/resources/samples/compilation/source/` and `transformed/`. Use `TransformationSample.sampleFromResource("FileName")` and `assertTransformation()` from `BaseCompilationTest`. This compiles the source with the Spockk plugin and the expected file without it, then compares IR dumps.
- **Error scenarios → inline specs**: Use `TestDataFactory.specWithFeatureBody()` or `specWithBody()` to define the spec inline in the test. Call `transform()` and assert `result.isSuccess()` is false and `result.compilation.messages` contains expected error text.
- **Runtime behavior → smoke tests**: Write actual Spockk specs in `smoke/` that exercise the feature end-to-end. These are real specs that run via the Spock test engine.
- **Test discovery/execution → engine tests**: Use `EngineTestKitUtils.execute()` with JUnit Platform selectors in `runtime/` tests.

## Publishing

- **Maven Central**: spockk-core, spockk-compiler-plugin
- **Gradle Plugin Portal**: spockk-gradle-plugin (ID: `io.github.pshevche.spockk`)
- **JetBrains Marketplace**: spockk-intellij-plugin
- **GitHub Pages**: spockk-docs
- Version defined in `gradle.properties` (currently `0.2.0`)

## Git Conventions

- **Commit messages**: Use [Conventional Commits](https://www.conventionalcommits.org/) — e.g., `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
- **Branch naming**: `<username>/<short-description>` where `<username>` is the `$USER` environment variable (e.g., `pshevche/configure-claude`)

## Code Review Workflow

When presenting changes for review, always refresh the in-app diff by running `git diff HEAD` so the user sees the current uncommitted changes.

## CI

- GitHub Actions: `verify.yml` (push/PR to main), `publish.yml` (manual dispatch)
- PGP signing enabled in CI for non-fork builds
- Renovate configured for automated dependency updates
