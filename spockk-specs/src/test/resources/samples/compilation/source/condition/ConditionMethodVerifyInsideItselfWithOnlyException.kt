class ConditionMethodVerifyInsideItselfWithOnlyException : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    io.github.pshevche.spockk.lang.verify(listOf("")) {
      io.github.pshevche.spockk.lang.verify(listOf("")) {
        throw Exception("foo")
      }
    }
  }
}
