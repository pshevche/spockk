import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

class ReferenceFeatureVariableInDataTableSpec : spock.lang.Specification() {
  fun `a feature`(a: Int, b: Int) {
    expect
    assertEquals(b, a)

    where
    a ; b
    1 ; 1
    2 ; a
    3 ; 3
  }
}
