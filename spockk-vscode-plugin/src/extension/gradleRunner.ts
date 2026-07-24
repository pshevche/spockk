import * as path from "path";
import * as fs from "fs";
import { spawn } from "child_process";

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
      const parent = path.dirname(dir);
      const parentBuild = path.join(parent, "build.gradle.kts");
      const parentHasBuild = existingBuildFiles
        ? existingBuildFiles.includes(parentBuild)
        : fs.existsSync(parentBuild);
      if (parentHasBuild && parent !== dir) {
        return `:${path.basename(parent)}`;
      }
      return `:${path.basename(dir)}`;
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
      const moduleDir = options.modulePath.replace(/^:/, "").replace(/:/g, "/");
      const testXmlDir = path.join(options.workspaceRoot, moduleDir, "build", "test-results", "test");
      resolve({ exitCode: code ?? -1, testXmlDir });
    });
    proc.on("error", reject);
  });
}
