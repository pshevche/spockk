# AGENTS.md

This file is the single source of truth for both coding agents used on this project: OpenCode reads it directly, and
Claude Code reads it via the `@AGENTS.md` import in `CLAUDE.md`. Skills under `.opencode/skills/` are symlinked into
`.claude/skills/` so both tools can use them. Edit only this file for project instructions - don't fork content into
a separate CLAUDE.md.

## Project Overview

Spockk is a Kotlin compiler plugin that brings Spock-style BDD testing syntax to Kotlin. It transforms concise specification syntax (block labels like `given`, `when`, `then`, `expect`, `where`, `setup`, `cleanup`) into executable JUnit Platform tests.

## Modules

- **spockk-core** - Runtime library with block labels and data variable APIs
- **spockk-compiler-plugin** - Kotlin IR transformations for test syntax
- **spockk-gradle-plugin** - Gradle plugin applying the compiler plugin to Kotlin tasks
- **spockk-intellij-plugin** - IntelliJ IDEA integration (warning suppression, data table formatting)
- **spockk-specs** - Framework test suite (dogfooding: tests written using Spockk itself)
- **spockk-docs** - User guide in AsciiDoc, published to GitHub Pages

## Tech Stack

- Kotlin 2.4.10, JDK 21 (toolchain)
- Gradle 9.6.1 with version catalog (`gradle/libs.versions.toml`)
- Convention plugins in `gradle/plugins/`
- JUnit Platform 6.0.3, Spock 2.4-groovy-5.0

## Build Commands

```bash
./gradlew build                     # Full build (compile + test + code quality checks)
./gradlew test                      # Run all tests
./gradlew :spockk-specs:test        # Run framework tests only
./gradlew spotlessApply             # Auto-format code
./gradlew spotlessCheck             # Check formatting
./gradlew detekt                    # Static analysis
./gradlew :spockk-docs:asciidoctor  # Generate documentation
```

## Code Style

- **Formatter**: Spotless with ktlint (IntelliJ IDEA style)
- **Static analysis**: Detekt (config: `gradle/config/detekt.yml`)
- **Indent**: 2 spaces for Kotlin/Kts files
- **License headers**: Apache 2.0 on all source files (`gradle/config/licenseHeader.txt`)
- **Line endings**: LF, with final newline
- **Comments**: 2-3 lines max, straight to the point. If it's clear from the code, skip the comment. Don't narrate
  framework/library internals (e.g. Groovy AST node names) in comments - state the constraint or behavior directly.

## Off-Limits

Never modify these without explicit instruction:
- `gradle/wrapper/` - Gradle wrapper files
- `gradle/config/licenseHeader.txt` - license header template
- `.github/workflows/` - CI/CD pipeline definitions
- `gradle.properties` (version field) - version bumps are deliberate releases

## IR Transformations

When writing or modifying Kotlin IR transformations in `spockk-compiler-plugin`:

- **Prefer `DeclarationIrBuilder` extension functions** over raw `IrFactory` or `Ir*Impl` constructors. The project's `DeclarationIrBuilder.kt` (`spockk-compiler-plugin/.../compilation/ir/DeclarationIrBuilder.kt`) provides helpers like `irAnnotation()`, `irListOf()`, `irArrayOf()`, `irVar()`, `irVal()`, `irEnumValue()`, etc. Use these instead of manually constructing IR nodes.
- **Implement the `SpockkIrRewriter` interface** for new rewriters. It provides `context: IrGeneratorContext` and `irBuilder(owner)` factory method.
- **Extend `BaseSpockkIrElementTransformer`** when writing visitor-based transformers that need `currentIrClass`/`currentIrFunction` access.
- **Follow the composition pattern**: `SpockkIrTransformer` delegates to `SpecRewriter`/`FeatureRewriter`, which in turn delegate to specialized rewriters like `WhereBlockRewriter`. Prefer composing rewriters over deep inheritance.

## Condition Rendering Internals

Failure-message rendering (`expect`/`assert` diagrams) is not reimplemented in Kotlin - it reuses Spock's real
runtime classes (`SpockRuntime`, `ValueRecorder`, `Condition`, `ExpressionInfoBuilder`), shaded unmodified into
`spockk-core`. At runtime, that code re-parses the condition's source text as Groovy to build the diagram, then
matches recorded values to tree nodes purely by position (a post-order traversal count). This means:

- `ConditionValueRecordingTransformer` must produce the exact same number and order of recorded slots Groovy's
  own `ConditionRewriter` would for that text, including slots with no value (e.g. a method call's name and
  argument list, which Groovy indexes but never displays) - getting the count wrong desyncs every later value.
- Groovy is a required transitive runtime dependency, not incidental: `spock.lang.Specification`/`MockingApi`
  expose `groovy.lang.Closure` in their own bytecode, so it can't be dropped without abandoning "extend real
  Spock" entirely.
- To check what Spock would actually render for a given text/values pair, construct
  `org.spockframework.runtime.Condition(values, text, position, null, null, null)` directly and call
  `.getRendering()` - far faster and more reliable than deriving the packing algorithm by hand.
- `spockk-specs:compileTestFixturesKotlin` can report UP-TO-DATE after changing `spockk-compiler-plugin`; pass
  `--rerun` when validating compiler-plugin changes against fixture specs.

## Testing

- Tests live in `spockk-specs/src/test/kotlin/`, organized by type: `compilation/`, `e2e/`, `runtime/`, `smoke/`
- Test fixtures in `spockk-specs/src/testFixtures/`
- Uses Kotlin power-assert for enhanced assertion messages
- Parallel execution enabled (half available processors)

### Testing conventions

- **Transformation correctness -> snapshot tests**: Create a source/transformed file pair under `spockk-specs/src/test/resources/samples/compilation/source/` and `transformed/`. Use `TransformationSample.sampleFromResource("FileName")` and `assertTransformation()` from `BaseCompilationTest`. This compiles the source with the Spockk plugin and the expected file without it, then compares IR dumps.
- **Error scenarios -> inline specs**: Use `TestDataFactory.specWithFeatureBody()` or `specWithBody()` to define the spec inline in the test. Call `transform()` and assert `result.isSuccess()` is false and `result.compilation.messages` contains expected error text.
- **Runtime behavior -> smoke tests**: Write actual Spockk specs in `smoke/` that exercise the feature end-to-end. These are real specs that run via the Spock test engine.
- **Test discovery/execution -> engine tests**: Use `EngineTestKitUtils.execute()` with JUnit Platform selectors in `runtime/` tests.

## Issue-Based Workflow

Use the `/gh-issue` skill to hand off a GitHub issue to a coding agent (OpenCode or Claude Code):

```
/gh-issue 42
```

The skill fetches the issue, creates a worktree, and routes based on the issue label:
- **`type::task`** - Implement directly, submit a PR when done
- **`type::story`** - Write a design spec and implementation plan first; implementation starts only after approval

## Design Specs & Plans

For larger features (`type::story` issues), design before coding:
- **Design specs** live in `_docs/specs/`
- **Implementation plans** live in `_docs/plans/`

The `/gh-issue` skill uses the Superpowers brainstorming workflow for `type::story` issues on OpenCode; on Claude
Code, run the same brainstorming process manually (see the skill for details).

Review and approve the spec and plan before implementation starts.

## Publishing

- **Maven Central**: spockk-core, spockk-compiler-plugin
- **Gradle Plugin Portal**: spockk-gradle-plugin (ID: `io.github.pshevche.spockk`)
- **JetBrains Marketplace**: spockk-intellij-plugin
- **GitHub Pages**: spockk-docs
- Version defined in `gradle.properties` (currently `0.5.0`)

## Git Conventions

- **Commit messages**: Use [Conventional Commits](https://www.conventionalcommits.org/) - e.g., `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
- **Branch naming**: `<username>/<short-description>` where `<username>` is the `$USER` environment variable (e.g., `pshevche/configure-opencode`)

## Code Review Workflow

When presenting changes for review, always refresh the in-app diff by running `git diff HEAD` so reviewers see current uncommitted changes.

## CI

- GitHub Actions: `verify.yml` (push/PR to main), `publish.yml` (manual dispatch)
- PGP signing enabled in CI for non-fork builds
- Renovate configured for automated dependency updates
