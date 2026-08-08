class VerifyInExtensionFunction : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    checkValue()
  }
}

fun spock.lang.Specification.checkValue() {
  io.github.pshevche.spockk.lang.verify(1) { this == 1 }
}
