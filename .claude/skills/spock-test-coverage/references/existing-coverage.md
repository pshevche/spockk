# Searching for existing coverage

`spockk-specs` has hundreds of feature methods written before this coverage-tracking programme, and some already
overlap the upstream suite in places nobody has mapped. This search is the first thing to do on every ticket,
before writing any new test. The failure mode it prevents is annotating a test that merely looks close enough,
which converts a real gap into a false tick that nobody will revisit.

## Where to look

Narrow by the ticket's area and recipe first:

- `area: conditions` -> `spockk-specs/src/test/kotlin/.../smoke/condition/` and `.../runtime/ConditionRenderingTest.kt`
- `area: mocking` -> `spockk-specs/src/test/kotlin/.../smoke/mock/`
- `area: parameterization` -> `spockk-specs/src/test/kotlin/.../smoke/parametrization/`
- `area: extensions`, `runtime` -> `spockk-specs/src/test/kotlin/.../runtime/`
- compile-error / ast-snapshot recipes -> `spockk-specs/src/test/kotlin/.../compilation/`

Search by the behavior, not by name similarity: grep for the operators, method calls, or block shapes the upstream
feature exercises, not just for a class name that sounds similar. `TopLevelImplicitConditionSmokeTest` and
`TopLevelExplicitConditionSmokeTest` sound almost identical but cover different constructs (`expect: <expr>` vs.
`expect: assert(<expr>)`) - name resemblance is a starting point for the search, never the basis for a match.

## Judging a real match vs. a superficial resemblance

A real match must exercise the same construct with the same semantics, not just produce a similarly-shaped result.
Ask specifically:

- Does the existing test's expression shape match the upstream feature's (implicit vs. explicit condition, same
  operator, same failure/success expectation)?
- Does it fail for the same reason the upstream feature would, if the underlying behavior broke? (This is the same
  question `fidelity.md` asks about a fresh port - it applies equally to claiming an existing test as coverage.)
- Is it testing the same Kotlin-level behavior, not just an adjacent one that happens to use similar syntax?

If any of these is unclear, treat it as no match and write a fresh test. A missed match costs a duplicate test; a
false match costs a permanently unreviewed gap.

## Partial overlap

An existing test covering *some* of the upstream feature's assertions is moved into the ticket's target class and
then **extended** to cover the rest - it is not left as-is with the annotation attached regardless. The annotation
asserts the feature is fully covered, so that must be true before it lands. If the existing test asserts 2 of an
upstream feature's 4 conditions, add the other 2 before annotating.

## One test, several upstream features

Common, since `spockk-specs` was written independently of the upstream suite. `@MigratedFrom` takes `vararg keys`
for exactly this case:

```kotlin
@MigratedFrom(
  "org.spockframework.smoke.condition.SatisfiedConditions#boolean",
  "org.spockframework.smoke.condition.SatisfiedConditions#involving operators and methods",
)
fun `covers both boolean and arithmetic implicit conditions`() { ... }
```

Do not split one working test into several just to give each upstream feature its own annotation - that adds
maintenance surface for no benefit. Annotate the one test with every key it genuinely covers.

## When a match is found: move, don't copy

Moving a test into its target class means the diff shows the test body unchanged (a rename/move) plus the new
`@MigratedFrom` annotation, package, and import adjustments. It does not mean deleting the old test and writing a
new one with the same assertions - that loses the "this was already passing" signal a move preserves and makes the
diff much harder to review.
