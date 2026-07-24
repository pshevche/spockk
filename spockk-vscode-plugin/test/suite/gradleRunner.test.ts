import * as assert from "assert";
import {
  findModuleForFile,
  buildTestFilter,
} from "../../src/extension/gradleRunner";

describe("Gradle Runner", () => {
  it("finds module by walking up to build.gradle.kts", () => {
    const module = findModuleForFile(
      "/project/spockk-core/src/test/kotlin/FooTest.kt",
      ["/project/spockk-core/build.gradle.kts", "/project/build.gradle.kts"]
    );
    assert.strictEqual(module, ":spockk-core");
  });

  it("resolves root module when no parent build file", () => {
    const module = findModuleForFile(
      "/project/submodule/src/test/kotlin/FooTest.kt",
      ["/project/submodule/build.gradle.kts"]
    );
    assert.strictEqual(module, ":submodule");
  });

  it("builds test filter for single feature", () => {
    const filter = buildTestFilter("com.example.MySpec", "my feature");
    assert.strictEqual(filter, '--tests "com.example.MySpec.my feature"');
  });

  it("builds test filter for full spec", () => {
    const filter = buildTestFilter("com.example.MySpec");
    assert.strictEqual(filter, '--tests "com.example.MySpec"');
  });
});
