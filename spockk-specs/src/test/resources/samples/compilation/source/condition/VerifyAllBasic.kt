class VerifyAllBasic : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    io.github.pshevche.spockk.lang.verifyAll(1) { this == 1 }
  }
}
