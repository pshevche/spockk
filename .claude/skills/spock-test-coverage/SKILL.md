---
name: spock-test-coverage
description: Port a single upstream Spock feature (from spockframework/spock's spock-specs module) into a Kotlin test in spockk-specs, tracked via the @MigratedFrom annotation. Invoke when working a test-coverage::spec ticket.
---

You are porting one upstream Spock feature into `spockk-specs` and recording the port so the coverage tracker can
see it. Work through the steps below in order; each one depends on the last.

## Step 1 - Search for existing coverage first

Before writing anything, search `spockk-specs` for a test that already covers this feature. `spockk-specs` has
hundreds of feature methods written before this tracking programme, and some already overlap the upstream suite in
places nobody has mapped yet.

Read `references/existing-coverage.md` before doing this search. It covers where to look, how to tell a real match
from a superficial resemblance, and the partial-overlap and one-test-many-features rules. Getting this wrong in
either direction is bad: annotating a test that looks close enough turns a real gap into a false tick, and missing
a genuine match means writing a duplicate test.

If a match exists: move that test into the ticket's target class (a move, not a copy, not a rewrite - the body
stays as-is so a passing test stays passing) and add `@MigratedFrom`. If it only partially covers the upstream
feature, extend it to cover the rest before annotating; the annotation asserts full coverage, so it must be true
when it lands. If one test already covers several upstream features, use `@MigratedFrom`'s `vararg` to list every
key rather than splitting the test apart.

If no match exists, continue to Step 2.

## Step 2 - Classify the upstream class

Classify the upstream spec class into exactly one of five rewrite recipes, then load **only** that recipe's
reference file - the others are irrelevant to this ticket and will waste context.

| Recipe | Detection |
|---|---|
| `smoke` | plain `Specification`, no embedded compiler |
| `engine-runtime` | `EmbeddedSpecification` + `runner.run*` |
| `compile-error` | `compiler.compile` + `InvalidSpecCompileException` (or a sibling compile-error exception) |
| `ast-snapshot` | `@Snapshot` + `SpockSnapshotter` + `transpile*` |
| `condition-rendering` | extends `ConditionRenderingSpec` |

Load the matching reference:

- `references/recipe-smoke.md`
- `references/recipe-engine-runtime.md`
- `references/recipe-compile-error.md`
- `references/recipe-ast-snapshot.md`
- `references/recipe-condition-rendering.md`

The ticket carries a classification as a hint, not a contract. If the code disagrees with the ticket's stated
recipe, follow the code and say so in the PR description; a systematically wrong classification in the generator
is worth reporting separately.

Also load `references/groovy-to-kotlin.md` now. It is the reference most likely to determine whether the port is
any good, covering Groovy truth, GStrings, `def`, closures, operator overloading, property access, and named/
default arguments/spread.

## Step 3 - Place the file

The recipe determines where the port lives, per the recipe's own reference for the exact pattern:

| Recipe | Target in `spockk-specs` |
|---|---|
| `smoke` | `src/test/kotlin/.../smoke/`, a real Spockk spec run by the Spock engine |
| `engine-runtime` | `src/test/kotlin/.../runtime/`, via `EngineTestKitUtils.execute()` with a fixture spec in `src/testFixtures/` |
| `compile-error` | `src/test/kotlin/.../compilation/`, inline via `TestDataFactory.specWithFeatureBody()` |
| `ast-snapshot` | a source/transformed pair under `src/test/resources/samples/compilation/`, via `assertTransformation()` |
| `condition-rendering` | condition rendering assertions against the shared fixture specs in `ConditionRenderingSpecs.kt` |

## Step 4 - Write the port and pick the outcome

There are exactly four outcomes. Pick the one that matches reality; do not force a port to "pass" by weakening it.

1. **Ported.** The feature compiles and passes. Write the test, add `@MigratedFrom("<upstream class>#<feature name>")`.
2. **Ported but failing at runtime.** Commit it anyway, with `@PendingFeature`. `reason` is the bare gap issue
   URL, nothing else - the issue itself carries the explanation:
   ```kotlin
   @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/412")
   @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#range pipe")
   fun `range pipe`() { ... }
   ```
   A failing `@PendingFeature` feature reports as skipped and keeps the suite green, but Spock fails the build if
   it ever starts passing - so a fixed gap cannot silently stay marked pending. Always prefer this over any bespoke
   disabling mechanism.
3. **Will not compile.** Nothing can land in the test suite. Do not fight the compiler. Record it in
   `_docs/test-coverage/exclusions.toml` with `status = "blocked"`, a `reason`, and a gap issue number. Load
   `spock-gap-triage` for how to open that gap issue and dedup it against existing ones.
4. **Not applicable.** Behavior that cannot exist in Kotlin (Groovy truth, GString interpolation, metaclass
   mutation). Record it in `_docs/test-coverage/exclusions.toml` with `status = "not-applicable"` and a `reason`.
   This is permanent and reviewed like documentation, unlike `blocked`.

Before finishing, read `references/fidelity.md`: a port that passes for the wrong reason (weaker assertion, wrong
code path, a data table truncated to its first row) is worse than an honest gap.

## Step 5 - Verify

Run, in order:

```bash
./gradlew spotlessApply
./gradlew :spockk-specs:test --tests "*<NewClass>*" --rerun
python3 .github/test-coverage/validate.py
```

The `--rerun` flag matters: `:spockk-specs:compileTestFixturesKotlin` (and the test tasks that depend on it) can
report UP-TO-DATE and hide a real compile error otherwise, a caveat also documented in `CLAUDE.md`.

## Definition of done

- `@MigratedFrom` is present on the ported (or moved) test and every key is spelled exactly as it appears in the
  inventory (`_docs/test-coverage/spock-inventory.json`).
- The test passes, or carries `@PendingFeature` backed by a live gap issue, or the feature is recorded in
  `exclusions.toml` with the outcome that applies.
- `python3 .github/test-coverage/validate.py` exits 0.
- The PR body links the upstream source file the port came from.
