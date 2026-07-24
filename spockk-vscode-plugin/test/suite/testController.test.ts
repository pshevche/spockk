import * as assert from "assert";
import { mapTestResultsToItems } from "../../src/extension/testController";

describe("Test Controller", () => {
  it("maps XML results to test items by className + name", () => {
    const items = new Map<string, { spec: string; feature: string }>();
    items.set("com.example.MySpec.feature one", { spec: "com.example.MySpec", feature: "feature one" });
    items.set("com.example.MySpec.feature two", { spec: "com.example.MySpec", feature: "feature two" });

    const results = [
      { name: "feature one", className: "com.example.MySpec", passed: true, skipped: false, time: 0.1 },
      { name: "feature two", className: "com.example.MySpec", passed: false, skipped: false, time: 0.1, failureMessage: "failed" },
    ];

    const mapped = mapTestResultsToItems(results, items);
    assert.strictEqual(mapped.get("com.example.MySpec.feature one")?.passed, true);
    assert.strictEqual(mapped.get("com.example.MySpec.feature two")?.passed, false);
  });
});
