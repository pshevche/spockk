import unittest
from pathlib import Path

from scanner import scan

FIXTURES = Path(__file__).parent / "fixtures"


class ScannerTest(unittest.TestCase):
    def setUp(self):
        self.cov = scan([FIXTURES])

    def test_single_key_is_ported(self):
        self.assertEqual("ported", self.cov["org.spockframework.smoke.A#one"].status)

    def test_vararg_keys_all_recorded(self):
        self.assertIn("org.spockframework.smoke.A#two", self.cov)
        self.assertIn("org.spockframework.smoke.A#three", self.cov)

    def test_pending_feature_marks_pending_and_extracts_gap_from_an_issue_url(self):
        # The reason convention is the full issue URL, nothing else.
        c = self.cov["org.spockframework.smoke.B#blocked"]
        self.assertEqual("pending", c.status)
        self.assertEqual(412, c.gap)
        self.assertEqual("https://github.com/pshevche/spockk/issues/412", c.reason)

    def test_records_source_location_for_error_messages(self):
        self.assertIn("PortedTest.kt", self.cov["org.spockframework.smoke.A#one"].source)

    def test_parens_in_migrated_from_key_do_not_break_parsing(self):
        # Real upstream feature names often carry parens, e.g. "(no undesired aliasing)"; the
        # balanced-paren annotation scanner must not stumble on them.
        key = "org.spockframework.smoke.C#each condition gets its own values (no undesired aliasing)"
        self.assertEqual("ported", self.cov[key].status)


if __name__ == "__main__":
    unittest.main()
