class VerifyEachBasic : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    io.github.pshevche.spockk.lang.verifyEach(listOf(1, 2)) { this == 1 }
  }
}
