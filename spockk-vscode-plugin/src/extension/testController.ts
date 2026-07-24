import * as vscode from "vscode";
import * as path from "path";
import { SpockkSpec } from "./types";
import { runGradleTests, findModuleForFile, buildTestFilter } from "./gradleRunner";
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
  const mapped = new Map<string, { passed: boolean; skipped: boolean; failureMessage?: string }>();
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
    if (item) return;
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
        const range = new vscode.Range(feature.startLine, 0, feature.startLine, 0);
        const featItem = controller.createTestItem(featKey, feature.name, specUri);
        featItem.range = range;
        featItem.canResolveChildren = false;
        specItem.children.add(featItem);
        testItems.set(featKey, { item: featItem, spec: spec.className, feature: feature.name });
      }
      controller.items.add(specItem);
    }
  }

  const runProfile = controller.createRunProfile(
    "run",
    vscode.TestRunProfileKind.Run,
    async (request: vscode.TestRunRequest, _token: vscode.CancellationToken) => {
    const root = workspaceRoot();
    if (!root) {
      vscode.window.showErrorMessage("Spockk: No workspace folder open");
      return;
    }

    const run = controller.createTestRun(request);
    const specsToRun = new Set<string>();
    const itemKeys = new Map<string, TestItemKey>();

    for (const item of request.include ?? []) {
      const info = testItems.get(item.id);
      if (!info) continue;
      itemKeys.set(item.id, { spec: info.spec, feature: info.feature });
      specsToRun.add(info.spec);
    }

    const firstSpec = Array.from(getSpecs()).find(s => specsToRun.has(s.className));
    if (!firstSpec) { run.end(); return; }
    const modulePath = findModuleForFile(firstSpec.filePath);
    if (!modulePath) {
      vscode.window.showErrorMessage(`Spockk: Could not find Gradle module for ${firstSpec.filePath}`);
      run.end();
      return;
    }

    const filters = Array.from(specsToRun).map(spec => {
      const features = Array.from(itemKeys.values())
        .filter(k => k.spec === spec && k.feature)
        .map(k => k.feature!);
      if (features.length > 0) return features.map(f => buildTestFilter(spec, f)).join(" ");
      return buildTestFilter(spec);
    });

    try {
      const result = await runGradleTests({
        workspaceRoot: root,
        modulePath,
        testFilter: filters.join(" "),
      });

      const xmlFiles = await readXmlFiles(result.testXmlDir);

      if (result.exitCode !== 0 && xmlFiles.length === 0) {
        for (const [id, info] of testItems) {
          if (itemKeys.has(id)) {
            run.failed(info.item, new vscode.TestMessage("Gradle build failed"));
          }
        }
        run.end();
        return;
      }

      const allResults: TestResult[] = [];
      for (const xml of xmlFiles) {
        allResults.push(...parseJunitXml(xml));
      }

      const mapped = mapTestResultsToItems(allResults, itemKeys);

      const matchedKeys = new Set(allResults.map(r => key({ spec: r.className, feature: r.name })));
      const requestedKeys = new Set(itemKeys.keys());
      const unmatched = [...requestedKeys].filter(k => !matchedKeys.has(k));
      if (unmatched.length > 0) {
        vscode.window.showWarningMessage(
          `Spockk: No tests matched filter for: ${unmatched.join(", ")}. ` +
          `Check that the display names match features in the spec.`
        );
        for (const k of unmatched) {
          const info = itemKeys.get(k);
          if (info) {
            const item = testItems.get(k)?.item;
            if (item) run.skipped(item);
          }
        }
      }

      for (const [k, status] of mapped) {
        const info = itemKeys.get(k);
        if (!info) continue;
        const testItem = testItems.get(k)?.item;
        if (!testItem) continue;
        if (status.skipped) {
          run.skipped(testItem);
        } else if (status.passed) {
          run.passed(testItem, 0);
        } else {
          const msg = new vscode.TestMessage(status.failureMessage ?? "Test failed");
          run.failed(testItem, msg);
        }
      }
    } catch (err: any) {
      for (const [id, info] of testItems) {
        if (itemKeys.has(id)) {
          run.failed(info.item, new vscode.TestMessage(err.message));
        }
      }
    }
    run.end();
  },
  true
);

  controller.createRunProfile(
    "debug",
    vscode.TestRunProfileKind.Debug,
    async (request: vscode.TestRunRequest, _token: vscode.CancellationToken) => {
      const root = workspaceRoot();
      if (!root) { return; }
      const run = controller.createTestRun(request);
      for (const item of request.include ?? []) {
        const info = testItems.get(item.id);
        if (!info) continue;
        const spec = Array.from(getSpecs()).find(s => s.className === info.spec);
        if (!spec) continue;
        const modulePath = findModuleForFile(spec.filePath);
        if (!modulePath) continue;

        try {
          const filter = info.feature ? buildTestFilter(info.spec, info.feature) : buildTestFilter(info.spec);
          await runGradleTests({ workspaceRoot: root, modulePath, testFilter: filter });
        } catch {}
      }
      run.end();
    },
    false
  );

  controller.refreshHandler = () => refresh();
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
