---
name: gh-issue
description: Hand off a GitHub issue to Claude for implementation. Fetches the issue, creates a worktree, then either brainstorms a design spec (type::story) or starts implementation immediately (type::task).
allowed-tools: Bash, Read, Glob, Grep, WebFetch, Agent, TodoWrite, Skill, EnterWorktree
user-invocable: true
---

You are being asked to take ownership of a GitHub issue in the Spockk project and drive it to completion.

The user will invoke this skill with an issue number, e.g. `/gh-issue 42`.

## Step 1 — Fetch the issue

Run:
```bash
gh issue view <number> --json number,title,body,labels,assignees,milestone
```

Read the issue carefully:
- **Title and body**: understand the requirement
- **Labels**: determine the workflow (see Step 3)

## Step 2 — Create a worktree

Use the `EnterWorktree` tool to create an isolated worktree for this issue. Name the worktree using the issue slug: the issue title lowercased, spaces replaced by hyphens, non-alphanumeric characters removed, truncated to 50 characters.

The worktree gives you an isolated copy of the repo so your work is independent from the main working directory.

Confirm the worktree was created and report its path and branch to the user.

## Step 3 — Route based on label

Inspect the issue labels and choose the workflow:

### If labeled `type::task` (or `type::bug`)

Proceed directly to implementation:

1. Read `CLAUDE.md` to orient yourself in the project
2. Understand the codebase relevant to the issue (use Grep/Glob/Read as needed)
3. Implement the changes following the conventions in `CLAUDE.md`
4. Run `./gradlew spotlessApply` to format
5. Run `./gradlew build` to verify everything passes
6. Commit with a conventional commit message referencing the issue: `feat: <summary> (#<number>)`
7. Push the branch and create a PR:
   ```bash
   gh pr create --title "<title>" --body "$(cat <<'EOF'
   Closes #<number>

   ## Summary
   <bullet points>

   ## Test plan
   <checklist>

   🤖 Generated with [Claude Code](https://claude.com/claude-code)
   EOF
   )"
   ```
8. Show the user the PR URL

### If labeled `type::story`

A design spec and implementation plan are required before coding. Use the superpowers brainstorming and planning skills:

1. Invoke `/superpowers:brainstorming` — this will explore the design space, write a spec to `docs/superpowers/specs/`, and then invoke the writing-plans skill automatically.
2. The writing-plans skill writes an implementation plan to `docs/superpowers/plans/`.

Tell the user: "This is a `type::story` issue. I've started a design spec — please review and approve it before I proceed to implementation."

**Do not start coding until the user explicitly approves the design spec and plan.**

### If labeled `type::chore`

Follow the same flow as `type::task` but use a `chore:` commit prefix.

### If no matching label

Ask the user: "This issue has no `type::` label. Should I treat it as a task (implement now) or a story (design spec first)?"

## Conventions reminder

- Commit messages: Conventional Commits — `feat:`, `fix:`, `chore:`, `refactor:`, `test:`, `docs:`
- Always run `./gradlew spotlessApply` before committing
- Always run `./gradlew build` before creating a PR — fix any failures before proceeding
