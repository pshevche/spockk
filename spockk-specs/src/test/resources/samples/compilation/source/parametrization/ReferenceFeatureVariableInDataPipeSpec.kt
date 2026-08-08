import io.github.pshevche.spockk.lang.*

class ReferenceFeatureVariableInDataPipeSpec : spock.lang.Specification() {
  fun `a feature`(a: Int, b: Int) {
    expect
    a == b

    where
    variable(a).from(1, 2, 3)
    variable(b).from(a + 1)
  }
}
