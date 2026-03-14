class SetupSpecMethod : spock.lang.Specification() {
  fun setupSpec() {
    println("setupSpec")
  }

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
