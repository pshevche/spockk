import io
import unittest
from contextlib import redirect_stdout

from reconcile import BlastRadiusExceeded, check_blast_radius, run


class FakeGitHub:
    def __init__(self):
        self.calls = []
        self._next_id = 1000

    def post(self, path, body):
        self.calls.append(("POST", path, body))
        self._next_id += 1
        return {"number": 999, "id": self._next_id}

    def patch(self, path, body):
        self.calls.append(("PATCH", path, body))
        return None

    def get(self, path):
        self.calls.append(("GET", path, None))
        return {"labels": []}

    def add_sub_issue(self, parent_number, sub_issue_id):
        self.calls.append(("ADD_SUB_ISSUE", parent_number, sub_issue_id))


def _manifest(n):
    return {
        "classes": [
            {"key": f"org.spockframework.smoke.A{i}", "area": "smoke-core", "features": [{"name": "one", "hash": "a"}]}
            for i in range(n)
        ]
    }


class BlastRadiusTest(unittest.TestCase):
    def test_check_blast_radius_allows_under_the_cap(self):
        check_blast_radius([object()] * 10, max_mutations=50)  # must not raise

    def test_check_blast_radius_raises_over_the_cap(self):
        with self.assertRaises(BlastRadiusExceeded):
            check_blast_radius([object()] * 51, max_mutations=50)

    def test_dry_run_performs_zero_writes_and_prints_every_mutation(self):
        # 5 create_issue + hierarchy (1 dashboard + 1 area "smoke-core" + 5 spec->area links +
        # 1 area->dashboard link), since run() always plans the hierarchy alongside class issues.
        github = FakeGitHub()
        buffer = io.StringIO()

        with redirect_stdout(buffer):
            mutations = run(
                _manifest(5), coverage={}, exclusions={}, existing_issues={}, github=github, dry_run=True
            )

        self.assertEqual(13, len(mutations))
        self.assertEqual([], github.calls)
        output = buffer.getvalue()
        for m in mutations:
            self.assertIn(m.reason, output)

    def test_dry_run_is_not_capped_by_max_mutations(self):
        # 100 create_issue + hierarchy (1 dashboard + 2 sub-areas, since 100 exceeds the default
        # 80-class split threshold + 100 spec->area links + 2 area->dashboard links).
        github = FakeGitHub()
        buffer = io.StringIO()

        with redirect_stdout(buffer):
            mutations = run(
                _manifest(100),
                coverage={},
                exclusions={},
                existing_issues={},
                github=github,
                dry_run=True,
                max_mutations=50,
            )

        self.assertEqual(205, len(mutations))

    def test_exceeding_max_mutations_aborts_before_the_first_write(self):
        github = FakeGitHub()

        with self.assertRaises(BlastRadiusExceeded):
            run(
                _manifest(51),
                coverage={},
                exclusions={},
                existing_issues={},
                github=github,
                dry_run=False,
                max_mutations=50,
            )

        self.assertEqual([], github.calls)

    def test_under_the_cap_applies_every_mutation(self):
        github = FakeGitHub()

        mutations = run(
            _manifest(5),
            coverage={},
            exclusions={},
            existing_issues={},
            github=github,
            dry_run=False,
            max_mutations=50,
            sleep=lambda seconds: None,
        )

        self.assertEqual(len(mutations), len(github.calls))

    def test_paces_one_sleep_per_mutation_to_avoid_the_secondary_rate_limit(self):
        github = FakeGitHub()
        sleeps = []

        mutations = run(
            _manifest(5),
            coverage={},
            exclusions={},
            existing_issues={},
            github=github,
            dry_run=False,
            max_mutations=50,
            sleep=sleeps.append,
        )

        self.assertEqual(len(mutations), len(sleeps))


if __name__ == "__main__":
    unittest.main()
