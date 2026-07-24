import * as assert from "assert";
import { parseJunitXml } from "../../src/extension/junitXmlParser";

describe("JUnit XML Parser", () => {
  it("parses test results from XML", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.MySpec" tests="2" failures="1" time="0.2">
  <testcase name="feature one" classname="com.example.MySpec" time="0.1"/>
  <testcase name="feature two" classname="com.example.MySpec" time="0.1">
    <failure message="expected: 42 but was: 43" type="AssertionError">
      java.lang.AssertionError: expected: 42 but was: 43
        at com.example.MySpec.feature two(MySpec.kt:10)
    </failure>
  </testcase>
</testsuite>`;
    const results = parseJunitXml(xml);
    assert.strictEqual(results.length, 2);
    assert.strictEqual(results[0].name, "feature one");
    assert.strictEqual(results[0].passed, true);
    assert.strictEqual(results[1].name, "feature two");
    assert.strictEqual(results[1].passed, false);
    assert.ok(results[1].failureMessage?.includes("42 but was 43"));
  });

  it("parses empty XML", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.MySpec" tests="0" failures="0" time="0.0"/>`;
    const results = parseJunitXml(xml);
    assert.strictEqual(results.length, 0);
  });

  it("handles skipped testcase", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.MySpec" tests="1" failures="0" time="0.1">
  <testcase name="skipped feature" classname="com.example.MySpec" time="0.0">
    <skipped message="pending"/>
  </testcase>
</testsuite>`;
    const results = parseJunitXml(xml);
    assert.strictEqual(results.length, 1);
    assert.strictEqual(results[0].name, "skipped feature");
    assert.strictEqual(results[0].passed, false);
    assert.strictEqual(results[0].skipped, true);
  });
});
