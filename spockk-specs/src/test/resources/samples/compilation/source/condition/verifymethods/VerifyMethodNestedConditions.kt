class Assertions {
  companion object {
    @JvmStatic
    @spock.lang.Verify
    fun evenIfPositive(a: Int) {
      a != 0
      if (a > 0) {
        a % 2 == 0
      }
    }
  }
}
