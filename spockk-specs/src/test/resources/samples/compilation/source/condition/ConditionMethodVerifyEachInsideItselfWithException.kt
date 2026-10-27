class ConditionMethodVerifyEachInsideItselfWithException : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    io.github.pshevche.spockk.lang.verifyEach(listOf("")) {
      io.github.pshevche.spockk.lang.verifyEach(listOf("")) {
        true
        throw Exception("foo")
      }
    }
  }
}
