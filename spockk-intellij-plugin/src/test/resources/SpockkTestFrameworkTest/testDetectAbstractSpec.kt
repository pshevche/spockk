abstract class AbstractBaseSpec : spock.lang.Specification() {
  fun `base feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}

class ConcreteSpec : AbstractBaseSpec() {
  fun `concrete feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
