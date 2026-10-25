---
name: spock-migration-pr-review
description: Review a test-coverage migration PR (a port of an upstream Spock spec into spockk-specs, labeled test-coverage::spec). Runs the generic code-review pass, then checks the port against the maintainer's recurring review feedback. Invoke when a PR titled "test: migrate ... from upstream Spock" or "test: record ... migration as ..." is opened.
---

You are reviewing one migration PR produced by `spock-test-coverage`. The goal is a port that mirrors upstream as
closely as Spockk allows, with every deviation backed by a gap issue. The checklist below comes from the maintainer's
review comments on earlier migration PRs (#654 to #753); each rule cites the PR where it was raised.

## Step 1 - Generic pass

Run the `code-review` skill on the PR first (`/code-review <pr-number>`, without `--comment`). Keep its findings
for Step 4; do not post them yet.

## Step 2 - Load the upstream source

1. Read the `@MigratedFrom` keys in the diff. Each is `<upstream fqcn>#<feature name>`.
2. Look the class up in `_docs/test-coverage/spock-inventory.json` to get its upstream `path`.
3. Fetch that file from `https://raw.githubusercontent.com/spockframework/spock/master/<path>`.
4. Put each upstream feature next to its Kotlin port and compare them line by line.

For a PR that only edits `_docs/test-coverage/exclusions.toml` (blocked or not-applicable), skip to Step 3, rule 9.

## Step 3 - Checklist

Report only what you verified in the diff. Cite the file and line.

1. **Block structure matches upstream 1-to-1.** Same labels, same order, no added `given`, and a leading
   anonymous block stays anonymous (#738, #729, #691, #681, #666). If upstream's structure does not work in
   Spockk, the port keeps upstream's structure, carries `@PendingFeature(reason = "<gap issue URL>")`, and
   links a gap issue. Reshaping the test to make it pass is a finding (#681, #666).
2. **`where` block mirrors upstream.** Variables upstream declares in `where` are not moved to `given` or
   `then` (#746, #737). Each variable has its own `variable(x).from(...)`; values are not repeated per row
   and constants are declared once (#738). Derived variables stay derived (#737).
3. **Conditions, messages and exception creation are identical to upstream.** Only Groovy-truth translations
   (see `spock-test-coverage/references/groovy-to-kotlin.md`) may differ (#751). Count the rows of every
   upstream data table against the port (`fidelity.md`, failure mode 3).
4. **Assertions are not weakened or dropped to get green.** Fix the type-level or API-level cause instead, and
   keep both upstream assertions (#676). Apply the `fidelity.md` general check.
5. **No `@Issue` annotation.** Migrated specs do not reference upstream issues (#751, #721, #704).
6. **Naming.**
   - Class and package mirror upstream; feature names mirror upstream verbatim (see `spock-test-coverage`
     Step 3).
   - Exceptions the maintainer allowed: drop Groovy references from names (for example "in Groovy 3&4 Issue
     #1501"), rename "defined in Groovy" to "defined in Kotlin", spell out "w/" and "wo/" as "with" and
     "without", and keep names lowercase (#654, #704).
   - `@MigratedFrom` keys always keep upstream's exact spelling.
7. **Annotations on fields.** `@Subject`, `@Shared` and similar annotations upstream puts on a field are present
   on the Kotlin field (#744). Field declarations match upstream's shape (`val inv: IMockInvocation = Mock()`),
   unless a gap issue explains why not (#681).
8. **Groovy-only features are not ported.** `GroovyMock`, `GroovyStub`, `GroovySpy` and similar are dropped from
   the test and recorded in `exclusions.toml` as `not-applicable` (#672, #659).
9. **Gap handling.**
   - `@PendingFeature.reason` is the bare issue URL and nothing else.
   - The linked issue exists, is open, and is about this failure. Check it, because the author may have linked
     a nearby but unrelated gap (#681 pointed at #693 for the right reason).
   - A feature that compiles is `@PendingFeature`, never `blocked`. `blocked` entries need `reason`, `gap` and
     `decided`.
   - The gap was deduplicated per `spock-gap-triage` (look for the `<!-- gap-slug: ... -->` marker).
10. **Existing coverage.** If the PR moved an existing test, the body is unchanged except for the annotation, and
    a partial match was extended before annotating (`existing-coverage.md`).
11. **PR hygiene.**
    - Body links the upstream file on `master`, never a commit SHA.
    - No em dashes in code comments, commit messages or PR text.
    - Comments are 2-3 lines and do not restate the code.
    - Commit subject follows Conventional Commits (`test: migrate X from upstream Spock`).
12. **Checks.** `validate.py` and `:spockk-specs:test` are green on the head commit. If you can run locally, use
    `./gradlew :spockk-specs:test --tests "*<Class>*" --rerun`.

## Step 4 - Post the review

- Post one review with inline comments (`pull_request_review_write` create, `add_comment_to_pending_review`,
  then submit as a plain comment). Never approve, never request changes on the maintainer's behalf, never merge.
- One comment per issue. State the rule and the fix in one or two sentences, in the maintainer's voice: direct,
  imperative, no praise padding. If a deviation is acceptable only with a gap issue, say "Open a gap issue if
  this is not supported" rather than asking the author to guess.
- Prefix every comment with 🤖 and end it with the attribution footer from the session instructions.
- Run the text through the `humanizer` skill first (final text only).
- Do not repeat a finding that an earlier review comment on the same PR already raised and the author answered.
- If there are no findings, post a single short comment saying the port matches upstream and which upstream
  features you compared.
- When unsure whether the maintainer would flag something (formatting, blank lines), leave it out.
