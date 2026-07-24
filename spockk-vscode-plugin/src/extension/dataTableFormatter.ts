import * as vscode from "vscode";

export function alignDataTable(content: string): string {
  const lines = content.split("\n");
  let inWhereBlock = false;
  const dataRows: number[] = [];
  const result = lines.slice();

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (/^\s*where\s*[{:]?\s*$/.test(line) || /^\s*where\s*\(.*\)\s*{/.test(line)) {
      inWhereBlock = true;
      dataRows.length = 0;
      continue;
    }
    if (!inWhereBlock) continue;

    if (
      /^\s*(given|when|then|expect|setup|cleanup|and)\s*[(:]/.test(line) ||
      (/^\s*}\s*$/.test(line) && dataRows.length > 0)
    ) {
      alignRows(result, dataRows);
      inWhereBlock = false;
      continue;
    }

    if (line.includes("|")) {
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
  const colCount = columns[0].length;
  for (let col = 0; col < colCount; col++) {
    maxColWidths[col] = Math.max(...columns.map(row => row[col]?.length ?? 0));
  }
  for (let i = 0; i < rows.length; i++) {
    const row = rows[i];
    const commentMatch = result[row].match(/(\/\/.*)$/);
    const comment = commentMatch?.[1] ?? "";
    const aligned = columns[i]
      .map((cell, col) => (col < colCount - 1 ? cell.padEnd(maxColWidths[col]) : cell))
      .join(" | ");
    result[row] = result[row].includes("//")
      ? `${aligned}  ${comment}`
      : aligned;
  }
}

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
