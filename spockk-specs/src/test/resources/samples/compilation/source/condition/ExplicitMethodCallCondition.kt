class ExplicitMethodCallCondition : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert("hello".startsWith("he"))
  }
}
