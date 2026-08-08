class VerifyInHelperMethod : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    checkValue()
  }

  private fun checkValue() {
    io.github.pshevche.spockk.lang.verify(1) { this == 1 }
  }
}
