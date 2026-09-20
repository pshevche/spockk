# Test coverage tooling

Measures how much of Spock's `spock-specs` suite Spockk reproduces, and tracks the
remaining work as GitHub issues. See `_docs/specs/2026-09-15-spock-test-migration-design.md`
for the design and `_docs/plans/2026-09-19-spock-test-coverage-tracking.md` for the build plan.

**Standard library only.** No `pip install`, no `requirements.txt`. Python 3.11 or newer.

Run the tests:

```bash
cd .github/test-coverage && python3 -m unittest discover -s tests -v
```

## Scripts

- `config.toml` - scope, area mapping and thresholds. Data, not code.
- `parser.py` - parses upstream Groovy sources into classes and features.
- `classify.py` - classifies a parsed class by recipe, area and scope.
- `inventory.py` - generates the checked-in manifest from an upstream clone.
- `scanner.py` - reads `@MigratedFrom` coverage annotations out of `spockk-specs`.
- `exclusions.py` - loads the not-applicable/blocked exclusions ledger.
- `validate.py` - the PR-time check that coverage annotations are consistent.
- `github_api.py` - a stdlib GitHub REST client.
- `render.py` - renders dashboard, area and class issue bodies.
- `reconcile.py` - computes and applies the mutation plan against live issues.
- `open-manifest-pr.sh` - opens a PR for the regenerated manifest after a sync.

## Sub-issues and the sync workflow's token

The three-tier issue hierarchy (spec section 7.1) relies on GitHub's sub-issues API to link a class issue under
its area, and an area issue under the dashboard. That API is newer than the classic issues API. The workflow
(`.github/workflows/test-coverage-sync.yml`) currently uses the default `secrets.GITHUB_TOKEN` with
`issues: write`. **This has not yet been confirmed sufficient for creating sub-issue links** (as opposed to plain
issues) - confirm it during the first supervised bootstrap run (plan Task 15), before relying on scheduled syncs.
If it is not sufficient, add a fine-grained PAT with the `Issues` (and `Sub-issues`, if listed separately)
permission as a repository secret (e.g. `TEST_COVERAGE_PAT`) and switch the workflow's `GH_TOKEN` env vars to it.
