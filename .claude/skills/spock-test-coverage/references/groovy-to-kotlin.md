# Groovy to Kotlin: traps in porting a condition or assertion

This is the reference most likely to determine whether a port is faithful or subtly wrong. Read it before writing
the ported test body, not after it fails to compile.

## Groovy truth vs. Kotlin's strict `Boolean`

Groovy treats many values as truthy/falsy in a boolean context: `0`, `""`, `null`, and empty collections/maps are
falsy; any other number, non-empty string, non-empty collection, or non-null object is truthy.

```groovy
// Groovy - all four are valid implicit conditions
expect: 1
expect: "hello"
expect: [1, 2]
expect: new Object()
```

Kotlin's `expect` block requires an actual `Boolean`. None of the above type-checks. Do not "fix" this by writing
`1 != 0` or `"hello".isNotEmpty()` and calling it a faithful port - that is a different assertion, testing string
emptiness instead of Groovy truthiness, which does not exist in Kotlin at all. A feature that depends specifically
on Groovy truth for values other than an actual boolean is a candidate for `not-applicable` in the exclusions
ledger, not a rewrite.

## GStrings vs. Kotlin string templates

```groovy
def name = "world"
expect: "hello ${name}" == "hello world"
```

Kotlin's `"hello $name"` is a compile-time string concatenation, not a lazy `GString`. This matters for anything
that depends on `GString`'s deferred evaluation or its distinct runtime type (`instanceof GString` checks,
`toString()` called lazily). A feature testing GString identity/laziness itself, rather than just using string
interpolation incidentally, is `not-applicable`.

## `def` and dynamic typing

```groovy
def x = 42        // type is inferred as int, but x could be reassigned to any type
x = "now a string" // legal in Groovy
```

Kotlin's `val`/`var` are statically typed once inferred; `var x = 42; x = "now a string"` does not compile. A
feature relying on genuinely dynamic retyping (not just type inference) is a Kotlin-native rewrite at best, or
`not-applicable`.

## Closures vs. lambdas

```groovy
def block = { int a, int b -> a + b }
block(1, 2)
```

```kotlin
val block: (Int, Int) -> Int = { a, b -> a + b }
block(1, 2)
```

Straightforward for simple cases. The traps: Groovy closures have an implicit `it` for single-argument closures
(Kotlin's lambda also has `it`, so this one usually ports directly), closures can be curried
(`block.curry(1)`, no direct Kotlin equivalent - rewrite as a partial application or a second lambda), and Groovy
closures resolve `this`/delegate dynamically (`delegate`, `owner`), which Kotlin lambdas do not do at all. A
feature exercising closure delegation strategies is a strong `not-applicable` candidate.

## Operator overloading differences

Groovy overloads operators through method name conventions (`plus`, `minus`, `isCase` for `in`/`switch`, `call` for
`()`), which is a broader and more implicit set than Kotlin's `operator fun` declarations. A Groovy feature that
relies on an operator Kotlin does not support at all (e.g. `isCase` overloading behind `switch`) needs a Kotlin-
native equivalent construct (an explicit `when` with an explicit predicate) or is `not-applicable`.

## Property access vs. getters

```groovy
class Person { String name }
def p = new Person(name: "Fred")
p.name           // calls getName() under the hood
```

```kotlin
class Person(val name: String)
val p = Person("Fred")
p.name            // calls the generated getter, same idea
```

Ports directly in the common case. The trap is Groovy's **named-argument constructor from a map**
(`new Person(name: "Fred")`), which does not exist in Kotlin at all; use an ordinary constructor call or a builder.

## Named and default arguments

```groovy
def greet(String name, String greeting = "Hello") {
  "$greeting, $name"
}
greet("World")
greet(name: "World", greeting: "Hi")
```

Kotlin has real named and default parameters (`fun greet(name: String, greeting: String = "Hello")`), which is
usually a closer match than the Groovy feature being tested, not a divergence to work around:

```kotlin
fun greet(name: String, greeting: String = "Hello") = "$greeting, $name"
greet("World")
greet(name = "World", greeting = "Hi")
```

## Spread operator

```groovy
def nums = [1, 2, 3]
Math.max(*nums)          // spread into varargs
def combined = [*nums, 4, 5]
```

```kotlin
val nums = intArrayOf(1, 2, 3)
maxOf(*nums.toTypedArray()) // spread into varargs, similar syntax
val combined = nums.toList() + listOf(4, 5)
```

Kotlin's `*` spread operator only works into `vararg` parameters, not general list construction; list
concatenation uses `+` instead. Port the intent (combining values), not the exact Groovy syntax shape.

## Condition rendering is not a divergence

Unlike everything above, condition rendering is **not** a place to expect Kotlin/Groovy differences. Spockk
rewrites conditions through Spock's own condition rewriter and renders failures with the shaded Spock runtime
(`SpockRuntime`, `ValueRecorder`, `Condition`, `ExpressionInfoBuilder`), unmodified from upstream. A ported
condition should produce the same rendered diagram as the original, and this holds for `spockk-specs`' own
assertions too, since the suite dogfoods Spockk and routes its own condition checks through the identical path.
There is one condition-rendering mechanism in this codebase, not two, and no Kotlin `power-assert` plugin anywhere
in the build. If a rendered diagram differs from upstream's, that is a bug in the compiler plugin to report, never
a "Kotlin quirk" to accept or paper over.
