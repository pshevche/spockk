import io.github.pshevche.spockk.lang.*

class SinglePipeMultiVariableSpec : spock.lang.Specification() {
  fun `single data pipe with multiple variables`(a: Int, b: Int, c: Int) {
    expect
    a + b == c

    where
    variables(a, b, c).from(listOf(1, 2), listOf(3, 4), listOf(4, 6))
  }
}
