#!/usr/bin/env bash
# Opens a PR for the regenerated inventory manifest, carrying the mutation plan and upstream SHA.
# Exits 0 without opening anything when the manifest is unchanged - the common case, and it must
# not create weekly noise.
#
# Commits the manifest change through GitHub's Contents API rather than a local `git commit`: a
# commit made that way is automatically shown as "Verified" (GitHub itself co-signs it), which a
# plain unsigned local commit is not - required if the base branch enforces signed commits.
set -euo pipefail

MANIFEST_PATH="_docs/test-coverage/spock-inventory.json"
BASE_BRANCH="main"

if git diff --quiet -- "$MANIFEST_PATH"; then
  echo "No manifest changes; nothing to open."
  exit 0
fi

DATE="$(date -u +%Y-%m-%d)"
BRANCH="automation/test-coverage-sync-${DATE}"
PLAN_FILE="/tmp/plan.txt"
COMMIT_MESSAGE="chore: sync test coverage inventory (${DATE})"

BASE_SHA="$(gh api "repos/${GITHUB_REPOSITORY}/git/ref/heads/${BASE_BRANCH}" --jq .object.sha)"

# The branch name is date-only, so a same-day rerun (e.g. a manual re-dispatch after a fix) hits
# an already-existing ref. Reset it to the current base instead of failing: whatever open PR
# already points at it just picks up the fresh commit pushed below.
if ! CREATE_REF_OUTPUT=$(gh api --method POST "repos/${GITHUB_REPOSITORY}/git/refs" \
  -f ref="refs/heads/${BRANCH}" \
  -f sha="${BASE_SHA}" 2>&1); then
  if echo "$CREATE_REF_OUTPUT" | grep -q "Reference already exists"; then
    echo "Branch ${BRANCH} already exists; resetting it to ${BASE_SHA}."
    gh api --method PATCH "repos/${GITHUB_REPOSITORY}/git/refs/heads/${BRANCH}" \
      -f sha="${BASE_SHA}" \
      -F force=true
  else
    echo "$CREATE_REF_OUTPUT" >&2
    exit 1
  fi
fi

FILE_SHA="$(gh api "repos/${GITHUB_REPOSITORY}/contents/${MANIFEST_PATH}?ref=${BRANCH}" --jq .sha)"
CONTENT_B64="$(base64 -w0 "$MANIFEST_PATH")"
jq -n \
  --arg message "$COMMIT_MESSAGE" \
  --arg content "$CONTENT_B64" \
  --arg sha "$FILE_SHA" \
  --arg branch "$BRANCH" \
  '{message: $message, content: $content, sha: $sha, branch: $branch}' |
  gh api --method PUT "repos/${GITHUB_REPOSITORY}/contents/${MANIFEST_PATH}" --input -

PLAN_BODY="(no mutation plan captured)"
if [ -s "$PLAN_FILE" ]; then
  PLAN_BODY="$(cat "$PLAN_FILE")"
fi

PR_BODY="$(cat <<EOF
Weekly test coverage sync.

**Upstream:** \`spockframework/spock@${UPSTREAM_SHA:-unknown}\`

## Mutation plan applied to issues

\`\`\`
${PLAN_BODY}
\`\`\`
EOF
)"

# A same-day rerun may find a still-open PR for this branch from an earlier run today; the fresh
# commit already landed on it above, so just refresh its body with the current plan instead of
# failing on "already exists".
if ! CREATE_PR_OUTPUT=$(gh pr create \
  --title "$COMMIT_MESSAGE" \
  --body "$PR_BODY" \
  --base "$BASE_BRANCH" \
  --head "$BRANCH" 2>&1); then
  if echo "$CREATE_PR_OUTPUT" | grep -qi "already exists"; then
    echo "A PR for ${BRANCH} already exists; updating its body with the fresh plan instead."
    gh pr edit "$BRANCH" --body "$PR_BODY"
  else
    echo "$CREATE_PR_OUTPUT" >&2
    exit 1
  fi
fi
