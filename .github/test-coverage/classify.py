"""Classify a parsed upstream spec class by rewrite recipe, area and scope."""
import tomllib
from pathlib import Path

_CONFIG_PATH = Path(__file__).parent / "config.toml"


def load_config(path: Path | None = None) -> dict:
    with open(path or _CONFIG_PATH, "rb") as f:
        return tomllib.load(f)


def classify_source(source: str, base: str) -> str:
    """Recipe detection. Order matters: later markers co-occur with earlier ones."""
    has_snapshot = "SpockSnapshotter" in source or "@Snapshot" in source
    has_transpile = "transpile" in source
    if has_snapshot and has_transpile:
        return "ast-snapshot"

    if base == "ConditionRenderingSpec":
        return "condition-rendering"

    has_compile = "compiler.compile" in source
    has_compile_error = any(
        marker in source
        for marker in ("InvalidSpecCompileException", "CompilationFailedException",
                        "MultipleCompilationErrorsException")
    )
    if has_compile and has_compile_error:
        return "compile-error"

    if "runner.run" in source or base == "EmbeddedSpecification":
        return "engine-runtime"

    return "smoke"


def area_for(class_key: str, config: dict) -> str | None:
    """Longest configured package prefix wins."""
    areas = config.get("area", {})
    best_prefix = None
    for prefix in areas:
        if class_key.startswith(prefix + ".") or class_key == prefix:
            if best_prefix is None or len(prefix) > len(best_prefix):
                best_prefix = prefix
    return areas[best_prefix] if best_prefix is not None else None


def is_excluded(class_key: str, config: dict) -> tuple[bool, str]:
    for entry in config.get("exclude", []):
        prefix = entry["prefix"]
        if class_key.startswith(prefix + "."):
            return True, entry["reason"]
    return False, ""
