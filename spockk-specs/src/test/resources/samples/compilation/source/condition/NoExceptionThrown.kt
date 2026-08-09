class NoExceptionThrown : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.`when`
    check(true)

    io.github.pshevche.spockk.lang.then
    noExceptionThrown()
  }
}
