class NestedVerifyInVerifyAll : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    io.github.pshevche.spockk.lang.verifyAll {
      io.github.pshevche.spockk.lang.verify(1) { this == 1 }
    }
  }
}
