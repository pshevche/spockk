# Spock Test Coverage Tracking Implementation Plan

**Goal:** Build the machinery that measures how much of Spock's `spock-specs` suite Spockk reproduces, tracks the
remaining work as GitHub issues an agent can be handed one at a time, and keeps both in sync with upstream
automatically.

**Architecture:** Four components, three of them pure functions and one that talks to GitHub. An *inventory
generator* parses upstream `spock-specs` into a checked-in manifest. A *coverage scanner* reads `@MigratedFrom`
annotations out of `spockk-specs` and reports which upstream features are covered. A *reconciler* takes both plus
an exclusions ledger and renders a three-tier issue hierarchy. A *weekly workflow* runs the lot. Coverage is
derived from the repo, never stored in issue bodies, so the reconciler can regenerate any issue body at will.

**Tech Stack:** Python 3.11+ (standard library only), Kotlin (one annotation), GitHub Actions, GitHub REST API.

**Spec:** `_docs/specs/2026-09-15-spock-test-migration-design.md`. Read it in full before starting. This plan
implements it and does not restate its reasoning.

---

## Global Constraints

- **Python 3.11 or newer, standard library only.** No `pip install`, no `requirements.txt`. Uses `json`, `re`,
  `pathlib`, `hashlib`, `tomllib`, `urllib.request`, `argparse`, `unittest`. Rationale: CI gains no new toolchain,
  and Renovate gains no new dependency tree to chase. `tomllib` is read-only and stdlib from 3.11, which is why
  config is TOML (see the deviations section).
- **Tooling lives in `.github/test-coverage/`. Data lives in `_docs/test-coverage/`.** Per spec section 15.
- **Issue labels are `test-coverage::*`**: `dashboard`, `area`, `spec`, `gap`, `blocked`, `drift`.
- **Feature key format is `<fully-qualified-class>#<feature name>`**, verbatim from the upstream feature string.
- **Apache 2.0 license header on every Kotlin source file**, from `gradle/config/licenseHeader.txt`. Spotless
  enforces this on `src/{main,test,testFixtures}/kotlin/**`. It does not cover Python; do not add headers there.
- **Kotlin style is 2-space indent, ktlint via Spotless.** Run `./gradlew spotlessApply` before committing Kotlin.
- **Prose rule: no em dashes** in any file this plan creates, including skill files and issue templates.
- **Never edit `.github/workflows/verify.yml`.** `CLAUDE.md` lists it off-limits. New workflows are new files.
- **Conventional Commits** on every commit.

## Deviations from the spec, and why

Two places where the spec's letter cannot be followed as written. Both are deliberate and should be reflected back
into the spec if approved.

1. **Config and exclusions are TOML, not YAML.** The spec names `config.yml` and `exclusions.yml`. Python's
   standard library has no YAML parser, so YAML would mean a PyYAML dependency and a `pip install` step in every
   job. `tomllib` is stdlib from 3.11 and TOML keeps the comments and readability the spec wanted from YAML. Files
   become `.github/test-coverage/config.toml` and `_docs/test-coverage/exclusions.toml`.
2. **The PR-time scanner check is its own workflow**, `.github/workflows/test-coverage.yml`, rather than a job
   added to `verify.yml`. `verify.yml` is off-limits per `CLAUDE.md`, and the check has no reason to share a
   runner with the Gradle build.

## Phase map

Each phase leaves the repo in a working, reviewable state and can merge on its own.

| Phase | Tasks | Deliverable | Needs a token |
|---|---|---|---|
| 1. Inventory | 1 to 4 | Checked-in manifest of upstream, reproducible | no |
| 2. Traceability | 5 to 8 | `@MigratedFrom`, scanner, CI check | no |
| 3. Skills | 9 to 11 | Agent instructions, validated on real ports | no |
| 4. Reconciler | 12 to 15 | Issue hierarchy, dry-run first | yes |
| 5. Automation | 16 to 17 | Weekly sync | yes |

Phases 1 to 3 need no credentials at all. The token question only arrives at phase 4, which is deliberate: the
measurement half is provable before anything is allowed to write to the issue tracker.

---

## Phase 1: Inventory

### Task 1: Scaffold the tooling directory and its test harness

**Files:**
- Create: `.github/test-coverage/README.md`
- Create: `.github/test-coverage/config.toml`
- Create: `.github/test-coverage/tests/__init__.py`
- Create: `.github/test-coverage/tests/fixtures/sample_spec.groovy`

**Interfaces:**
- Produces: the `config.toml` schema every later task reads, and the `python3 -m unittest discover` invocation
  that every later task's tests run under.

- [ ] **Step 1:** Write `config.toml`. Scope is data, not code, so re-scoping is a reviewable diff:

```toml
[upstream]
repo = "spockframework/spock"
ref = "master"
module = "spock-specs"
source_root = "spock-specs/src/test/groovy"

[guard]
# Fail the run if the parsed class count moves by more than this fraction
# between manifest regenerations. Catches a parser regression masquerading
# as upstream churn.
class_count_tolerance = 0.10

# Package prefixes excluded from scope, each with a written reason.
# An excluded package produces no issues but still appears in the dashboard
# footer, so the decision stays visible.
[[exclude]]
prefix = "org.spockframework.docs"
reason = "Documentation samples for Spock's own manual, not behavior tests"

[[exclude]]
prefix = "org.spockframework.util"
reason = "Unit tests for Java utility classes with no Spockk equivalent"

[[exclude]]
prefix = "org.spockframework.groovy"
reason = "Groovy language semantics with no Kotlin analogue"

[[exclude]]
prefix = "spock.util.mop"
reason = "Groovy metaprogramming with no Kotlin analogue"

[[exclude]]
prefix = "org.spockframework.buildsupport"
reason = "Spock-internal build infrastructure"

[[exclude]]
prefix = "org.spockframework.idea"
reason = "Spock-internal IDE support"

[[exclude]]
prefix = "org.spockframework.serialization"
reason = "Spock-internal infrastructure"

[[exclude]]
prefix = "org.spockframework.builder"
reason = "Spock-internal infrastructure"

[[exclude]]
prefix = "org.spockframework.example"
reason = "Spock-internal infrastructure"

# Package prefix to area name, longest prefix wins. Middle tier of the
# issue hierarchy. Note spock.util.concurrent is deliberately IN scope;
# see spec section 6 "Deliberately in scope".
[area]
"org.spockframework.smoke.condition" = "conditions"
"org.spockframework.smoke.mock" = "mocking"
"org.spockframework.smoke.extension" = "extensions"
"org.spockframework.smoke.parameterization" = "parameterization"
"org.spockframework.smoke.ast" = "ast"
"org.spockframework.smoke.traits" = "traits"
"org.spockframework.smoke" = "smoke-core"
"org.spockframework.runtime" = "runtime"
"org.spockframework.mock" = "mocking"
"org.spockframework.datapipes" = "parameterization"
"org.spockframework.verifyall" = "conditions"
"spock.util.concurrent" = "util-api"
"spock.util" = "util-api"
"spock.mock" = "mocking"
"spock.timeout" = "extensions"
"spock.config" = "config"

[issues]
# Split a class into numbered part-issues above this many features.
split_threshold = 20
# Split an area into numbered sub-areas above this many child issues.
# GitHub caps sub-issues at 100 per parent; 80 leaves headroom.
area_split_threshold = 80
```

- [ ] **Step 2:** Write `README.md` covering: stdlib-only rule, how to run the tests
  (`cd .github/test-coverage && python3 -m unittest discover -s tests -v`), and a one-line description of each
  script. Keep it under 40 lines. It exists so the next person does not have to infer the constraints.

- [ ] **Step 3:** Write `tests/fixtures/sample_spec.groovy`, a hand-built Groovy file exercising every parser case
  the real tree contains. This fixture is the contract for Tasks 2 and 3, so make it comprehensive now:

```groovy
package org.spockframework.smoke.condition

import org.spockframework.EmbeddedSpecification
import spock.lang.Snapshot

class SimpleConditions extends Specification {
  def "plain feature"() {
    expect: 1 == 1
  }

  def 'single quoted feature'() {
    expect: true
  }

  def "feature with #placeholder and, punctuation"() {
    expect: x
    where: x << [true]
  }

  // not a feature: helper method
  private def helper() { 42 }
}

abstract class AbstractBase extends Specification {
  def "abstract classes are skipped entirely"() {
    expect: true
  }
}

class EmbeddedConditions extends EmbeddedSpecification {
  def "runs an embedded spec"() {
    when:
    def result = runner.runSpecBody("...")
    then:
    result.totalFailureCount == 0
  }
}
```

- [ ] **Step 4:** Create `tests/helpers.py` holding the fixtures every later test module imports. Defining them
  once here is what keeps Tasks 4, 8 and 13 consistent with each other:

```python
from pathlib import Path

FIXTURE_TREE = Path(__file__).parent / "fixtures"
MANIFEST = {"classes": [{"key": "org.spockframework.smoke.A",
                         "features": [{"name": "one", "hash": "aaaa1111"}]}]}
KEY = "org.spockframework.smoke.A#one"
FEATURE = "one"
CLASS = MANIFEST["classes"][0]

def ported(source="PortedTest.kt:10"):
    from scanner import Coverage
    return Coverage(status="ported", gap=None, source=source)

def pending(gap, source="PortedTest.kt:20"):
    from scanner import Coverage
    return Coverage(status="pending", gap=gap, source=source)

def na_exclusion(reason="Kotlin has no GString"):
    from exclusions import Exclusion
    return Exclusion(status="not-applicable", reason=reason, gap=None, decided="2026-09-19")

def class_with(feature_count):
    return {"key": "org.spockframework.smoke.Big",
            "features": [{"name": f"f{i}", "hash": f"{i:08x}"} for i in range(feature_count)]}
```

Tasks 4, 6, 7 and 13 define the `Coverage`, `Exclusion` and manifest shapes these helpers reference, so the
imports stay unresolved until those tasks land. That is expected: each task's tests fail until its own
implementation exists.

- [ ] **Step 5:** Create the empty `tests/__init__.py`. Verify the harness runs and finds zero tests:

```bash
cd .github/test-coverage && python3 -m unittest discover -s tests -v
```
Expected: `Ran 0 tests`, exit 0.

- [ ] **Step 6:** Commit.

```bash
git add .github/test-coverage
git commit -m "chore: scaffold test-coverage tooling directory"
```

### Task 2: Parse upstream specs into classes and features

**Files:**
- Create: `.github/test-coverage/parser.py`
- Create: `.github/test-coverage/tests/test_parser.py`

**Interfaces:**
- Consumes: `tests/fixtures/sample_spec.groovy` from Task 1.
- Produces: `parse_file(path: Path, source_root: Path) -> list[SpecClass]` where
  `SpecClass = dataclass(key: str, path: str, name: str, base: str, features: list[Feature])` and
  `Feature = dataclass(name: str, hash: str)`. Task 3 consumes `SpecClass`; Task 4 serialises it.

- [ ] **Step 1: Write the failing tests.** Cover exactly the cases the fixture contains, because these are the
  ones the real tree contains:

```python
import unittest
from pathlib import Path
from parser import parse_file

FIXTURE = Path(__file__).parent / "fixtures" / "sample_spec.groovy"
ROOT = Path(__file__).parent / "fixtures"

class ParserTest(unittest.TestCase):
    def setUp(self):
        self.classes = {c.name: c for c in parse_file(FIXTURE, ROOT)}

    def test_skips_abstract_classes(self):
        self.assertNotIn("AbstractBase", self.classes)

    def test_finds_concrete_spec_classes(self):
        self.assertEqual({"SimpleConditions", "EmbeddedConditions"}, set(self.classes))

    def test_key_is_package_qualified(self):
        self.assertEqual(
            "org.spockframework.smoke.condition.SimpleConditions",
            self.classes["SimpleConditions"].key,
        )

    def test_records_base_class(self):
        self.assertEqual("EmbeddedSpecification", self.classes["EmbeddedConditions"].base)

    def test_extracts_feature_names_verbatim(self):
        names = [f.name for f in self.classes["SimpleConditions"].features]
        self.assertEqual(
            ["plain feature", "single quoted feature",
             "feature with #placeholder and, punctuation"],
            names,
        )

    def test_ignores_non_feature_methods(self):
        names = [f.name for f in self.classes["SimpleConditions"].features]
        self.assertNotIn("helper", names)

    def test_hash_is_stable_and_ignores_formatting(self):
        from parser import body_hash
        a = body_hash("expect:\n  1 == 1  // trailing comment")
        b = body_hash("expect:\n        1 == 1")
        self.assertEqual(a, b)

    def test_hash_changes_when_meaning_changes(self):
        from parser import body_hash
        self.assertNotEqual(body_hash("expect: 1 == 1"), body_hash("expect: 1 == 2"))
```

- [ ] **Step 2: Run the tests, confirm they fail.**

```bash
cd .github/test-coverage && python3 -m unittest discover -s tests -v
```
Expected: FAIL with `ModuleNotFoundError: No module named 'parser'`.

- [ ] **Step 3: Implement `parser.py`.** Regex and line-based, per spec section 5.3, deliberately not a Groovy
  parser. Required behaviours:
  - `PACKAGE_RE = re.compile(r'^package\s+([\w.]+)', re.M)`.
  - Class detection at column 0 only:
    `r'^(?P<abstract>abstract\s+)?class\s+(?P<name>[A-Za-z0-9_]+)\b[^{]*?\bextends\s+(?P<base>[A-Za-z0-9_.]+)'`
    with `re.M`. Skip any match whose `abstract` group is set.
  - Feature detection inside a class body:
    `r'^\s+(?:def|void)\s+(?P<q>["\'])(?P<name>.+?)(?P=q)\s*\('` with `re.M`. The quoted-string requirement is
    what separates features from helper methods; do not relax it.
  - A class body runs from its own match end to the next class match start, or end of file.
  - `body_hash(text)`: strip `//` line comments and `/* */` blocks, collapse all whitespace runs to one space,
    strip, then `hashlib.sha256(...).hexdigest()[:8]`.
  - A class with zero features is dropped: it is a helper, not a spec.
  - `path` is stored relative to the upstream repo root, not `source_root`, so manifest paths link correctly to
    GitHub.

- [ ] **Step 4: Run the tests, confirm they pass.**

```bash
cd .github/test-coverage && python3 -m unittest discover -s tests -v
```
Expected: 8 passed.

- [ ] **Step 5: Commit.**

```bash
git add .github/test-coverage
git commit -m "feat: parse upstream spock-specs into classes and features"
```

### Task 3: Classify recipe, area and scope

**Files:**
- Create: `.github/test-coverage/classify.py`
- Create: `.github/test-coverage/tests/test_classify.py`

**Interfaces:**
- Consumes: `SpecClass` from Task 2, `config.toml` from Task 1.
- Produces: `classify_source(source: str, base: str) -> str` returning one of the five recipe names, plus the
  convenience wrapper `classify(spec_class, source_text) -> str` that unpacks `spec_class.base` and calls it.
  `classify_source` takes raw strings rather than a `SpecClass` so recipe detection is testable without building
  one. Also `area_for(class_key, config) -> str | None`; `is_excluded(class_key, config) -> tuple[bool, str]`
  returning the reason alongside the verdict; and `load_config(path=None) -> dict` reading `config.toml`.

- [ ] **Step 1: Write the failing tests.** The recipe order matters, so test it explicitly:

```python
class ClassifyTest(unittest.TestCase):
    def test_ast_snapshot_wins_over_engine_runtime(self):
        src = "@Snapshot SpockSnapshotter snapshotter\ncompiler.transpileSpecBody('x')\nrunner.run()"
        self.assertEqual("ast-snapshot", classify_source(src, base="EmbeddedSpecification"))

    def test_condition_rendering_detected_by_base(self):
        self.assertEqual("condition-rendering",
                         classify_source("", base="ConditionRenderingSpec"))

    def test_compile_error_needs_both_markers(self):
        src = "compiler.compile('x')\nthrown(InvalidSpecCompileException)"
        self.assertEqual("compile-error", classify_source(src, base="EmbeddedSpecification"))

    def test_compile_error_marker_alone_is_engine_runtime(self):
        self.assertEqual("engine-runtime",
                         classify_source("compiler.compile('x')", base="EmbeddedSpecification"))

    def test_plain_specification_is_smoke(self):
        self.assertEqual("smoke", classify_source("expect: true", base="Specification"))

    def test_longest_area_prefix_wins(self):
        cfg = load_config()
        self.assertEqual("conditions",
                         area_for("org.spockframework.smoke.condition.Foo", cfg))
        self.assertEqual("smoke-core", area_for("org.spockframework.smoke.Foo", cfg))

    def test_util_concurrent_is_in_scope(self):
        cfg = load_config()
        excluded, _ = is_excluded("spock.util.concurrent.BlockingVariableSpec", cfg)
        self.assertFalse(excluded)

    def test_exclusion_carries_a_reason(self):
        cfg = load_config()
        excluded, reason = is_excluded("org.spockframework.util.TextUtilSpec", cfg)
        self.assertTrue(excluded)
        self.assertIn("utility classes", reason)
```

- [ ] **Step 2: Run the tests, confirm they fail.** Expected: `ModuleNotFoundError: No module named 'classify'`.

- [ ] **Step 3: Implement `classify.py`.** Recipe detection, applied strictly in this order because later markers
  co-occur with earlier ones:
  1. `ast-snapshot` when (`SpockSnapshotter` or `@Snapshot`) **and** `transpile` are present.
  2. `condition-rendering` when the base class is `ConditionRenderingSpec`.
  3. `compile-error` when `compiler.compile` **and** one of `InvalidSpecCompileException`,
     `CompilationFailedException`, `MultipleCompilationErrorsException` are present.
  4. `engine-runtime` when `runner.run` is present or the base is `EmbeddedSpecification`.
  5. `smoke` otherwise.

  `area_for` matches the longest configured prefix. `is_excluded` returns the configured reason so the dashboard
  footer can print it. Add `load_config()` using `tomllib.load` in binary mode.

- [ ] **Step 4: Run the tests, confirm they pass.** Expected: 8 passed.

- [ ] **Step 5: Commit.**

```bash
git add .github/test-coverage
git commit -m "feat: classify upstream specs by recipe, area and scope"
```

### Task 4: Generate the first manifest

**Files:**
- Create: `.github/test-coverage/inventory.py`
- Create: `_docs/test-coverage/spock-inventory.json`
- Create: `.github/test-coverage/tests/test_inventory.py`

**Interfaces:**
- Consumes: `parser.py`, `classify.py`, `config.toml`.
- Produces: the manifest file at the schema in spec section 5.2, and
  `main(argv) -> int` accepting `--upstream <path>`, `--out <path>`, `--check`.

- [ ] **Step 1: Write the failing tests** for manifest shape and the count guard:

```python
class InventoryTest(unittest.TestCase):
    def test_manifest_has_upstream_provenance(self):
        m = build_manifest(FIXTURE_TREE, sha="abc123", config=load_config())
        self.assertEqual("spockframework/spock", m["upstream"]["repo"])
        self.assertEqual("abc123", m["upstream"]["sha"])
        self.assertIn("generated_at", m["upstream"])

    def test_classes_sorted_by_key_for_stable_diffs(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        keys = [c["key"] for c in m["classes"]]
        self.assertEqual(sorted(keys), keys)

    def test_excluded_classes_are_absent_but_counted(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        self.assertNotIn("org.spockframework.util.TextUtilSpec",
                         [c["key"] for c in m["classes"]])
        self.assertGreater(m["excluded"]["classes"], 0)

    def test_guard_rejects_a_large_count_drop(self):
        old = {"classes": [{"key": f"K{i}", "features": []} for i in range(100)]}
        new = {"classes": [{"key": f"K{i}", "features": []} for i in range(50)]}
        with self.assertRaises(GuardTripped):
            check_guard(old, new, tolerance=0.10)

    def test_guard_allows_normal_churn(self):
        old = {"classes": [{"key": f"K{i}", "features": []} for i in range(100)]}
        new = {"classes": [{"key": f"K{i}", "features": []} for i in range(103)]}
        check_guard(old, new, tolerance=0.10)  # must not raise
```

- [ ] **Step 2: Run the tests, confirm they fail.**

- [ ] **Step 3: Implement `inventory.py`.** Walk `source_root` for `*.groovy`, parse, classify, drop excluded,
  sort by key, and emit the spec section 5.2 schema plus an `excluded` summary block for the dashboard footer.
  `--check` regenerates and diffs against the existing file without writing, exiting non-zero on drift, which is
  what the PR workflow will call. Sort features in source order, not alphabetically: part-issue numbering in Task
  13 depends on source order being stable.

- [ ] **Step 4: Run the tests, confirm they pass.**

- [ ] **Step 5: Generate the real manifest** against a real upstream clone:

```bash
git clone --depth 1 --filter=blob:none --sparse https://github.com/spockframework/spock.git /tmp/spock
cd /tmp/spock && git sparse-checkout set spock-specs && git rev-parse HEAD
cd - && python3 .github/test-coverage/inventory.py \
  --upstream /tmp/spock --out _docs/test-coverage/spock-inventory.json
```

- [ ] **Step 6: Verify the numbers against the spec.** The manifest must report **365 in-scope classes and 2,351
  features**, with 82 excluded classes. These are the figures in spec section 1, measured from the same tree. A
  mismatch means either the parser is wrong or upstream moved. Investigate before proceeding: every later phase
  trusts these counts. If upstream genuinely moved, update spec section 1 in the same commit and say so.

- [ ] **Step 7: Commit.**

```bash
git add .github/test-coverage _docs/test-coverage
git commit -m "feat: generate the upstream spock-specs inventory manifest"
```

---

## Phase 2: Traceability

### Task 5: The `@MigratedFrom` annotation

**Files:**
- Create: `spockk-specs/src/testFixtures/kotlin/io/github/pshevche/spockk/fixtures/coverage/MigratedFrom.kt`

**Interfaces:**
- Produces: `@MigratedFrom(vararg keys: String)`, consumed by the scanner in Task 6 and by every future
  porting ticket.

- [ ] **Step 1: Write the annotation.** Apache header first, per the Global Constraints:

```kotlin
package io.github.pshevche.spockk.fixtures.coverage

/**
 * Links a test to the upstream Spock feature it covers, as `<fqcn>#<feature name>`.
 *
 * Several keys are allowed in both directions: one test may cover several upstream
 * features, and one upstream feature may need several tests.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MigratedFrom(vararg val keys: String)
```

- [ ] **Step 2: Format and compile.**

```bash
./gradlew spotlessApply
./gradlew :spockk-specs:compileTestFixturesKotlin --rerun
```
Expected: BUILD SUCCESSFUL. The `--rerun` is the caveat documented in `CLAUDE.md`; without it this task can
report UP-TO-DATE and hide a compile error.

- [ ] **Step 3: Commit.**

```bash
git add spockk-specs
git commit -m "feat: add @MigratedFrom annotation for coverage traceability"
```

### Task 6: The coverage scanner

**Files:**
- Create: `.github/test-coverage/scanner.py`
- Create: `.github/test-coverage/tests/test_scanner.py`
- Create: `.github/test-coverage/tests/fixtures/PortedTest.kt`

**Interfaces:**
- Consumes: Kotlin sources under `spockk-specs/src/test` and `src/testFixtures`.
- Produces: `scan(roots: list[Path]) -> dict[str, Coverage]` where
  `Coverage = dataclass(status: Literal["ported","pending"], gap: int | None, source: str)`.

- [ ] **Step 1: Write the fixture** covering plain, multi-key and pending cases:

```kotlin
package io.github.pshevche.spockk.smoke.sample

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import spock.lang.PendingFeature

class PortedTest : Specification() {
  @MigratedFrom("org.spockframework.smoke.A#one")
  fun `covers one feature`() {}

  @MigratedFrom("org.spockframework.smoke.A#two", "org.spockframework.smoke.A#three")
  fun `covers two features`() {}

  @PendingFeature(reason = "no range pipes yet, see #412")
  @MigratedFrom("org.spockframework.smoke.B#blocked")
  fun `is pending`() {}
}
```

- [ ] **Step 2: Write the failing tests:**

```python
class ScannerTest(unittest.TestCase):
    def setUp(self):
        self.cov = scan([Path(__file__).parent / "fixtures"])

    def test_single_key_is_ported(self):
        self.assertEqual("ported", self.cov["org.spockframework.smoke.A#one"].status)

    def test_vararg_keys_all_recorded(self):
        self.assertIn("org.spockframework.smoke.A#two", self.cov)
        self.assertIn("org.spockframework.smoke.A#three", self.cov)

    def test_pending_feature_marks_pending_and_extracts_gap(self):
        c = self.cov["org.spockframework.smoke.B#blocked"]
        self.assertEqual("pending", c.status)
        self.assertEqual(412, c.gap)

    def test_records_source_location_for_error_messages(self):
        self.assertIn("PortedTest.kt", self.cov["org.spockframework.smoke.A#one"].source)
```

- [ ] **Step 3: Run the tests, confirm they fail.**

- [ ] **Step 4: Implement `scanner.py`.** Regex over `*.kt`:
  - `@MigratedFrom(...)` with a string-literal list; parse every double-quoted literal inside the parentheses.
  - A `@PendingFeature` annotation attached to the **same declaration**, meaning within the annotation block
    immediately preceding the same `fun`. Extract the first `#(\d+)` from its `reason` as the gap number.
  - Record the file path and line for every key so Task 8 can emit an actionable error.
  - A key claimed twice is not an error here; the scanner takes the union, per spec section 10.1.

- [ ] **Step 5: Run the tests, confirm they pass.**

- [ ] **Step 6: Commit.**

```bash
git add .github/test-coverage
git commit -m "feat: scan spockk-specs for @MigratedFrom coverage"
```

### Task 7: The exclusions ledger

**Files:**
- Create: `_docs/test-coverage/exclusions.toml`
- Create: `.github/test-coverage/exclusions.py`
- Create: `.github/test-coverage/tests/test_exclusions.py`

**Interfaces:**
- Produces: `load_exclusions(path) -> dict[str, Exclusion]` where
  `Exclusion = dataclass(status: Literal["not-applicable","blocked"], reason: str, gap: int | None, decided: str)`.

- [ ] **Step 1: Write the ledger** with its format documented in comments and one worked example of each status:

```toml
# Deliberate decisions not to port an upstream feature, and features that
# cannot be ported because they do not compile under Spockk.
#
# status = "not-applicable"  behavior that cannot exist in Kotlin. Permanent.
#                            Requires a reason. Reviewed like documentation.
# status = "blocked"         will not compile under Spockk today, so no test
#                            can be committed at all. Requires a gap issue.
#                            Temporary: delete the entry when the gap closes.
#
# A feature that compiles but fails at runtime does NOT belong here. It is
# committed with @PendingFeature instead (spec section 9).

[["org.spockframework.smoke.condition.ConditionRendering#renders GString diff"]]
status = "not-applicable"
reason = "Kotlin has no GString; string templates compile to concatenation"
decided = "2026-09-19"
```

- [ ] **Step 2: Write the failing tests:** a `not-applicable` entry requires a non-empty `reason`, a `blocked`
  entry requires a `gap`, an unknown `status` raises, and a key appearing twice raises.

- [ ] **Step 3: Run the tests, confirm they fail.**

- [ ] **Step 4: Implement `exclusions.py`** with exactly those validations. Fail loudly: a malformed ledger that
  loads silently would drop coverage from the dashboard without anyone noticing.

- [ ] **Step 5: Run the tests, confirm they pass.**

- [ ] **Step 6: Commit.**

```bash
git add .github/test-coverage _docs/test-coverage
git commit -m "feat: add the coverage exclusions ledger and its loader"
```

### Task 8: The PR-time validation check

**Files:**
- Create: `.github/test-coverage/validate.py`
- Create: `.github/workflows/test-coverage.yml`
- Create: `.github/test-coverage/tests/test_validate.py`

**Interfaces:**
- Consumes: manifest, scanner output, exclusions ledger.
- Produces: `validate(manifest, coverage, exclusions, closed_gaps) -> list[Violation]`, exit code 1 when non-empty.

- [ ] **Step 1: Write the failing tests** for the three checks in spec section 10.3:

```python
class ValidateTest(unittest.TestCase):
    def test_unknown_key_is_a_violation(self):
        v = validate(MANIFEST, {"does.not.Exist#nope": ported()}, {}, closed_gaps=set())
        self.assertEqual(1, len(v))
        self.assertIn("not in the inventory", v[0].message)

    def test_key_both_ported_and_excluded_is_a_violation(self):
        key = "org.spockframework.smoke.A#one"
        v = validate(MANIFEST, {key: ported()}, {key: na_exclusion()}, closed_gaps=set())
        self.assertIn("both ported and marked not-applicable", v[0].message)

    def test_pending_referencing_a_closed_gap_is_a_violation(self):
        key = "org.spockframework.smoke.A#one"
        v = validate(MANIFEST, {key: pending(gap=412)}, {}, closed_gaps={412})
        self.assertIn("gap #412 is closed", v[0].message)

    def test_clean_state_produces_no_violations(self):
        key = "org.spockframework.smoke.A#one"
        self.assertEqual([], validate(MANIFEST, {key: ported()}, {}, closed_gaps=set()))

    def test_violation_names_the_source_file(self):
        v = validate(MANIFEST, {"does.not.Exist#nope": ported()}, {}, closed_gaps=set())
        self.assertIn(".kt", v[0].source)
```

- [ ] **Step 2: Run the tests, confirm they fail.**

- [ ] **Step 3: Implement `validate.py`.** Every violation names the offending key and its source file and line.
  `closed_gaps` comes from the GitHub API when a token is present and is an empty set otherwise, so the check
  degrades to the first two validations on a fork rather than failing.

- [ ] **Step 4: Write the workflow.** A new file, never an edit to `verify.yml`:

```yaml
name: Test coverage

on:
  pull_request:
    branches:
      - main
  push:
    branches:
      - main

permissions:
  contents: read
  issues: read

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v7

      - name: Run tooling unit tests
        working-directory: .github/test-coverage
        run: python3 -m unittest discover -s tests -v

      - name: Validate coverage annotations
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: python3 .github/test-coverage/validate.py
```

- [ ] **Step 5: Run the tests locally, confirm they pass**, then verify the whole check against the real repo:

```bash
cd .github/test-coverage && python3 -m unittest discover -s tests -v
cd - && python3 .github/test-coverage/validate.py
```
Expected: exit 0, since no test carries `@MigratedFrom` yet.

- [ ] **Step 6: Prove the check actually fails.** Temporarily annotate one existing test with a deliberately wrong
  key, re-run `validate.py`, and confirm it exits 1 naming that file. Then revert the annotation. A validation
  that has never been seen to fail is not known to work.

- [ ] **Step 7: Annotate one real test for real.** Pick a single existing `spockk-specs` test whose upstream
  counterpart you can identify with confidence, add the correct `@MigratedFrom`, and confirm `validate.py` still
  exits 0 and the scanner counts it. This is the phase-2 validation from spec section 16: one or two by hand, not
  a retrofit pass.

- [ ] **Step 8: Commit.**

```bash
./gradlew spotlessApply
git add .github spockk-specs
git commit -m "feat: validate coverage annotations on every PR"
```

---

## Phase 3: Skills

Phase 3 produces prose, so its tasks have no unit tests. The deliverable is validated by using it: Task 11 ports
two real classes and the skill is done when that port needed no information the skill did not contain.

### Task 9: The `spock-migration` router skill

**Files:**
- Create: `.claude/skills/spock-migration/SKILL.md`

- [ ] **Step 1: Write `SKILL.md`** with frontmatter `name` and `description` matching the style of the existing
  `.claude/skills/*/SKILL.md` files. It must cover, in this order, because it is the order an agent works in:
  1. **The existing-coverage search first.** Per spec section 9, before writing anything: search `spockk-specs`
     for a test already covering the feature. If found, move it and annotate. Point at
     `references/existing-coverage.md`.
  2. **Classify the upstream class** into one of the five recipes, and load only that recipe reference.
  3. **File placement**, from the recipe table in spec section 8.
  4. **The four outcomes** from spec section 9, and which artifact each produces.
  5. **The verification loop**: `./gradlew spotlessApply`, then
     `./gradlew :spockk-specs:test --tests "*<NewClass>*" --rerun`, then
     `python3 .github/test-coverage/validate.py`. State the `--rerun` caveat explicitly.
  6. **Definition of done**: annotation present and valid, test passing or `@PendingFeature` with a live gap
     issue, `validate.py` exiting 0, PR body linking the upstream source file.

- [ ] **Step 2: Commit.**

```bash
git add .claude/skills/spock-migration
git commit -m "docs: add the spock-migration router skill"
```

### Task 10: The recipe and technique references

**Files:**
- Create: `.claude/skills/spock-migration/references/recipe-smoke.md`
- Create: `.claude/skills/spock-migration/references/recipe-engine-runtime.md`
- Create: `.claude/skills/spock-migration/references/recipe-compile-error.md`
- Create: `.claude/skills/spock-migration/references/recipe-ast-snapshot.md`
- Create: `.claude/skills/spock-migration/references/recipe-condition-rendering.md`
- Create: `.claude/skills/spock-migration/references/groovy-to-kotlin.md`
- Create: `.claude/skills/spock-migration/references/fidelity.md`
- Create: `.claude/skills/spock-migration/references/existing-coverage.md`

- [ ] **Step 1: Write the five recipe references.** Each one carries a real before/after: a genuine upstream class
  from the clone, and the port as it would land. Do not invent examples. Take the upstream side from the sparse
  clone and the target side from the matching existing test in `spockk-specs`, which exists for all five recipes:
  - `smoke` against `spockk-specs/.../smoke/`
  - `engine-runtime` against `spockk-specs/.../runtime/` with `EngineTestKitUtils.execute()`
  - `compile-error` against `ExceptionConditionValidationTest` and `TestDataFactory.specWithFeatureBody()`
  - `ast-snapshot` against a `samples/compilation/source` and `transformed` pair with `assertTransformation()`,
    and state that the annotation goes on the feature calling it, not in the resource files
  - `condition-rendering` against `ConditionRenderingSpecs.kt`

- [ ] **Step 2: Write `groovy-to-kotlin.md`**, the highest-leverage file here. Cover Groovy truth versus Kotlin's
  strict `Boolean`, GStrings versus string templates, `def` and dynamic typing, closures versus lambdas, operator
  overloading differences, property access versus getters, named and default arguments, and spread. Each entry is
  a Groovy snippet, the Kotlin equivalent, and the trap. **State plainly that condition rendering is not a
  divergence**: Spockk rewrites conditions through Spock's own rewriter and renders with the shaded Spock runtime,
  so a ported condition should produce the same diagram and a difference is a bug to report. Note that Kotlin
  power-assert backs `spockk-specs`' own assertions and is unrelated.

- [ ] **Step 3: Write `fidelity.md`** around the three failure modes from spec section 12: a test passing because
  it asserts something weaker than the original, a test no longer exercising the path the original targeted, and a
  data table silently reduced to its first row. Give each a concrete example of the wrong port next to the right
  one.

- [ ] **Step 4: Write `existing-coverage.md`**: where to look, how to judge a real match against a superficial
  resemblance, and the partial-overlap and one-test-many-features rules from spec section 9. Lead with the failure
  it prevents, which is annotating a test that looks close enough and converting a real gap into a false tick.

- [ ] **Step 5: Commit.**

```bash
git add .claude/skills/spock-migration
git commit -m "docs: add spock-migration recipe and technique references"
```

### Task 11: The gap-triage skill, validated against real ports

**Files:**
- Create: `.claude/skills/spock-gap-triage/SKILL.md`
- Create: test files produced by the validation ports

- [ ] **Step 1: Write `SKILL.md`** covering the decision between gap, not-applicable, and
  port-with-`@PendingFeature`; the slug-based dedup protocol from spec section 9 including searching open
  `test-coverage::gap` issues for the slug before opening a new one; and what a good gap issue contains, which is
  a minimal Kotlin reproducer, the Spock behavior it should match, and the upstream reference.

- [ ] **Step 2: Port a real class using only the skills**, choosing one from `conditions` with the `smoke` recipe.
  Work strictly from the skill as an agent would. Every time you need something the skill does not say, that is a
  skill defect: fix the skill, do not work around it.

- [ ] **Step 3: Port a second real class of a different recipe**, choosing `engine-runtime`. Two recipes exercised
  is the bar for calling the skill validated.

- [ ] **Step 4: Run the full verification loop** on both ports:

```bash
./gradlew spotlessApply
./gradlew :spockk-specs:test --rerun
python3 .github/test-coverage/validate.py
```
Expected: BUILD SUCCESSFUL and exit 0.

- [ ] **Step 5: Commit** the skill and both ports together, so the PR shows the skill and evidence it works.

```bash
git add .claude/skills spockk-specs
git commit -m "docs: add spock-gap-triage skill, validated on two real ports"
```

---

## Phase 4: The reconciler

Phase 4 is the first component that writes to GitHub. It is built dry-run first and never runs unattended until
Task 15 has shown its mutation plan to a human.

### Task 12: GitHub client and label bootstrap

**Files:**
- Create: `.github/test-coverage/github_api.py`
- Create: `.github/test-coverage/tests/test_github_api.py`

**Interfaces:**
- Produces: `GitHub(token, repo)` with `get(path)`, `post(path, body)`, `patch(path, body)`, `paginate(path)`, and
  `ensure_labels(specs)`. All on `urllib.request`, no `requests` dependency.

- [ ] **Step 1: Write the failing tests** against a stub HTTP layer, injected so no test touches the network:
  pagination follows `Link: rel="next"` to exhaustion; a 403 with `X-RateLimit-Remaining: 0` sleeps until reset
  rather than failing; a 404 on `get` returns `None` rather than raising; `ensure_labels` creates a missing label
  and leaves an existing one alone.

- [ ] **Step 2: Run the tests, confirm they fail.**

- [ ] **Step 3: Implement `github_api.py`.** Six labels with colours and descriptions:
  `test-coverage::dashboard`, `::area`, `::spec`, `::gap`, `::blocked`, `::drift`.

- [ ] **Step 4: Run the tests, confirm they pass.**

- [ ] **Step 5: Commit.**

```bash
git add .github/test-coverage
git commit -m "feat: add a stdlib GitHub client and coverage label bootstrap"
```

### Task 13: Render issue bodies

**Files:**
- Create: `.github/test-coverage/render.py`
- Create: `.github/test-coverage/tests/test_render.py`

**Interfaces:**
- Produces: `render_class_issue(spec_class, coverage, exclusions) -> tuple[str, str]` returning title and body;
  `render_area_issue(...)`; `render_dashboard(...)`; `split_class(spec_class, threshold) -> list[Part]`;
  `merge_generated_region(existing_body, new_region) -> str`.

- [ ] **Step 1: Write the failing tests.** The marker contract and the splitting rules are the load-bearing parts:

```python
class RenderTest(unittest.TestCase):
    def test_generated_region_replaced_human_text_preserved(self):
        existing = "My own note.\n<!-- spockk-coverage:begin -->\nOLD\n<!-- spockk-coverage:end -->\nTrailer."
        merged = merge_generated_region(existing, "NEW")
        self.assertIn("My own note.", merged)
        self.assertIn("Trailer.", merged)
        self.assertIn("NEW", merged)
        self.assertNotIn("OLD", merged)

    def test_first_render_appends_region_when_markers_absent(self):
        merged = merge_generated_region("Just a note.", "NEW")
        self.assertIn("Just a note.", merged)
        self.assertIn("NEW", merged)

    def test_ported_feature_is_checked(self):
        _, body = render_class_issue(CLASS, {KEY: ported()}, {})
        self.assertIn(f"- [x] {FEATURE}", body)

    def test_not_applicable_is_struck_through_with_reason(self):
        _, body = render_class_issue(CLASS, {}, {KEY: na_exclusion()})
        self.assertIn("~~", body)
        self.assertIn("n/a:", body)

    def test_pending_feature_links_its_gap(self):
        _, body = render_class_issue(CLASS, {KEY: pending(gap=412)}, {})
        self.assertIn("#412", body)

    def test_split_produces_stable_numbered_parts(self):
        parts = split_class(class_with(45), threshold=20)
        self.assertEqual(3, len(parts))
        self.assertEqual("(1/3)", parts[0].suffix)

    def test_appending_a_feature_extends_the_last_part(self):
        before = split_class(class_with(41), threshold=20)
        after = split_class(class_with(42), threshold=20)
        self.assertEqual(len(before), len(after))
        self.assertEqual(before[0].feature_names, after[0].feature_names)
```

- [ ] **Step 2: Run the tests, confirm they fail.**

- [ ] **Step 3: Implement `render.py`.** Markers are `<!-- spockk-coverage:begin key=... -->` and
  `<!-- spockk-coverage:end -->`, and everything outside them survives untouched, per spec section 7.2. Splitting
  appends to the last part rather than renumbering, which is what makes part identity stable across upstream
  additions.

- [ ] **Step 4: Run the tests, confirm they pass.**

- [ ] **Step 5: Commit.**

```bash
git add .github/test-coverage
git commit -m "feat: render coverage dashboard, area and class issue bodies"
```

### Task 14: Reconcile against live issues

**Files:**
- Create: `.github/test-coverage/reconcile.py`
- Create: `.github/test-coverage/tests/test_reconcile.py`

**Interfaces:**
- Produces: `plan(manifest, coverage, exclusions, existing_issues) -> list[Mutation]` where
  `Mutation = dataclass(kind, target, payload, reason)`. `plan` is pure; `apply(mutations, github)` performs them.
  Keeping them separate is what makes the dry-run trustworthy.

- [ ] **Step 1: Write the failing tests** for each row of the drift table in spec section 11.2:

```python
class ReconcileTest(unittest.TestCase):
    def test_new_class_creates_an_issue_under_its_area(self):
    def test_new_feature_appends_and_reopens_a_closed_issue(self):
    def test_deleted_feature_is_struck_not_dropped(self):
    def test_changed_body_hash_applies_drift_label_without_unticking(self):
    def test_rename_detected_by_matching_hash_rewrites_the_key(self):
    def test_rename_does_not_reset_progress(self):
    def test_deleted_class_closes_the_issue_leaving_ported_tests(self):
    def test_bootstrap_never_emits_a_blocked_label(self):
    def test_blocked_removed_once_every_cited_gap_is_closed(self):
    def test_area_over_threshold_splits_into_stable_sub_areas(self):
```

- [ ] **Step 2: Run the tests, confirm they fail.**

- [ ] **Step 3: Implement `reconcile.py`.** Rename detection is the subtle one: when a feature disappears and
  another appears **in the same class in the same run** with a matching body hash, treat it as a rename, rewrite
  the manifest key, and emit a comment asking for the `@MigratedFrom` string to be updated. Never reset progress.
  Two rules the tests above pin down and the implementation must respect: the bootstrap never applies
  `test-coverage::blocked` (spec section 7.4), and `test-coverage::drift` never unticks a checkbox.

- [ ] **Step 4: Run the tests, confirm they pass.**

- [ ] **Step 5: Commit.**

```bash
git add .github/test-coverage
git commit -m "feat: reconcile the inventory against live coverage issues"
```

### Task 15: Dry run, blast radius, and bootstrap

**Files:**
- Modify: `.github/test-coverage/reconcile.py`
- Create: `.github/test-coverage/tests/test_blast_radius.py`

- [ ] **Step 1: Write the failing tests:** `--dry-run` performs zero writes and prints every mutation with its
  reason; exceeding `--max-mutations` aborts before the first write, not partway through.

- [ ] **Step 2: Run the tests, confirm they fail.**

- [ ] **Step 3: Implement both.** Default `--max-mutations` to 50. The bootstrap legitimately exceeds it and must
  pass `--max-mutations 500` explicitly, which is the point: a routine run that suddenly wants to touch 400 issues
  is a bug, and the cap turns it into a failed job instead of 400 mangled issues.

- [ ] **Step 4: Run the tests, confirm they pass.**

- [ ] **Step 5: Dry-run against the real repo and read the plan.**

```bash
GH_TOKEN=<token> python3 .github/test-coverage/reconcile.py --dry-run | tee /tmp/plan.txt
wc -l /tmp/plan.txt
```
Expected: roughly 400 creates, zero updates, zero deletes, and no `blocked` label anywhere. Read a sample of the
rendered bodies before proceeding.

- [ ] **Step 6: Stop and get human sign-off on the plan.** This is the last point before ~400 issues exist. Do not
  proceed on your own judgement.

- [ ] **Step 7: Bootstrap for real.**

```bash
GH_TOKEN=<token> python3 .github/test-coverage/reconcile.py --max-mutations 500
```

- [ ] **Step 8: Spot-check the result.** Open the dashboard, one area issue, and one split class issue. Confirm
  sub-issue progress renders, no area exceeds 100 children, and no issue carries `test-coverage::blocked`.

- [ ] **Step 9: Commit.**

```bash
git add .github/test-coverage
git commit -m "feat: add dry-run and blast-radius cap to the reconciler"
```

---

## Phase 5: Automation

### Task 16: The weekly sync workflow

**Files:**
- Create: `.github/workflows/test-coverage-sync.yml`

- [ ] **Step 1: Write the workflow.** Weekly plus manual dispatch, per spec section 11.1:

```yaml
name: Test coverage sync

on:
  schedule:
    - cron: "0 4 * * 1"
  workflow_dispatch:

permissions:
  contents: write
  issues: write
  pull-requests: write

concurrency:
  group: test-coverage-sync
  cancel-in-progress: false

jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v7

      - name: Clone upstream spock-specs
        run: |
          git clone --depth 1 --filter=blob:none --sparse \
            https://github.com/spockframework/spock.git /tmp/spock
          cd /tmp/spock && git sparse-checkout set spock-specs
          echo "UPSTREAM_SHA=$(git rev-parse HEAD)" >> "$GITHUB_ENV"

      - name: Regenerate the inventory
        run: |
          python3 .github/test-coverage/inventory.py \
            --upstream /tmp/spock --out _docs/test-coverage/spock-inventory.json

      - name: Reconcile issues
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          python3 .github/test-coverage/reconcile.py --dry-run > /tmp/plan.txt
          cat /tmp/plan.txt
          python3 .github/test-coverage/reconcile.py

      - name: Open a PR for the manifest diff
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: .github/test-coverage/open-manifest-pr.sh
```

- [ ] **Step 2: Verify the token has the reach it needs.** Before relying on this, confirm `GITHUB_TOKEN` with
  `issues: write` can create a **sub-issue** link, not just an issue. The sub-issues API is newer than the classic
  issues API and may require a PAT. If it does, document the required secret in the tooling README and switch the
  workflow to it. Do not discover this on the first scheduled run.

- [ ] **Step 3: Commit.**

```bash
git add .github/workflows
git commit -m "feat: add the weekly test coverage sync workflow"
```

### Task 17: The manifest PR and a real run

**Files:**
- Create: `.github/test-coverage/open-manifest-pr.sh`

- [ ] **Step 1: Write the script.** Exit 0 without opening anything when the manifest is unchanged, which is the
  common case and must not create weekly noise. Otherwise branch as
  `automation/test-coverage-sync-<date>`, commit the manifest, and open a PR whose body carries the mutation plan
  from `/tmp/plan.txt` and the upstream SHA range.

- [ ] **Step 2: Trigger the workflow manually** with `workflow_dispatch` and watch it end to end.

- [ ] **Step 3: Confirm the no-op path.** Trigger it a second time with no upstream change and confirm it opens no
  PR and makes no issue mutations. A sync job that churns on every run is worse than no sync job.

- [ ] **Step 4: Commit.**

```bash
git add .github/test-coverage
git commit -m "feat: open a manifest-diff PR from the sync workflow"
```

---

## Self-review

**Spec coverage.** Every numbered section of the spec maps to a task: section 5 to Tasks 2 to 4, section 6 to
Task 1's config, section 7 to Task 13, section 8 to Task 10, section 9 to Tasks 7, 10 and 11, section 10 to Tasks
5, 6 and 8, section 11 to Tasks 14, 16 and 17, section 12 to Tasks 9 to 11, section 13 to Task 9, section 14 to
Tasks 4, 8 and 15.

**Deliberately not covered.** Spec section 13's suggested delegation order (conditions and parameterization
first) is a scheduling decision for when tickets get handed out, not something to build. Spec section 3's
rejected approaches A and C are recorded reasoning with nothing to implement.

**Known risks carried from the spec.** The regex parser can misclassify; Task 4 Step 6 is the check that catches
it, and the count guard catches it on later runs. Sub-issue API reach is unverified until Task 16 Step 2, which is
the single riskiest unknown in the plan and is why it is called out as an explicit step rather than assumed.
