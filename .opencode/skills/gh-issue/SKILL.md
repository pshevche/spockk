---
name: gh-issue
description: Hand off a GitHub issue to an OpenCode agent for implementation. Fetches the issue, creates a worktree, then either starts implementation (type::task) or prepares design docs first (type::story).
---

You are being asked to take ownership of a GitHub issue and drive it to completion.

The user will invoke this skill with an issue number, for example `/gh-issue 42`.

## Step 1 - Fetch the issue

Run:

```bash
gh issue view <number> --json number,title,body,labels,assignees,milestone
```

Read the issue carefully:

- **Title and body**: understand the requirement
- **Labels**: determine the workflow (see Step 3)

## Step 2 - Create a worktree

Create an isolated Git worktree for this issue.

1. Build a short slug from the issue title:
   - lowercase
   - spaces replaced with `-`
   - remove non-alphanumeric characters except `-`
   - truncate to 50 characters
2. Create branch `<user>/<slug>`.
3. Create worktree under `.opencode/worktrees/<slug>`.

Example:

```bash
git worktree add -b <user>/<slug> .opencode/worktrees/<slug> HEAD
```

Report the worktree path and branch to the user.

## Step 3 - Route based on label

Inspect issue labels and choose the workflow.

All projects using these conventions follow the same labeling pattern.

### If labeled `type::task` (or `type::bug`)

Proceed directly to implementation:

1. Read `AGENTS.md` to orient yourself in the project.
2. Understand the codebase relevant to the issue.
3. Implement changes following project conventions.
4. Run the project's formatting command (see `AGENTS.md`).
5. Run the project's build command (see `AGENTS.md`).
6. Commit with a conventional commit message referencing the issue: `feat: <summary> (#<number>)` (or `fix:` for bugs).
7. Push the branch and create a PR:

```bash
gh pr create --title "<title>" --body "$(cat <<'EOF'
Closes #<number>

## Summary
<bullet points>

## Test plan
<checklist>

Generated with OpenCode.
EOF
)"
```

8. Show the user the PR URL.

### If labeled `type::story`

A design spec and implementation plan are required before coding.

1. Load and run the `superpowers/brainstorming` skill via OpenCode's native `skill` tool.
2. The brainstorming workflow should produce a design spec under `_docs/specs/` and an implementation plan under `_docs/plans/`.
3. Ask the user to review and approve both documents.

Do not start implementation until approval is explicit.

### If labeled `type::chore`

Follow the same flow as `type::task` but use a `chore:` commit prefix.

### If no matching `type::` label

Ask the user whether to treat it as:

- task (implement now), or
- story (design first)

## Conventions reminder

- Commit messages: Conventional Commits (`feat:`, `fix:`, `chore:`, `refactor:`, `test:`, `docs:`)
- Always run the project's formatting command before committing (see `AGENTS.md`)
- Always run the project's build command before opening a PR (see `AGENTS.md`)