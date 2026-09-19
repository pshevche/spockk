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

    def test_guard_rejects_a_large_count_drop(self):
        old = {"classes": [{"key": f"K{i}", "features": []} for i in range(100)]}
        new = {"classes": [{"key": f"K{i}", "features": []} for i in range(50)]}
        with self.assertRaises(GuardTripped):
            check_guard(old, new, tolerance=0.10)

    def test_guard_allows_normal_churn(self):
        old = {"classes": [{"key": f"K{i}", "features": []} for i in range(100)]}
        new = {"classes": [{"key": f"K{i}", "features": []} for i in range(103)]}
        check_guard(old, new, tolerance=0.10)  # must not raise
