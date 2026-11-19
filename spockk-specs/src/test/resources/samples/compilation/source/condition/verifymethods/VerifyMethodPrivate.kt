class VerifyMethodPrivate : spock.lang.Specification() {
  fun foo() {
    io.github.pshevche.spockk.lang.expect
    listOf(true, false).any { it }
    if (true) {
      listOf(true, false).any { it }
    }
    io.github.pshevche.spockk.lang.verify(this) {
      listOf(true, false).any { it }
      if (true) {
        listOf(true, false).any { it }
      }
    }
  }

  @spock.lang.Verify
  private fun isPositive(a: Int) {
    a > 0
    listOf(true, false).any { it }
    if (true) {
      listOf(true, false).any { it }
    }
    io.github.pshevche.spockk.lang.verify(this) {
      listOf(true, false).any { it }
      if (true) {
        listOf(true, false).any { it }
      }
    }
  }
}
