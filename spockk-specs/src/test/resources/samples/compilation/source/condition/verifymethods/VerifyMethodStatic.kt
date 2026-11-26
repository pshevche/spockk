class Assertions {
  companion object {
    @JvmStatic
    @spock.lang.Verify
    fun isPositive(a: Int) {
      a > 0
    }
  }
}
