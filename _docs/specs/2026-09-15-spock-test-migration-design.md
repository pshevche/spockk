# Spock Test Migration Workflow: Design Spec

> **Status:** Draft for review
> **Date:** 2026-09-15
> **Upstream source:** `spockframework/spock`, module `spock-specs`
> **Target:** `spockk-specs` in this repository

---

## 1. Problem

Spockk reimplements Spock's specification syntax on top of Kotlin IR, but reuses Spock's real runtime. We have no
systematic measure of how much of Spock's behavior Spockk actually reproduces. Spock's own test suite
(`spock-specs`) is the most complete executable description of that behavior in existence, and it is sitting right
there.

The goal is to port that suite into `spockk-specs`, incrementally and by delegation to coding agents, and to use
the ports to find and record the places where Spockk falls short.

This is as much a gap-discovery programme as a test-porting one. Many ports are expected to fail, and a failing
port is a successful outcome: it converts an unknown gap into a tracked one.

### Measured scope

Numbers below come from a classifier run against `spock-specs/src/test/groovy` at the upstream default branch. They
are reproducible by the inventory generator described in section 5.

| | Classes | Features |
|---|---:|---:|
| Top-level spec classes with at least one feature | 447 | 2,700 |
| **In scope** | **365** | **2,351** |
| Excluded (see section 6) | 82 | 349 |

In-scope work by area:

| Area | Classes | Features |
|---|---:|---:|
| extensions | 109 | 389 |
| mocking | 86 | 705 |
| smoke-core | 62 | 277 |
| runtime | 33 | 166 |
| conditions | 28 | 354 |
| parameterization | 22 | 306 |
| ast | 12 | 52 |
| util-api | 10 | 87 |
| traits, config, smoke-misc | 3 | 15 |

Feature counts per class are heavily skewed: 253 classes have 1 to 3 features, while 19 classes have more than 25
(the largest, `InvalidWhereBlocks`, has 62). Issue sizing has to account for this; see section 7.3.

---

## 2. Goals and non-goals

### Goals

- A dashboard that reflects real migration state at all times, with no manual bookkeeping.
- Every upstream feature is either ported, shown to be covered by a test `spockk-specs` already had, explicitly
  marked not-applicable with a reason, or linked to a tracked Spockk gap. No silent omissions.
- Per-class tickets small and self-contained enough to hand to an agent with no extra context.
- Rewrite rules precise enough that two different agents porting the same class produce comparable results.
- Upstream churn (new, renamed, changed, deleted specs) is detected and surfaced rather than silently diverging.

### Non-goals

- Porting Spock's internal unit tests (`org.spockframework.util`, `buildsupport`, `idea`). They test Java
  utility classes, not specification behavior.
- Achieving 100% coverage. Some Spock behavior depends on Groovy semantics Kotlin does not have, and will never
  port.
- Fixing the Spockk gaps discovered. Gap issues are tracked separately and scheduled on their own cadence.
- Running Spock's suite against Spockk directly. The Groovy sources are not executable as Kotlin; this is a
  rewrite, not a test-harness exercise.

---

## 3. The central decision: derived coverage

The original sketch had each per-class issue carry a TODO list of feature names, ticked off as work completed, with
a scheduled job updating the dashboard. That design has a fatal interaction: **if the sync job rewrites issue
bodies, it destroys the ticks; if it does not, the dashboard drifts from reality within days.** With 2,351
checkboxes across 365 issues, hand-maintained state will be wrong almost immediately.

### Approaches considered

**A. Issue state is the source of truth.** Agents tick boxes; the sync job only appends new items and never
rewrites existing ones. Simple, and it is what the original sketch implies. But nothing connects a tick to actual
code: an agent can tick a box without porting anything, a reverted PR leaves the box ticked, and the job cannot
safely reformat or regroup anything it has already written. Coverage numbers become a claim rather than a
measurement.

**B. Repo state is the source of truth (recommended).** Every ported test carries a machine-readable
back-reference to the upstream feature it came from. A scanner reads `spockk-specs`, matches the back-references
against the upstream inventory, and *computes* every checkbox, every progress count and the whole dashboard. Issue
bodies become a rendered view, regenerated freely because they hold no state worth preserving. Merging a PR is
what ticks a box. Reverting one unticks it.

**C. A checked-in ledger file.** A YAML/JSON file in the repo maps upstream features to status, updated by hand in
each migration PR, with the dashboard rendered from it. Reviewable in diffs and avoids annotation machinery, but it
is still a parallel record that can disagree with the code, and it becomes a merge-conflict magnet with many
concurrent migration PRs.

**Recommendation: B.** It is the only option where the dashboard cannot lie, and it turns the weekly job from a
state mutator into a pure renderer, which is far safer. Its cost is an annotation on every ported test and a
scanner to read them. Approach C's ledger is retained in one narrow role where the code genuinely cannot carry the
information: recording deliberate not-applicable decisions (section 9).

### Consequences

- Nobody ticks a checkbox by hand. Checkbox state in issue bodies is generated output.
- Issue bodies are fully regenerated on every sync, inside explicit markers. Human commentary lives in issue
  comments, which are never touched.
- Coverage is computed from two inputs only: the upstream inventory and a scan of `spockk-specs`.

---

## 4. Architecture

```
  spockframework/spock (upstream)
            |
            |  weekly shallow sparse clone, pinned by SHA
            v
   +------------------+     +-----------------------------+
   | inventory        | --> | spock-inventory.json        |
   | generator        |     | (checked in)                |
   +------------------+     +-----------------------------+
                                    |
   spockk-specs/ (this repo)        |
            |                       |
            | scan @MigratedFrom    |
            v                       v
   +------------------+     +-----------------------------+
   | coverage scanner | --> | reconciler                  |
   +------------------+     | (inventory + coverage       |
                            |  + exclusions -> issues)    |
                            +-----------------------------+
                                    |
                    +---------------+---------------+
                    v               v               v
              dashboard        area issues     class issues
```

Four components, each independently testable:

| Component | Input | Output | Purity |
|---|---|---|---|
| Inventory generator | upstream clone | `spock-inventory.json` | pure function of upstream tree |
| Coverage scanner | `spockk-specs` sources | set of ported/pending feature keys | pure function of this repo |
| Reconciler | inventory + coverage + exclusions | GitHub issue mutations | only component touching the API |
| Sync workflow | schedule | PR with manifest diff, reconciled issues | orchestration only |

The first two are pure and get unit tests. The reconciler gets a `--dry-run` that prints the mutation plan, which
is what the workflow shows in its PR body.

---

## 5. The inventory

### 5.1 Feature keys

A feature is identified by a stable key:

```
<fully-qualified-class>#<feature name>
```

for example `org.spockframework.smoke.condition.ConditionRendering#renders collection diff`.

The class part is stable. The feature name is not: upstream renames feature strings routinely. Rename handling is
in section 11.2.

### 5.2 Manifest schema

`_docs/test-coverage/spock-inventory.json`, checked in and regenerated by the weekly job:

```jsonc
{
  "upstream": {
    "repo": "spockframework/spock",
    "ref": "master",
    "sha": "a1b2c3...",              // exact commit the inventory was generated from
    "generated_at": "2026-09-15T00:00:00Z"
  },
  "classes": [
    {
      "key": "org.spockframework.smoke.condition.ConditionRendering",
      "path": "spock-specs/src/test/groovy/org/spockframework/smoke/condition/ConditionRendering.groovy",
      "base": "EmbeddedSpecification",
      "recipe": "condition-rendering",   // section 8
      "area": "conditions",
      "features": [
        { "name": "renders collection diff", "hash": "9f2a1c4e" }
      ]
    }
  ]
}
```

`hash` is a digest of the feature's normalised body text (whitespace and comments stripped). It detects an upstream
feature whose *meaning* changed while its name stayed the same, which would otherwise leave a stale port silently
marked done.

### 5.3 Generator

A Python script under `.github/test-coverage/`. Python rather than Kotlin because it runs in CI and in the reconciler
without a Gradle invocation, and adding a Gradle module for build tooling that never ships is not worth it.

The generator is a regex/line-based parser, not a Groovy parser. That is a deliberate tradeoff: it is good enough
(validated against the real tree, see section 14) and avoids a Groovy runtime dependency in CI. It must be
conservative: anything it cannot confidently classify is reported rather than silently dropped, and the job fails
if the parsed class count moves by more than a configured tolerance between runs.

---

## 6. Scope

Scope lives in `.github/test-coverage/config.yml`, not in code, so changing it is a reviewable diff.

### Excluded, with reasons

| Package | Classes | Reason |
|---|---:|---|
| `org.spockframework.docs.*` | 48 | Documentation samples for Spock's own manual, not behavior tests |
| `org.spockframework.util.*` | 19 | Unit tests for Java utility classes with no Spockk equivalent |
| `org.spockframework.groovy.*`, `spock.util.mop.*` | 10 | Groovy-language and metaprogramming semantics with no Kotlin analogue |
| `buildsupport`, `idea`, `serialization`, `builder`, `example` | 5 | Spock-internal infrastructure |

Exclusion is per package prefix, and every entry carries a written reason in the config. An excluded package
produces no issues but still appears in the dashboard footer, so the decision stays visible rather than becoming an
invisible blind spot.

### Areas

Areas are derived from package prefix and are the middle tier of the issue hierarchy. The mapping is data in
`config.yml`, so re-grouping does not require a code change.

---

## 7. Issue model

### 7.1 Three tiers

GitHub caps sub-issues at 100 per parent, so a flat dashboard with 365 children is not expressible. The hierarchy
is:

```
Spock Test Migration Dashboard        (1 issue,  label migration::dashboard)
  └─ Area: Mocking                    (~11 issues, label migration::area)
       └─ Migrate InteractionScopes   (365 issues, label migration::spec)
```

Both GitHub's native sub-issue progress bars and the generated body tables then work at every level.

The `extensions` area has 109 classes, which exceeds the cap on its own. Any area above a configured threshold
(default 80, leaving headroom) splits into numbered sub-areas along its sub-packages. The reconciler does this
automatically and deterministically, so the split is stable across runs.

### 7.2 Per-class issue body

```markdown
<!-- spockk-migration:begin key=org.spockframework.smoke.condition.ConditionRendering -->
**Upstream:** [`ConditionRendering.groovy`](https://github.com/spockframework/spock/blob/<sha>/...)
**Recipe:** condition-rendering (see `/spock-migration`)
**Area:** conditions

### Features (3/58)

- [x] renders collection diff → `ConditionRenderingTest.kt`
- [ ] renders map diff
- [ ] ~~renders GString diff~~ (n/a: no Kotlin equivalent for GString interpolation)
- [ ] renders bean diff (blocked by #412)
<!-- spockk-migration:end -->
```

Everything between the markers is generated and rewritten on every sync. Anything outside is preserved, so a human
can add notes above or below without the job clobbering them. Discussion belongs in comments, which are never
touched.

### 7.3 Issue sizing

A per-class issue is the default unit because it maps one-to-one onto a single ported file. But with a range of 1
to 62 features per class, uniform per-class issues would produce tickets varying by two orders of magnitude in
effort.

The reconciler splits any class above a threshold (default 20 features) into numbered part-issues
(`Migrate InvalidWhereBlocks (1/4)`), chunking the feature list in source order. Parts are stable: adding an
upstream feature appends to the last part rather than renumbering everything. All parts of a class are siblings
under the same area issue.

At the other end, classes with 1 to 3 features are left as their own issues rather than merged. Merging would
couple unrelated ports into one PR and break the one-issue-one-file property that makes the tickets delegable.

### 7.4 Labels

| Label | Meaning |
|---|---|
| `migration::dashboard` | the single root issue |
| `migration::area` | an area or sub-area roll-up |
| `migration::spec` | a per-class (or per-part) porting ticket |
| `migration::gap` | a Spockk deficiency found while porting |
| `migration::blocked` | an agent tried this ticket and established it cannot be ported yet |

`migration::blocked` is evidence, not a prediction. It is applied only after an agent has picked the ticket up and
confirmed from an actual attempt that the remaining features cannot be ported, and it always points at the gap
issues that establish why. The bootstrap never applies it, and the reconciler never infers it: guessing in advance
which tickets are unworkable would bake today's assumptions about Spockk's surface into the tracker and hide
ports that would in fact have succeeded.

The reconciler does remove it, once every gap it cites is closed, which returns the ticket to the queue
automatically. The delegation queue is `migration::spec` minus `migration::blocked`.

---

## 8. Rewrite recipes

The measured distribution of upstream spec shapes:

| Recipe | Classes | Features | Detection |
|---|---:|---:|---|
| `engine-runtime` | 173 | 934 | `EmbeddedSpecification` + `runner.run*` |
| `smoke` | 144 | 995 | plain `Specification`, no embedded compiler |
| `compile-error` | 24 | 110 | `compiler.compile` + `InvalidSpecCompileException` |
| `ast-snapshot` | 14 | 160 | `@Snapshot` + `SpockSnapshotter` + `transpile*` |
| `condition-rendering` | 10 | 152 | extends `ConditionRenderingSpec` |

These map onto the testing conventions already in `CLAUDE.md`:

| Recipe | Target in `spockk-specs` |
|---|---|
| `smoke` | `src/test/kotlin/.../smoke/`, a real Spockk spec run by the Spock engine |
| `engine-runtime` | `src/test/kotlin/.../runtime/`, via `EngineTestKitUtils.execute()` with a fixture spec in `src/testFixtures/` |
| `compile-error` | `src/test/kotlin/.../compilation/`, inline via `TestDataFactory.specWithFeatureBody()`, asserting failure and message |
| `ast-snapshot` | a source/transformed pair under `src/test/resources/samples/compilation/`, via `assertTransformation()` |
| `condition-rendering` | condition rendering assertions against the shared fixture specs in `ConditionRenderingSpecs.kt` |

The recipe is a hint carried in the issue, not a contract. An agent that finds the classification wrong should
follow the code and say so in the PR; a systematically wrong classification is a generator bug worth fixing.

---

## 9. Outcomes for a single feature

Exactly four outcomes, and each one leaves a durable artifact:

### Already covered by an existing test
`spockk-specs` already has 364 feature methods across 46 test classes, written before this programme and
overlapping the upstream suite in places nobody has mapped. So the agent's **first** step on any feature is to
look for an existing test that already covers it.

When one exists, the agent does not write a second: it moves that test into the ticket's target class and adds
`@MigratedFrom`. A move, not a copy, and not a rewrite. The test keeps its body, so a passing test stays passing
and the diff shows plainly that nothing was reimplemented.

This is the only mechanism by which existing tests acquire annotations. There is no upfront retrofit pass:
coverage that already exists is discovered and recorded ticket by ticket, as the tickets are worked. Until a
ticket touches it, an existing test is simply uncounted, which understates coverage early on and is the right
direction to be wrong in.

Two judgement calls the skill has to pin down, since they decide whether this step helps or quietly loses
coverage:

- **Partial overlap.** An existing test covering some of the upstream feature's assertions is moved and then
  extended to cover the rest, not left as-is with the annotation attached. The annotation asserts the feature is
  covered, so it must be true when it lands.
- **One test, several upstream features.** Common, since `spockk-specs` was not written feature-by-feature
  against upstream. `@MigratedFrom` is `vararg` for exactly this; the test is annotated with every key it covers
  rather than split apart.

### Ported
No existing test covers it, so a new Kotlin test is written carrying `@MigratedFrom`. Checkbox ticks
automatically, as it does for a moved test.

### Ported but failing at runtime
The port is committed with Spock's `@PendingFeature`:

```kotlin
@PendingFeature(reason = "Spockk does not support data pipes over ranges, see #412")
@MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#range pipe")
fun `range pipe`() { ... }
```

This was verified empirically against this codebase: a failing `@PendingFeature` feature is reported as **skipped**,
the suite stays green, and Spock **fails the build if the feature ever starts passing**. That last property is why
`@PendingFeature` is strongly preferred over any bespoke disabling: a gap that gets fixed cannot silently stay
marked pending, so the tracker self-heals.

### Will not compile
Kotlin compile errors are hard failures, so the test cannot be committed in any form. `@PendingFeature` does not
help here, and this is the one case where nothing lands in the test suite. The feature is recorded in the
exclusions ledger with `status: blocked` and the gap issue number, so the dashboard still shows it as blocked
rather than merely absent.

### Not applicable
Behavior that cannot exist in Kotlin (Groovy truth, GString interpolation, metaclass mutation, dynamic dispatch).
Recorded in `_docs/test-coverage/exclusions.yml`:

```yaml
- key: "org.spockframework.smoke.condition.ConditionRendering#renders GString diff"
  status: not-applicable
  reason: "Kotlin has no GString; string templates are compiled to concatenation"
  decided: 2026-09-15
```

A checked-in ledger is used here because there is no code artifact to hang an annotation on. It is the narrow
survival of approach C from section 3, and it is deliberately the only hand-maintained state in the system. A
not-applicable decision is a reviewable diff, which is the right bar for permanently writing off coverage.

### Gap issues and deduplication

Many features fail for the same underlying reason. Without discipline, 365 agents would open hundreds of
near-duplicate gap issues.

Every gap carries a stable slug (`where-block-range-pipes`). Before opening one, an agent must search open issues
labelled `migration::gap` for that slug and link to the existing one if found. The slug lives in the gap issue body
inside a marker comment, so the search is exact rather than fuzzy. The reconciler aggregates: each gap issue's body
is regenerated with the list of features currently blocked by it, which gives an immediate impact ranking for
prioritising Spockk work.

---

## 10. Traceability

### 10.1 The annotation

```kotlin
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MigratedFrom(vararg val keys: String)
```

Placed in `spockk-specs/src/testFixtures/`, **not** `spockk-core`. This is test-suite bookkeeping and has no
business in the published user-facing API.

`SOURCE` retention because the scanner reads sources, not bytecode, and nothing at runtime needs it.

`vararg` because the mapping is not always one-to-one. A single Kotlin test may legitimately cover several upstream
features (Groovy's data tables sometimes fan out into what Kotlin expresses as one parameterized test), and one
upstream feature may need several Kotlin tests. Both directions are allowed; the scanner takes the union.

### 10.2 The scanner

Reads `@MigratedFrom` annotations across `spockk-specs/src/test` and `src/testFixtures`, plus `@PendingFeature`
on the same declarations, and emits:

```
key -> { ported | pending(gap#) }
```

Source scanning by regex, consistent with the generator, and cheap enough to run on every sync.

This holds for every recipe, `ast-snapshot` included. Although that recipe's artifact is a pair of resource files,
the pair is always driven by a feature that calls `assertTransformation()`, so the annotation attaches to that
feature like any other. There is no special case and the scanner has only one thing to look for.

### 10.3 Validation in CI

The scanner runs as a normal check on every PR and fails when:

- a `@MigratedFrom` key does not exist in the inventory (typo, or upstream deleted it)
- the same key is claimed by tests in a way that conflicts with the exclusions ledger
- a `@PendingFeature` references a gap issue that is closed

The third check is what stops the tracker rotting. It is the reason `@PendingFeature` is preferred to any ad hoc
mechanism.

---

## 11. Sync automation

### 11.1 Trigger

The original sketch wanted a push trigger on the `spock-specs` module. That is not possible: `spock-specs` lives in
`spockframework/spock`, and a push there raises no event in this repository. Options were a scheduled poll, or
asking upstream for a webhook (not realistic).

Weekly scheduled poll plus `workflow_dispatch`. Spock's test suite does not change fast enough to justify daily
churn, and the manual trigger covers the case where it does.

`.github/workflows/spock-migration-sync.yml`:

1. Shallow sparse clone of upstream `spock-specs`, recording the exact SHA.
2. Regenerate the inventory.
3. Run the coverage scanner over this repo.
4. Reconcile issues (create, update generated regions, apply computed labels).
5. Open a PR bumping `spock-inventory.json` and the pinned SHA, with the mutation plan in the body.

Issue mutations apply immediately (they are the live view and should not wait on a merge), while the manifest lands
via PR so that every change to the inventory is reviewable in a diff. This is the split the sketch asked for and it
falls out naturally from section 3: issues are a rendered view, the manifest is the record.

### 11.2 Drift handling

| Upstream change | Response |
|---|---|
| New class | Create issue under its area |
| New feature in existing class | Append to the generated list; comment on the issue if it was closed, and reopen |
| Feature deleted | Strike through in the body, comment once; keep the port (harmless, and deleting a passing test to chase upstream is worse) |
| Feature body changed (hash differs) but name same | Comment flagging the port as possibly stale, apply `migration::drift`; do not untick |
| Feature renamed | See below |
| Class deleted | Close the issue with a comment; leave the ported tests |

Renames are the dangerous case, because the naive diff sees a delete plus an add and silently drops the link. When
a feature disappears and another appears in the same class in the same sync, and their body hashes match, the
reconciler treats it as a rename: it rewrites the key in the manifest and comments on the issue asking for the
`@MigratedFrom` string to be updated, rather than resetting the work to zero.

---

## 12. Skills

Rewrite rules are encoded as project skills in `.claude/skills/`, following the structure established in PR #314.

### `spock-migration`

The router, invoked when working a `migration::spec` ticket. `SKILL.md` covers the existing-coverage search that
opens every ticket (section 9), classification, file placement and naming, the `@MigratedFrom` and
`@PendingFeature` conventions, the verification loop (including the `--rerun` caveat for
`compileTestFixturesKotlin` already documented in `CLAUDE.md`), and the definition of done. Detail is pushed into
references so the top-level file stays readable:

- `references/recipe-smoke.md`
- `references/recipe-engine-runtime.md`
- `references/recipe-compile-error.md`
- `references/recipe-ast-snapshot.md`
- `references/recipe-condition-rendering.md`

Each with a real before/after taken from an actual upstream class and its port.

- `references/groovy-to-kotlin.md` is the substantial one, and the part most likely to determine whether ports are
  any good: Groovy truth versus Kotlin's strict `Boolean`, GStrings, `def` and dynamic typing, closures versus
  lambdas, operator overloading differences, property access versus getters, named and default arguments, and
  spread.

  Condition rendering is not a divergence to document here: Spockk rewrites conditions through Spock's own
  rewriter and renders them with the shaded Spock runtime, so a ported condition should produce the same diagram
  as the original. A rendering that differs is a bug to report, not a Kotlin quirk to work around. (Kotlin
  power-assert is used for `spockk-specs`' own assertions and has nothing to do with how Spockk renders
  conditions.)

- `references/fidelity.md` states what a faithful port is and, more usefully, what the failure modes look like: a
  test that passes because it asserts something weaker than the original, a test that no longer exercises the code
  path the original targeted, a data table silently reduced to its first row.

- `references/existing-coverage.md` covers the search that opens every ticket: where to look in `spockk-specs`,
  how to judge whether an existing test really covers the upstream feature rather than merely resembling it, and
  the partial-overlap and one-test-many-features rules from section 9. The failure mode it exists to prevent is
  an agent annotating a test that looks close enough, which converts a real gap into a false tick.

### `spock-gap-triage`

Invoked when a port fails. Covers deciding between gap, not-applicable, and port-with-`@PendingFeature`; the
slug-based deduplication protocol; and what a good gap issue contains (minimal Kotlin reproducer, the Spock
behavior it should match, upstream reference).

Splitting this from `spock-migration` is deliberate. Triage is a different task with a different output (an issue,
not a PR), it is invoked at a specific point rather than continuously, and keeping it separate stops the migration
skill from growing a large branch that is irrelevant to the majority of tickets that simply succeed.

---

## 13. Delegation

A ticket is handed off with the existing `/gh-issue` skill. `migration::spec` issues are labelled `type::task`, so
they take the direct-implementation route and never trigger the design-spec path. The issue body links the recipe
reference, so the agent loads only what it needs.

One issue produces one PR touching one test file, or two when an existing test is moved into it (plus fixtures).
This is what makes roughly 400 tickets (365 classes, plus part-issues from section 7.3) tractable in parallel:
the only shared files are the exclusions ledger and occasional new fixtures, so conflicts stay rare. Moves are
the one source of cross-ticket contention, since two tickets can reach for the same existing test; the loser sees
a merge conflict on a file it did not expect to touch, which is noisy but safe.

Suggested order, by value per unit of effort: `conditions` and `parameterization` first (dense, high feature count
per class, core to Spockk's value), then `mocking`, then `smoke-core` and `runtime`, with `extensions` last since
it is the largest and most likely to hit gaps.

---

## 14. Risks

| Risk | Mitigation |
|---|---|
| Regex parsing of Groovy misclassifies or misses classes | Validated against the real tree; the job fails if the parsed class count moves beyond tolerance; unclassifiable files are reported, never dropped |
| Agents produce plausible but weak ports | `references/fidelity.md`, the PR must link upstream source, and review focuses on assertion strength |
| An agent annotates an existing test that only resembles the upstream feature, turning a real gap into a false tick | `references/existing-coverage.md` sets the bar; a move shows the test body unchanged in the diff, so a reviewer can judge the match directly |
| Gap issue flood | Slug-based dedup, enforced by the triage skill and visible in the aggregated gap bodies |
| ~400 issues overwhelm the issue tracker | Three-tier hierarchy, areas roll up, and `migration::blocked` drains proven-unworkable tickets out of the queue as they are attempted |
| Upstream renames silently reset progress | Hash-based rename detection (section 11.2) |
| Reconciler bug mass-edits issues | `--dry-run` mutation plan in the PR body; rate limits and a mutation cap that aborts the run if it would touch more than a configured share of issues |

The last one deserves emphasis. A component that can write to 365 issues needs a blast-radius limit before it ever
runs unattended.

---

## 15. Open decisions

Two remain open. Two were settled in review and are recorded here for the record.

**Settled: bootstrap scope.** All 365 class issues are created at once, and none are pre-marked blocked. Whether
a ticket is workable is established by attempting it, not predicted from today's reading of Spockk's surface
(section 7.4).

**Settled: manifest location.** Generated data and the exclusions ledger live in `_docs/test-coverage/`, tooling
and config in `.github/test-coverage/`. "Migration" implies an end state; measuring coverage against an upstream
suite that keeps moving does not have one.

Still open:

1. **`util-api` (10 classes, 87 features).** `spock.util.concurrent` (`BlockingVariable`, `PollingConditions`,
   `AsyncConditions`) is a user-facing Spock API that works unchanged from Kotlin, so these tests are portable but
   test Spock, not Spockk. *Recommendation: exclude. Passing tests that exercise no Spockk code inflate the
   coverage number without informing it.*

2. **Splitting threshold.** 20 features per issue, giving roughly 40 extra part-issues. *Recommendation: start at
   20, adjust after the first area completes and there is real data on how long a ticket takes.*

---

## 16. Implementation phases

Each phase is independently useful and independently reviewable.

1. **Inventory.** Generator, config, first checked-in manifest, unit tests. Produces the numbers in section 1 as a
   reproducible artifact and nothing else changes.
2. **Traceability.** `@MigratedFrom`, the scanner, CI validation, and the exclusions ledger format. No retrofit
   pass over the 364 existing tests: they are annotated ticket by ticket as agents find them (section 9). To
   validate the mechanism before anything depends on it, annotate one or two existing tests by hand and confirm
   the scanner counts them and CI rejects a bad key.
3. **Skills.** `spock-migration` with all recipe references, and `spock-gap-triage`. Validated by hand-porting two
   classes of different recipes and checking the skill was sufficient.
4. **Reconciler.** Dry-run first, then bootstrap the dashboard, areas and class issues.
5. **Automation.** The weekly workflow, once the reconciler has been run manually at least once.

Phase 3 before phase 4 on purpose: the skills should be proven on real ports before 365 tickets point at them.
