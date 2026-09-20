import unittest
from tests.helpers import FIXTURE_TREE
from classify import load_config
from inventory import build_manifest, check_guard, GuardTripped


class InventoryTest(unittest.TestCase):
    def test_manifest_has_upstream_provenance(self):
        m = build_manifest(FIXTURE_TREE, sha="abc123", config=load_config())
        self.assertEqual("spockframework/spock", m["upstream"]["repo"])
        self.assertEqual("abc123", m["upstream"]["sha"])
        self.assertIn("generated_at", m["upstream"])

    def test_classes_sorted_by_key_for_stable_diffs(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        keys = [c["key"] for c in m["classes"]]
        self.assertEqual(sorted(keys), keys)

    def test_excluded_classes_are_absent_but_counted(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        self.assertNotIn("org.spockframework.util.TextUtilSpec",
                         [c["key"] for c in m["classes"]])
        self.assertGreater(m["excluded"]["classes"], 0)

    def test_abstract_classes_never_get_their_own_manifest_entry(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        keys = [c["key"] for c in m["classes"]]
        self.assertNotIn("org.spockframework.smoke.condition.AbstractWithFeature", keys)
        self.assertNotIn("org.spockframework.smoke.condition.AbstractWithNoSubclass", keys)

    def test_a_concrete_class_inherits_features_from_its_abstract_base(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        by_key = {c["key"]: c for c in m["classes"]}
        concrete = by_key["org.spockframework.smoke.condition.ConcreteFromAbstractBase"]
        names = [f["name"] for f in concrete["features"]]
        self.assertEqual(
            ["feature declared in the abstract base", "concrete class's own feature"], names
        )

    def test_an_abstract_class_with_no_concrete_descendant_contributes_nothing(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        all_feature_names = {f["name"] for c in m["classes"] for f in c["features"]}
        self.assertNotIn("orphaned abstract feature", all_feature_names)

    def test_a_concrete_class_with_no_features_of_its_own_still_gets_its_inherited_ones(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        by_key = {c["key"]: c for c in m["classes"]}
        concrete = by_key["org.spockframework.smoke.condition.ConcreteWithNoOwnFeatures"]
        names = [f["name"] for f in concrete["features"]]
        self.assertEqual(["feature declared in the abstract base"], names)

    def test_a_framework_root_name_checked_into_the_tree_does_not_swallow_classification(self):
        # EmbeddedSpecification here has a real (feature-less) definition in the fixture tree,
        # like upstream's own does. A subclass of it must still classify as engine-runtime, not
        # walk through it into whatever it itself extends.
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        by_key = {c["key"]: c for c in m["classes"]}
        concrete = by_key["org.spockframework.shadow.UsesShadowedEmbeddedSpecification"]
        self.assertEqual("engine-runtime", concrete["recipe"])

    def test_guard_rejects_a_large_count_drop(self):
        old = {"classes": [{"key": f"K{i}", "features": []} for i in range(100)]}
        new = {"classes": [{"key": f"K{i}", "features": []} for i in range(50)]}
        with self.assertRaises(GuardTripped):
            check_guard(old, new, tolerance=0.10)

    def test_guard_allows_normal_churn(self):
        old = {"classes": [{"key": f"K{i}", "features": []} for i in range(100)]}
        new = {"classes": [{"key": f"K{i}", "features": []} for i in range(103)]}
        check_guard(old, new, tolerance=0.10)  # must not raise
