class Assertions {
  companion object {
    @spock.lang.VerifyAll
    @JvmStatic
    fun isPositive(a: Int) {
      a > 0
    }
  }
}
