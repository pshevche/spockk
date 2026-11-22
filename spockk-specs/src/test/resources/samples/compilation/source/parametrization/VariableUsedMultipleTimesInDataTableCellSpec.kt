import io.github.pshevche.spockk.lang.*

class VariableUsedMultipleTimesInDataTableCellSpec : spock.lang.Specification() {
  fun `a feature`(a: Int, b: Int, result: Int) {
    expect
    a + b == result

    where
    a ; b ; result
    1 ; 2 ; a + b
    3 ; 4 ; a + a
  }
}
