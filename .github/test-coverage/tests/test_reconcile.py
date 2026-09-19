import unittest

from reconcile import ExistingIssue, Mutation, index_issues, plan
from render import render_class_issue


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
                number=1, state="closed", body=old_body, labels=["test-coverage::spec"]
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
                number=1, state="open", body=old_body, labels=["test-coverage::spec"]
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
                number=1, state="open", body=old_body, labels=["test-coverage::spec"]
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
                number=1, state="open", body=old_body, labels=["test-coverage::spec"]
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
                number=1, state="open", body=old_body, labels=["test-coverage::spec"]
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
                number=1, state="open", body=old_body, labels=["test-coverage::spec"]
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
        raw = [{"number": 7, "state": "open", "body": body, "labels": ["test-coverage::spec"]}]

        indexed = index_issues(raw)

        self.assertIn("org.spockframework.smoke.A", indexed)
        self.assertEqual(7, indexed["org.spockframework.smoke.A"].number)

    def test_skips_issues_without_a_recognizable_key(self):
        raw = [{"number": 1, "state": "open", "body": "unrelated issue", "labels": []}]

        self.assertEqual({}, index_issues(raw))


def _Ported():
    from scanner import Coverage

    return Coverage(status="ported", gap=None, source="Some.kt:1")


if __name__ == "__main__":
    unittest.main()
