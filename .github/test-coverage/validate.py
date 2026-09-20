#!/usr/bin/env python3
"""Validates @MigratedFrom coverage against the inventory and exclusions ledger.

Fails the check when a key does not exist in the inventory, when a key is both
ported and marked not-applicable, or when a @PendingFeature references a gap
issue that has since closed (spec section 10.3).
"""
import re
import sys
import urllib.request
import json
from dataclasses import dataclass
from pathlib import Path

from exclusions import Exclusion, load_exclusions
from scanner import Coverage, scan

REPO = "pshevche/spockk"
REPO_ROOT = Path(__file__).parents[2]
SPOCKK_SPECS_ROOTS = [
    REPO_ROOT / "spockk-specs" / "src" / "test",
    REPO_ROOT / "spockk-specs" / "src" / "testFixtures",
]

ISSUE_URL_RE = re.compile(r"^https://github\.com/pshevche/spockk/issues/\d+$")


@dataclass
class Violation:
    message: str
    source: str


def _inventory_keys(manifest: dict) -> set[str]:
    keys = set()
    for cls in manifest["classes"]:
        for feature in cls["features"]:
            keys.add(f"{cls['key']}#{feature['name']}")
    return keys


def validate(
    manifest: dict,
    coverage: dict[str, Coverage],
    exclusions: dict[str, Exclusion],
    closed_gaps: set[int],
) -> list[Violation]:
    violations: list[Violation] = []
    inventory_keys = _inventory_keys(manifest)

    for key, cov in coverage.items():
        if key not in inventory_keys:
            violations.append(Violation(
                message=f"{key!r} is not in the inventory (typo, or upstream removed it)",
                source=cov.source,
            ))
            continue

        exclusion = exclusions.get(key)
        if exclusion is not None and exclusion.status == "not-applicable":
            violations.append(Violation(
                message=f"{key!r} is both ported and marked not-applicable",
                source=cov.source,
            ))

        if cov.status == "pending" and cov.gap in closed_gaps:
            violations.append(Violation(
                message=f"{key!r} is pending on gap #{cov.gap}, but gap #{cov.gap} is closed",
                source=cov.source,
            ))

        if cov.status == "pending" and not (cov.reason and ISSUE_URL_RE.match(cov.reason)):
            violations.append(Violation(
                message=(
                    f"{key!r}'s @PendingFeature reason must be a bare issue URL "
                    f"(https://github.com/pshevche/spockk/issues/N), got {cov.reason!r}"
                ),
                source=cov.source,
            ))

    return violations


def _fetch_closed_gaps(token: str | None) -> set[int]:
    if not token:
        return set()
    closed: set[int] = set()
    page = 1
    while True:
        url = (
            f"https://api.github.com/repos/{REPO}/issues"
            f"?state=closed&per_page=100&page={page}"
        )
        request = urllib.request.Request(
            url,
            headers={
                "Authorization": f"Bearer {token}",
                "Accept": "application/vnd.github+json",
            },
        )
        with urllib.request.urlopen(request) as response:
            issues = json.load(response)
        if not issues:
            break
        closed.update(issue["number"] for issue in issues)
        page += 1
    return closed


MANIFEST_PATH = REPO_ROOT / "_docs" / "test-coverage" / "spock-inventory.json"


def main(argv: list[str]) -> int:
    import os

    manifest = json.loads(MANIFEST_PATH.read_text())
    coverage = scan(SPOCKK_SPECS_ROOTS)
    exclusions = load_exclusions()
    closed_gaps = _fetch_closed_gaps(os.environ.get("GH_TOKEN"))

    violations = validate(manifest, coverage, exclusions, closed_gaps)
    if violations:
        for v in violations:
            print(f"::error file={v.source}::{v.message}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
