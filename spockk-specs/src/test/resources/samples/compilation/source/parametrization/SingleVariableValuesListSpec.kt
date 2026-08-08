import io.github.pshevche.spockk.lang.*

class SingleVariableValuesListSpec : spock.lang.Specification() {
  fun `single variable values list`(a: Int) {
    expect
    a % 2 == 0

    where
    variable(a).from(listOf(2, 4, 6))
  }
}
