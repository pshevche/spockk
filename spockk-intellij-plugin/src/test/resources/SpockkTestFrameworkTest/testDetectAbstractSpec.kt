import io.github.pshevche.spockk.lang.expect

abstract class AbstractBaseSpec : spock.lang.Specification() {
  fun `base feature`() {
    expect
    assert(true)
  }
}
