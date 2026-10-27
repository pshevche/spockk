class ConditionMethodVerifyEachWithOnlyException : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    io.github.pshevche.spockk.lang.verifyEach(listOf("")) {
      throw Exception("foo")
    }
  }
}
