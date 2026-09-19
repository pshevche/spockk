#!/usr/bin/env bash
# Opens a PR for the regenerated inventory manifest, carrying the mutation plan and upstream SHA.
# Exits 0 without opening anything when the manifest is unchanged - the common case, and it must
# not create weekly noise.
set -euo pipefail

MANIFEST_PATH="_docs/test-coverage/spock-inventory.json"

if git diff --quiet -- "$MANIFEST_PATH"; then
  echo "No manifest changes; nothing to open."
  exit 0
fi

DATE="$(date -u +%Y-%m-%d)"
BRANCH="automation/test-coverage-sync-${DATE}"
PLAN_FILE="/tmp/plan.txt"

git config user.name "github-actions[bot]"
git config user.email "github-actions[bot]@users.noreply.github.com"

git checkout -b "$BRANCH"
git add "$MANIFEST_PATH"
git commit -m "chore: sync test coverage inventory (${DATE})"
git push -u origin "$BRANCH"

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

gh pr create \
  --title "chore: sync test coverage inventory (${DATE})" \
  --body "$PR_BODY" \
  --base main \
  --head "$BRANCH"
