#!/usr/bin/env python3
"""A minimal stdlib GitHub REST client: no `requests` dependency, transport injectable for tests."""
import json
import re
import time
import urllib.error
import urllib.request

API_ROOT = "https://api.github.com"

LABELS = [
    {"name": "test-coverage::dashboard", "color": "5319e7", "description": "The single coverage dashboard issue"},
    {"name": "test-coverage::area", "color": "1d76db", "description": "An area or sub-area roll-up"},
    {"name": "test-coverage::spec", "color": "0e8a16", "description": "A per-class (or per-part) porting ticket"},
    {"name": "test-coverage::gap", "color": "d93f0b", "description": "A Spockk deficiency found while porting"},
    {
        "name": "test-coverage::blocked",
        "color": "b60205",
        "description": "An agent confirmed this ticket cannot be ported yet",
    },
    {
        "name": "test-coverage::drift",
        "color": "fbca04",
        "description": "Upstream changed a feature's body after it was ported",
    },
]

LINK_NEXT_RE = re.compile(r'<([^>]+)>;\s*rel="next"')

SECONDARY_RATE_LIMIT_INITIAL_WAIT = 60


class GitHubAPIError(Exception):
    """Raised when the API returns an unexpected non-2xx status."""

    def __init__(self, status, body):
        self.status = status
        self.body = body
        super().__init__(f"GitHub API returned {status}: {body!r}")


class _UrllibTransport:
    def request(self, method, url, headers, body=None):
        data = json.dumps(body).encode("utf-8") if body is not None else None
        request = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(request) as response:
                return _Response(response.status, dict(response.headers), response.read())
        except urllib.error.HTTPError as e:
            return _Response(e.code, dict(e.headers), e.read())

    def sleep(self, seconds):
        time.sleep(seconds)


class _Response:
    def __init__(self, status, headers, body):
        self.status = status
        self.headers = headers
        self.body = body


class GitHub:
    def __init__(self, token, repo, transport=None, now=time.time):
        self.token = token
        self.repo = repo
        self._transport = transport or _UrllibTransport()
        self._now = now

    def _headers(self):
        return {
            "Authorization": f"Bearer {self.token}",
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
        }

    def _url(self, path):
        return path if path.startswith("http") else f"{API_ROOT}/repos/{self.repo}{path}"

    def _request(self, method, path, body=None):
        url = self._url(path)
        secondary_limit_wait = SECONDARY_RATE_LIMIT_INITIAL_WAIT
        while True:
            response = self._transport.request(method, url, self._headers(), body)
            if response.status == 403 and response.headers.get("X-RateLimit-Remaining") == "0":
                reset_at = int(response.headers.get("X-RateLimit-Reset", "0"))
                self._transport.sleep(max(0, reset_at - self._now()) + 1)
                continue
            if self._is_secondary_rate_limit(response):
                retry_after = response.headers.get("Retry-After")
                wait = int(retry_after) if retry_after else secondary_limit_wait
                self._transport.sleep(wait)
                secondary_limit_wait *= 2
                continue
            return response

    @staticmethod
    def _is_secondary_rate_limit(response):
        if response.status not in (403, 429):
            return False
        if response.headers.get("Retry-After"):
            return True
        return b"secondary rate limit" in response.body.lower()

    def get(self, path):
        response = self._request("GET", path)
        if response.status == 404:
            return None
        self._raise_for_status(response)
        return json.loads(response.body) if response.body else None

    def post(self, path, body):
        response = self._request("POST", path, body)
        self._raise_for_status(response)
        return json.loads(response.body) if response.body else None

    def patch(self, path, body):
        response = self._request("PATCH", path, body)
        self._raise_for_status(response)
        return json.loads(response.body) if response.body else None

    def paginate(self, path):
        items = []
        url = path
        while url:
            response = self._request("GET", url)
            self._raise_for_status(response)
            items.extend(json.loads(response.body) if response.body else [])
            link = response.headers.get("Link", "")
            match = LINK_NEXT_RE.search(link)
            url = match.group(1) if match else None
        return items

    def _raise_for_status(self, response):
        if not 200 <= response.status < 300:
            raise GitHubAPIError(response.status, response.body)

    def ensure_labels(self, specs):
        for spec in specs:
            existing = self.get(f"/labels/{spec['name']}")
            if existing is None:
                self.post("/labels", spec)
