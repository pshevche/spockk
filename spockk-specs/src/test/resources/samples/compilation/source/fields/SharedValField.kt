class SharedValField : spock.lang.Specification() {
  @spock.lang.Shared
  val sharedAnswer = 42

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(sharedAnswer == 42)
  }
}
