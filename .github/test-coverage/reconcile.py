#!/usr/bin/env python3
"""Reconciles the freshly regenerated inventory against live coverage issues.

`plan()` is pure: it never touches the network, so a dry run and a real run compute the exact same
mutation list. `apply()` is the only place that talks to GitHub, which is what makes the dry run
trustworthy (spec section 11, Task 14).
"""
import re
import time
from dataclasses import dataclass, field

from exclusions import Exclusion
from render import DASHBOARD_KEY, parse_class_key, parse_known_features, render_area_issue, render_class_issue, render_dashboard
from scanner import Coverage

BLOCKED_BY_RE = re.compile(r"<!--\s*spockk-coverage:blocked-by\s+([\d,\s]+?)\s*-->", re.I)

AREA_LABEL = "test-coverage::area"
BLOCKED_LABEL = "test-coverage::blocked"
DASHBOARD_LABEL = "test-coverage::dashboard"
DRIFT_LABEL = "test-coverage::drift"
SPEC_LABEL = "test-coverage::spec"

DEFAULT_AREA_SPLIT_THRESHOLD = 80


@dataclass
class ExistingIssue:
    number: int
    id: int
    state: str  # "open" | "closed"
    body: str
    labels: list[str] = field(default_factory=list)


@dataclass
class ExistingRollup:
    """An existing area or dashboard issue: same shape, so one dataclass covers both tiers."""

    number: int
    id: int
    body: str


@dataclass
class Mutation:
    kind: str
    target: str
    payload: dict
    reason: str


def index_issues(raw_issues: list[dict]) -> dict[str, ExistingIssue]:
    indexed: dict[str, ExistingIssue] = {}
    for raw in raw_issues:
        key = parse_class_key(raw.get("body", ""))
        if key is None:
            continue
        indexed[key] = ExistingIssue(
            number=raw["number"],
            id=raw["id"],
            state=raw["state"],
            body=raw.get("body", ""),
            labels=list(raw.get("labels", [])),
        )
    return indexed


def index_area_issues(raw_issues: list[dict]) -> dict[str, ExistingRollup]:
    indexed: dict[str, ExistingRollup] = {}
    for raw in raw_issues:
        key = parse_class_key(raw.get("body", ""))
        if key is None:
            continue
        indexed[key] = ExistingRollup(number=raw["number"], id=raw["id"], body=raw.get("body", ""))
    return indexed


def find_dashboard_issue(raw_issues: list[dict]) -> ExistingRollup | None:
    for raw in raw_issues:
        if parse_class_key(raw.get("body", "")) == DASHBOARD_KEY:
            return ExistingRollup(number=raw["number"], id=raw["id"], body=raw.get("body", ""))
    return None


def linked_keys(raw_sub_issues: list[dict]) -> set[str]:
    """Class/area keys already attached as sub-issues of some parent, parsed from each sub-issue's
    own body marker (the same one `index_issues`/`index_area_issues` key on)."""
    keys = {parse_class_key(raw.get("body", "")) for raw in raw_sub_issues}
    keys.discard(None)
    return keys


def _cited_gaps(body: str) -> set[int]:
    match = BLOCKED_BY_RE.search(body)
    if not match:
        return set()
    return {int(n) for n in match.group(1).split(",") if n.strip()}


def _split_areas(manifest: dict, area_split_threshold: int) -> dict[str, str]:
    """class_key -> possibly-suffixed area name, split deterministically by sorted class key."""
    by_area: dict[str, list[str]] = {}
    for spec_class in manifest["classes"]:
        by_area.setdefault(spec_class["area"], []).append(spec_class["key"])

    resolved: dict[str, str] = {}
    for area, keys in by_area.items():
        if len(keys) <= area_split_threshold:
            for key in keys:
                resolved[key] = area
            continue
        for key in sorted(keys):
            index = sorted(keys).index(key)
            sub_area = f"{area}-{index // area_split_threshold + 1}"
            resolved[key] = sub_area
    return resolved


def plan(
    manifest: dict,
    coverage: dict[str, Coverage],
    exclusions: dict[str, Exclusion],
    existing_issues: dict[str, ExistingIssue],
    closed_gaps: set[int] = frozenset(),
    area_split_threshold: int = DEFAULT_AREA_SPLIT_THRESHOLD,
) -> list[Mutation]:
    mutations: list[Mutation] = []
    resolved_areas = _split_areas(manifest, area_split_threshold)
    current_keys = {c["key"] for c in manifest["classes"]}

    for spec_class in manifest["classes"]:
        class_key = spec_class["key"]
        area = resolved_areas[class_key]
        rendered_class = {**spec_class, "area": area}
        existing = existing_issues.get(class_key)

        if existing is None:
            title, body = render_class_issue(rendered_class, coverage, exclusions)
            mutations.append(
                Mutation(
                    kind="create_issue",
                    target=class_key,
                    payload={"title": title, "body": body, "area": area, "labels": [SPEC_LABEL]},
                    reason="new class",
                )
            )
            continue

        known_features = parse_known_features(existing.body)
        current_features = {f["name"]: f["hash"] for f in spec_class["features"]}

        deleted_names = set(known_features) - set(current_features)
        added_names = set(current_features) - set(known_features)

        renames: list[tuple[str, str]] = []
        for deleted_name in sorted(deleted_names):
            for added_name in sorted(added_names):
                if current_features[added_name] == known_features[deleted_name]:
                    renames.append((deleted_name, added_name))
                    break
            if renames and renames[-1][0] == deleted_name:
                added_names.discard(renames[-1][1])

        renamed_old_names = {old for old, _ in renames}
        deleted_names -= renamed_old_names

        for old_name, new_name in renames:
            mutations.append(
                Mutation(
                    kind="rewrite_key",
                    target=class_key,
                    payload={"old": f"{class_key}#{old_name}", "new": f"{class_key}#{new_name}"},
                    reason=f"renamed from '{old_name}' to '{new_name}' (matching body hash)",
                )
            )
            mutations.append(
                Mutation(
                    kind="comment",
                    target=class_key,
                    payload={
                        "issue": existing.number,
                        "body": (
                            f"Upstream feature `{old_name}` appears to have been renamed to `{new_name}` "
                            f"(same body hash). Please update `@MigratedFrom(\"{class_key}#{old_name}\")` "
                            f"to `@MigratedFrom(\"{class_key}#{new_name}\")`."
                        ),
                    },
                    reason="rename detected",
                )
            )

        for name in sorted(deleted_names):
            mutations.append(
                Mutation(
                    kind="comment",
                    target=class_key,
                    payload={
                        "issue": existing.number,
                        "body": f"Upstream feature `{name}` no longer exists. Keeping the ported test.",
                    },
                    reason="feature deleted upstream",
                )
            )

        drifted_names = sorted(
            name
            for name in current_features
            if name in known_features and known_features[name] != current_features[name]
        )
        if drifted_names:
            mutations.append(
                Mutation(
                    kind="add_label",
                    target=class_key,
                    payload={"issue": existing.number, "label": DRIFT_LABEL},
                    reason=f"body hash changed for {', '.join(drifted_names)}",
                )
            )
            for name in drifted_names:
                mutations.append(
                    Mutation(
                        kind="comment",
                        target=class_key,
                        payload={
                            "issue": existing.number,
                            "body": f"Upstream feature `{name}`'s body changed; the port may be stale. Please review.",
                        },
                        reason="possible drift",
                    )
                )

        if added_names and existing.state == "closed":
            mutations.append(
                Mutation(
                    kind="reopen_issue",
                    target=class_key,
                    payload={"issue": existing.number},
                    reason="new feature added to a closed issue",
                )
            )
            mutations.append(
                Mutation(
                    kind="comment",
                    target=class_key,
                    payload={
                        "issue": existing.number,
                        "body": f"New upstream feature(s) added: {', '.join(sorted(added_names))}.",
                    },
                    reason="new feature appended",
                )
            )

        if BLOCKED_LABEL in existing.labels:
            cited = _cited_gaps(existing.body)
            if cited and cited <= closed_gaps:
                mutations.append(
                    Mutation(
                        kind="remove_label",
                        target=class_key,
                        payload={"issue": existing.number, "label": BLOCKED_LABEL},
                        reason=f"every cited gap ({', '.join(str(g) for g in sorted(cited))}) is closed",
                    )
                )

        # A rename is detected before the source's @MigratedFrom string is updated, so coverage/
        # exclusions still carry the old key. Render under the new key too, so progress isn't lost
        # for the render(s) between detection and the human updating the annotation.
        effective_coverage = dict(coverage)
        effective_exclusions = dict(exclusions)
        for old_name, new_name in renames:
            old_key, new_key = f"{class_key}#{old_name}", f"{class_key}#{new_name}"
            if old_key in coverage:
                effective_coverage[new_key] = coverage[old_key]
            if old_key in exclusions:
                effective_exclusions[new_key] = exclusions[old_key]

        title, body = render_class_issue(rendered_class, effective_coverage, effective_exclusions)
        mutations.append(
            Mutation(
                kind="update_issue",
                target=class_key,
                payload={"issue": existing.number, "id": existing.id, "title": title, "body": body},
                reason="regenerate generated region",
            )
        )

    for class_key, existing in existing_issues.items():
        if class_key not in current_keys and existing.state != "closed":
            mutations.append(
                Mutation(
                    kind="close_issue",
                    target=class_key,
                    payload={"issue": existing.number},
                    reason="upstream class deleted; leaving ported tests in place",
                )
            )

    return mutations


def plan_hierarchy(
    manifest: dict,
    existing_areas: dict[str, ExistingRollup],
    existing_dashboard: ExistingRollup | None,
    existing_area_links: dict[str, set[str]],
    existing_dashboard_links: set[str],
    area_split_threshold: int = DEFAULT_AREA_SPLIT_THRESHOLD,
) -> list[Mutation]:
    """The three-tier hierarchy (spec section 7.1): one dashboard issue, one issue per (possibly
    split) area, and a sub-issue link from every spec issue up through its area to the dashboard.

    Pure like `plan()`: `existing_area_links`/`existing_dashboard_links` (which keys are already
    attached as a sub-issue of which parent) are precomputed by the caller so this never needs the
    network, and only emits a `link_sub_issue` mutation for a pair that isn't linked yet.
    """
    mutations: list[Mutation] = []
    resolved_areas = _split_areas(manifest, area_split_threshold)

    child_counts: dict[str, int] = {}
    for area_name in resolved_areas.values():
        child_counts[area_name] = child_counts.get(area_name, 0) + 1
    area_names = sorted(child_counts)

    dashboard_title, dashboard_body = render_dashboard(area_names)
    if existing_dashboard is None:
        mutations.append(
            Mutation(
                kind="create_dashboard_issue",
                target=DASHBOARD_KEY,
                payload={"title": dashboard_title, "body": dashboard_body, "labels": [DASHBOARD_LABEL]},
                reason="dashboard does not exist yet",
            )
        )
    else:
        mutations.append(
            Mutation(
                kind="update_dashboard_issue",
                target=DASHBOARD_KEY,
                payload={
                    "issue": existing_dashboard.number,
                    "id": existing_dashboard.id,
                    "title": dashboard_title,
                    "body": dashboard_body,
                },
                reason="regenerate generated region",
            )
        )

    for area_name in area_names:
        title, body = render_area_issue(area_name, child_counts[area_name])
        existing = existing_areas.get(area_name)
        if existing is None:
            mutations.append(
                Mutation(
                    kind="create_area_issue",
                    target=area_name,
                    payload={"title": title, "body": body, "labels": [AREA_LABEL]},
                    reason="new area",
                )
            )
        else:
            mutations.append(
                Mutation(
                    kind="update_area_issue",
                    target=area_name,
                    payload={"issue": existing.number, "id": existing.id, "title": title, "body": body},
                    reason="regenerate generated region",
                )
            )

    for spec_class in manifest["classes"]:
        class_key = spec_class["key"]
        area_name = resolved_areas[class_key]
        if class_key not in existing_area_links.get(area_name, set()):
            mutations.append(
                Mutation(
                    kind="link_sub_issue",
                    target=f"{area_name}->{class_key}",
                    payload={
                        "parent_ns": "area",
                        "parent_key": area_name,
                        "child_ns": "spec",
                        "child_key": class_key,
                    },
                    reason=f"attach spec issue under area '{area_name}'",
                )
            )

    for area_name in area_names:
        if area_name not in existing_dashboard_links:
            mutations.append(
                Mutation(
                    kind="link_sub_issue",
                    target=f"dashboard->{area_name}",
                    payload={
                        "parent_ns": "dashboard",
                        "parent_key": DASHBOARD_KEY,
                        "child_ns": "area",
                        "child_key": area_name,
                    },
                    reason="attach area issue under the dashboard",
                )
            )

    return mutations


MUTATION_PACING_SECONDS = 1


def apply(mutations: list[Mutation], github, sleep=time.sleep) -> None:
    """Paces one request-ish per mutation; GitHub throttles bulk content creation (issues,
    comments, labels) more strictly than plain API reads, via a secondary rate limit that
    `GitHub._request` retries but that pacing avoids tripping in the first place.

    Also threads a namespaced ("spec"/"area"/"dashboard") key -> (issue number, issue id) map,
    seeded as each issue is created or updated, so a later `link_sub_issue` mutation can resolve
    both ends whether they're pre-existing or were just created earlier in this same run."""
    resolved: dict[str, tuple[int, int]] = {}

    def _create(ns):
        created = github.post(
            "/issues",
            {
                "title": mutation.payload["title"],
                "body": mutation.payload["body"],
                "labels": mutation.payload["labels"],
            },
        )
        resolved[f"{ns}:{mutation.target}"] = (created["number"], created["id"])

    def _update(ns):
        github.patch(
            f"/issues/{mutation.payload['issue']}",
            {"title": mutation.payload["title"], "body": mutation.payload["body"]},
        )
        resolved[f"{ns}:{mutation.target}"] = (mutation.payload["issue"], mutation.payload["id"])

    for mutation in mutations:
        if mutation.kind == "create_issue":
            _create("spec")
        elif mutation.kind == "update_issue":
            _update("spec")
        elif mutation.kind == "reopen_issue":
            github.patch(f"/issues/{mutation.payload['issue']}", {"state": "open"})
        elif mutation.kind == "close_issue":
            github.patch(f"/issues/{mutation.payload['issue']}", {"state": "closed"})
        elif mutation.kind == "comment":
            github.post(f"/issues/{mutation.payload['issue']}/comments", {"body": mutation.payload["body"]})
        elif mutation.kind == "add_label":
            github.post(f"/issues/{mutation.payload['issue']}/labels", {"labels": [mutation.payload["label"]]})
        elif mutation.kind == "remove_label":
            issue = github.get(f"/issues/{mutation.payload['issue']}")
            remaining = [label["name"] for label in issue["labels"] if label["name"] != mutation.payload["label"]]
            github.patch(f"/issues/{mutation.payload['issue']}", {"labels": remaining})
        elif mutation.kind == "rewrite_key":
            pass  # manifest key rewrites are applied to the manifest file, not to GitHub
        elif mutation.kind == "create_dashboard_issue":
            _create("dashboard")
        elif mutation.kind == "update_dashboard_issue":
            _update("dashboard")
        elif mutation.kind == "create_area_issue":
            _create("area")
        elif mutation.kind == "update_area_issue":
            _update("area")
        elif mutation.kind == "link_sub_issue":
            parent_number, _ = resolved[f"{mutation.payload['parent_ns']}:{mutation.payload['parent_key']}"]
            _, child_id = resolved[f"{mutation.payload['child_ns']}:{mutation.payload['child_key']}"]
            github.add_sub_issue(parent_number, child_id)

        if mutation.kind != "rewrite_key":
            sleep(MUTATION_PACING_SECONDS)


DEFAULT_MAX_MUTATIONS = 50


class BlastRadiusExceeded(Exception):
    pass


def check_blast_radius(mutations: list, max_mutations: int) -> None:
    if len(mutations) > max_mutations:
        raise BlastRadiusExceeded(
            f"{len(mutations)} mutations exceeds --max-mutations {max_mutations}; "
            "a routine run wanting to touch this many issues is a bug, not a bootstrap. "
            "Pass a higher --max-mutations explicitly if this is really the one-time bootstrap."
        )


def run(
    manifest: dict,
    coverage: dict[str, Coverage],
    exclusions: dict[str, Exclusion],
    existing_issues: dict[str, ExistingIssue],
    github,
    closed_gaps: set[int] = frozenset(),
    existing_areas: dict[str, ExistingRollup] = None,
    existing_dashboard: ExistingRollup | None = None,
    existing_area_links: dict[str, set[str]] = None,
    existing_dashboard_links: set[str] = frozenset(),
    dry_run: bool = False,
    max_mutations: int = DEFAULT_MAX_MUTATIONS,
    sleep=time.sleep,
) -> list[Mutation]:
    mutations = plan(manifest, coverage, exclusions, existing_issues, closed_gaps=closed_gaps)
    mutations += plan_hierarchy(
        manifest,
        existing_areas or {},
        existing_dashboard,
        existing_area_links or {},
        existing_dashboard_links,
    )

    if dry_run:
        for m in mutations:
            print(f"{m.kind:14s} {m.target}: {m.reason}")
        return mutations

    check_blast_radius(mutations, max_mutations)
    apply(mutations, github, sleep=sleep)
    return mutations


def main(argv: list[str]) -> int:
    import argparse
    import json
    import os
    from pathlib import Path

    from exclusions import load_exclusions
    from github_api import GitHub
    from scanner import scan

    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--max-mutations", type=int, default=DEFAULT_MAX_MUTATIONS)
    args = parser.parse_args(argv)

    repo_root = Path(__file__).parents[2]
    manifest = json.loads((repo_root / "_docs" / "test-coverage" / "spock-inventory.json").read_text())
    coverage = scan([repo_root / "spockk-specs" / "src" / "test", repo_root / "spockk-specs" / "src" / "testFixtures"])
    exclusions = load_exclusions()

    github = GitHub(token=os.environ["GH_TOKEN"], repo="pshevche/spockk")
    raw_issues = github.paginate(f"/issues?labels={SPEC_LABEL}&state=all&per_page=100")
    existing_issues = index_issues(raw_issues)
    closed_gaps = {
        issue["number"] for issue in github.paginate("/issues?state=closed&per_page=100")
    }

    raw_area_issues = github.paginate(f"/issues?labels={AREA_LABEL}&state=all&per_page=100")
    existing_areas = index_area_issues(raw_area_issues)
    raw_dashboard_issues = github.paginate(f"/issues?labels={DASHBOARD_LABEL}&state=all&per_page=100")
    existing_dashboard = find_dashboard_issue(raw_dashboard_issues)

    existing_area_links = {
        area_name: linked_keys(github.get_sub_issues(area.number)) for area_name, area in existing_areas.items()
    }
    existing_dashboard_links = (
        linked_keys(github.get_sub_issues(existing_dashboard.number)) if existing_dashboard else set()
    )

    try:
        run(
            manifest,
            coverage,
            exclusions,
            existing_issues,
            github,
            closed_gaps=closed_gaps,
            existing_areas=existing_areas,
            existing_dashboard=existing_dashboard,
            existing_area_links=existing_area_links,
            existing_dashboard_links=existing_dashboard_links,
            dry_run=args.dry_run,
            max_mutations=args.max_mutations,
        )
    except BlastRadiusExceeded as e:
        print(f"error: {e}")
        return 1
    return 0


if __name__ == "__main__":
    import sys

    sys.exit(main(sys.argv[1:]))
