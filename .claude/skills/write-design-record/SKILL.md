---
name: write-design-record
description: Write a design record for a Spockk feature before implementation begins. Researches Spock's implementation, designs the Spockk approach, and saves AsciiDoc files to design-records/<NN>-<slug>/ for user review.
allowed-tools: Bash, Read, Glob, Grep, WebFetch, WebSearch, Write, Agent, Skill, TodoWrite
user-invocable: true
---

You are being asked to write a design record for a Spockk feature before implementation begins.

The user will invoke this skill with an optional issue number, e.g. `/write-design-record 42`.
It may also be invoked automatically by the `gh-issue` skill for `type::story` issues.

## Step 1 — Gather context

If an issue number was provided:
```bash
gh issue view <number> --json number,title,body,labels
```

Read the issue's motivation, desired behavior, acceptance criteria, and Spock reference sections.

Also read `CLAUDE.md` to understand the project structure and conventions.

## Step 2 — Research Spock's implementation

Use the `spock-expert` skill to research how Spock implements the feature:
```
/spock-expert <feature description>
```

This gives you the user-facing behavior, transformation details, runtime semantics, and test coverage in Spock to inform the design.

## Step 3 — Explore the Spockk codebase

Understand how similar features are already implemented in Spockk. Relevant directories:
- Compiler plugin: `spockk-compiler-plugin/src/main/kotlin/io/github/pshevche/spockk/compilation/`
- Core library: `spockk-core/src/main/kotlin/io/github/pshevche/spockk/lang/`
- IntelliJ plugin: `spockk-intellij-plugin/src/main/kotlin/`
- Existing design records: `design-records/` — read the most recent `main.adoc` as a style reference

## Step 4 — Determine the next design record number

```bash
ls -d design-records/[0-9]*/ 2>/dev/null | sort | tail -1
```

The next folder should be numbered one higher (e.g., if `design-records/02-foo/` is the last, create `design-records/03-<slug>/`). Use two-digit zero-padded numbers. The `<slug>` is the feature name lowercased with spaces replaced by hyphens.

## Step 5 — Write the design record

Create the folder `design-records/<NN>-<slug>/` and write two types of files inside it:

### `main.adoc` — the primary design record

Follow the template in `design-records/TEMPLATE.adoc`. It has exactly three sections:

1. **Context** — What is this feature? What is the user-facing value? What does Spockk currently lack?
2. **Functional Design** — Everything needed to understand the proposed implementation: how Spock does it, the gap to Spockk, the proposed approach, key IR transformations with code snippets, affected modules and files.
3. **Implementation Plan** — An ordered list of independently committable increments. Each increment links to its plan document (see below). Example:
   ```asciidoc
   . link:01-plan-block-labels.adoc[Add setup and cleanup block labels] — core library + compiler plugin block registration
   . link:02-plan-cleanup-transform.adoc[Implement cleanup block IR transformation] — try-catch-finally wrapping
   ```

### `NN-plan-<short-desc>.adoc` — one file per implementation increment

For each item in the implementation plan, create a corresponding plan document. Follow the template in `design-records/PLAN-TEMPLATE.adoc`. Each plan document covers:
- What to change and why (specific files, classes, methods)
- Code snippets for non-obvious transformations
- Test plan for this increment (snapshot tests, error scenario tests, smoke tests, engine tests)

Number plan documents starting from `01` within the feature folder.

Use AsciiDoc format throughout. Reference specific file paths and class names. Link to Spock source files on GitHub where relevant.

## Step 6 — Present for review

After writing all files, tell the user:

```
Design record written to design-records/<NN>-<slug>/

  main.adoc                     — overview, functional design, implementation plan
  01-plan-<desc>.adoc           — implementation increment 1
  02-plan-<desc>.adoc           — implementation increment 2
  ...

Please review and let me know when you're ready for me to start implementation, or if you'd like changes.
```

**Do not begin implementation until the user explicitly says to proceed.**

## Quality checklist

Before presenting, verify:
- [ ] `main.adoc` covers all acceptance criteria from the issue
- [ ] Functional design correctly describes the Spock implementation (cross-checked with spock-expert output)
- [ ] Implementation plan lists all files that will be modified across all increments
- [ ] Each plan document has a concrete test plan with specific test class/resource names
- [ ] Plan documents are independently committable (no increment depends on a later one)
- [ ] All files follow the AsciiDoc style of existing design records
