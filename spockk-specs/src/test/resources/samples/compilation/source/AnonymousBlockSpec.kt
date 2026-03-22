class AnonymousBlockSpec : spock.lang.Specification() {
  fun `feature with implicit given`() {
    val a = 1
    val b = 2

    io.github.pshevche.spockk.lang.expect
    assert(a + b == 3)
  }
}
