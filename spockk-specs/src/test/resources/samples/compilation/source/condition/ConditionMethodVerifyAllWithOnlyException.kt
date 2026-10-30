class ConditionMethodVerifyAllWithOnlyException : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    io.github.pshevche.spockk.lang.verifyAll(listOf("")) {
      throw Exception("foo")
    }
  }
}
