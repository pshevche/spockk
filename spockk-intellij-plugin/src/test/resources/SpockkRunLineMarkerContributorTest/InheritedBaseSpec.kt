import io.github.pshevche.spockk.lang.expect
import spock.lang.Specification

abstract class InheritedBaseSpec : Specification() {
  fun `base feature`() {
    expect
    assert(true)
  }
}
