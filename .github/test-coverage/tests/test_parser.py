import unittest
from pathlib import Path
from parser import parse_file

FIXTURE = Path(__file__).parent / "fixtures" / "sample_spec.groovy"
ROOT = Path(__file__).parent / "fixtures"


class ParserTest(unittest.TestCase):
    def setUp(self):
        self.classes = {c.name: c for c in parse_file(FIXTURE, ROOT)}

    def test_parses_abstract_classes_too(self):
        # Spock allows a feature declared directly in an abstract base, run by every concrete
        # subclass; inventory.py resolves that inheritance, so the parser itself must not drop
        # abstract classes, only tag them.
        self.assertIn("AbstractWithFeature", self.classes)
        self.assertTrue(self.classes["AbstractWithFeature"].is_abstract)
        self.assertFalse(self.classes["ConcreteFromAbstractBase"].is_abstract)

    def test_finds_every_class_matching_class_extends_including_feature_less_ones(self):
        # A concrete class can inherit its entire feature set from an abstract base while
        # declaring none of its own (ConcreteWithNoOwnFeatures); it must still be returned here,
        # or inventory.py's inheritance resolution would never see it to attribute those
        # inherited features to it.
        self.assertEqual(
            {"SimpleConditions", "AbstractWithNoSubclass", "AbstractWithFeature",
             "ConcreteFromAbstractBase", "ConcreteWithNoOwnFeatures", "EmbeddedConditions"},
            set(self.classes),
        )
        self.assertEqual([], self.classes["ConcreteWithNoOwnFeatures"].features)

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
