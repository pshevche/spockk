import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

class ReferenceFeatureVariableSpec : spock.lang.Specification() {
  fun `a feature`(a: Int, b: Int) {
    expect
    assertEquals(b, a)

    where
    variable(a).from(1, 2, 3)
    variable(b).from(a + 1)
  }
}
