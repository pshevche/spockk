class StaticField : spock.lang.Specification() {
  companion object {
    val staticField = "static"
  }

  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(staticField == "static")
  }
}
