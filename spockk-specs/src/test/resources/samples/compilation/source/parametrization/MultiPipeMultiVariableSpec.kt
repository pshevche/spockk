import io.github.pshevche.spockk.lang.*

class MultiPipeMultiVariableSpec : spock.lang.Specification() {
  fun `multiple data pipes`(a: Int, b: Int, c: Int) {
    expect
    a + b == c

    where
    variables(a, b).from(listOf(1, 2), listOf(3, 4))
    variable(c).from(listOf(4, 6))
  }
}
