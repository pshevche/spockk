abstract class BaseSpec : spock.lang.Specification() {
  fun `inherited feature 1`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}

abstract class IntermediateSpec : BaseSpec() {
  fun `inherited feature 2`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}

class Spec : IntermediateSpec() {
  fun `own feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
