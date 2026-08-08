import io.github.pshevche.spockk.lang.*

class SingleVariableVarargValuesSpec : spock.lang.Specification() {
  fun `single variable vararg values`(a: Int) {
    expect
    a % 2 == 0

    where
    variable(a).from(2, 4, 6)
  }
}
