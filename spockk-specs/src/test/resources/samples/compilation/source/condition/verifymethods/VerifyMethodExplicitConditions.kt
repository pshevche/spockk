class Assertions {
  companion object {
    @JvmStatic
    @spock.lang.Verify
    fun isPositiveAndEven(a: Int) {
      assert(a > 0)
      assert(a % 2 == 0)
    }
  }
}
