class InstanceValField : spock.lang.Specification() {
  val answer = 42

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(answer == 42)
  }
}
