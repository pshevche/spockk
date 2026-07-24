# VSCode Plugin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development
> (recommended) or superpowers:executing-plans to implement this plan task-by-task.
> Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a VSCode extension for Spockk that provides test run/debug via Gradle, warning suppression, and data table formatting in the Test Explorer.

**Architecture:** TypeScript extension in `spockk-vscode-plugin/` monorepo module. Gradle orchestrates npm. Block Detector uses import-based FQN verification. Test Controller uses VSCode TestController API with JUnit XML result mapping. Gradle Runner spawns the wrapper as a child process.

**Tech Stack:** TypeScript, VSCode Extension API (`@types/vscode`), Node.js, Gradle (via child process), JUnit XML (parsed with `fast-xml-parser` or manual parsing).

---

### Task 1: Project scaffolding

Set up the `spockk-vscode-plugin/` module with Gradle build, npm config, and TypeScript compiler.

**Files:**
- Create: `spockk-vscode-plugin/build.gradle.kts`
- Create: `spockk-vscode-plugin/package.json`
- Create: `spockk-vscode-plugin/tsconfig.json`
- Create: `spockk-vscode-plugin/.vscodeignore`
- Create: `spockk-vscode-plugin/src/extension/extension.ts`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Create module directory and build.gradle.kts**

```kotlin
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("com.github.node-gradle.node") version "7.1.0"
  id("spockk.kotlin-library")
}

node {
  version.set("22.0.0")
  npmVersion.set("10.0.0")
  download.set(false)
  nodeProjectDir.set(layout.projectDirectory)
}

val npmInstall by tasks.existing(NpmInstallTask::class)

val npmRunCompile by tasks.registering(NpmTask::class) {
  dependsOn(npmInstall)
  npmCommand.set(listOf("run", "compile"))
  args.set(listOf("--", "--noEmit", "false"))
}

val npmRunPackage by tasks.registering(NpmTask::class) {
  dependsOn(npmRunCompile)
  npmCommand.set(listOf("run", "package"))
}

tasks.assemble { dependsOn(npmRunCompile) }
tasks.build { dependsOn(npmRunPackage) }

tasks.register("cleanVsCode") {
  delete("out/", "*.vsix")
}
```

- [ ] **Step 2: Create package.json with version placeholder**

The `version` field will be injected by the Gradle build from `gradle.properties`. Use a placeholder:

```json
{
  "name": "spockk-vscode-plugin",
  "displayName": "Spockk",
  "description": "Spockk test support for VS Code",
  "version": "__VERSION__",
  "publisher": "pshevche",
  "engines": {
    "vscode": "^1.96.0"
  },
  "categories": ["Testing", "Formatters", "Debuggers"],
  "activationEvents": ["onLanguage:kotlin"],
  "main": "./out/extension.js",
  "contributes": {},
  "scripts": {
    "compile": "tsc -p ./tsconfig.json",
    "package": "vsce package --out build/vsix/",
    "test": "node ./out/test/runTest.js"
  },
  "devDependencies": {
    "@types/vscode": "^1.96.0",
    "typescript": "^5.7.0",
    "@vscode/vsce": "^3.0.0",
    "@vscode/test-electron": "^2.4.0"
  },
  "dependencies": {}
}
```

- [ ] **Step 3: Create tsconfig.json**

```json
{
  "compilerOptions": {
    "module": "commonjs",
    "target": "ES2022",
    "outDir": "out",
    "rootDir": "src",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "declaration": true,
    "sourceMap": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "out"]
}
```

- [ ] **Step 4: Create .vscodeignore**

```
node_modules/**
out/test/**
.git/**
.gitignore
*.md
tsconfig.json
```

- [ ] **Step 5: Create minimal extension.ts**

```typescript
import * as vscode from "vscode";

export function activate(context: vscode.ExtensionContext) {
  console.log("Spockk VSCode plugin activated");
}

export function deactivate() {}
```

- [ ] **Step 6: Add module to settings.gradle.kts**

Read `settings.gradle.kts` and add `spockk-vscode-plugin` to the `include` list.

- [ ] **Step 7: Add version injection to build.gradle.kts**

The `__VERSION__` placeholder in `package.json` is replaced with the project version from `gradle.properties` during the build:

```kotlin
val projectVersion = project.version.toString()
val npmRunCompile by tasks.registering(NpmTask::class) {
  dependsOn(npmInstall)
  npmCommand.set(listOf("run", "compile"))
  args.set(listOf("--", "--noEmit", "false"))
  doFirst {
    val pkg = layout.projectDirectory.file("package.json").asFile
    pkg.writeText(pkg.readText().replace("__VERSION__", projectVersion))
  }
}
```

- [ ] **Step 8: Bootstrap npm and verify**

Run: `npm install` in `spockk-vscode-plugin/`
Run: `npm run compile`
Expected: `out/extension.js` is created with the compiled extension.

- [ ] **Step 8: Commit**

```bash
git add settings.gradle.kts spockk-vscode-plugin/
git commit -m "feat: scaffold spockk-vscode-plugin module (#261)"
```

---

### Task 2: Block Detector

Implement the core detection logic for Spockk blocks, features, and specs using import-based FQN verification.

**Files:**
- Create: `spockk-vscode-plugin/src/extension/blockDetector.ts`
- Create: `spockk-vscode-plugin/src/extension/types.ts`
- Test: `spockk-vscode-plugin/test/suite/blockDetector.test.ts`

- [ ] **Step 1: Define types**

```typescript
export interface SpockkBlock {
  label: string;
  startLine: number;
  startColumn: number;
  endLine: number;
  endColumn: number;
}

export interface SpockkFeature {
  name: string;
  startLine: number;
  startColumn: number;
  blocks: SpockkBlock[];
}

export interface SpockkSpec {
  className: string;
  filePath: string;
  features: SpockkFeature[];
}
```

Write these to `types.ts`.

- [ ] **Step 2: Write the failing test for import scanning**

```typescript
import * as assert from "assert";
import { scanFile } from "../../src/extension/blockDetector";

describe("Block Detector", () => {
  it("detects Spockk imports via wildcard", () => {
    const content = `package com.example
import io.github.pshevche.spockk.lang.*

class MySpec {
  fun feature() {
    expect { 1 == 1 }
  }
}`;
    const result = scanFile("file.kt", content);
    assert.strictEqual(result.length, 1);
    assert.strictEqual(result[0].className, "MySpec");
    assert.strictEqual(result[0].features.length, 1);
    assert.strictEqual(result[0].features[0].name, "feature");
  });
});
```

- [ ] **Step 3: Implement import scanning and block detection**

```typescript
import { SpockkSpec, SpockkFeature, SpockkBlock } from "./types";

const SPOCKK_BLOCKS = new Set([
  "given", "setup", "expect", "when", "then", "and", "where", "cleanup"
]);

const SPOCKK_PACKAGE = "io.github.pshevche.spockk.lang";

export function scanFile(filePath: string, content: string): SpockkSpec[] {
  const lines = content.split("\n");
  const imports = collectImports(lines);
  const spockkImported = hasSpockkImport(imports);
  if (!spockkImported) return [];
  return detectSpecs(filePath, lines, imports);
}
```

Implement `collectImports`, `hasSpockkImport`, `detectSpecs`, `detectFeatures`, and `detectBlocks`.

Key logic:
- `hasSpockkImport`: check for `import ${SPOCKK_PACKAGE}.*` or `import ${SPOCKK_PACKAGE}.given` (any block name)
- `detectSpecs`: for each top-level class that contains features, create a spec
- `detectFeatures`: for each function that contains Spockk blocks at the top level, create a feature
- `detectBlocks`: for each Spockk block call (matched by FQN-verified import), record its range

- [ ] **Step 4: Write tests covering edge cases**

```typescript
it("detects individual block imports", () => {
  const content = `import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.where

class MySpec {
  fun test1() {
    expect { 1 == 1 }
  }
  fun test2() {
    expect { 2 == 2 }
    where {
      a | b
      1 | 2
    }
  }
}`;
  const result = scanFile("f.kt", content);
  assert.strictEqual(result.length, 1);
  assert.strictEqual(result[0].features.length, 2);
});

it("ignores non-Spockk blocks with same name", () => {
  const content = `class MySpec {
  fun test() {
    // This 'given' is not from spockk.lang
    val given = "hello"
    println(given)
  }
}`;
  const result = scanFile("f.kt", content);
  assert.strictEqual(result.length, 0);
});

it("handles multiple spec classes in one file", () => {
  const content = `import io.github.pshevche.spockk.lang.*
class SpecA {
  fun featureA() { expect { 1 == 1 } }
}
class SpecB {
  fun featureB() { expect { 2 == 2 } }
}`;
  const result = scanFile("f.kt", content);
  assert.strictEqual(result.length, 2);
});
```

- [ ] **Step 5: Run tests, verify they pass**

Run: `npx mocha test/suite/blockDetector.test.ts` (or via configured test runner)
Expected: All tests PASS.

- [ ] **Step 6: Commit**

```bash
git add spockk-vscode-plugin/src/extension/types.ts spockk-vscode-plugin/src/extension/blockDetector.ts
git commit -m "feat: add Spockk block detector (#261)"
```

---

### Task 3: Gradle Runner

Implement the Gradle wrapper detection, module resolution, test process spawning, and JUnit XML parsing.

**Files:**
- Create: `spockk-vscode-plugin/src/extension/gradleRunner.ts`
- Create: `spockk-vscode-plugin/src/extension/junitXmlParser.ts`
- Test: `spockk-vscode-plugin/test/suite/junitXmlParser.test.ts`

- [ ] **Step 1: Write the failing test for JUnit XML parsing**

```typescript
import * as assert from "assert";
import { parseJunitXml } from "../../src/extension/junitXmlParser";

describe("JUnit XML Parser", () => {
  it("parses test results from XML", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.MySpec" tests="2" failures="1" time="0.2">
  <testcase name="feature one" classname="com.example.MySpec" time="0.1"/>
  <testcase name="feature two" classname="com.example.MySpec" time="0.1">
    <failure message="expected: 42 but was: 43" type="AssertionError">
      java.lang.AssertionError: expected: 42 but was: 43
        at com.example.MySpec.feature two(MySpec.kt:10)
    </failure>
  </testcase>
</testsuite>`;
    const results = parseJunitXml(xml);
    assert.strictEqual(results.length, 2);
    assert.strictEqual(results[0].name, "feature one");
    assert.strictEqual(results[0].passed, true);
    assert.strictEqual(results[1].name, "feature two");
    assert.strictEqual(results[1].passed, false);
    assert.ok(results[1].failureMessage?.includes("42 but was 43"));
  });

  it("parses empty XML", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.MySpec" tests="0" failures="0" time="0.0"/>`;
    const results = parseJunitXml(xml);
    assert.strictEqual(results.length, 0);
  });
});
```

- [ ] **Step 2: Implement JUnit XML parser**

```typescript
export interface TestResult {
  name: string;
  className: string;
  passed: boolean;
  skipped: boolean;
  time: number;
  failureMessage?: string;
  stackTrace?: string;
}

export function parseJunitXml(xml: string): TestResult[] {
  // Simple XML parsing without external dependencies
  const results: TestResult[] = [];
  const testcaseRegex = /<testcase\s+[^>]*\/?>[\s\S]*?(?:<\/testcase>)?/g;
  const nameRegex = /name="([^"]*)"/;
  const classnameRegex = /classname="([^"]*)"/;
  const timeRegex = /time="([^"]*)"/;

  let match;
  while ((match = testcaseRegex.exec(xml)) !== null) {
    const block = match[0];
    const name = nameRegex.exec(block)?.[1] ?? "";
    const className = classnameRegex.exec(block)?.[1] ?? "";
    const time = parseFloat(timeRegex.exec(block)?.[1] ?? "0");
    const hasFailure = /<failure\b/.test(block);
    const isSkipped = /<skipped\b/.test(block);
    const failureMsg = hasFailure
      ? /<failure\s+message="([^"]*)"/.exec(block)?.[1]
      : undefined;
    const stackMatch = hasFailure
      ? /<failure[^>*>([\s\S]*?)<\/failure>/.exec(block)
      : undefined;

    results.push({
      name, className, passed: !hasFailure && !isSkipped,
      skipped: isSkipped, time,
      failureMessage: failureMsg,
      stackTrace: stackMatch?.[1]?.trim()
    });
  }
  return results;
}
```

- [ ] **Step 3: Write the failing test for Gradle runner**

```typescript
import * as assert from "assert";
import { findModuleForFile, buildTestFilter } from "../../src/extension/gradleRunner";

describe("Gradle Runner", () => {
  it("finds module by walking up to build.gradle.kts", () => {
    // In test env, mock the filesystem
    const module = findModuleForFile(
      "/project/spockk-core/src/test/kotlin/FooTest.kt",
      ["/project/spockk-core/build.gradle.kts", "/project/build.gradle.kts"]
    );
    assert.strictEqual(module, ":spockk-core");
  });

  it("builds test filter for single feature", () => {
    const filter = buildTestFilter("com.example.MySpec", "my feature");
    assert.strictEqual(filter, "--tests \"com.example.MySpec.my feature\"");
  });

  it("builds test filter for full spec", () => {
    const filter = buildTestFilter("com.example.MySpec");
    assert.strictEqual(filter, "--tests \"com.example.MySpec\"");
  });
});
```

- [ ] **Step 4: Implement Gradle runner**

```typescript
import * as path from "path";
import * as fs from "fs";
import { ChildProcess, spawn } from "child_process";

export interface GradleRunOptions {
  workspaceRoot: string;
  modulePath: string;
  testFilter: string;
}

export interface GradleRunResult {
  exitCode: number;
  testXmlDir: string;
}

export function findModuleForFile(
  filePath: string,
  existingBuildFiles?: string[]
): string | undefined {
  let dir = path.dirname(filePath);
  while (true) {
    const buildFile = path.join(dir, "build.gradle.kts");
    const buildFileExists = existingBuildFiles
      ? existingBuildFiles.includes(buildFile)
      : fs.existsSync(buildFile);
    if (buildFileExists) {
      const moduleName = path.basename(dir);
      if (existingBuildFiles?.includes(path.join(dir, "../build.gradle.kts")) ||
          !fs.existsSync(path.join(dir, "..", "build.gradle.kts"))) {
        // Skip root project, return the deepest module
        const parent = path.dirname(dir);
        const parentBuild = path.join(parent, "build.gradle.kts");
        if ((existingBuildFiles?.includes(parentBuild) || fs.existsSync(parentBuild)) &&
            parent !== dir) {
          return `:${path.basename(parent)}`;
        }
      }
      return `:${moduleName}`;
    }
    const parent = path.dirname(dir);
    if (parent === dir) return undefined;
    dir = parent;
  }
}

export function buildTestFilter(specFqn: string, featureName?: string): string {
  if (featureName) {
    return `--tests "${specFqn}.${featureName}"`;
  }
  return `--tests "${specFqn}"`;
}

export async function runGradleTests(
  options: GradleRunOptions
): Promise<GradleRunResult> {
  const gradlew = process.platform === "win32"
    ? "gradlew.bat"
    : "gradlew";
  const gradlewPath = path.join(options.workspaceRoot, gradlew);

  if (!fs.existsSync(gradlewPath)) {
    throw new Error(
      `Gradle wrapper not found at ${gradlewPath}. Run 'gradle wrapper' first.`
    );
  }

  return new Promise((resolve, reject) => {
    const args = [
      `${options.modulePath}:test`,
      options.testFilter,
      "--console=plain",
    ];
    const proc = spawn(gradlewPath, args, {
      cwd: options.workspaceRoot,
      stdio: ["ignore", "pipe", "pipe"],
    });

    let stdout = "";
    let stderr = "";

    proc.stdout?.on("data", (chunk: Buffer) => { stdout += chunk.toString(); });
    proc.stderr?.on("data", (chunk: Buffer) => { stderr += chunk.toString(); });
    proc.on("close", (code) => {
      const moduleDir = options.modulePath.replace(":", "").replace(":", "/");
      const testXmlDir = path.join(options.workspaceRoot, moduleDir, "build", "test-results", "test");
      resolve({ exitCode: code ?? -1, testXmlDir });
    });
    proc.on("error", reject);
  });
}
```

- [ ] **Step 5: Run tests, verify they pass**

Expected: JUnit XML parser tests pass, Gradle runner logic tests pass.

- [ ] **Step 6: Commit**

```bash
git add spockk-vscode-plugin/src/extension/{gradleRunner,junitXmlParser}.ts
git commit -m "feat: add Gradle runner and JUnit XML parser (#261)"
```

---

### Task 4: Test Controller

Wire up VSCode's TestController API for test discovery and execution.

**Files:**
- Create: `spockk-vscode-plugin/src/extension/testController.ts`
- Modify: `spockk-vscode-plugin/src/extension/extension.ts`

- [ ] **Step 1: Write the failing test for test controller logic**

```typescript
import * as assert from "assert";
import { mapTestResultsToItems } from "../../src/extension/testController";

describe("Test Controller", () => {
  it("maps XML results to test items by className + name", () => {
    const items = new Map<string, { spec: string; feature: string }>();
    items.set("com.example.MySpec.feature one", { spec: "com.example.MySpec", feature: "feature one" });
    items.set("com.example.MySpec.feature two", { spec: "com.example.MySpec", feature: "feature two" });

    const results = [
      { name: "feature one", className: "com.example.MySpec", passed: true, skipped: false, time: 0.1 },
      { name: "feature two", className: "com.example.MySpec", passed: false, skipped: false, time: 0.1, failureMessage: "failed" },
    ];

    const mapped = mapTestResultsToItems(results, items);
    assert.strictEqual(mapped.get("com.example.MySpec.feature one")?.passed, true);
    assert.strictEqual(mapped.get("com.example.MySpec.feature two")?.passed, false);
  });
});
```

- [ ] **Step 2: Implement TestController**

```typescript
import * as vscode from "vscode";
import { SpockkSpec, SpockkFeature } from "./types";
import { runGradleTests, findModuleForFile, buildTestFilter, GradleRunResult } from "./gradleRunner";
import { parseJunitXml, TestResult } from "./junitXmlParser";

interface TestItemKey {
  spec: string;
  feature?: string;
}

export function key(item: TestItemKey): string {
  return item.feature ? `${item.spec}.${item.feature}` : item.spec;
}

export function mapTestResultsToItems(
  results: TestResult[],
  items: Map<string, TestItemKey>
): Map<string, { passed: boolean; skipped: boolean; failureMessage?: string }> {
  const mapped = new Map();
  for (const r of results) {
    const k = key({ spec: r.className, feature: r.name });
    if (items.has(k)) {
      mapped.set(k, { passed: r.passed, skipped: r.skipped, failureMessage: r.failureMessage });
    }
  }
  return mapped;
}

export function createTestController(
  context: vscode.ExtensionContext,
  getSpecs: () => SpockkSpec[],
  workspaceRoot: () => string | undefined
): vscode.TestController {
  const controller = vscode.tests.createTestController("spockk-test-controller", "Spockk");
  const testItems = new Map<string, { item: vscode.TestItem; spec: string; feature?: string }>();

  controller.resolveHandler = async (item?: vscode.TestItem) => {
    if (item) return; // We resolve all at discovery time
    refresh();
  };

  function refresh() {
    testItems.clear();
    controller.items.replace([]);

    const root = workspaceRoot();
    if (!root) return;

    for (const spec of getSpecs()) {
      const specKey = spec.className;
      const specUri = vscode.Uri.file(spec.filePath);
      const specItem = controller.createTestItem(specKey, spec.className, specUri);
      specItem.canResolveChildren = false;
      testItems.set(key({ spec: spec.className }), { item: specItem, spec: spec.className });

      for (const feature of spec.features) {
        const featKey = key({ spec: spec.className, feature: feature.name });
        const range = new vscode.Range(feature.startLine - 1, feature.startColumn, feature.startLine - 1, feature.startColumn + 1);
        const featItem = controller.createTestItem(featKey, feature.name, specUri);
        featItem.range = range;
        featItem.canResolveChildren = false;
        specItem.children.add(featItem);
        testItems.set(featKey, { item: featItem, spec: spec.className, feature: feature.name });
      }
      controller.items.add(specItem);
    }
  }

  const runHandler = controller.createRunHandler();
  runHandler.runHandler = async (request, token) => {
    const root = workspaceRoot();
    if (!root) return;

    const run = controller.createTestRun(request);
    const specsToRun = new Set<string>();
    const featuresToRun = new Map<string, string[]>();
    const itemKeys = new Map<string, TestItemKey>();

    for (const item of request.include ?? []) {
      const info = testItems.get(item.id);
      if (!info) continue;
      itemKeys.set(item.id, { spec: info.spec, feature: info.feature });
      if (info.feature) {
        const arr = featuresToRun.get(info.spec) ?? [];
        arr.push(info.feature);
        featuresToRun.set(info.spec, arr);
      } else {
        specsToRun.add(info.spec);
      }
      specsToRun.add(info.spec);
    }

    // Find module from first spec's file
    const firstSpec = Array.from(getSpecs()).find(s => specsToRun.has(s.className));
    if (!firstSpec) { run.end(); return; }
    const modulePath = findModuleForFile(firstSpec.filePath);
    if (!modulePath) { run.end(); return; }

    // Build filters
    const filters = Array.from(specsToRun).map(spec => {
      const features = featuresToRun.get(spec);
      if (features && features.length > 0) {
        return buildTestFilter(spec, features[0]);
      }
      return buildTestFilter(spec);
    });

    // Check if any testcase entries matched
    const allXmlFiles = await readXmlFiles(result.testXmlDir);
    const allResults = allXmlFiles.flatMap(xml => parseJunitXml(xml));
    const matchedKeys = new Set(allResults.map(r => key({ spec: r.className, feature: r.name })));
    const requestedKeys = new Set(itemKeys.keys());
    const unmatched = [...requestedKeys].filter(k => !matchedKeys.has(k));
    if (unmatched.length > 0) {
      vscode.window.showWarningMessage(
        `No tests matched filter for: ${unmatched.join(", ")}. ` +
        `Check that the display names match features in the spec.`
      );
      for (const key of unmatched) {
        const info = itemKeys.get(key);
        if (info) {
          run.skipped(testItems.get(key)?.item);
        }
      }
    }

    // Execute
    try {
      const result = await runGradleTests({
        workspaceRoot: root,
        modulePath,
        testFilter: filters.join(" "),
      });

      if (result.exitCode !== 0) {
        // Check for XML
        const xmlFiles = await readXmlFiles(result.testXmlDir);
        if (xmlFiles.length === 0) {
          // Build failure
          for (const key of itemKeys.keys()) {
            run.failed(testItems.get(key)!.item, new vscode.TestMessage("Gradle build failed"));
          }
          run.end();
          return;
        }
      }

      // Parse XML and map results
      const xmlFiles = await readXmlFiles(result.testXmlDir);
      for (const xml of xmlFiles) {
        const results = parseJunitXml(xml);
        const mapped = mapTestResultsToItems(results, itemKeys);
        for (const [k, status] of mapped) {
          const info = itemKeys.get(k);
          if (!info) continue;
          const testItem = testItems.get(k)?.item;
          if (!testItem) continue;
          if (status.skipped) {
            run.skipped(testItem);
          } else if (status.passed) {
            run.passed(testItem, status.failureMessage ? status.failureMessage.length * 10 : 0);
          } else {
            const msg = new vscode.TestMessage(status.failureMessage ?? "Test failed");
            run.failed(testItem, msg, status.failureMessage ? status.failureMessage.length * 10 : 0);
          }
        }
      }
    } catch (err: any) {
      for (const [key, info] of testItems) {
        if (itemKeys.has(key)) {
          run.failed(info.item, new vscode.TestMessage(err.message));
        }
      }
    }
    run.end();
  };

  controller.refreshHandler = () => refresh();

  context.subscriptions.push(controller, runHandler);
  return controller;
}

async function readXmlFiles(dir: string): Promise<string[]> {
  const fs = await import("fs/promises");
  try {
    const files = await fs.readdir(dir);
    const xmlFiles: string[] = [];
    for (const f of files) {
      if (f.startsWith("TEST-") && f.endsWith(".xml")) {
        xmlFiles.push(await fs.readFile(path.join(dir, f), "utf-8"));
      }
    }
    return xmlFiles;
  } catch {
    return [];
  }
}
```

Note: Need to add `import * as path from "path"` at the top.

- [ ] **Step 3: Update extension.ts to wire the test controller**

```typescript
import * as vscode from "vscode";
import { scanFile } from "./blockDetector";
import { SpockkSpec } from "./types";
import { createTestController } from "./testController";

let controller: vscode.TestController | undefined;
let specsCache: SpockkSpec[] = [];

function getWorkspaceRoot(): string | undefined {
  return vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
}

export function activate(context: vscode.ExtensionContext) {
  console.log("Spockk VSCode plugin activated");

  controller = createTestController(context, () => specsCache, getWorkspaceRoot);

  // Scan on save
  context.subscriptions.push(
    vscode.workspace.onDidSaveTextDocument((doc) => {
      if (doc.languageId !== "kotlin") return;
      const specs = scanFile(doc.uri.fsPath, doc.getText());
      specsCache = specsCache.filter(s => s.filePath !== doc.uri.fsPath);
      specsCache.push(...specs);
      // TestController handles tree refresh internally
    })
  );

  // Scan active editor on activation
  const editor = vscode.window.activeTextEditor;
  if (editor && editor.document.languageId === "kotlin") {
    const specs = scanFile(editor.document.uri.fsPath, editor.document.getText());
    specsCache.push(...specs);
  }
}

export function deactivate() {}
```

- [ ] **Step 4: Commit**

```bash
git add spockk-vscode-plugin/src/extension/{testController,extension}.ts
git commit -m "feat: add TestController for spec/feature execution (#261)"
```

---

### Task 5: Debug Adapter

Implement debug session management via the Java Debug Extension.

**Files:**
- Create: `spockk-vscode-plugin/src/extension/debugAdapter.ts`
- Modify: `spockk-vscode-plugin/src/extension/extension.ts`

- [ ] **Step 1: Write failing test for debug adapter configuration**

```typescript
import * as assert from "assert";
import { buildDebugConfig } from "../../src/extension/debugAdapter";

describe("Debug Adapter", () => {
  it("builds debug configuration for a feature", () => {
    const config = buildDebugConfig("com.example.MySpec", "my feature");
    assert.strictEqual(config.name, "Debug Spockk: com.example.MySpec.my feature");
    assert.strict(config.request, "attach");
    assert.strictEqual(config.type, "java");
    assert.strictEqual(config.port, 5005);
  });

  it("builds debug configuration for a spec", () => {
    const config = buildDebugConfig("com.example.MySpec");
    assert.strictEqual(config.name, "Debug Spockk: com.example.MySpec");
  });
});
```

- [ ] **Step 2: Implement debug adapter**

```typescript
import * as vscode from "vscode";

export interface DebugConfig {
  name: string;
  type: string;
  request: string;
  hostName: string;
  port: number;
  projectName?: string;
}

export function buildDebugConfig(specFqn: string, featureName?: string): DebugConfig {
  const label = featureName ? `${specFqn}.${featureName}` : specFqn;
  return {
    name: `Debug Spockk: ${label}`,
    type: "java",
    request: "attach",
    hostName: "localhost",
    port: 5005,
  };
}

export async function startDebugSession(
  specFqn: string,
  featureName?: string
): Promise<boolean> {
  const config = buildDebugConfig(specFqn, featureName);
  return vscode.debug.startDebugging(undefined, config);
}
```

- [ ] **Step 3: Wire debug into TestController**

Update `testController.ts` to support debug runs. Add to the run handler:

```typescript
// In the run handler, when request is a debug run
if (request.kind === vscode.TestRunKind.Debug) {
  // Mark the test as running
  run.started(testItem);
  // Launch Gradle with --debug-jvm
  const gradleArgs = [
    `${modulePath}:test`,
    testFilter,
    "--debug-jvm",
    "--console=plain",
  ];
  // Start debug session after a short delay (JVM needs to start and suspend)
  setTimeout(async () => {
    await vscode.commands.executeCommand("java.debug");
    // The Java Debug Extension handles attachment
    // After Gradle completes, read XML and report results (same as normal run)
  }, 3000);
}
```

- [ ] **Step 4: Commit**

```bash
git add spockk-vscode-plugin/src/extension/debugAdapter.ts
git commit -m "feat: add debug adapter for Java Debug Extension (#261)"
```

---

### Task 6: Data Table Formatter

Implement format-on-save for `where` block data table semicolon alignment.

**Files:**
- Create: `spockk-vscode-plugin/src/extension/dataTableFormatter.ts`
- Modify: `spockk-vscode-plugin/src/extension/extension.ts`
- Modify: `spockk-vscode-plugin/package.json`
- Test: `spockk-vscode-plugin/test/suite/dataTableFormatter.test.ts`

- [ ] **Step 1: Write failing test**

```typescript
import * as assert from "assert";
import { alignDataTable } from "../../src/extension/dataTableFormatter";

describe("Data Table Formatter", () => {
  it("aligns semicolons in a where block", () => {
    const input = `expect: Math.min(a, b) == c
where:
a | b | c
1 | 2 | 1
100 | 200 | 100`;
    const expected = `expect: Math.min(a, b) == c
where:
a   | b   | c
1   | 2   | 1
100 | 200 | 100`;
    assert.strictEqual(alignDataTable(input), expected);
  });

  it("ignores non-where blocks", () => {
    const input = `given:
def x = 1
def y = 2`;
    assert.strictEqual(alignDataTable(input), input);
  });

  it("handles data tables with comments", () => {
    const input = `where:
a | b | c // comment
1 | 2 | 1
3 | 4 | 3`;
    const expected = `where:
a | b | c // comment
1 | 2 | 1
3 | 4 | 3`; // Already aligned
    assert.strictEqual(alignDataTable(input), input);
  });
});
```

- [ ] **Step 2: Implement data table formatter**

```typescript
export function alignDataTable(content: string): string {
  const lines = content.split("\n");
  let inWhereBlock = false;
  const dataRows: number[] = [];
  let result = lines.slice();

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (/^\s*where\s*[{:]?\s*$/.test(line) || /^\s*where\s*\(.*\)\s*{/.test(line)) {
      inWhereBlock = true;
      dataRows.length = 0;
      continue;
    }
    if (!inWhereBlock) continue;

    // Check for end of where block (next block label, closing brace at feature level)
    if (/^\s*(given|when|then|expect|setup|cleanup|and)\s*[(:]/.test(line) ||
        /^\s*}\s*$/.test(line) && dataRows.length > 0) {
      alignRows(result, dataRows);
      inWhereBlock = false;
      continue;
    }

    if (line.includes("|")) {
      // Strip trailing comments
      const commentIdx = line.indexOf("//");
      const effectiveLine = commentIdx >= 0 ? line.substring(0, commentIdx) : line;
      if (/^\s*\w/.test(effectiveLine) && effectiveLine.includes("|")) {
        dataRows.push(i);
      }
    }
  }
  if (dataRows.length > 0) {
    alignRows(result, dataRows);
  }
  return result.join("\n");
}

function alignRows(result: string[], rows: number[]): void {
  if (rows.length < 2) return;
  const columns: string[][] = rows.map(r =>
    result[r].split("|").map(c => c.trim())
  );
  const maxColWidths: number[] = [];
  for (let col = 0; col < columns[0].length; col++) {
    maxColWidths[col] = Math.max(...columns.map(row => row[col]?.length ?? 0));
  }
  for (let i = 0; i < rows.length; i++) {
    const row = rows[i];
    const commentMatch = result[row].match(/(\/\/.*)$/);
    const comment = commentMatch?.[1] ?? "";
    const aligned = columns[i]
      .map((cell, col) => cell.padEnd(maxColWidths[col]))
      .join(" | ");
    result[row] = result[row].includes("//")
      ? `${aligned}  ${comment}`
      : aligned;
  }
}
```

- [ ] **Step 3: Implement VSCode formatting provider**

```typescript
import * as vscode from "vscode";

export class SpockkDataTableFormattingProvider
  implements vscode.DocumentFormattingEditProvider
{
  provideDocumentFormattingEdits(
    document: vscode.TextDocument,
    _options: vscode.FormattingOptions,
    _token: vscode.CancellationToken
  ): vscode.TextEdit[] {
    const content = document.getText();
    const aligned = alignDataTable(content);
    if (aligned === content) return [];
    const fullRange = new vscode.Range(
      document.positionAt(0),
      document.positionAt(content.length)
    );
    return [vscode.TextEdit.replace(fullRange, aligned)];
  }
}
```

- [ ] **Step 4: Wire into extension.ts and package.json**

In `extension.ts`:
```typescript
import { SpockkDataTableFormattingProvider } from "./dataTableFormatter";

// After controller creation:
context.subscriptions.push(
  vscode.languages.registerDocumentFormattingEditProvider(
    "kotlin",
    new SpockkDataTableFormattingProvider()
  )
);
```

In `package.json`, add to `contributes`:
```json
"contributes": {
  "languages": [{
    "id": "kotlin",
    "aliases": ["Kotlin"]
  }]
}
```

- [ ] **Step 5: Run tests, verify they pass**

- [ ] **Step 6: Commit**

```bash
git add spockk-vscode-plugin/src/extension/dataTableFormatter.ts
git commit -m "feat: add data table formatter for where blocks (#261)"
```

---

### Task 7: Diagnostic Filter Investigation

Investigate whether the Kotlin LSP emits false-positive diagnostics for Spockk blocks, and implement suppression if needed.

**Files:**
- Create: `spockk-vscode-plugin/src/extension/diagnosticFilter.ts`
- Modify: `spockk-vscode-plugin/src/extension/extension.ts`

- [ ] **Step 1: Write a test script to check Kotlin LSP diagnostics**

Create a test Spockk spec file and open it in VSCode. Run the "Developer: Inspect Editor Tokens and Scopes" tool to see what diagnostics fire. Document findings.

```typescript
// Test script — run manually during implementation
import * as vscode from "vscode";

export function diagnosticInvestigation(context: vscode.ExtensionContext) {
  context.subscriptions.push(
    vscode.languages.onDidChangeDiagnostics((e) => {
      for (const uri of e.uris) {
        if (uri.fsPath.endsWith(".kt")) {
          const diagnostics = vscode.languages.getDiagnostics(uri);
          for (const d of diagnostics) {
            console.log(`[${d.code}] ${d.message} at ${d.range.start.line}:${d.range.start.character}`);
          }
        }
      }
    })
  );
}
```

Run this in the extension test environment with a real Spockk spec file and observe output.

- [ ] **Step 2: Implement suppression if needed**

Depending on findings from Step 1, choose the mechanism:

**If Kotlin LSP supports diagnostic exclusion via settings:**
```typescript
// In activate():
const config = vscode.workspace.getConfiguration("kotlin");
const excluded = config.get<string[]>("diagnostics.exclude", []);
if (!excluded.includes("UnusedExpression")) {
  await config.update("diagnostics.exclude", [...excluded, "UnusedExpression"], vscode.ConfigurationTarget.Workspace);
}
```

**If Kotlin LSP does NOT support exclusion and diagnostics are a problem:**
Implement CodeAction with `@Suppress` annotation insertion:

```typescript
import * as vscode from "vscode";
import { SpockkSpec } from "./types";

export class SpockkSuppressionCodeActionProvider
  implements vscode.CodeActionProvider
{
  constructor(private getSpecs: () => SpockkSpec[]) {}

  provideCodeActions(
    document: vscode.TextDocument,
    range: vscode.Range
  ): vscode.CodeAction[] {
    const specs = this.getSpecs();
    const isSpecFile = specs.some(s => s.filePath === document.uri.fsPath);
    if (!isSpecFile) return [];

    const diagnostics = vscode.languages.getDiagnostics(document.uri);
    const spockkDiagnostics = diagnostics.filter(d => {
      const code = typeof d.code === "string" ? d.code : String(d.code);
      return (
        (code === "UnusedExpression" || code === "UnreachableCode") &&
        range.intersection(d.range)
      );
    });
    if (spockkDiagnostics.length === 0) return [];

    const action = new vscode.CodeAction(
      "Suppress Spockk warning",
      vscode.CodeActionKind.QuickFix
    );
    action.edit = new vscode.WorkspaceEdit();
    action.edit.insert(
      document.uri,
      new vscode.Position(0, 0),
      "@file:Suppress(\"UNUSED_EXPRESSION\", \"UNREACHABLE_CODE\")\n"
    );
    action.diagnostics = spockkDiagnostics;
    return [action];
  }
}
```

Register in `extension.ts`:
```typescript
context.subscriptions.push(
  vscode.languages.registerCodeActionsProvider(
    "kotlin",
    new SpockkSuppressionCodeActionProvider(() => specsCache),
    { providedCodeActionKinds: [vscode.CodeActionKind.QuickFix] }
  )
);
```

- [ ] **Step 3: Commit**

```bash
git add spockk-vscode-plugin/src/extension/diagnosticFilter.ts
git commit -m "feat: add diagnostic filter for Spockk warning suppression (#261)"
```

---

### Task 8: Integration Tests

Create an end-to-end test that verifies the extension works with a real Gradle project.

**Files:**
- Create: `spockk-vscode-plugin/test/suite/index.ts`
- Create: `spockk-vscode-plugin/test/runTest.ts`
- Create: `spockk-vscode-plugin/test/test-project/` (minimal Gradle project with Spockk spec)

- [ ] **Step 1: Create test runner entry point**

```typescript
// test/suite/index.ts
import * as path from "path";
import * as Mocha from "mocha";
import * as glob from "glob";

export function run(): Promise<void> {
  const mocha = new Mocha({ ui: "bdd", color: true });
  const testsRoot = path.resolve(__dirname);
  return new Promise((resolve, reject) => {
    glob("**/**.test.js", { cwd: testsRoot }, (err, files) => {
      if (err) return reject(err);
      files.forEach(f => mocha.addFile(path.resolve(testsRoot, f)));
      mocha.run(failures => {
        if (failures > 0) reject(new Error(`${failures} tests failed`));
        else resolve();
      });
    });
  });
}
```

- [ ] **Step 2: Create test-project with a minimal Spockk spec**

Create a minimal Gradle project under `test/test-project/`:
- `build.gradle.kts` with Spockk plugin applied
- `settings.gradle.kts`
- `src/test/kotlin/com/example/MySpec.kt` with a simple Spockk spec
- `gradlew`/`gradlew.bat` (symlink to project root's wrapper)

- [ ] **Step 3: Write end-to-end test**

```typescript
// test/suite/e2e.test.ts
import * as assert from "assert";
import * as vscode from "vscode";
import * as path from "path";

describe("Spockk Extension E2E", () => {
  it("discovers specs in the test project", async () => {
    const testProject = path.resolve(__dirname, "../../test-project");
    const uri = vscode.Uri.file(path.join(testProject, "src/test/kotlin/com/example/MySpec.kt"));

    // Open the file
    const doc = await vscode.workspace.openTextDocument(uri);
    await vscode.window.showTextDocument(doc);

    // Give the extension time to scan
    await new Promise(r => setTimeout(r, 2000));

    // Check test controller
    const controller = vscode.tests.controllers;
    assert.ok(controller.length > 0);
    const spockkCtrl = controller.find(c => c.id === "spockk-test-controller");
    assert.ok(spockkCtrl, "Spockk test controller should exist");
  });

  it("runs a spec and reports results", async () => {
    // This test run takes ~30s due to Gradle startup
    // For CI, this would be a smoke test only
  });
});
```

- [ ] **Step 4: Wire test into build.gradle.kts**

In `build.gradle.kts`, add a test task:
```kotlin
val npmTest by tasks.registering(NpmTask::class) {
  dependsOn(npmRunCompile)
  npmCommand.set(listOf("run", "test"))
}

tasks.check { dependsOn(npmTest) }
```

Add to `package.json` scripts:
```json
"test": "node ./out/test/runTest.js"
```

- [ ] **Step 5: Commit**

```bash
git add spockk-vscode-plugin/test/
git commit -m "test: add integration tests for VSCode plugin (#261)"
```

---

### Task 9: Polish and final configuration

Finalize the extension manifest, add icons, handle edge cases.

**Files:**
- Modify: `spockk-vscode-plugin/package.json`
- Create: `spockk-vscode-plugin/resources/icon.svg`

- [ ] **Step 1: Update package.json with contributes**

```json
"contributes": {
  "testControllers": [{
    "id": "spockk-test-controller",
    "label": "Spockk"
  }],
  "languages": [{
    "id": "kotlin"
  }],
  "debuggers": [{
    "type": "java",
    "label": "Java"
  }]
}
```

- [ ] **Step 2: Copy icon from IntelliJ plugin**

Copy `spockk-intellij-plugin/src/main/resources/pluginIcon.svg` to `resources/icon.svg`.

- [ ] **Step 3: Run spotlessApply and build**

```bash
./gradlew spotlessApply
./gradlew :spockk-vscode-plugin:build
```

Expected: Build produces `build/vsix/spockk-vscode-plugin-0.4.0.vsix`.

- [ ] **Step 4: Manual smoke test**

Install the `.vsix` in VSCode via `Extensions: Install from VSIX...`, open a Spockk project, and verify:
- CodeLens appears on spec classes
- Running tests produces results in Test Explorer

- [ ] **Step 5: Commit**

```bash
git add spockk-vscode-plugin/
git commit -m "feat: finalize VSCode plugin configuration (#261)"
```
