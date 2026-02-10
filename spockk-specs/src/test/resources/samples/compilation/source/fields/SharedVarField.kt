class SharedVarField : spock.lang.Specification() {
  @spock.lang.Shared
  var sharedField = 42

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(sharedField == 42)
  }
}
