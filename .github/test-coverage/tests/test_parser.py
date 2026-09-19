import unittest
from pathlib import Path
from parser import parse_file

FIXTURE = Path(__file__).parent / "fixtures" / "sample_spec.groovy"
ROOT = Path(__file__).parent / "fixtures"


class ParserTest(unittest.TestCase):
    def setUp(self):
        self.classes = {c.name: c for c in parse_file(FIXTURE, ROOT)}

    def test_skips_abstract_classes(self):
        self.assertNotIn("AbstractBase", self.classes)

    def test_finds_concrete_spec_classes(self):
        self.assertEqual({"SimpleConditions", "EmbeddedConditions"}, set(self.classes))

    def test_key_is_package_qualified(self):
        self.assertEqual(
            "org.spockframework.smoke.condition.SimpleConditions",
            self.classes["SimpleConditions"].key,
        )

    def test_records_base_class(self):
        self.assertEqual("EmbeddedSpecification", self.classes["EmbeddedConditions"].base)

    def test_extracts_feature_names_verbatim(self):
        names = [f.name for f in self.classes["SimpleConditions"].features]
        self.assertEqual(
            ["plain feature", "single quoted feature",
             "feature with #placeholder and, punctuation"],
            names,
        )

    def test_ignores_non_feature_methods(self):
        names = [f.name for f in self.classes["SimpleConditions"].features]
        self.assertNotIn("helper", names)

    def test_hash_is_stable_and_ignores_formatting(self):
        from parser import body_hash
        a = body_hash("expect:\n  1 == 1  // trailing comment")
        b = body_hash("expect:\n        1 == 1")
        self.assertEqual(a, b)

    def test_hash_changes_when_meaning_changes(self):
        from parser import body_hash
        self.assertNotEqual(body_hash("expect: 1 == 1"), body_hash("expect: 1 == 2"))
