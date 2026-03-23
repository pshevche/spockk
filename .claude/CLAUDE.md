# CLAUDE.md

## Project Overview

Spockk is a Kotlin compiler plugin that brings Spock-style BDD testing syntax to Kotlin. It transforms concise specification syntax (block labels like `given`, `when`, `then`, `expect`, `where`, `setup`, `cleanup`) into executable JUnit Platform tests.

## Modules

- **spockk-core** — Runtime library with block labels and data variable APIs
- **spockk-compiler-plugin** — Kotlin IR transformations for test syntax
- **spockk-gradle-plugin** — Gradle plugin applying the compiler plugin to Kotlin tasks
- **spockk-intellij-plugin** — IntelliJ IDEA integration (warning suppression, data table formatting)
- **spockk-specs** — Framework test suite (dogfooding: tests written using Spockk itself)
- **spockk-docs** — User guide in AsciiDoc, published to GitHub Pages

## Tech Stack

- Kotlin 2.3.20, JDK 21 (toolchain)
- Gradle 9.4.1 with version catalog (`gradle/libs.versions.toml`)
- Convention plugins in `gradle/plugins/`
- JUnit Platform 6.0.3, Spock 2.4-groovy-5.0

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

## Off-Limits

Never modify these without explicit instruction:
- `gradle/wrapper/` — Gradle wrapper files
- `gradle/config/licenseHeader.txt` — license header template
- `.github/workflows/` — CI/CD pipeline definitions
- `gradle.properties` (version field) — version bumps are deliberate releases

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

## Issue-Based Workflow

Use the `/gh-issue` skill to hand off a GitHub issue to Claude:

```
/gh-issue 42
```

The skill fetches the issue, creates a worktree, and routes based on the issue label:
- **`type::task`** — Claude implements directly, submits a PR when done
- **`type::story`** — Claude brainstorms a design spec and implementation plan first; implementation starts only after you approve them

## Design Specs & Plans

For larger features (`type::story` issues), Claude uses the superpowers plugin to design before coding:
- **Design specs** live in `_docs/specs/` (written by the brainstorming skill)
- **Implementation plans** live in `_docs/plans/` (written by the writing-plans skill)

**Superpowers plugin directory override:** Write specs to `_docs/specs/` and plans to `_docs/plans/` instead of the default `docs/superpowers/` paths.

The `/gh-issue` skill invokes brainstorming automatically for `type::story` issues. Review and approve the spec and plan before Claude proceeds to implementation.

## Publishing

- **Maven Central**: spockk-core, spockk-compiler-plugin
- **Gradle Plugin Portal**: spockk-gradle-plugin (ID: `io.github.pshevche.spockk`)
- **JetBrains Marketplace**: spockk-intellij-plugin
- **GitHub Pages**: spockk-docs
- Version defined in `gradle.properties` (currently `0.3.0`)

## Git Conventions

- **Commit messages**: Use [Conventional Commits](https://www.conventionalcommits.org/) — e.g., `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
- **Branch naming**: `<username>/<short-description>` where `<username>` is the `$USER` environment variable (e.g., `pshevche/configure-claude`)

## Code Review Workflow

When presenting changes for review, always refresh the in-app diff by running `git diff HEAD` so the user sees the current uncommitted changes.

## CI

- GitHub Actions: `verify.yml` (push/PR to main), `publish.yml` (manual dispatch)
- PGP signing enabled in CI for non-fork builds
- Renovate configured for automated dependency updates
