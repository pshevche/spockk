#!/usr/bin/env python3
"""Renders coverage dashboard, area and per-class issue bodies.

Everything between the `spockk-coverage` markers is generated and rewritten on every sync; anything
outside is preserved, so a human can add notes without the job clobbering them (spec section 7.2).
"""
import json
import re
from dataclasses import dataclass

from exclusions import Exclusion
from scanner import Coverage

BEGIN_RE = re.compile(r"<!--\s*spockk-coverage:begin[^>]*-->", re.I)
BEGIN_KEY_RE = re.compile(r'<!--\s*spockk-coverage:begin\s+key="([^"]*)"\s*-->', re.I)
END_RE = re.compile(r"<!--\s*spockk-coverage:end\s*-->", re.I)
FEATURES_SNAPSHOT_RE = re.compile(r"<!--\s*spockk-coverage:features\s+(\{.*?\})\s*-->", re.I | re.S)

DASHBOARD_KEY = "dashboard"

SKILL_PATH = ".claude/skills/spock-test-coverage/SKILL.md"
SKILL_URL = f"https://github.com/pshevche/spockk/blob/main/{SKILL_PATH}"

AREA_SPLIT_SUFFIX_RE = re.compile(r"^(?P<base>.+)-(?P<part>\d+)$")


@dataclass
class Part:
    index: int
    total: int
    suffix: str
    feature_names: list[str]


def merge_generated_region(existing_body: str, new_region: str, key: str | None = None) -> str:
    begin = BEGIN_RE.search(existing_body)
    end = END_RE.search(existing_body)
    begin_marker = f'<!-- spockk-coverage:begin key="{key}" -->' if key else "<!-- spockk-coverage:begin -->"
    region = f"{begin_marker}\n{new_region}\n<!-- spockk-coverage:end -->"

    if begin and end and begin.start() < end.start():
        return existing_body[: begin.start()] + region + existing_body[end.end() :]

    separator = "\n\n" if existing_body.strip() else ""
    return existing_body + separator + region


def parse_class_key(body: str) -> str | None:
    match = BEGIN_KEY_RE.search(body)
    return match.group(1) if match else None


def parse_known_features(body: str) -> dict[str, str]:
    match = FEATURES_SNAPSHOT_RE.search(body)
    return json.loads(match.group(1)) if match else {}


def _feature_line(name: str, key: str, coverage: dict[str, Coverage], exclusions: dict[str, Exclusion]) -> str:
    cov = coverage.get(key)
    if cov is not None:
        if cov.status == "ported":
            return f"- [x] {name} → `{cov.source}`"
        gap_suffix = f" (blocked by #{cov.gap})" if cov.gap is not None else ""
        return f"- [ ] {name} (pending){gap_suffix}"

    exclusion = exclusions.get(key)
    if exclusion is not None:
        if exclusion.status == "not-applicable":
            return f"- [ ] ~~{name}~~ (n/a: {exclusion.reason})"
        gap_suffix = f" (blocked by #{exclusion.gap})" if exclusion.gap is not None else ""
        return f"- [ ] {name} ({exclusion.reason}){gap_suffix}"

    return f"- [ ] {name}"


def render_class_issue(
    spec_class: dict, coverage: dict[str, Coverage], exclusions: dict[str, Exclusion], upstream: dict | None = None
) -> tuple[str, str]:
    class_key = spec_class["key"]
    class_name = class_key.rsplit(".", 1)[-1]
    features = spec_class["features"]

    lines = [_feature_line(f["name"], f"{class_key}#{f['name']}", coverage, exclusions) for f in features]
    done = sum(1 for f in features if coverage.get(f"{class_key}#{f['name']}") is not None
               and coverage[f"{class_key}#{f['name']}"].status == "ported")

    if upstream:
        blob_url = f"https://github.com/{upstream['repo']}/blob/{upstream['sha']}/{spec_class['path']}"
        upstream_line = f"**Upstream:** [`{class_name}`]({blob_url})"
    else:
        upstream_line = f"**Upstream:** `{class_name}`"

    intro_parts = ["Ports this upstream Spock test class into an equivalent Spockk test"]
    if "recipe" in spec_class:
        intro_parts.append(f"using the **{spec_class['recipe']}** recipe")
    intro = " ".join(intro_parts) + f" (see [`{SKILL_PATH}`]({SKILL_URL}) for how)."
    if "area" in spec_class:
        intro += f" Part of the **{spec_class['area']}** test coverage area."

    header_lines = [intro, "", upstream_line]

    features_snapshot = json.dumps({f["name"]: f["hash"] for f in features}, sort_keys=True)
    region = "\n".join(
        [
            *header_lines,
            "",
            f"### Features ({done}/{len(features)})",
            "",
            *lines,
            "",
            f"<!-- spockk-coverage:features {features_snapshot} -->",
        ]
    )

    title = f"Migrate {class_key}"
    body = merge_generated_region("", region, key=class_key)
    return title, body


def split_class(spec_class: dict, threshold: int) -> list[Part]:
    features = spec_class["features"]
    if len(features) <= threshold:
        return [Part(index=1, total=1, suffix="", feature_names=[f["name"] for f in features])]

    total_parts = (len(features) + threshold - 1) // threshold
    parts = []
    for i in range(total_parts):
        chunk = features[i * threshold : (i + 1) * threshold]
        parts.append(
            Part(
                index=i + 1,
                total=total_parts,
                suffix=f"({i + 1}/{total_parts})",
                feature_names=[f["name"] for f in chunk],
            )
        )
    return parts


def render_area_issue(area: str, child_count: int, packages: list[str] = ()) -> tuple[str, str]:
    split = AREA_SPLIT_SUFFIX_RE.match(area)
    title = f"Test coverage area: {split.group('base')} (part {split.group('part')})" if split else f"Test coverage area: {area}"

    package_list = ", ".join(f"`{p}`" for p in packages) if packages else "a related set of upstream classes"
    region = "\n".join(
        [
            "One of several areas in the Spock → Spockk test coverage effort: upstream Spock test "
            "classes grouped by package, split further when an area would otherwise exceed GitHub's "
            f"100-sub-issue limit. This area covers {package_list}.",
            "",
            f"**Classes:** {child_count} (see sub-issues below)",
        ]
    )
    body = merge_generated_region("", region, key=area)
    return title, body


def render_dashboard(area_counts: dict[str, int]) -> tuple[str, str]:
    title = "Spock → Spockk Test Coverage Dashboard"
    lines = [f"- {area} — {count} classes" for area, count in sorted(area_counts.items())]
    region = "\n".join(
        [
            "**What this is:** Spockk reimplements Spock's BDD syntax in Kotlin. To prove it actually "
            "behaves like Spock, we port Spock's own test suite (`spock-specs`) feature-by-feature into "
            "Spockk tests. This issue tracks that effort across every upstream test class, grouped into "
            "areas below.",
            "",
            "**Hierarchy:** this dashboard → one issue per area → one issue per upstream class, "
            "linked as GitHub sub-issues so progress bars roll up automatically.",
            "",
            "**Checkbox states on a class issue:** unchecked (not started) · checked (ported) · "
            "~~struck through~~ (not applicable to Kotlin) · \"(blocked by #N)\" (needs a Spockk fix first).",
            "",
            "### Areas",
            "",
            *lines,
        ]
    )
    body = merge_generated_region("", region, key=DASHBOARD_KEY)
    return title, body
