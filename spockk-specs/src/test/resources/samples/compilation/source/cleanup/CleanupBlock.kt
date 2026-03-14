class CleanupBlock : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)

    io.github.pshevche.spockk.lang.cleanup
    println("cleanup")
  }
}
