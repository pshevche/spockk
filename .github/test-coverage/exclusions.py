"""Loads the coverage exclusions ledger: deliberate not-applicable and blocked decisions."""
import re
import tomllib
from dataclasses import dataclass
from pathlib import Path
from typing import Literal

_LEDGER_PATH = Path(__file__).parents[2] / "_docs" / "test-coverage" / "exclusions.toml"

_VALID_STATUSES = {"not-applicable", "blocked"}
_ISSUE_URL_RE = re.compile(r"^https://github\.com/pshevche/spockk/issues/(\d+)$")


@dataclass
class Exclusion:
    status: Literal["not-applicable", "blocked"]
    reason: str
    gap: int | None
    decided: str


def load_exclusions(path: Path | None = None) -> dict[str, Exclusion]:
    with open(path or _LEDGER_PATH, "rb") as f:
        raw = tomllib.load(f)

    exclusions: dict[str, Exclusion] = {}
    for key, entries in raw.items():
        if len(entries) > 1:
            raise ValueError(f"duplicate exclusion entry for key: {key!r}")
        entry = entries[0]

        status = entry.get("status")
        if status not in _VALID_STATUSES:
            raise ValueError(f"{key!r}: unknown status {status!r}")

        reason = entry.get("reason")
        if not reason:
            raise ValueError(f"{key!r}: status {status!r} requires a non-empty reason")

        gap_value = entry.get("gap")
        gap = None
        if status == "blocked":
            if not gap_value:
                raise ValueError(f"{key!r}: status 'blocked' requires a gap issue URL")
            match = _ISSUE_URL_RE.match(gap_value) if isinstance(gap_value, str) else None
            if not match:
                raise ValueError(
                    f"{key!r}: gap must be a full issue URL "
                    f"(https://github.com/pshevche/spockk/issues/N), got {gap_value!r}"
                )
            gap = int(match.group(1))

        exclusions[key] = Exclusion(
            status=status,
            reason=reason,
            gap=gap,
            decided=entry.get("decided", ""),
        )
    return exclusions
