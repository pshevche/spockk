"""Generate the checked-in upstream spock-specs inventory manifest (spec section 5)."""
import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

from classify import area_for, classify_source, is_excluded, load_config
from parser import SpecClass, parse_file


class GuardTripped(Exception):
    """Raised when the parsed class count moves more than the configured tolerance."""


# classify_source's own base==... checks depend on seeing these names verbatim as a chain's
# terminal base. spock-specs happens to check in real source for two of them (EmbeddedSpecification,
# ConditionRenderingSpec are themselves plain Specification subclasses living in the scanned tree,
# with no features of their own), so without this they'd be walked straight through and the
# classification signal would be lost several layers up instead.
FRAMEWORK_ROOT_NAMES = {"Specification", "EmbeddedSpecification", "ConditionRenderingSpec"}


def _index_classes(classes: list[SpecClass]):
    by_file_and_name: dict[tuple[str, str], SpecClass] = {}
    by_name: dict[str, list[SpecClass]] = {}
    for spec_class in classes:
        by_file_and_name[(spec_class.path, spec_class.name)] = spec_class
        by_name.setdefault(spec_class.name, []).append(spec_class)
    return by_file_and_name, by_name


def _resolve_ancestor(spec_class: SpecClass, by_file_and_name, by_name) -> SpecClass | None:
    """The parsed `SpecClass` `spec_class.base` refers to, or None when it names a framework
    root (see `FRAMEWORK_ROOT_NAMES`) or something else outside the parsed corpus entirely. A
    same-file match is preferred, since most base/subclass pairs are private scaffolding
    declared side by side and names like `Base`/`Derived` are reused, unrelated, across many
    files. A cross-file match is only trusted when the name is globally unique."""
    if spec_class.base in FRAMEWORK_ROOT_NAMES:
        return None
    same_file = by_file_and_name.get((spec_class.path, spec_class.base))
    if same_file is not None:
        return same_file
    candidates = by_name.get(spec_class.base, [])
    return candidates[0] if len(candidates) == 1 else None


def _accumulate_inheritance(spec_class: SpecClass, by_file_and_name, by_name, _seen=None):
    """Features and body text inherited from every resolvable ancestor (root-most first),
    followed by `spec_class`'s own; a feature declared directly on an abstract base is one
    every concrete descendant runs, so it must count for each of them. Also returns the base
    name classification should use: the immediate base of the outermost resolvable ancestor,
    since that is what actually determines the recipe (e.g. an EmbeddedSpecification several
    abstract layers up)."""
    seen = _seen if _seen is not None else set()
    if id(spec_class) in seen:
        return list(spec_class.features), spec_class.body, spec_class.base  # defend a cycle
    seen.add(id(spec_class))

    ancestor = _resolve_ancestor(spec_class, by_file_and_name, by_name)
    if ancestor is None:
        return list(spec_class.features), spec_class.body, spec_class.base

    inherited_features, inherited_body, base_for_classification = _accumulate_inheritance(
        ancestor, by_file_and_name, by_name, seen
    )
    return (
        inherited_features + list(spec_class.features),
        inherited_body + spec_class.body,
        base_for_classification,
    )


def collect_classes(source_root: Path, repo_root: Path, config: dict):
    all_classes: list[SpecClass] = []
    for groovy_file in sorted(source_root.rglob("*.groovy")):
        all_classes.extend(parse_file(groovy_file, repo_root))

    by_file_and_name, by_name = _index_classes(all_classes)

    included = []
    excluded_count = 0
    for spec_class in all_classes:
        if spec_class.is_abstract:
            continue  # only a concrete class is ever instantiated and run; contributes via inheritance only

        features, body, classify_base = _accumulate_inheritance(spec_class, by_file_and_name, by_name)
        if not features:
            continue  # no feature of its own, and none inherited either: not a real spec class

        excluded, reason = is_excluded(spec_class.key, config)
        if excluded:
            excluded_count += 1
            continue

        included.append((
            SpecClass(key=spec_class.key, path=spec_class.path, name=spec_class.name,
                       base=spec_class.base, features=features, body=body),
            classify_base,
        ))
    return included, excluded_count


def build_manifest(source_root: Path, sha: str, config: dict, repo_root: Path | None = None) -> dict:
    """Scan `source_root` for *.groovy files. Manifest paths are stored relative to
    `repo_root` (defaults to `source_root`) so they link correctly to GitHub when the
    caller passes the upstream clone root rather than the deeper source directory."""
    repo_root = repo_root or source_root
    classes, excluded_count = collect_classes(source_root, repo_root, config)

    class_entries = []
    for spec_class, classify_base in classes:
        recipe = classify_source(spec_class.body, classify_base)
        area = area_for(spec_class.key, config)
        class_entries.append({
            "key": spec_class.key,
            "path": spec_class.path,
            "base": spec_class.base,
            "recipe": recipe,
            "area": area,
            "features": [{"name": f.name, "hash": f.hash} for f in spec_class.features],
        })

    class_entries.sort(key=lambda c: c["key"])

    return {
        "upstream": {
            "repo": config["upstream"]["repo"],
            "ref": config["upstream"]["ref"],
            "sha": sha,
            "generated_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        },
        "classes": class_entries,
        "excluded": {
            "classes": excluded_count,
        },
    }


def check_guard(old_manifest: dict, new_manifest: dict, tolerance: float) -> None:
    old_count = len(old_manifest["classes"])
    new_count = len(new_manifest["classes"])
    if old_count == 0:
        return
    delta = abs(new_count - old_count) / old_count
    if delta > tolerance:
        raise GuardTripped(
            f"class count moved from {old_count} to {new_count} "
            f"({delta:.1%} > {tolerance:.1%} tolerance)"
        )


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--upstream", required=True, help="path to the upstream clone")
    parser.add_argument("--out", required=True, help="path to write the manifest to")
    parser.add_argument("--check", action="store_true",
                         help="regenerate and diff against --out without writing; exit nonzero on drift")
    args = parser.parse_args(argv)

    upstream_root = Path(args.upstream)
    out_path = Path(args.out)
    config = load_config()

    source_root = upstream_root / config["upstream"]["source_root"]
    sha = _git_rev_parse(upstream_root)
    new_manifest = build_manifest(source_root, sha=sha, config=config, repo_root=upstream_root)

    if args.check:
        if not out_path.exists():
            print(f"{out_path} does not exist", file=sys.stderr)
            return 1
        old_manifest = json.loads(out_path.read_text())
        try:
            check_guard(old_manifest, new_manifest, tolerance=config["guard"]["class_count_tolerance"])
        except GuardTripped as e:
            print(f"guard tripped: {e}", file=sys.stderr)
            return 1
        # Compare ignoring generated_at/sha, which always differ.
        old_comparable = {**old_manifest, "upstream": {**old_manifest["upstream"], "sha": None, "generated_at": None}}
        new_comparable = {**new_manifest, "upstream": {**new_manifest["upstream"], "sha": None, "generated_at": None}}
        if old_comparable != new_comparable:
            print("manifest is out of date", file=sys.stderr)
            return 1
        return 0

    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(json.dumps(new_manifest, indent=2) + "\n")
    print(f"wrote {out_path}: {len(new_manifest['classes'])} classes, "
          f"{sum(len(c['features']) for c in new_manifest['classes'])} features, "
          f"{new_manifest['excluded']['classes']} excluded")
    return 0


def _git_rev_parse(repo_root: Path) -> str:
    import subprocess
    result = subprocess.run(
        ["git", "rev-parse", "HEAD"], cwd=repo_root, capture_output=True, text=True, check=True
    )
    return result.stdout.strip()


if __name__ == "__main__":
    sys.exit(main())
