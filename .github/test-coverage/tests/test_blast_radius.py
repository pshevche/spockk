import io
import unittest
from contextlib import redirect_stdout

from reconcile import BlastRadiusExceeded, check_blast_radius, run


class FakeGitHub:
    def __init__(self):
        self.calls = []

    def post(self, path, body):
        self.calls.append(("POST", path, body))
        return {"number": 999}

    def patch(self, path, body):
        self.calls.append(("PATCH", path, body))
        return None

    def get(self, path):
        self.calls.append(("GET", path, None))
        return {"labels": []}


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
        github = FakeGitHub()
        buffer = io.StringIO()

        with redirect_stdout(buffer):
            mutations = run(
                _manifest(5), coverage={}, exclusions={}, existing_issues={}, github=github, dry_run=True
            )

        self.assertEqual(5, len(mutations))
        self.assertEqual([], github.calls)
        output = buffer.getvalue()
        for m in mutations:
            self.assertIn(m.reason, output)

    def test_dry_run_is_not_capped_by_max_mutations(self):
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

        self.assertEqual(100, len(mutations))

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

        run(
            _manifest(5),
            coverage={},
            exclusions={},
            existing_issues={},
            github=github,
            dry_run=False,
            max_mutations=50,
            sleep=lambda seconds: None,
        )

        self.assertEqual(5, len(github.calls))

    def test_paces_one_sleep_per_mutation_to_avoid_the_secondary_rate_limit(self):
        github = FakeGitHub()
        sleeps = []

        run(
            _manifest(5),
            coverage={},
            exclusions={},
            existing_issues={},
            github=github,
            dry_run=False,
            max_mutations=50,
            sleep=sleeps.append,
        )

        self.assertEqual(5, len(sleeps))


if __name__ == "__main__":
    unittest.main()
