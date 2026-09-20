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

TRIPLE_QUOTED_STRING_RE = re.compile(r'"""(?:[^\\]|\\.)*?"""|\'\'\'(?:[^\\]|\\.)*?\'\'\'', re.S)


def _mask_embedded_source(source: str) -> str:
    """Blanks out triple-quoted string literals, used throughout spock-specs to compile literal
    Groovy source at runtime (e.g. `EmbeddedSpecification` tests), so a `class Foo extends ...`
    inside one isn't mistaken for a real top-level class. Replaces every non-newline character
    with a space so match offsets into the original source stay valid."""
    return TRIPLE_QUOTED_STRING_RE.sub(lambda m: re.sub(r'[^\n]', ' ', m.group(0)), source)


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
    is_abstract: bool = False


def body_hash(text: str) -> str:
    """Digest of a feature body's normalised text: comments and formatting stripped."""
    stripped = BLOCK_COMMENT_RE.sub('', text)
    stripped = LINE_COMMENT_RE.sub('', stripped)
    normalised = WHITESPACE_RE.sub(' ', stripped).strip()
    return hashlib.sha256(normalised.encode('utf-8')).hexdigest()[:8]


def parse_source(source: str, rel_path: str) -> list[SpecClass]:
    """Parse Groovy source text into its spec classes, concrete and abstract alike.

    Abstract classes are kept (tagged `is_abstract`) rather than dropped, and a class with zero
    of its *own* feature methods is kept too: Spock allows a feature declared directly in an
    abstract base, run by every concrete subclass, and a concrete subclass can inherit its
    entire feature set this way while declaring none of its own (only overriding non-feature
    helper methods). `inventory.py` resolves that inheritance and drops only whatever still has
    no feature at all once inheritance is accounted for.
    """
    package_match = PACKAGE_RE.search(source)
    package = package_match.group(1) if package_match else ''

    class_scan_source = _mask_embedded_source(source)
    class_matches = list(CLASS_RE.finditer(class_scan_source))
    classes: list[SpecClass] = []

    for i, match in enumerate(class_matches):
        body_start = match.end()
        body_end = class_matches[i + 1].start() if i + 1 < len(class_matches) else len(source)
        body = source[body_start:body_end]
        masked_body = class_scan_source[body_start:body_end]

        # Scan the masked body so a feature-like `def "..."()` inside an embedded fixture string
        # isn't mistaken for one of this class's own features, but hash the unmasked text: real
        # feature bodies must stay sensitive to their actual content.
        features = [
            Feature(name=fm.group('name'), hash=body_hash(body[fm.start():fm.end()]))
            for fm in FEATURE_RE.finditer(masked_body)
        ]

        name = match.group('name')
        key = f"{package}.{name}" if package else name
        classes.append(SpecClass(key=key, path=rel_path, name=name, base=match.group('base'),
                                  features=features, body=body,
                                  is_abstract=bool(match.group('abstract'))))

    return classes


def parse_file(path: Path, source_root: Path) -> list[SpecClass]:
    """Parse a single Groovy file. `path` is stored relative to `source_root`, so callers
    pass the upstream repo root here (not the deeper src/test/groovy directory) to get
    manifest paths that link correctly to GitHub."""
    source = path.read_text(encoding='utf-8', errors='replace')
    rel_path = str(path.relative_to(source_root))
    return parse_source(source, rel_path)
