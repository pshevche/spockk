import tempfile
import unittest
from pathlib import Path

from classify import load_config
from inventory import build_manifest
from manifest import dumps_manifest, load_manifest, loads_manifest
from tests.helpers import FIXTURE_TREE


class ManifestTest(unittest.TestCase):
    def test_round_trips_the_fixture_inventory(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        self.assertEqual(m, loads_manifest(dumps_manifest(m)))

    def test_escapes_feature_names_with_toml_special_characters(self):
        m = {
            "upstream": {"repo": "r", "ref": "master", "sha": "s", "generated_at": "t"},
            "excluded": {"classes": 0},
            "classes": [{
                "key": "a.b.C", "path": "p", "base": "Specification", "recipe": "smoke", "area": "x",
                "features": [{"name": 'quote " back\\slash #hash\ttab\x01', "hash": "0000aaaa"}],
            }],
        }
        self.assertEqual(m, loads_manifest(dumps_manifest(m)))

    def test_keeps_feature_order(self):
        m = build_manifest(FIXTURE_TREE, sha="abc", config=load_config())
        loaded = loads_manifest(dumps_manifest(m))
        self.assertEqual(
            [[f["name"] for f in c["features"]] for c in m["classes"]],
            [[f["name"] for f in c["features"]] for c in loaded["classes"]],
        )

    def test_one_top_level_table_per_class(self):
        text = dumps_manifest(build_manifest(FIXTURE_TREE, sha="abc", config=load_config()))
        self.assertIn('["org.spockframework.smoke.condition.SimpleConditions"]\n', text)
        self.assertIn('["org.spockframework.smoke.condition.SimpleConditions".features]\n', text)

    def test_duplicate_feature_names_collapse_to_the_first(self):
        with tempfile.TemporaryDirectory() as tmp:
            spec = Path(tmp) / "Dup.groovy"
            spec.write_text(
                "package org.spockframework.smoke\n\n"
                "class Dup extends Specification {\n"
                '  def "same"() {\n    expect: 1\n  }\n'
                '  def "same"() {\n    expect: 2\n  }\n'
                "}\n"
            )
            m = build_manifest(Path(tmp), sha="abc", config=load_config())
        self.assertEqual(["same"], [f["name"] for f in m["classes"][0]["features"]])

    def test_checked_in_manifest_loads(self):
        self.assertGreater(len(load_manifest()["classes"]), 0)
