import * as assert from "assert";
import { alignDataTable } from "../../src/extension/dataTableFormatter";

describe("Data Table Formatter", () => {
  it("aligns pipe-separated columns in a where block", () => {
    const input = `expect: Math.min(a, b) == c
where:
a | b | c
1 | 2 | 1
100 | 200 | 100`;
    const expected = `expect: Math.min(a, b) == c
where:
a   | b   | c
1   | 2   | 1
100 | 200 | 100`;
    assert.strictEqual(alignDataTable(input), expected);
  });

  it("ignores non-where blocks", () => {
    const input = `given:
def x = 1
def y = 2`;
    assert.strictEqual(alignDataTable(input), input);
  });
});
