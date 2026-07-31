class ExplicitNegationCondition : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(!(1 == 2))
  }
}
