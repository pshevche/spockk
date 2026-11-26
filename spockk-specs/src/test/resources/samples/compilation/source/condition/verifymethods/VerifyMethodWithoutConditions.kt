class NonAssertions {
  companion object {
    @JvmStatic
    @spock.lang.Verify
    fun copy(a: Int?) {
      val uselessCopy = a
    }
  }
}
