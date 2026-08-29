class NoExceptionConditionNoWrapping : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.`when`
    val x = 1

    io.github.pshevche.spockk.lang.then
    true
  }
}
