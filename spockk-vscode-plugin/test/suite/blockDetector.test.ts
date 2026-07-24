import * as assert from "assert";
import { scanFile } from "../../src/extension/blockDetector";

describe("Block Detector", () => {
  it("detects Spockk imports via wildcard", () => {
    const content = `package com.example
import io.github.pshevche.spockk.lang.*

class MySpec {
  fun feature() {
    expect { 1 == 1 }
  }
}`;
    const result = scanFile("file.kt", content);
    assert.strictEqual(result.length, 1);
    assert.strictEqual(result[0].className, "MySpec");
    assert.strictEqual(result[0].features.length, 1);
    assert.strictEqual(result[0].features[0].name, "feature");
  });

  it("detects individual block imports", () => {
    const content = `import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.where

class MySpec {
  fun test1() {
    expect { 1 == 1 }
  }
  fun test2() {
    expect { 2 == 2 }
    where {
      a | b
      1 | 2
    }
  }
}`;
    const result = scanFile("f.kt", content);
    assert.strictEqual(result.length, 1);
    assert.strictEqual(result[0].features.length, 2);
  });

  it("ignores non-Spockk blocks with same name", () => {
    const content = `class MySpec {
  fun test() {
    val given = "hello"
    println(given)
  }
}`;
    const result = scanFile("f.kt", content);
    assert.strictEqual(result.length, 0);
  });

  it("handles multiple spec classes in one file", () => {
    const content = `import io.github.pshevche.spockk.lang.*
class SpecA {
  fun featureA() { expect { 1 == 1 } }
}
class SpecB {
  fun featureB() { expect { 2 == 2 } }
}`;
    const result = scanFile("f.kt", content);
    assert.strictEqual(result.length, 2);
  });
});
