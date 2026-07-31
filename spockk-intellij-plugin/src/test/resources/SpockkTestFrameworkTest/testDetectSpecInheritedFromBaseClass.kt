import io.github.pshevche.spockk.lang.expect

class DerivedSpec : InheritedBaseSpec() {
  fun `derived feature`() {
    expect
    assert(true)
  }
}
