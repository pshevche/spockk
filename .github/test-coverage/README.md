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
