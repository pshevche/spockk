class InstanceVarField : spock.lang.Specification() {
  var instanceField = "hello"

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(instanceField == "hello")
  }
}
