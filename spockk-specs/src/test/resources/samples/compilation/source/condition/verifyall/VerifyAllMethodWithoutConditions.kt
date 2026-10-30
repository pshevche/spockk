class NonAssertions {
  companion object {
    @spock.lang.VerifyAll
    @JvmStatic
    fun copy(a: Int?) {
      val uselessCopy = a
    }
  }
}
