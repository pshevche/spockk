import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

class SingleVariableValuesListSpec : spock.lang.Specification() {
  fun `single variable values list`(a: Int) {
    expect
    assertEquals(0, a % 2)

    where
    variable(a).from(listOf(2, 4, 6))
  }
}
