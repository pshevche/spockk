---
name: spock-gap-triage
description: Decide what to do when a port from spock-test-coverage fails to pass or fails to compile, and open or link a deduplicated gap issue when the failure is a real Spockk limitation. Invoke whenever a port does not simply pass.
---

You are triaging a single upstream Spock feature that did not port cleanly. This is invoked mid-port, from
`spock-test-coverage`'s Step 4, whenever the straightforward "write it, it passes" path does not apply.

## Step 1 - Decide which of the three outcomes applies

A port that does not simply pass falls into exactly one of these. Do not default to the easiest-looking one; pick
the one that is actually true.

1. **Not applicable.** The upstream feature exercises behavior that cannot exist in Kotlin at all: Groovy truth on
   a non-boolean value, GString identity, dynamic retyping via `def`, metaclass mutation, closure delegation
   strategies. This is a permanent decision, reviewed like documentation. See `groovy-to-kotlin.md` in
   `spock-test-coverage/references/` for the canonical list of what falls here. Record it in
   `_docs/test-coverage/exclusions.toml`:
   ```toml
   [["org.spockframework.smoke.condition.SatisfiedConditions#number"]]
   status = "not-applicable"
   reason = "Kotlin's expect block requires a Boolean; Groovy truth on a raw Int has no Kotlin equivalent"
   decided = "2026-09-19"
   ```

2. **Blocked (will not compile).** The Kotlin equivalent is a real, in-principle-portable feature, but Spockk does
   not support the construct needed to write it at all, so nothing compiles. Go to Step 2 to open or link a gap
   issue, then record it in the ledger with `status = "blocked"` and that issue's number:
   ```toml
   [["org.spockframework.smoke.parameterization.DataProviders#range pipe"]]
   status = "blocked"
   reason = "Spockk does not support data pipes over ranges"
   gap = "https://github.com/pshevche/spockk/issues/412"
   decided = "2026-09-19"
   ```
   Delete this entry once the gap issue closes and the feature can be ported for real; a `blocked` entry is
   temporary, unlike `not-applicable`.

3. **Ported but failing at runtime.** The Kotlin test compiles and runs, but fails because of a genuine Spockk
   runtime gap, not a bug in the port itself. Go to Step 2 to open or link a gap issue, then commit the test with
   `@PendingFeature`. `reason` is the bare gap issue URL, nothing else:
   ```kotlin
   @PendingFeature(reason = "https://github.com/pshevche/spockk/issues/412")
   @MigratedFrom("org.spockframework.smoke.parameterization.DataProviders#range pipe")
   fun `range pipe`() { ... }
   ```
   Prefer this over `blocked` whenever the test can compile at all: a `@PendingFeature` test reports as skipped and
   keeps the suite green, but Spock fails the build the moment it starts passing, so a closed gap cannot silently
   stay marked pending. Only fall back to `blocked` when the code genuinely does not compile.

Before concluding "this doesn't work," make sure it actually is a Spockk limitation and not a mistake in the port
itself - re-read `fidelity.md` and the matching recipe reference. A port that fails because it was written wrong is
not a gap.

## Step 2 - Deduplicate the gap issue

Many failing features share one underlying cause. Without this step, every ticket that hits the same gap opens its
own near-duplicate issue.

1. Derive a stable slug for the underlying cause, not the specific feature: `where-block-range-pipes`, not
   `data-providers-range-pipe-feature`. Two different upstream features failing for the same reason must produce
   the same slug.
2. Search open issues labeled `test-coverage::gap` for that slug. The slug lives in a marker comment inside the
   gap issue body (`<!-- gap-slug: where-block-range-pipes -->`), so search for the exact marker, not a fuzzy title
   match.
3. If found, link to it (use its issue number in `@PendingFeature`'s `reason` / the ledger's `gap` field) and stop -
   do not open a second issue for the same cause.
4. If not found, open a new issue labeled `test-coverage::gap` with the marker comment containing the slug.

## What a good gap issue contains

- **A minimal Kotlin reproducer**: the smallest snippet that demonstrates the gap, not the full ported test.
- **The Spock behavior it should match**: what upstream does, in plain language, with a link to the upstream
  source feature.
- **The upstream reference**: `<fqcn>#<feature name>` and a link to the file in `spockframework/spock`.

The reconciler regenerates each gap issue's body with the current list of features it blocks, so the issue doubles
as an impact ranking; keep the reproducer and behavior description stable so that regeneration only touches the
generated list, not your own analysis.
