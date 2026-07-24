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
