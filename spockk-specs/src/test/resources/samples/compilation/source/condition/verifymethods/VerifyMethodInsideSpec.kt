class VerifyMethodInsideSpec : spock.lang.Specification() {
  @spock.lang.Verify
  fun isPositiveAndEven(a: Int) {
    a > 0
    a % 2 == 0
  }
}
