class SetupMethod : spock.lang.Specification() {
  fun setup() {
    println("setup")
  }

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
