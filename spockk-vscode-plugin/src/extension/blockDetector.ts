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
  return detectSpecs(filePath, lines);
}

function collectImports(lines: string[]): string[] {
  const imports: string[] = [];
  for (const line of lines) {
    const match = line.match(/^import\s+(\S+)\s*$/);
    if (match) {
      imports.push(match[1]);
    }
  }
  return imports;
}

function hasSpockkImport(imports: string[]): boolean {
  for (const imp of imports) {
    if (imp === `${SPOCKK_PACKAGE}.*`) return true;
    for (const block of SPOCKK_BLOCKS) {
      if (imp === `${SPOCKK_PACKAGE}.${block}`) return true;
    }
  }
  return false;
}

interface ClassRange {
  name: string;
  startLine: number;
  endLine: number;
}

function detectSpecs(filePath: string, lines: string[]): SpockkSpec[] {
  const specs: SpockkSpec[] = [];
  const classes = detectTopLevelClasses(lines);

  for (const cls of classes) {
    const features = detectFeatures(lines, cls.startLine, cls.endLine);
    if (features.length > 0) {
      specs.push({
        className: cls.name,
        filePath,
        features,
      });
    }
  }

  return specs;
}

function detectTopLevelClasses(lines: string[]): ClassRange[] {
  const classes: ClassRange[] = [];
  const classRegex = /^(?:public|private|internal)?\s*(?:abstract\s+)?(?:open\s+)?class\s+(\w+)/;
  let currentClass: ClassRange | null = null;
  let braceDepth = 0;
  let inClass = false;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const classMatch = line.match(classRegex);
    if (classMatch && braceDepth === 0) {
      if (currentClass) {
        currentClass.endLine = i - 1;
        classes.push(currentClass);
      }
      currentClass = { name: classMatch[1], startLine: i, endLine: lines.length - 1 };
      inClass = true;
      const openBraces = (line.match(/\{/g) || []).length;
      const closeBraces = (line.match(/\}/g) || []).length;
      braceDepth += openBraces - closeBraces;
      continue;
    }

    if (inClass) {
      const openBraces = (line.match(/\{/g) || []).length;
      const closeBraces = (line.match(/\}/g) || []).length;
      braceDepth += openBraces - closeBraces;
      if (braceDepth <= 0) {
        if (currentClass) {
          currentClass.endLine = i;
          classes.push(currentClass);
          currentClass = null;
        }
        inClass = false;
        braceDepth = 0;
      }
    }
  }

  if (currentClass) {
    classes.push(currentClass);
  }

  return classes;
}

function detectFeatures(lines: string[], classStart: number, classEnd: number): SpockkFeature[] {
  const features: SpockkFeature[] = [];
  const funRegex = /^\s*(?:private|public|internal)?\s*(?:abstract\s+)?(?:open\s+)?(?:override\s+)?fun\s+`?(\w[\w\s]*)`?\s*\(/;

  for (let i = classStart; i <= classEnd; i++) {
    const line = lines[i];
    const funMatch = line.match(funRegex);
    if (!funMatch) continue;

    const featureName = funMatch[1].trim();
    const featureStart = i;
    const featureEnd = findMethodEnd(lines, i, classEnd);
    if (featureEnd < 0) continue;

    const blocks = detectBlocks(lines, featureStart, featureEnd);
    if (blocks.length > 0) {
      features.push({
        name: featureName,
        startLine: featureStart,
        startColumn: line.search(/\S/),
        blocks,
      });
    }
    i = featureEnd;
  }

  return features;
}

function findMethodEnd(lines: string[], start: number, maxEnd: number): number {
  let braceDepth = 0;
  let started = false;

  for (let i = start; i <= maxEnd; i++) {
    const line = lines[i];
    const openBraces = (line.match(/\{/g) || []).length;
    const closeBraces = (line.match(/\}/g) || []).length;

    if (!started && openBraces > 0) {
      started = true;
    }

    braceDepth += openBraces - closeBraces;

    if (started && braceDepth <= 0) {
      return i;
    }
  }

  return -1;
}

function detectBlocks(lines: string[], featureStart: number, featureEnd: number): SpockkBlock[] {
  const blocks: SpockkBlock[] = [];
  const blockRegex = /^\s*(given|setup|expect|when|then|and|where|cleanup)\s*(?:\{|\()/;

  for (let i = featureStart; i <= featureEnd; i++) {
    const line = lines[i];
    const match = line.match(blockRegex);
    if (!match) continue;

    const blockLabel = match[1];
    if (!SPOCKK_BLOCKS.has(blockLabel)) continue;

    const blockStart = i;
    const blockEnd = findBlockEnd(lines, i, featureEnd);
    if (blockEnd < 0) continue;

    blocks.push({
      label: blockLabel,
      startLine: blockStart,
      startColumn: line.search(/\S/),
      endLine: blockEnd,
      endColumn: lines[blockEnd].length,
    });

    i = blockEnd;
  }

  return blocks;
}

function findBlockEnd(lines: string[], start: number, maxEnd: number): number {
  let braceDepth = 0;
  let started = false;

  for (let i = start; i <= maxEnd; i++) {
    const line = lines[i];
    const openBraces = (line.match(/\{/g) || []).length;
    const closeBraces = (line.match(/\}/g) || []).length;

    if (!started && openBraces > 0) {
      started = true;
    }

    braceDepth += openBraces - closeBraces;

    if (started && braceDepth <= 0) {
      return i;
    }
  }

  return -1;
}
