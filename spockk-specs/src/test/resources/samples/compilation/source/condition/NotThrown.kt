class NotThrown : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.`when`
    check(false)

    io.github.pshevche.spockk.lang.then
    notThrown(IllegalArgumentException::class.java)
  }
}
