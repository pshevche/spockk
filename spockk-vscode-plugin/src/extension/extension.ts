import * as vscode from "vscode";
import { scanFile } from "./blockDetector";
import { SpockkSpec } from "./types";
import { createTestController } from "./testController";

let specsCache: SpockkSpec[] = [];

function getWorkspaceRoot(): string | undefined {
  return vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
}

export function activate(context: vscode.ExtensionContext) {
  console.log("Spockk VSCode plugin activated");

  createTestController(context, () => specsCache, getWorkspaceRoot);

  context.subscriptions.push(
    vscode.workspace.onDidSaveTextDocument((doc) => {
      if (doc.languageId !== "kotlin") return;
      const specs = scanFile(doc.uri.fsPath, doc.getText());
      specsCache = specsCache.filter(s => s.filePath !== doc.uri.fsPath);
      specsCache.push(...specs);
    })
  );

  const editor = vscode.window.activeTextEditor;
  if (editor && editor.document.languageId === "kotlin") {
    const specs = scanFile(editor.document.uri.fsPath, editor.document.getText());
    specsCache.push(...specs);
  }
}

export function deactivate() {}
