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
    name: `Debug Spockk