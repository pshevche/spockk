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
  const results: TestResult[] = [];
  const testcaseRegex = /<testcase\s+[^>]*\/?>[\s\S]*?(?:<\/testcase>)?/g;
  const nameRegex = /name="([^"]*)"/;
  const classnameRegex = /classname="([^"]*)"/;
  const timeRegex = /time="([^"]*)"/;

  let match: RegExpExecArray | null;
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
      ? /<failure[^>]*>([\s\S]*?)<\/failure>/.exec(block)
      : undefined;

    results.push({
      name,
      className,
      passed: !hasFailure && !isSkipped,
      skipped: isSkipped,
      time,
      failureMessage: failureMsg,
      stackTrace: stackMatch?.[1]?.trim(),
    });
  }
  return results;
}
