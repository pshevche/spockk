class VerifyAllMethodWithoutConditionsInSpec : spock.lang.Specification() {
  fun foo() {
    io.github.pshevche.spockk.lang.expect
    true
  }

  companion object {
    @spock.lang.VerifyAll
    @JvmStatic
    fun copy(a: Int?) {
      val uselessCopy = a
    }
  }
}
