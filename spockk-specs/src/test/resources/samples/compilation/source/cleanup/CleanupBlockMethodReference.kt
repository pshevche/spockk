class CleanupBlockMethodReference : spock.lang.Specification() {
  @Suppress("UNINITIALIZED_VARIABLE")
  fun `some feature`() {
    io.github.pshevche.spockk.lang.`when`
    val foobar = foobar()

    io.github.pshevche.spockk.lang.then
    println(foobar)

    io.github.pshevche.spockk.lang.cleanup
    foobar.length
  }

  fun foobar(): String = "foo"
}
