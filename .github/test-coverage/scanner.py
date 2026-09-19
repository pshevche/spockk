#!/usr/bin/env python3
"""Scans Kotlin sources for @MigratedFrom annotations to derive coverage state."""
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Literal

ANNOTATION_NAME_RE = re.compile(r"@(\w+)")
STRING_LITERAL_RE = re.compile(r'"([^"]*)"')
GAP_RE = re.compile(r"(?:#|/issues/)(\d+)")

Annotation = tuple[str, str | None, int, int]  # name, raw args, start, end


@dataclass
class Coverage:
    status: Literal["ported", "pending"]
    gap: int | None
    source: str


def _line_of(text: str, index: int) -> int:
    return text.count("\n", 0, index) + 1


def _find_annotations(text: str) -> list[Annotation]:
    """Every `@Name(...)` in the file, with args balanced by paren depth so a
    parenthesis inside a string argument (e.g. a reason mentioning `assert()`)
    does not truncate the match early."""
    annotations: list[Annotation] = []
    for m in ANNOTATION_NAME_RE.finditer(text):
        name = m.group(1)
        i = m.end()
        while i < len(text) and text[i] in " \t":
            i += 1
        if i < len(text) and text[i] == "(":
            depth = 1
            j = i + 1
            while j < len(text) and depth > 0:
                if text[j] == "(":
                    depth += 1
                elif text[j] == ")":
                    depth -= 1
                j += 1
            annotations.append((name, text[i + 1 : j - 1], m.start(), j))
        else:
            annotations.append((name, None, m.start(), i))
    return annotations


def _group_into_declaration_blocks(text: str, annotations: list[Annotation]) -> list[list[Annotation]]:
    """Consecutive annotations separated only by whitespace belong to the same
    declaration's annotation stack."""
    blocks: list[list[Annotation]] = []
    current: list[Annotation] = []
    prev_end: int | None = None
    for annotation in annotations:
        _, _, start, end = annotation
        if prev_end is not None and text[prev_end:start].strip() != "":
            blocks.append(current)
            current = []
        current.append(annotation)
        prev_end = end
    if current:
        blocks.append(current)
    return blocks


def _scan_file(path: Path) -> dict[str, Coverage]:
    text = path.read_text(encoding="utf-8")
    found: dict[str, Coverage] = {}

    for block in _group_into_declaration_blocks(text, _find_annotations(text)):
        migrated = next((a for a in block if a[0] == "MigratedFrom"), None)
        if migrated is None:
            continue
        keys = STRING_LITERAL_RE.findall(migrated[1] or "")
        if not keys:
            continue

        pending = next((a for a in block if a[0] == "PendingFeature"), None)
        if pending is not None:
            status: Literal["ported", "pending"] = "pending"
            gap_match = GAP_RE.search(pending[1] or "")
            gap = int(gap_match.group(1)) if gap_match else None
        else:
            status = "ported"
            gap = None

        line = _line_of(text, block[0][2])
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
