class CleanupMethod : spock.lang.Specification() {
  fun cleanup() {
    println("cleanup")
  }

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
