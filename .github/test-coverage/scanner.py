#!/usr/bin/env python3
"""Scans Kotlin sources for @MigratedFrom annotations to derive coverage state."""
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Literal

MIGRATED_FROM_RE = re.compile(r"@MigratedFrom\(([^)]*)\)", re.S)
STRING_LITERAL_RE = re.compile(r'"([^"]*)"')
PENDING_FEATURE_RE = re.compile(r"@PendingFeature\(([^)]*)\)", re.S)
GAP_RE = re.compile(r"#(\d+)")
DECLARATION_RE = re.compile(
    r"((?:@\w+(?:\([^)]*\))?\s*)+)(?:fun|class)\b", re.S
)


@dataclass
class Coverage:
    status: Literal["ported", "pending"]
    gap: int | None
    source: str


def _line_of(text: str, index: int) -> int:
    return text.count("\n", 0, index) + 1


def _scan_file(path: Path) -> dict[str, Coverage]:
    text = path.read_text(encoding="utf-8")
    found: dict[str, Coverage] = {}
    for decl_match in DECLARATION_RE.finditer(text):
        annotations = decl_match.group(1)
        migrated_match = MIGRATED_FROM_RE.search(annotations)
        if not migrated_match:
            continue
        keys = STRING_LITERAL_RE.findall(migrated_match.group(1))
        if not keys:
            continue

        pending_match = PENDING_FEATURE_RE.search(annotations)
        if pending_match:
            status: Literal["ported", "pending"] = "pending"
            gap_match = GAP_RE.search(pending_match.group(1))
            gap = int(gap_match.group(1)) if gap_match else None
        else:
            status = "ported"
            gap = None

        line = _line_of(text, decl_match.start(1))
        source = f"{path.name}:{line}"
        for key in keys:
            found[key] = Coverage(status=status, gap=gap, source=source)
    return found


def scan(roots: list[Path]) -> dict[str, Coverage]:
    coverage: dict[str, Coverage] = {}
    for root in roots:
        for path in sorted(root.rglob("*.kt")):
            coverage.update(_scan_file(path))
    return coverage
