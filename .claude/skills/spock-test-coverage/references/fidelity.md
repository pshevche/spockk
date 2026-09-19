# Fidelity: what a faithful port is, and how it fails

A ported test that passes is not automatically a faithful port. `validate.py` cannot detect any of the three
failure modes below; only careful reading during the port can. Each one converts a real gap into a false tick,
which is worse than an honest gap: a false tick looks done and nobody will look at it again.

## Failure mode 1: asserting something weaker than the original

```groovy
// Upstream
def "computes the total"() {
  expect:
  cart.total() == 42.50
}
```

```kotlin
// Wrong port: passes, asserts almost nothing
fun `computes the total`() {
  expect
  cart.total() > 0
}
```

The wrong port passes for many carts whose total upstream would reject. If the exact expected value is awkward to
reproduce (floating point formatting, locale-dependent rendering), that awkwardness is the actual thing worth
solving, not a reason to loosen the assertion. Match the original's precision.

## Failure mode 2: no longer exercising the path the original targeted

```groovy
// Upstream: this feature exists specifically to test cleanup-on-exception ordering
def "cleanup runs even if the feature throws"() {
  when:
  throw new RuntimeException("boom")

  then:
  thrown(RuntimeException)
  cleanupCalled
}
```

```kotlin
// Wrong port: passes, but never actually reaches a thrown exception
fun `cleanup runs even if the feature throws`() {
  `when`
  val threw = true

  then
  threw
}
```

The wrong port is a tautology dressed up as a regression test. It will keep passing forever regardless of whether
Spockk's cleanup-on-exception ordering actually works. When the upstream feature's whole point is to exercise a
specific control-flow path (an exception, a specific ordering, a specific interaction count), the port must
actually trigger that path, not merely assert something that happens to be true.

## Failure mode 3: a data table silently reduced to its first row

```groovy
// Upstream: 4 rows
def "classifies numbers"() {
  expect:
  classify(n) == label

  where:
  n  | label
  -1 | "negative"
  0  | "zero"
  1  | "positive"
  100 | "positive"
}
```

```kotlin
// Wrong port: only the first row survived
fun `classifies numbers`() {
  expect
  classify(-1) == "negative"
}
```

This is the easiest failure mode to introduce by accident when translating a Spock `where:` table into a Kotlin
`where` block or a handful of test methods, because the wrong port still looks like a real test and still passes.
Count the upstream table's rows and confirm every one of them has a corresponding case in the port, whether that is
a Kotlin `where` block with the same number of rows or one test method per row. If some rows genuinely do not
apply under Kotlin (see `groovy-to-kotlin.md` for Groovy-truth-only cases), drop only those specific rows and say so
in the PR description, rather than silently keeping just the first one.

## The general check

Before calling a port done, ask: if the behavior under test were subtly broken in Spockk, would this test actually
fail? If the honest answer is "probably not," the port has one of the three failure modes above, whatever its
outcome status.
