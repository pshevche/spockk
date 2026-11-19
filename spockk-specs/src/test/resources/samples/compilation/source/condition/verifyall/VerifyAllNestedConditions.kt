class Assertions {
  companion object {
    @spock.lang.VerifyAll
    @JvmStatic
    fun evenIfPositive(a: Int) {
      a != 0
      if (a > 0) {
        a % 2 == 0
      }
    }
  }
}
