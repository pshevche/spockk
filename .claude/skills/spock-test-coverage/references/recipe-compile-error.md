# Recipe: compile-error

**Detection:** `compiler.compile*` combined with `InvalidSpecCompileException` (or a sibling compile-error
exception).
**Target:** `spockk-specs/src/test/kotlin/.../compilation/`, inline via `TestDataFactory.specWithFeatureBody()`,
asserting failure and message.

Upstream compiles a deliberately invalid spec body and asserts the compiler rejects it with a specific message.
Spockk's equivalent compiles a feature body through the real Kotlin compiler plugin pipeline
(`BaseCompilationTest` + `CompilationUtils.transform`) and asserts on `result.isSuccess()` and
`result.compilation.messages`.

## Before (upstream, `org.spockframework.smoke.condition.InvalidConditions`)

```groovy
class InvalidConditions extends EmbeddedSpecification {
  def "assignments are not allowed in then-blocks"() {
    when:
    compiler.compileFeatureBody("""
def x = 42

when:
true

then:
x $op 42
    """)

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("assignment")

    where:
    op << ["=", "+=", "-="]
  }
}
```

## After (the same pattern, from `ExceptionConditionValidationTest`)

```kotlin
class ExceptionConditionValidationTest : BaseCompilationTest() {

  fun `rejects a then block with more than one exception condition`() {
    `when`
    val result =
      transform(
        specWithFeatureBody(
          """
          io.github.pshevche.spockk.lang.`when`
          "".substring(5)

          io.github.pshevche.spockk.lang.then
          thrown(StringIndexOutOfBoundsException::class.java)
          noExceptionThrown()
          """
            .trimIndent()
        )
      )

    then
    !result.isSuccess()
    result.compilation.messages.contains("A 'then' block may only have a single exception condition")
  }
}
```

## Porting notes

- `TestDataFactory.specWithFeatureBody(body)` builds a minimal spec wrapping the given Kotlin source as one
  feature's body; `CompilationUtils.transform(source)` runs it through the real Spockk compiler plugin pipeline and
  returns a result carrying `isSuccess()` and `compilation.messages`.
- Upstream's `where:`-driven parameterization over several invalid operators (`"="`, `"+="`, `"-="`) does not need
  to become a Kotlin `where` block: a handful of separate test methods, one per invalid shape, is clearer and is
  the existing convention in `ExceptionConditionValidationTest`.
- Assert on the specific error message text Spockk actually produces, not a paraphrase of Groovy's. The messages
  are unrelated compiler implementations; matching upstream's wording is not the goal, matching upstream's
  *behavior* (this shape is rejected, for this reason) is.
- If the Kotlin compiler plugin does not reject the equivalent construct at all, that is a real gap: do not adjust
  the assertion to make it pass. Route it through `spock-gap-triage` instead.
