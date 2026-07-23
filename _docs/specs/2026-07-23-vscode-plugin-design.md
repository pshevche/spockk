# VSCode Plugin for Spockk

**Date:** 2026-07-23
**Issue:** #261

## Context

The IntelliJ IDEA plugin has become increasingly painful to maintain due to constant API changes and compatibility breaks (e.g., the 2026.2 release broke run line markers and unused-expression suppression). The built-in Spock plugin also intervenes with the custom plugin, creating a confusing experience.

This spec proposes a VSCode extension that provides the same core functionality — warning suppression, test running/debugging, and data table formatting — but via VSCode's stable extension APIs. The goal is a best-effort dogfooding experience, not necessarily full feature parity.

## Functional Design

### What the user sees

- **No false-positive warnings** — Block statements (`given`, `when`, `then`, `expect`, `where`, `setup`, `cleanup`, `and`) are not marked as unused. Expressions inside `where` and `cleanup` blocks are not marked as unreachable.
- **Run/debug buttons** — CodeLenses appear above spec classes and feature methods. Clicking "Run" runs the test via Gradle. Clicking "Debug" runs it with the Java debugger attached.
- **Test results in UI** — VSCode's native Test Explorer shows discovered specs and features with pass/fail/skip status, stack traces, and durations.
- **Formatted data tables** — On save, semicolons in `where` block data tables are column-aligned.

### What the user does NOT see

- No custom language server — the existing Kotlin LSP handles syntax highlighting and basic diagnostics.
- No compiler plugin modifications — detection is purely text-based on the source file.
- No Gradle plugin changes — the existing Gradle test task works as-is.

## Architecture

**Module:** `spockk-vscode-plugin/` in the monorepo. TypeScript, Node.js runtime, compiled to JavaScript, packaged as a `.vsix`.

**Build:** `build.gradle.kts` orchestrates `npm install`, `npm run compile`, and `npm run package`. The `.vsix` lands in `build/vsix/`.

### Component Diagram

```
spockk-vscode-plugin/
├── build.gradle.kts
├── package.json
├── tsconfig.json
├── src/extension/
│   ├── extension.ts              # Entry point, activation
│   ├── blockDetector.ts          # Text-based Spockk block detection
│   ├── diagnosticFilter.ts       # Suppress false-positive diagnostics
│   ├── testController.ts         # VSCode TestController integration
│   ├── gradleRunner.ts           # Gradle child process management
│   ├── debugAdapter.ts           # Debug session launcher
│   └── dataTableFormatter.ts     # Format-on-save for where blocks
├── test/suite/
│   ├── blockDetector.test.ts
│   ├── diagnosticFilter.test.ts
│   ├── testController.test.ts
│   └── dataTableFormatter.test.ts
└── resources/
    └── icon.svg
```

### Data Flow

```
File saved/opened
  → blockDetector re-analyzes file
    → testController: update TestItem tree (specs/features)
    → diagnosticFilter: check if Kotlin LSP emits false-positive warnings
      → If yes: offer workspace config change or CodeAction to suppress
      → If no (likely): no action needed

User clicks "Run" CodeLens
  → testController: create TestRun
  → gradleRunner: spawn ./gradlew :module:test --tests "*.<Spec>.<feature>" --console=plain
  → Gradle runs tests, exits
  → gradleRunner: read JUnit XML from build/test-results/test/
  → testController: map testcase entries to TestItems, report results

User clicks "Debug" CodeLens
  → gradleRunner: spawn ./gradlew :module:test --tests "<filter>" --debug-jvm --console=plain
  → debugAdapter: launch Java DAP attach on port 5005 (via Java Debug Extension)
  → User hits breakpoints in spec source
  → Gradle exits, read JUnit XML for results

File saved
  → dataTableFormatter: if file contains where block, align semicolons
```

## Component Specifications

### 1. Block Detector

**Purpose:** Identify Spockk spec classes, feature methods, and Spockk block regions in Kotlin source files.

**Detection rules:**
- **Spec class:** A Kotlin class that has at least one method containing a Spockk block label call (`given {`, `when {`, `then {`, `expect {`, `where {`, `setup {`, `cleanup {`, `and {`), or extends a class named `Specification`.
- **Feature method:** A method inside a spec that contains at least one Spockk block label.
- **Block region:** The range of source text covered by a Spockk block label invocation (from the label call through its trailing lambda).

**Implementation:** Import-based FQN verification. First, scan `import` statements for `io.github.pshevche.spockk.lang.*` or individual Spockk block imports. Only flag occurrences of `given`, `when`, `then`, `expect`, `where`, `setup`, `cleanup`, `and` that are confirmed to use the Spockk FQN (via import tracking). Within a file, this is a two-pass approach: collect imports, then scan the file body for Spockk block calls. A method is a feature if it contains at least one import-verified Spockk block call at the top level of the method body.

**Output:** A `SpockkFileAnalysis` object with:
```typescript
interface SpockkBlock {
  label: string;         // "given" | "when" | "then" | etc.
  startLine: number;
  startColumn: number;
  endLine: number;
  endColumn: number;
}
interface SpockkFeature {
  name: string;
  startLine: number;
  blocks: SpockkBlock[];
}
interface SpockkSpec {
  className: string;
  filePath: string;
  features: SpockkFeature[];
}
```

### 2. Diagnostic Filter

**Purpose:** Suppress false-positive `UnusedExpression` and `UnreachableCode` diagnostics inside Spockk blocks.

**Challenge:** VSCode's diagnostic aggregation is additive — each extension's `DiagnosticCollection` is merged in the UI, and there is no API to remove diagnostics set by another extension (the Kotlin LSP). This makes the IntelliJ-style "inspection suppressor" pattern unavailable.

**Approach:** Two-tier strategy:

**Tier 1 (workspace configuration):** Detect Spockk usage in the project and offer to configure the Kotlin LSP to disable `UnusedExpression` and `UnreachableCode` diagnostics globally. This is coarse but effective. The user can accept or decline.

**Tier 2 (CodeAction):** Register a `CodeActionProvider` that provides "Suppress Spockk warning" actions. When invoked on a Spockk block's diagnostic:
- Adds `@Suppress("UNUSED_EXPRESSION")` or `@Suppress("UNREACHABLE_CODE")` annotations to the enclosing spec class.
- This changes the source, but is a standard Kotlin suppression mechanism.

Additionally, the Diagnostic Filter does its own lightweight analysis:
- Subscribe to `languages.onDidChangeDiagnostics`.
- For each Kotlin diagnostic, check if its range falls within a known Spockk block region (from the Block Detector).
- If so, log it (for telemetry/debugging) and provide the CodeAction suggestion.
- Maintain a cache of `Uri → Diagnostic[]` to track which diagnostics are Spockk-related.

**Investigation needed at implementation time:**
- Verify whether the Kotlin LSP actually emits `UnusedExpression`/`UnreachableCode` diagnostics for Spockk code (it may not, since Kotlin's compiler warnings differ from IntelliJ inspections).
- If it does not, suppression is unnecessary for MVP.
- If it does, verify whether Tier 1 (LSP config) is supported by the installed Kotlin LSP version.

**Edge cases:**
- If the file is not a Spockk spec, no filtering occurs.
- If a diagnostic is partially inside and partially outside a block, it is NOT suppressed (conservative).

### 3. Test Controller

**Purpose:** Integrate with VSCode's native Test Explorer for spec/feature discovery and result reporting.

**Implementation:**
- Create a `TestController` with the ID `spockk-test-controller` and label `"Spockk"`.
- **Activation:** `onLanguage:kotlin` — fires when the user opens a Kotlin file. The extension reads only that file, checks imports for Spockk FQNs, and only activates further (test discovery, diagnostic filtering) if the file is a Spockk spec. No workspace-wide scan.
- **Discovery:** On save of a Kotlin file, re-run the Block Detector on that file. Update or remove `TestItem`s as needed. Create a tree of `TestItem`s:
  - Root level: spec classes → `TestItem` with `kind: TestItemKind.Suite`
  - Child level: features → `TestItem` with `kind: TestItemKind.Test`
  - Each `TestItem` has a URI and range pointing to the corresponding source location (enabling CodeLens and "Go to Test").
- **Execution:** When the user triggers a test run:
  1. Create a `TestRun` via `testController.createTestRun()`.
   2. Determine the Gradle test filter from the `TestItem` hierarchy:
     - For a single feature: `--tests "*.<SpecFQN>.<displayName>"` (display name matches the Kotlin function name as-is)
     - For a full spec: `--tests "*.<SpecFQN>"`
     - For a file with multiple specs: run each spec separately.
   3. Determine the Gradle module by matching the test file path against the project structure (walk up from the file until a `build.gradle.kts` is found; skip the root project's `build.gradle.kts`; use the directory name as the module name `:<dirName>`).
   4. Spawn `./gradlew :<module>:test --tests "<filter>" --console=plain` and pipe stdout.
   5. **Do not parse stdout for live results.** Results are reported only after Gradle exits.
   6. After Gradle exits, read `build/test-results/test/TEST-<SpecClass>.xml` for test results — PASSED, FAILED, SKIPPED, durations, stack traces, assertion diffs. Map `<testcase classname="SpecFQN" name="displayName">` entries to `TestItem`s by matching classname + name.
   7. Call `passed()`, `failed()`, `skipped()`, `appendMessages()` on the `TestRun` accordingly. End the `TestRun`.
- **CodeLenses:** The TestController automatically registers CodeLenses by default; no separate `CodeLensProvider` registration is needed for run/debug buttons above each `TestItem`.

### 4. Gradle Runner

**Purpose:** Manage Gradle child processes for test execution.

**Implementation:**
- Auto-detect the Gradle wrapper in the workspace root (`gradlew` on Unix, `gradlew.bat` on Windows).
- Accept a test filter string and Gradle module path.
- Spawn the process with `child_process.spawn()`, passing `--console=plain` for cleaner output.
- Do NOT parse stdout for live results. Results are determined from JUnit XML after the process exits.
- On process exit, resolve with the exit code and path to the JUnit XML results directory (`build/test-results/test/`).

**Error handling:**
- Gradle wrapper auto-detected at workspace root. If not found, show an error asking the user to run `gradle wrapper` or set up a Gradle project.
- If Gradle build fails (non-zero exit) and no JUnit XML exists, surface the build error in the test run output.
- If a test times out, kill the Gradle process and mark remaining tests as skipped.

### 5. Debug Adapter

**Purpose:** Enable debugging of Spockk features via Gradle.

**Implementation:**
- When the user clicks "Debug" on a spec/feature:

  1. Calculate the Gradle module and test filter (same as Test Controller).
  2. Spawn `./gradlew :<module>:test --tests "<filter>" --debug-jvm --console=plain`.
  3. Gradle's `--debug-jvm` flag suspends the JVM before running tests and waits for a debugger to attach on port 5005 by default.
  4. Start a VSCode debug session using the `java` debug type with configuration:
     ```json
     { "type": "java", "name": "Debug Spockk Test", "request": "attach", "hostName": "localhost", "port": 5005 }
     ```
  5. The Java Debug Extension (`vscode-java`) handles breakpoints, step-through, variable inspection, etc.
  6. When the test finishes (Gradle exits), end the debug session.
  7. Read JUnit XML for results (same as Test Controller).

**Dependency:** Requires the Java Debug Extension (`vscjava.vscode-java-debug`) to be installed.

- Register the debug configuration provider via `debug.registerDebugAdapterDescriptorFactory()`.

### 6. Data Table Formatter

**Purpose:** Column-align semicolons in `where` block data tables on file save.

**Implementation:**
- Implement `DocumentFormattingEditProvider`.
- On `textDocument/formatting` (triggered on save if `editor.formatOnSave` is enabled):
  1. Find all `where` blocks in the file via the Block Detector.
  2. For each `where` block, find lines containing data table rows (non-comment lines with semicolons).
  3. Split each row by semicolons.
  4. Column-align: for each column, find the maximum width across all rows, pad shorter values with trailing spaces.
  5. Rejoin with `; ` separator.
  6. Return the edits as a `TextEdit[]`.
- Only fires when the document contains a recognized `where` block.
- Ignores lines that are block comments (`/* */`) or line comments (`//`).
- Does NOT touch semicolons outside of `where` blocks.

**Edge cases:**
- Data tables with missing values: pad with spaces to maintain alignment.
- Data tables with inline comments on individual rows: strip trailing comments before alignment, re-add after.
- Escaped semicolons in string literals: do not split on these.

## Build Configuration

**`spockk-vscode-plugin/build.gradle.kts`:**
- Uses `com.github.node-gradle.node` plugin (or manual `exec` tasks) for npm lifecycle.
- Tasks:
  - `npmInstall` — runs `npm install`
  - `npmRunCompile` — runs `npm run compile` (tsc)
  - `npmRunPackage` — runs `npm run package` (vsce package)
  - `assemble` — depends on `npmRunCompile`
  - `build` — depends on `npmRunPackage`
- Produces `.vsix` at `build/vsix/spockk-vscode-plugin-<version>.vsix`.

**`package.json`:**
- Engine: `vscode` with `^1.96.0`.
- Activation events: `onLanguage:kotlin` only (lazy activation, per-file).
- Contributes: test controller, formatting provider, debug configuration provider.
- Dependencies: `@types/vscode`, `typescript`, `@vscode/test-electron`, `vsce`.

**Versioning:** Shares the project version from `gradle.properties`. The `.vsix` artifact name is `spockk-vscode-plugin-<version>.vsix`. Published as part of the regular release workflow.

## Testing

- **Unit tests** via `@vscode/test-electron` for each component (block detector, diagnostic filter, data table formatter).
- **Integration tests** use a minimal Gradle project with Spockk specs, verifying:
  - Test discovery finds specs and features.
  - Test execution produces correct PASSED/FAILED results.
  - Debug session attaches successfully.
- Tests run during `gradle build` (orchestrated via the Node Gradle plugin).

## Out of Scope (for MVP)

- Custom syntax highlighting — delegates to the Kotlin LSP.
- Inline parameterized test table editing — format-only.
- Multi-root workspace support — single workspace root only.
- Gradle task configuration UI — editing Gradle test configuration manually.
- Non-Gradle build systems (Maven, Bazel).
- Test history / re-run failed tests — relies on VSCode's built-in Test Explorer capabilities.
