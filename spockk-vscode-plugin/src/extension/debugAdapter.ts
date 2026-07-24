import * as vscode from "vscode";

export interface DebugConfig {
  name: string;
  type: string;
  request: string;
  hostName: string;
  port: number;
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
