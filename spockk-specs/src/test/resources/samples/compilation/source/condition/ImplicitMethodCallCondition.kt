class ImplicitMethodCallCondition : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    "hello".startsWith("he")
  }
}
