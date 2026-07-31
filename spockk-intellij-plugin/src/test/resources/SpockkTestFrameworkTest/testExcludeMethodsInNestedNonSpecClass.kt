import io.github.pshevche.spockk.lang.expect

class OuterSpec : spock.lang.Specification() {
  fun `real feature`() {
    expect
    assert(true)
  }

  class NestedHelper {
    fun `looks like a feature`() {
      expect
      assert(true)
    }
  }
}
