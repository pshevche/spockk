class MultipleFields : spock.lang.Specification() {
  val answer = 42
  var instanceField = "hello"
  @spock.lang.Shared
  val sharedField = 24

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(answer == 42)
    assert(instanceField == "hello")
    assert(sharedField == 24)
  }
}
