import io.github.pshevche.spockk.lang.*

class SingleVariableSingleValueSpec : spock.lang.Specification() {
  fun `single variable single value`(a: Int) {
    expect
    a == 1

    where
    variable(a).from(1)
  }
}
