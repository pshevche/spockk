import io.github.pshevche.spockk.lang.*

class ReferenceFeatureVariableInDataTableSpec : spock.lang.Specification() {
  fun `a feature`(a: Int, b: Int) {
    expect
    a == b

    where
    a ; b
    1 ; 1
    2 ; a
    3 ; 3
  }
}
