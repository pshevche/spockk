class InstanceLateinitVarField : spock.lang.Specification() {
  lateinit var uninitializedField: String

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    uninitializedField = "initialized"
    assert(uninitializedField == "initialized")
  }
}
