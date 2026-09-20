import unittest

from github_api import GitHub, GitHubAPIError


class FakeResponse:
    def __init__(self, status, headers=None, body=b""):
        self.status = status
        self.headers = headers or {}
        self.body = body


class FakeTransport:
    """Stub HTTP layer: a queue of canned responses per call, keyed by call order."""

    def __init__(self, responses):
        self.responses = list(responses)
        self.requests = []
        self.sleeps = []

    def request(self, method, url, headers, body=None):
        self.requests.append((method, url, headers, body))
        return self.responses.pop(0)

    def sleep(self, seconds):
        self.sleeps.append(seconds)


class GitHubClientTest(unittest.TestCase):
    def test_paginate_follows_link_header_to_exhaustion(self):
        transport = FakeTransport(
            [
                FakeResponse(
                    200,
                    {"Link": '<https://api.github.com/x?page=2>; rel="next"'},
                    b'[{"id": 1}]',
                ),
                FakeResponse(200, {}, b'[{"id": 2}]'),
            ]
        )
        gh = GitHub(token="t", repo="o/r", transport=transport)

        items = gh.paginate("/issues")

        self.assertEqual([{"id": 1}, {"id": 2}], items)
        self.assertEqual(2, len(transport.requests))

    def test_secondary_rate_limit_message_sleeps_then_retries(self):
        transport = FakeTransport(
            [
                FakeResponse(403, {}, b'{"message": "You have exceeded a secondary rate limit"}'),
                FakeResponse(201, {}, b'{"number": 42}'),
            ]
        )
        gh = GitHub(token="t", repo="o/r", transport=transport)

        result = gh.post("/issues", {"title": "x"})

        self.assertEqual({"number": 42}, result)
        self.assertEqual(1, len(transport.sleeps))

    def test_secondary_rate_limit_honors_retry_after_header(self):
        transport = FakeTransport(
            [
                FakeResponse(403, {"Retry-After": "30"}, b'{"message": "blocked"}'),
                FakeResponse(201, {}, b'{"number": 42}'),
            ]
        )
        gh = GitHub(token="t", repo="o/r", transport=transport)

        gh.post("/issues", {"title": "x"})

        self.assertEqual([30], transport.sleeps)

    def test_rate_limit_sleeps_until_reset_then_retries(self):
        transport = FakeTransport(
            [
                FakeResponse(403, {"X-RateLimit-Remaining": "0", "X-RateLimit-Reset": "1000"}, b""),
                FakeResponse(200, {}, b'{"ok": true}'),
            ]
        )
        gh = GitHub(token="t", repo="o/r", transport=transport, now=lambda: 990)

        result = gh.get("/rate-limited")

        self.assertEqual({"ok": True}, result)
        self.assertEqual(1, len(transport.sleeps))
        self.assertGreaterEqual(transport.sleeps[0], 10)

    def test_get_returns_none_on_404(self):
        transport = FakeTransport([FakeResponse(404, {}, b"")])
        gh = GitHub(token="t", repo="o/r", transport=transport)

        self.assertIsNone(gh.get("/missing"))

    def test_paginate_raises_instead_of_treating_an_error_body_as_items(self):
        transport = FakeTransport(
            [
                FakeResponse(
                    200,
                    {"Link": '<https://api.github.com/x?page=2>; rel="next"'},
                    b'[{"id": 1}]',
                ),
                FakeResponse(403, {}, b'{"message": "not supported", "documentation_url": "..."}'),
            ]
        )
        gh = GitHub(token="t", repo="o/r", transport=transport)

        with self.assertRaises(GitHubAPIError):
            gh.paginate("/issues")

    def test_get_raises_on_an_unexpected_non_2xx_status(self):
        transport = FakeTransport([FakeResponse(422, {}, b'{"message": "unprocessable"}')])
        gh = GitHub(token="t", repo="o/r", transport=transport)

        with self.assertRaises(GitHubAPIError):
            gh.get("/broken")

    def test_post_raises_on_an_unexpected_non_2xx_status(self):
        transport = FakeTransport([FakeResponse(500, {}, b'{"message": "server error"}')])
        gh = GitHub(token="t", repo="o/r", transport=transport)

        with self.assertRaises(GitHubAPIError):
            gh.post("/issues", {"title": "x"})

    def test_ensure_labels_creates_a_missing_label(self):
        transport = FakeTransport(
            [
                FakeResponse(404, {}, b""),  # get existing label: not found
                FakeResponse(201, {}, b'{"name": "test-coverage::gap"}'),  # create
            ]
        )
        gh = GitHub(token="t", repo="o/r", transport=transport)

        gh.ensure_labels([{"name": "test-coverage::gap", "color": "ededed", "description": "a gap"}])

        methods = [r[0] for r in transport.requests]
        self.assertEqual(["GET", "POST"], methods)

    def test_ensure_labels_leaves_an_existing_label_alone(self):
        transport = FakeTransport(
            [FakeResponse(200, {}, b'{"name": "test-coverage::gap", "color": "000000"}')],
        )
        gh = GitHub(token="t", repo="o/r", transport=transport)

        gh.ensure_labels([{"name": "test-coverage::gap", "color": "ededed", "description": "a gap"}])

        methods = [r[0] for r in transport.requests]
        self.assertEqual(["GET"], methods)

    def test_get_sub_issues_paginates_the_sub_issues_endpoint(self):
        transport = FakeTransport([FakeResponse(200, {}, b'[{"number": 12, "id": 111}]')])
        gh = GitHub(token="t", repo="o/r", transport=transport)

        result = gh.get_sub_issues(99)

        self.assertEqual([{"number": 12, "id": 111}], result)
        method, url, _, _ = transport.requests[0]
        self.assertEqual("GET", method)
        self.assertIn("/issues/99/sub_issues", url)

    def test_add_sub_issue_posts_the_sub_issue_id(self):
        transport = FakeTransport([FakeResponse(201, {}, b"{}")])
        gh = GitHub(token="t", repo="o/r", transport=transport)

        gh.add_sub_issue(parent_number=99, sub_issue_id=555)

        method, url, _, body = transport.requests[0]
        self.assertEqual("POST", method)
        self.assertIn("/issues/99/sub_issues", url)
        self.assertEqual({"sub_issue_id": 555}, body)


if __name__ == "__main__":
    unittest.main()
