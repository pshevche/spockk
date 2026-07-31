import io.github.pshevche.spockk.lang.expect

class DerivedSpec : InheritedBaseSpec() {
  fun setup() {
    println("setup")
  }

  fun `derived feature`() {
    expect
    assert(true)
  }
}
