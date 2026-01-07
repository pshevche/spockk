import io.github.pshevche.spockk.lang.*
import kotlin.test.assertEquals

class MultiPipeMultiVariableSpec : spock.lang.Specification() {
  fun `multiple data pipes`(a: Int, b: Int, c: Int) {
    expect
    assertEquals(c, a + b)

    where
    variables(a, b).from(listOf(1, 2), listOf(3, 4))
    variable(c).from(listOf(4, 6))
  }
}
