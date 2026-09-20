import unittest
from classify import classify_source, area_for, is_excluded, load_config


class ClassifyTest(unittest.TestCase):
    def test_ast_snapshot_wins_over_engine_runtime(self):
        src = "@Snapshot SpockSnapshotter snapshotter\ncompiler.transpileSpecBody('x')\nrunner.run()"
        self.assertEqual("ast-snapshot", classify_source(src, base="EmbeddedSpecification"))

    def test_condition_rendering_detected_by_base(self):
        self.assertEqual("condition-rendering",
                         classify_source("", base="ConditionRenderingSpec"))

    def test_compile_error_needs_both_markers(self):
        src = "compiler.compile('x')\nthrown(InvalidSpecCompileException)"
        self.assertEqual("compile-error", classify_source(src, base="EmbeddedSpecification"))

    def test_compile_error_marker_alone_is_engine_runtime(self):
        self.assertEqual("engine-runtime",
                         classify_source("compiler.compile('x')", base="EmbeddedSpecification"))

    def test_plain_specification_is_smoke(self):
        self.assertEqual("smoke", classify_source("expect: true", base="Specification"))

    def test_longest_area_prefix_wins(self):
        cfg = load_config()
        self.assertEqual("conditions",
                         area_for("org.spockframework.smoke.condition.Foo", cfg))
        self.assertEqual("smoke-core", area_for("org.spockframework.smoke.Foo", cfg))

    def test_util_concurrent_is_in_scope(self):
        cfg = load_config()
        excluded, _ = is_excluded("spock.util.concurrent.BlockingVariableSpec", cfg)
        self.assertFalse(excluded)

    def test_exclusion_carries_a_reason(self):
        cfg = load_config()
        excluded, reason = is_excluded("org.spockframework.util.TextUtilSpec", cfg)
        self.assertTrue(excluded)
        self.assertIn("utility classes", reason)
