class CleanupBlockWithWhere : spock.lang.Specification() {
  fun `some feature`(a: Int) {
    io.github.pshevche.spockk.lang.expect
    assert(a > 0)

    io.github.pshevche.spockk.lang.cleanup
    println("cleanup")

    io.github.pshevche.spockk.lang.where
    io.github.pshevche.spockk.lang.variable(a).from(1, 2, 3)
  }
}
