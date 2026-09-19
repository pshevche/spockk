import tempfile
import unittest
from pathlib import Path

from exclusions import load_exclusions


def _write(content: str) -> Path:
    f = tempfile.NamedTemporaryFile(mode="w", suffix=".toml", delete=False)
    f.write(content)
    f.close()
    return Path(f.name)


class ExclusionsTest(unittest.TestCase):
    def test_not_applicable_requires_reason(self):
        path = _write("""
[["some.Class#feature"]]
status = "not-applicable"
decided = "2026-09-19"
""")
        with self.assertRaises(ValueError):
            load_exclusions(path)

    def test_not_applicable_with_reason_loads(self):
        path = _write("""
[["some.Class#feature"]]
status = "not-applicable"
reason = "Kotlin has no GString"
decided = "2026-09-19"
""")
        exclusions = load_exclusions(path)
        e = exclusions["some.Class#feature"]
        self.assertEqual("not-applicable", e.status)
        self.assertEqual("Kotlin has no GString", e.reason)
        self.assertIsNone(e.gap)

    def test_blocked_requires_gap(self):
        path = _write("""
[["some.Class#feature"]]
status = "blocked"
reason = "does not compile yet"
decided = "2026-09-19"
""")
        with self.assertRaises(ValueError):
            load_exclusions(path)

    def test_blocked_with_gap_loads(self):
        path = _write("""
[["some.Class#feature"]]
status = "blocked"
reason = "does not compile yet"
gap = 412
decided = "2026-09-19"
""")
        exclusions = load_exclusions(path)
        e = exclusions["some.Class#feature"]
        self.assertEqual("blocked", e.status)
        self.assertEqual(412, e.gap)

    def test_unknown_status_raises(self):
        path = _write("""
[["some.Class#feature"]]
status = "wontfix"
reason = "whatever"
decided = "2026-09-19"
""")
        with self.assertRaises(ValueError):
            load_exclusions(path)

    def test_duplicate_key_raises(self):
        path = _write("""
[["some.Class#feature"]]
status = "not-applicable"
reason = "first"
decided = "2026-09-19"

[["some.Class#feature"]]
status = "not-applicable"
reason = "second"
decided = "2026-09-19"
""")
        with self.assertRaises(ValueError):
            load_exclusions(path)


if __name__ == "__main__":
    unittest.main()
