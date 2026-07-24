import * as assert from "assert";
import { buildDebugConfig } from "../../src/extension/debugAdapter";

describe("Debug Adapter", () => {
  it("builds debug configuration for a feature", () => {
    const config = buildDebugConfig("com.example.MySpec", "my feature");
    assert.strictEqual(config.name, "Debug Spockk: com.example.MySpec.my feature");
    assert.strictEqual(config.request, "attach");
    assert.strictEqual(config.type, "java");
    assert.strictEqual(config.port, 5005);
  });

  it("builds debug configuration for a spec", () => {
    const config = buildDebugConfig("com.example.MySpec");
    assert.strictEqual(config.name, "Debug Spockk: com.example.MySpec");
  });
});
