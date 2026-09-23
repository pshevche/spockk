import unittest

from reconcile import (
    ExistingIssue,
    ExistingRollup,
    Mutation,
    _packages_by_area,
    apply,
    find_dashboard_issue,
    index_area_issues,
    index_issues,
    linked_keys,
    plan,
    plan_hierarchy,
)
from render import DASHBOARD_KEY, render_area_issue, render_class_issue, render_dashboard


def _manifest(*classes):
    return {"classes": list(classes)}


def _class(key, area, *features):
    return {
        "key": key,
        "area": area,
        "features": [{"name": name, "hash": h} for name, h in features],
    }


class ReconcileTest(unittest.TestCase):
    def test_new_class_creates_an_issue_under_its_area(self):
        manifest = _manifest(_class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111")))

        mutations = plan(manifest, coverage={}, exclusions={}, existing_issues={})

        self.assertEqual(1, len(mutations))
        m = mutations[0]
        self.assertEqual("create_issue", m.kind)
        self.assertEqual("org.spockframework.smoke.A", m.target)
        self.assertEqual("smoke-core", m.payload["area"])

    def test_new_feature_appends_and_reopens_a_closed_issue(self):
        old_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"))
        _, old_body = render_class_issue(old_class, {}, {})
        existing = {
            "org.spockframework.smoke.A": ExistingIssue(
                number=1,
                id=101, state="closed", body=old_body, labels=["test-coverage::spec"]
            )
        }
        new_class = _class(
            "org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"), ("two", "bbbb2222")
        )
        manifest = _manifest(new_class)

        mutations = plan(manifest, coverage={}, exclusions={}, existing_issues=existing)

        kinds = [m.kind for m in mutations]
        self.assertIn("reopen_issue", kinds)
        self.assertIn("comment", kinds)
        self.assertIn("update_issue", kinds)

    def test_deleted_feature_is_struck_not_dropped(self):
        old_class = _class(
            "org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"), ("two", "bbbb2222")
        )
        _, old_body = render_class_issue(old_class, {}, {})
        existing = {
            "org.spockframework.smoke.A": ExistingIssue(
                number=1,
                id=101, state="open", body=old_body, labels=["test-coverage::spec"]
            )
        }
        new_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"))
        manifest = _manifest(new_class)

        mutations = plan(manifest, coverage={}, exclusions={}, existing_issues=existing)

        comments = [m for m in mutations if m.kind == "comment"]
        self.assertEqual(1, len(comments))
        self.assertIn("two", comments[0].payload["body"])
        update = next(m for m in mutations if m.kind == "update_issue")
        self.assertNotIn("- [ ] two", update.payload["body"])

    def test_changed_body_hash_applies_drift_label_without_unticking(self):
        old_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"))
        _, old_body = render_class_issue(old_class, {"org.spockframework.smoke.A#one": _Ported()}, {})
        existing = {
            "org.spockframework.smoke.A": ExistingIssue(
                number=1,
                id=101, state="open", body=old_body, labels=["test-coverage::spec"]
            )
        }
        new_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "cccc3333"))
        manifest = _manifest(new_class)
        coverage = {"org.spockframework.smoke.A#one": _Ported()}

        mutations = plan(manifest, coverage=coverage, exclusions={}, existing_issues=existing)

        add_label = next(m for m in mutations if m.kind == "add_label")
        self.assertEqual("test-coverage::drift", add_label.payload["label"])
        update = next(m for m in mutations if m.kind == "update_issue")
        self.assertIn("- [x] one", update.payload["body"])

    def test_rename_detected_by_matching_hash_rewrites_the_key(self):
        old_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"))
        _, old_body = render_class_issue(old_class, {"org.spockframework.smoke.A#one": _Ported()}, {})
        existing = {
            "org.spockframework.smoke.A": ExistingIssue(
                number=1,
                id=101, state="open", body=old_body, labels=["test-coverage::spec"]
            )
        }
        new_class = _class("org.spockframework.smoke.A", "smoke-core", ("uno", "aaaa1111"))
        manifest = _manifest(new_class)

        mutations = plan(manifest, coverage={}, exclusions={}, existing_issues=existing)

        rewrites = [m for m in mutations if m.kind == "rewrite_key"]
        self.assertEqual(1, len(rewrites))
        self.assertEqual("org.spockframework.smoke.A#one", rewrites[0].payload["old"])
        self.assertEqual("org.spockframework.smoke.A#uno", rewrites[0].payload["new"])
        self.assertFalse(any(m.kind == "comment" and "no longer exists" in m.payload.get("body", "") for m in mutations))

    def test_rename_does_not_reset_progress(self):
        old_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"))
        _, old_body = render_class_issue(old_class, {"org.spockframework.smoke.A#one": _Ported()}, {})
        existing = {
            "org.spockframework.smoke.A": ExistingIssue(
                number=1,
                id=101, state="open", body=old_body, labels=["test-coverage::spec"]
            )
        }
        new_class = _class("org.spockframework.smoke.A", "smoke-core", ("uno", "aaaa1111"))
        manifest = _manifest(new_class)
        # coverage is keyed by the OLD name still (the @MigratedFrom string hasn't been updated yet)
        coverage = {"org.spockframework.smoke.A#one": _Ported()}

        mutations = plan(manifest, coverage=coverage, exclusions={}, existing_issues=existing)

        update = next(m for m in mutations if m.kind == "update_issue")
        self.assertIn("- [x] uno", update.payload["body"])

    def test_deleted_class_closes_the_issue_leaving_ported_tests(self):
        old_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"))
        _, old_body = render_class_issue(old_class, {}, {})
        existing = {
            "org.spockframework.smoke.A": ExistingIssue(
                number=1,
                id=101, state="open", body=old_body, labels=["test-coverage::spec"]
            )
        }
        manifest = _manifest()

        mutations = plan(manifest, coverage={}, exclusions={}, existing_issues=existing)

        self.assertEqual(1, len(mutations))
        self.assertEqual("close_issue", mutations[0].kind)

    def test_bootstrap_never_emits_a_blocked_label(self):
        manifest = _manifest(
            _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111")),
            _class("org.spockframework.smoke.B", "smoke-core", ("two", "bbbb2222")),
        )

        mutations = plan(manifest, coverage={}, exclusions={}, existing_issues={})

        self.assertFalse(
            any(
                m.kind == "add_label" and m.payload.get("label") == "test-coverage::blocked"
                for m in mutations
            )
        )

    def test_blocked_removed_once_every_cited_gap_is_closed(self):
        old_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"))
        _, old_body = render_class_issue(old_class, {}, {})
        old_body += "\n<!-- spockk-coverage:blocked-by 412 -->"
        existing = {
            "org.spockframework.smoke.A": ExistingIssue(
                number=1,
                id=101,
                state="open",
                body=old_body,
                labels=["test-coverage::spec", "test-coverage::blocked"],
            )
        }
        manifest = _manifest(old_class)

        mutations = plan(
            manifest, coverage={}, exclusions={}, existing_issues=existing, closed_gaps={412}
        )

        remove = next(
            m for m in mutations if m.kind == "remove_label" and m.payload.get("label") == "test-coverage::blocked"
        )
        self.assertEqual("org.spockframework.smoke.A", remove.target)

    def test_blocked_stays_while_a_cited_gap_is_still_open(self):
        old_class = _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111"))
        _, old_body = render_class_issue(old_class, {}, {})
        old_body += "\n<!-- spockk-coverage:blocked-by 412,500 -->"
        existing = {
            "org.spockframework.smoke.A": ExistingIssue(
                number=1,
                id=101,
                state="open",
                body=old_body,
                labels=["test-coverage::spec", "test-coverage::blocked"],
            )
        }
        manifest = _manifest(old_class)

        mutations = plan(
            manifest, coverage={}, exclusions={}, existing_issues=existing, closed_gaps={412}
        )

        self.assertFalse(any(m.kind == "remove_label" for m in mutations))

    def test_area_over_threshold_splits_into_stable_sub_areas(self):
        classes = [
            _class(f"org.spockframework.smoke.Extensions{i:02d}", "extensions", ("f", "aaaa1111"))
            for i in range(90)
        ]
        manifest = _manifest(*classes)

        mutations = plan(
            manifest, coverage={}, exclusions={}, existing_issues={}, area_split_threshold=80
        )

        areas = [m.payload["area"] for m in mutations if m.kind == "create_issue"]
        distinct_areas = set(areas)
        self.assertEqual({"extensions-1", "extensions-2"}, distinct_areas)
        for area in distinct_areas:
            self.assertLessEqual(areas.count(area), 80)

    def test_area_split_is_stable_when_a_class_is_appended(self):
        classes = [
            _class(f"org.spockframework.smoke.Extensions{i:02d}", "extensions", ("f", "aaaa1111"))
            for i in range(90)
        ]
        before = plan(
            _manifest(*classes), coverage={}, exclusions={}, existing_issues={}, area_split_threshold=80
        )
        classes.append(_class("org.spockframework.smoke.Extensions90", "extensions", ("f", "aaaa1111")))
        after = plan(
            _manifest(*classes), coverage={}, exclusions={}, existing_issues={}, area_split_threshold=80
        )

        before_first_80 = sorted(m.target for m in before if m.payload["area"] == "extensions-1")
        after_first_80 = sorted(m.target for m in after if m.payload["area"] == "extensions-1")
        self.assertEqual(before_first_80, after_first_80)


class IndexIssuesTest(unittest.TestCase):
    def test_indexes_by_class_key_parsed_from_the_body(self):
        _, body = render_class_issue(
            _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111")), {}, {}
        )
        raw = [{"number": 7, "id": 707, "state": "open", "body": body, "labels": ["test-coverage::spec"]}]

        indexed = index_issues(raw)

        self.assertIn("org.spockframework.smoke.A", indexed)
        self.assertEqual(7, indexed["org.spockframework.smoke.A"].number)

    def test_skips_issues_without_a_recognizable_key(self):
        raw = [{"number": 1, "state": "open", "body": "unrelated issue", "labels": []}]

        self.assertEqual({}, index_issues(raw))

    def test_indexes_area_issues_by_area_name(self):
        _, body = render_area_issue("smoke-core", child_count=3)
        raw = [{"number": 20, "id": 2020, "body": body}]

        indexed = index_area_issues(raw)

        self.assertIn("smoke-core", indexed)
        self.assertEqual(20, indexed["smoke-core"].number)
        self.assertEqual(2020, indexed["smoke-core"].id)

    def test_finds_the_dashboard_issue_by_its_key(self):
        _, body = render_dashboard({"smoke-core": 1})
        raw = [
            {"number": 1, "id": 11, "body": "unrelated issue"},
            {"number": 2, "id": 22, "body": body},
        ]

        dashboard = find_dashboard_issue(raw)

        self.assertEqual(2, dashboard.number)
        self.assertEqual(22, dashboard.id)

    def test_finds_no_dashboard_issue_when_none_exists(self):
        self.assertIsNone(find_dashboard_issue([{"number": 1, "id": 1, "body": "unrelated"}]))

    def test_linked_keys_extracts_the_key_from_each_sub_issues_body(self):
        _, body_a = render_class_issue(_class("org.spockframework.smoke.A", "smoke-core", ("one", "h")), {}, {})
        raw_sub_issues = [{"number": 1, "body": body_a}, {"number": 2, "body": "no marker here"}]

        self.assertEqual({"org.spockframework.smoke.A"}, linked_keys(raw_sub_issues))


class PackagesByAreaTest(unittest.TestCase):
    def test_groups_every_configured_prefix_under_its_area(self):
        config = {"area": {"org.spockframework.mock": "mocking", "spock.mock": "mocking", "org.spockframework.runtime": "runtime"}}

        packages = _packages_by_area(config)

        self.assertEqual(["org.spockframework.mock", "spock.mock"], packages["mocking"])
        self.assertEqual(["org.spockframework.runtime"], packages["runtime"])


class PlanHierarchyTest(unittest.TestCase):
    def test_area_issue_body_names_the_real_upstream_packages_it_covers(self):
        # "mocking" is a real area from config.toml, mapping several upstream packages.
        manifest = _manifest(_class("org.spockframework.smoke.mock.M", "mocking", ("one", "aaaa1111")))

        mutations = plan_hierarchy(manifest, existing_areas={}, existing_dashboard=None,
                                    existing_area_links={}, existing_dashboard_links=set())

        create_area = next(m for m in mutations if m.kind == "create_area_issue")
        self.assertIn("org.spockframework.mock", create_area.payload["body"])

    def test_creates_the_dashboard_and_area_issues_when_none_exist(self):
        manifest = _manifest(
            _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111")),
            _class("org.spockframework.smoke.B", "mocking", ("two", "bbbb2222")),
        )

        mutations = plan_hierarchy(manifest, existing_areas={}, existing_dashboard=None,
                                    existing_area_links={}, existing_dashboard_links=set())

        kinds = [m.kind for m in mutations]
        self.assertEqual(1, kinds.count("create_dashboard_issue"))
        self.assertEqual(2, kinds.count("create_area_issue"))
        area_targets = {m.target for m in mutations if m.kind == "create_area_issue"}
        self.assertEqual({"smoke-core", "mocking"}, area_targets)

    def test_updates_an_existing_dashboard_and_area_issue_instead_of_recreating(self):
        manifest = _manifest(_class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111")))
        existing_areas = {"smoke-core": ExistingRollup(number=20, id=2020, body="")}
        existing_dashboard = ExistingRollup(number=1, id=11, body="")

        mutations = plan_hierarchy(manifest, existing_areas, existing_dashboard,
                                    existing_area_links={}, existing_dashboard_links=set())

        update_dashboard = next(m for m in mutations if m.kind == "update_dashboard_issue")
        self.assertEqual(1, update_dashboard.payload["issue"])
        self.assertEqual(11, update_dashboard.payload["id"])
        update_area = next(m for m in mutations if m.kind == "update_area_issue")
        self.assertEqual(20, update_area.payload["issue"])
        self.assertEqual(2020, update_area.payload["id"])

    def test_links_every_spec_issue_under_its_area_and_every_area_under_the_dashboard(self):
        manifest = _manifest(
            _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111")),
            _class("org.spockframework.smoke.B", "mocking", ("two", "bbbb2222")),
        )

        mutations = plan_hierarchy(manifest, existing_areas={}, existing_dashboard=None,
                                    existing_area_links={}, existing_dashboard_links=set())

        links = [m for m in mutations if m.kind == "link_sub_issue"]
        spec_links = {(m.payload["parent_key"], m.payload["child_key"]) for m in links if m.payload["child_ns"] == "spec"}
        area_links = {(m.payload["parent_key"], m.payload["child_key"]) for m in links if m.payload["child_ns"] == "area"}
        self.assertEqual(
            {("smoke-core", "org.spockframework.smoke.A"), ("mocking", "org.spockframework.smoke.B")},
            spec_links,
        )
        self.assertEqual({(DASHBOARD_KEY, "smoke-core"), (DASHBOARD_KEY, "mocking")}, area_links)

    def test_does_not_relink_a_spec_issue_already_attached_to_its_area(self):
        manifest = _manifest(
            _class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111")),
            _class("org.spockframework.smoke.B", "smoke-core", ("two", "bbbb2222")),
        )

        mutations = plan_hierarchy(
            manifest,
            existing_areas={"smoke-core": ExistingRollup(number=20, id=2020, body="")},
            existing_dashboard=None,
            existing_area_links={"smoke-core": {"org.spockframework.smoke.A"}},
            existing_dashboard_links=set(),
        )

        spec_link_targets = {
            m.payload["child_key"] for m in mutations if m.kind == "link_sub_issue" and m.payload["child_ns"] == "spec"
        }
        self.assertEqual({"org.spockframework.smoke.B"}, spec_link_targets)

    def test_does_not_relink_an_area_already_attached_to_the_dashboard(self):
        manifest = _manifest(_class("org.spockframework.smoke.A", "smoke-core", ("one", "aaaa1111")))

        mutations = plan_hierarchy(
            manifest,
            existing_areas={},
            existing_dashboard=ExistingRollup(number=1, id=11, body=""),
            existing_area_links={},
            existing_dashboard_links={"smoke-core"},
        )

        area_links = [m for m in mutations if m.kind == "link_sub_issue" and m.payload["child_ns"] == "area"]
        self.assertEqual([], area_links)

    def test_splits_an_over_threshold_area_and_links_every_sub_area_under_the_dashboard(self):
        classes = [
            _class(f"org.spockframework.smoke.Extensions{i:02d}", "extensions", ("f", "aaaa1111"))
            for i in range(90)
        ]
        manifest = _manifest(*classes)

        mutations = plan_hierarchy(manifest, existing_areas={}, existing_dashboard=None,
                                    existing_area_links={}, existing_dashboard_links=set(),
                                    area_split_threshold=80)

        area_targets = {m.target for m in mutations if m.kind == "create_area_issue"}
        self.assertEqual({"extensions-1", "extensions-2"}, area_targets)
        dashboard_links = {
            m.payload["child_key"] for m in mutations if m.kind == "link_sub_issue" and m.payload["child_ns"] == "area"
        }
        self.assertEqual({"extensions-1", "extensions-2"}, dashboard_links)


class ApplyHierarchyTest(unittest.TestCase):
    """Verifies apply() resolves sub-issue links correctly for both a spec issue created earlier
    in the same run and one that already existed before this run started - the scenario the
    bootstrap actually hits: most spec issues already exist, some don't yet."""

    def test_links_a_pre_existing_spec_issue_and_a_newly_created_one_to_the_same_area(self):
        github = _FakeGitHub()
        mutations = [
            # B already has an issue (number=50, id=5050); A does not yet.
            Mutation(kind="create_issue", target="A", payload={"title": "t", "body": "b", "labels": []}, reason="new"),
            Mutation(kind="update_issue", target="B", payload={"issue": 50, "id": 5050, "title": "t", "body": "b"}, reason="refresh"),
            Mutation(kind="create_area_issue", target="smoke-core", payload={"title": "t", "body": "b", "labels": []}, reason="new area"),
            Mutation(kind="link_sub_issue", target="smoke-core->A",
                     payload={"parent_ns": "area", "parent_key": "smoke-core", "child_ns": "spec", "child_key": "A"},
                     reason="attach"),
            Mutation(kind="link_sub_issue", target="smoke-core->B",
                     payload={"parent_ns": "area", "parent_key": "smoke-core", "child_ns": "spec", "child_key": "B"},
                     reason="attach"),
        ]

        apply(mutations, github, sleep=lambda seconds: None)

        # post_results[0] is A's create_issue result, post_results[1] is the area's create result.
        newly_created_spec_id = github.post_results[0]["id"]
        area_number = github.post_results[1]["number"]
        self.assertEqual(
            [(area_number, newly_created_spec_id), (area_number, 5050)],
            github.sub_issue_calls,
        )

    def test_links_an_area_to_the_dashboard_when_both_already_exist(self):
        github = _FakeGitHub()
        mutations = [
            Mutation(kind="update_dashboard_issue", target=DASHBOARD_KEY,
                     payload={"issue": 1, "id": 11, "title": "t", "body": "b"}, reason="refresh"),
            Mutation(kind="update_area_issue", target="smoke-core",
                     payload={"issue": 20, "id": 2020, "title": "t", "body": "b"}, reason="refresh"),
            Mutation(kind="link_sub_issue", target="dashboard->smoke-core",
                     payload={"parent_ns": "dashboard", "parent_key": DASHBOARD_KEY, "child_ns": "area", "child_key": "smoke-core"},
                     reason="attach"),
        ]

        apply(mutations, github, sleep=lambda seconds: None)

        self.assertEqual([(1, 2020)], github.sub_issue_calls)


class _FakeGitHub:
    def __init__(self):
        self.post_results = []
        self.sub_issue_calls = []
        self._next_id = 9000
        self._next_number = 900

    def post(self, path, body):
        self._next_id += 1
        self._next_number += 1
        result = {"number": self._next_number, "id": self._next_id}
        self.post_results.append(result)
        return result

    def patch(self, path, body):
        return None

    def add_sub_issue(self, parent_number, sub_issue_id):
        self.sub_issue_calls.append((parent_number, sub_issue_id))


def _Ported():
    from scanner import Coverage

    return Coverage(status="ported", gap=None, source="Some.kt:1")


if __name__ == "__main__":
    unittest.main()
