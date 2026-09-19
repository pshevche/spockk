#!/usr/bin/env python3
"""Renders coverage dashboard, area and per-class issue bodies.

Everything between the `spockk-coverage` markers is generated and rewritten on every sync; anything
outside is preserved, so a human can add notes without the job clobbering them (spec section 7.2).
"""
import re
from dataclasses import dataclass

from exclusions import Exclusion
from scanner import Coverage

BEGIN_RE = re.compile(r"<!--\s*spockk-coverage:begin[^>]*-->", re.I)
END_RE = re.compile(r"<!--\s*spockk-coverage:end\s*-->", re.I)

UPSTREAM_BLOB_ROOT = "https://github.com/spockframework/spock/blob"


@dataclass
class Part:
    index: int
    total: int
    suffix: str
    feature_names: list[str]


def merge_generated_region(existing_body: str, new_region: str) -> str:
    begin = BEGIN_RE.search(existing_body)
    end = END_RE.search(existing_body)
    region = f"<!-- spockk-coverage:begin -->\n{new_region}\n<!-- spockk-coverage:end -->"

    if begin and end and begin.start() < end.start():
        return existing_body[: begin.start()] + region + existing_body[end.end() :]

    separator = "\n\n" if existing_body.strip() else ""
    return existing_body + separator + region


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
    spec_class: dict, coverage: dict[str, Coverage], exclusions: dict[str, Exclusion]
) -> tuple[str, str]:
    class_key = spec_class["key"]
    class_name = class_key.rsplit(".", 1)[-1]
    features = spec_class["features"]

    lines = [_feature_line(f["name"], f"{class_key}#{f['name']}", coverage, exclusions) for f in features]
    done = sum(1 for f in features if coverage.get(f"{class_key}#{f['name']}") is not None
               and coverage[f"{class_key}#{f['name']}"].status == "ported")

    header_lines = [f"**Upstream:** `{class_name}`"]
    if "recipe" in spec_class:
        header_lines.append(f"**Recipe:** {spec_class['recipe']} (see `/spock-test-coverage`)")
    if "area" in spec_class:
        header_lines.append(f"**Area:** {spec_class['area']}")

    region = "\n".join(
        [
            *header_lines,
            "",
            f"### Features ({done}/{len(features)})",
            "",
            *lines,
        ]
    )

    title = f"Migrate {class_name}"
    body = merge_generated_region("", region)
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


def render_area_issue(area: str, child_count: int) -> tuple[str, str]:
    title = f"Area: {area}"
    region = f"**Area:** {area}\n**Classes:** {child_count}"
    body = merge_generated_region("", region)
    return title, body


def render_dashboard(areas: list[str]) -> tuple[str, str]:
    title = "Spock Test Coverage Dashboard"
    lines = [f"- {area}" for area in areas]
    region = "\n".join(["### Areas", "", *lines])
    body = merge_generated_region("", region)
    return title, body
