import unittest

from tests.helpers import CLASS, KEY, FEATURE, class_with, ported, pending, na_exclusion
from render import (
    DASHBOARD_KEY,
    merge_generated_region,
    render_class_issue,
    render_area_issue,
    render_dashboard,
    split_class,
    parse_class_key,
    parse_known_features,
)


class RenderTest(unittest.TestCase):
    def test_generated_region_replaced_human_text_preserved(self):
        existing = "My own note.\n<!-- spockk-coverage:begin -->\nOLD\n<!-- spockk-coverage:end -->\nTrailer."
        merged = merge_generated_region(existing, "NEW")
        self.assertIn("My own note.", merged)
        self.assertIn("Trailer.", merged)
        self.assertIn("NEW", merged)
        self.assertNotIn("OLD", merged)

    def test_first_render_appends_region_when_markers_absent(self):
        merged = merge_generated_region("Just a note.", "NEW")
        self.assertIn("Just a note.", merged)
        self.assertIn("NEW", merged)

    def test_ported_feature_is_checked(self):
        _, body = render_class_issue(CLASS, {KEY: ported()}, {})
        self.assertIn(f"- [x] {FEATURE}", body)

    def test_not_applicable_is_struck_through_with_reason(self):
        _, body = render_class_issue(CLASS, {}, {KEY: na_exclusion()})
        self.assertIn("~~", body)
        self.assertIn("n/a:", body)

    def test_pending_feature_links_its_gap(self):
        _, body = render_class_issue(CLASS, {KEY: pending(gap=412)}, {})
        self.assertIn("#412", body)

    def test_unstarted_feature_is_unchecked(self):
        _, body = render_class_issue(CLASS, {}, {})
        self.assertIn(f"- [ ] {FEATURE}", body)

    def test_title_uses_the_fully_qualified_class_name(self):
        title, _ = render_class_issue(CLASS, {}, {})
        self.assertEqual(f"Migrate {CLASS['key']}", title)

    def test_split_produces_stable_numbered_parts(self):
        parts = split_class(class_with(45), threshold=20)
        self.assertEqual(3, len(parts))
        self.assertEqual("(1/3)", parts[0].suffix)

    def test_appending_a_feature_extends_the_last_part(self):
        before = split_class(class_with(41), threshold=20)
        after = split_class(class_with(42), threshold=20)
        self.assertEqual(len(before), len(after))
        self.assertEqual(before[0].feature_names, after[0].feature_names)

    def test_class_at_or_below_threshold_is_not_split(self):
        parts = split_class(class_with(3), threshold=20)
        self.assertEqual(1, len(parts))
        self.assertEqual("", parts[0].suffix)

    def test_render_area_issue_names_the_area(self):
        _, body = render_area_issue("mocking", child_count=11)
        self.assertIn("mocking", body)
        self.assertIn("11", body)

    def test_area_title_is_descriptive_not_a_bare_label(self):
        title, _ = render_area_issue("mocking", child_count=11)
        self.assertEqual("Test coverage area: mocking", title)

    def test_split_area_title_names_the_base_area_and_part_number(self):
        title, _ = render_area_issue("mocking-2", child_count=20)
        self.assertEqual("Test coverage area: mocking (part 2)", title)

    def test_area_body_lists_the_packages_it_covers(self):
        _, body = render_area_issue("mocking", child_count=11, packages=["org.spockframework.mock", "spock.mock"])
        self.assertIn("org.spockframework.mock", body)
        self.assertIn("spock.mock", body)

    def test_area_issue_body_round_trips_its_key(self):
        # The reconciler must be able to find an existing area issue again on rerun, the same way
        # it does for class issues.
        _, body = render_area_issue("mocking", child_count=11)
        self.assertEqual("mocking", parse_class_key(body))

    def test_render_dashboard_names_every_area(self):
        _, body = render_dashboard({"mocking": 3, "conditions": 5})
        self.assertIn("mocking", body)
        self.assertIn("conditions", body)

    def test_dashboard_shows_the_class_count_for_each_area(self):
        _, body = render_dashboard({"mocking": 3, "conditions": 5})
        self.assertIn("3 classes", body)
        self.assertIn("5 classes", body)

    def test_dashboard_body_round_trips_its_key(self):
        _, body = render_dashboard({"mocking": 3, "conditions": 5})
        self.assertEqual(DASHBOARD_KEY, parse_class_key(body))

    def test_class_issue_body_round_trips_its_key_and_feature_hashes(self):
        _, body = render_class_issue(CLASS, {}, {})
        self.assertEqual(CLASS["key"], parse_class_key(body))
        self.assertEqual({"one": "aaaa1111"}, parse_known_features(body))

    def test_upstream_line_links_to_the_real_source_file_when_upstream_info_is_given(self):
        class_with_path = {**CLASS, "path": "spock-specs/src/test/groovy/org/spockframework/smoke/A.groovy"}
        upstream = {"repo": "spockframework/spock", "sha": "abc1234"}

        _, body = render_class_issue(class_with_path, {}, {}, upstream=upstream)

        self.assertIn(
            "https://github.com/spockframework/spock/blob/abc1234/"
            "spock-specs/src/test/groovy/org/spockframework/smoke/A.groovy",
            body,
        )

    def test_upstream_line_falls_back_to_plain_text_without_upstream_info(self):
        _, body = render_class_issue(CLASS, {}, {})
        self.assertIn("**Upstream:** `A`", body)
        self.assertNotIn("github.com/spockframework/spock/blob", body)

    def test_class_body_explains_the_recipe_and_area_in_plain_language(self):
        annotated = {**CLASS, "recipe": "engine-runtime", "area": "smoke-core"}

        _, body = render_class_issue(annotated, {}, {})

        self.assertIn("engine-runtime", body)
        self.assertIn("smoke-core", body)
        self.assertIn("SKILL.md", body)


if __name__ == "__main__":
    unittest.main()
