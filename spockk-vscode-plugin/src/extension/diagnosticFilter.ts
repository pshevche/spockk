import * as vscode from "vscode";
import { SpockkSpec } from "./types";

export function registerDiagnosticFilter(
  context: vscode.ExtensionContext,
  getSpecs: () => SpockkSpec[]
): void {
  context.subscriptions.push(
    vscode.languages.onDidChangeDiagnostics((e) => {
      for (const uri of e.uris) {
        if (!uri.fsPath.endsWith(".kt")) continue;
        const isSpecFile = getSpecs().some(s => s.filePath === uri.fsPath);
        if (!isSpecFile) continue;

        const diagnostics = vscode.languages.getDiagnostics(uri);
        for (const d of diagnostics) {
          const code = typeof d.code === "string" ? d.code : String(d.code);
          if (code === "UnusedExpression" || code === "UnreachableCode") {
            console.log(
              `[Spockk] Detected diagnostic ${code} at ${uri.fsPath}:${d.range.start.line}`
            );
          }
        }
      }
    })
  );

  context.subscriptions.push(
    vscode.languages.registerCodeActionsProvider(
      "kotlin",
      new SpockkSuppressionCodeActionProvider(getSpecs),
      { providedCodeActionKinds: [vscode.CodeActionKind.QuickFix] }
    )
  );
}

class SpockkSuppressionCodeActionProvider
  implements vscode.CodeActionProvider
{
  constructor(private getSpecs: () => SpockkSpec[]) {}

  provideCodeActions(
    document: vscode.TextDocument,
    range: vscode.Range
  ): vscode.CodeAction[] {
    const isSpecFile = this.getSpecs().some(
      s => s.filePath === document.uri.fsPath
    );
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
