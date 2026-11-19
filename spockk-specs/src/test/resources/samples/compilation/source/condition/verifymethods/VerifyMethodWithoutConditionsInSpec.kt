class VerifyMethodWithoutConditionsInSpec : spock.lang.Specification() {
  fun foo() {
    io.github.pshevche.spockk.lang.expect
    true
  }

  companion object {
    @JvmStatic
    @spock.lang.Verify
    fun copy(a: Int?) {
      val uselessCopy = a
    }
  }
}
