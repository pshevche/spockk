import unittest

from tests.helpers import MANIFEST, ported, pending, na_exclusion
from validate import validate


class ValidateTest(unittest.TestCase):
    def test_unknown_key_is_a_violation(self):
        v = validate(MANIFEST, {"does.not.Exist#nope": ported()}, {}, closed_gaps=set())
        self.assertEqual(1, len(v))
        self.assertIn("not in the inventory", v[0].message)

    def test_key_both_ported_and_excluded_is_a_violation(self):
        key = "org.spockframework.smoke.A#one"
        v = validate(MANIFEST, {key: ported()}, {key: na_exclusion()}, closed_gaps=set())
        self.assertIn("both ported and marked not-applicable", v[0].message)

    def test_pending_referencing_a_closed_gap_is_a_violation(self):
        key = "org.spockframework.smoke.A#one"
        v = validate(MANIFEST, {key: pending(gap=412)}, {}, closed_gaps={412})
        self.assertIn("gap #412 is closed", v[0].message)

    def test_clean_state_produces_no_violations(self):
        key = "org.spockframework.smoke.A#one"
        self.assertEqual([], validate(MANIFEST, {key: ported()}, {}, closed_gaps=set()))

    def test_violation_names_the_source_file(self):
        v = validate(MANIFEST, {"does.not.Exist#nope": ported()}, {}, closed_gaps=set())
        self.assertIn(".kt", v[0].source)


if __name__ == "__main__":
    unittest.main()
