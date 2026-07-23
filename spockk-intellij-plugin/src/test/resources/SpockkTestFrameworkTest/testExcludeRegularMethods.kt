import io.github.pshevche.spockk.lang.expect

class RegularMethodSpec : spock.lang.Specification() {
  fun helperMethod(): Int {
    return 42
  }

  fun `a real feature`() {
    expect
    assert(helperMethod() == 42)
  }
}
