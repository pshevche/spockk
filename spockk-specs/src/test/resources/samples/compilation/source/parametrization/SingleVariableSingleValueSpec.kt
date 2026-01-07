import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

class SingleVariableSingleValueSpec : spock.lang.Specification() {
  fun `single variable single value`(a: Int) {
    expect
    assertEquals(1, a)

    where
    variable(a).from(1)
  }
}
