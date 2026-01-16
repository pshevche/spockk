import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

class SingleVariableVarargValuesSpec : spock.lang.Specification() {
  fun `single variable vararg values`(a: Int) {
    expect
    assertEquals(0, a % 2)

    where
    variable(a).from(2, 4, 6)
  }
}
