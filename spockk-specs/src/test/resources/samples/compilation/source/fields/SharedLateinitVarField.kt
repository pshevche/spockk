class SharedLateinitVarField : spock.lang.Specification() {
  @spock.lang.Shared
  lateinit var uninitializedSharedField: String

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    uninitializedSharedField = "initialized"
    assert(uninitializedSharedField == "initialized")
  }
}
