# CLAUDE.md

This project is developed with both [OpenCode](https://opencode.ai) and Claude Code. Project instructions live in
`AGENTS.md` so both tools share one source of truth instead of two files drifting apart - this file only routes
Claude Code to it.

@AGENTS.md
@.opencode/instructions/conventional-commits.md

Skills used by OpenCode (`.opencode/skills/`) are also available to Claude Code via symlinks under `.claude/skills/`.
The `gh-issue` skill's `type::story` route calls OpenCode's `superpowers/brainstorming` plugin skill, which has no
Claude Code equivalent - run that step as a manual discussion instead when using Claude Code.
