class Assertions {
  companion object {
    @spock.lang.VerifyAll
    @JvmStatic
    fun isPositiveAndEven(a: Int) {
      assert(a > 0)
      assert(a % 2 == 0)
    }
  }
}
