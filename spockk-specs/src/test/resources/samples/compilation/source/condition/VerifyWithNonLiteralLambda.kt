class VerifyWithNonLiteralLambda : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    val block: Int.() -> Unit = { this == 1 }
    io.github.pshevche.spockk.lang.verify(1, block)
  }
}
