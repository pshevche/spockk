"""Regex-based parser for upstream Spock spec sources.

Deliberately not a Groovy parser (see spec section 5.3): good enough for the
shapes the real tree contains, and avoids a Groovy runtime dependency in CI.
"""
import hashlib
import re
from dataclasses import dataclass, field
from pathlib import Path

PACKAGE_RE = re.compile(r'^package\s+([\w.]+)', re.M)

CLASS_RE = re.compile(
    r'^(?P<abstract>abstract\s+)?class\s+(?P<name>[A-Za-z0-9_]+)\b[^{]*?\bextends\s+(?P<base>[A-Za-z0-9_.]+)',
    re.M,
)

FEATURE_RE = re.compile(r'^\s+(?:def|void)\s+(?P<q>["\'])(?P<name>.+?)(?P=q)\s*\(', re.M)

LINE_COMMENT_RE = re.compile(r'//[^\n]*')
BLOCK_COMMENT_RE = re.compile(r'/\*.*?\*/', re.S)
WHITESPACE_RE = re.compile(r'\s+')


@dataclass
class Feature:
    name: str
    hash: str


@dataclass
class SpecClass:
    key: str
    path: str
    name: str
    base: str
    features: list[Feature] = field(default_factory=list)
    body: str = ""


def body_hash(text: str) -> str:
    """Digest of a feature body's normalised text: comments and formatting stripped."""
    stripped = BLOCK_COMMENT_RE.sub('', text)
    stripped = LINE_COMMENT_RE.sub('', stripped)
    normalised = WHITESPACE_RE.sub(' ', stripped).strip()
    return hashlib.sha256(normalised.encode('utf-8')).hexdigest()[:8]


def parse_source(source: str, rel_path: str) -> list[SpecClass]:
    """Parse Groovy source text into its concrete spec classes."""
    package_match = PACKAGE_RE.search(source)
    package = package_match.group(1) if package_match else ''

    class_matches = list(CLASS_RE.finditer(source))
    classes: list[SpecClass] = []

    for i, match in enumerate(class_matches):
        if match.group('abstract'):
            continue

        body_start = match.end()
        body_end = class_matches[i + 1].start() if i + 1 < len(class_matches) else len(source)
        body = source[body_start:body_end]

        features = [
            Feature(name=fm.group('name'), hash=body_hash(fm.group(0)))
            for fm in FEATURE_RE.finditer(body)
        ]
        if not features:
            continue

        name = match.group('name')
        key = f"{package}.{name}" if package else name
        classes.append(SpecClass(key=key, path=rel_path, name=name, base=match.group('base'),
                                  features=features, body=body))

    return classes


def parse_file(path: Path, source_root: Path) -> list[SpecClass]:
    """Parse a single Groovy file. `path` is stored relative to `source_root`, so callers
    pass the upstream repo root here (not the deeper src/test/groovy directory) to get
    manifest paths that link correctly to GitHub."""
    source = path.read_text(encoding='utf-8', errors='replace')
    rel_path = str(path.relative_to(source_root))
    return parse_source(source, rel_path)
