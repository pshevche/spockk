class CleanupSpecMethod : spock.lang.Specification() {
  fun cleanupSpec() {
    println("cleanupSpec")
  }

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}
