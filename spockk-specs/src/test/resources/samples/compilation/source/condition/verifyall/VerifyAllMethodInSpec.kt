class VerifyAllMethodInSpec : spock.lang.Specification() {
  @spock.lang.VerifyAll
  fun isPositiveAndEven(a: Int) {
    a > 0
    a % 2 == 0
  }
}
