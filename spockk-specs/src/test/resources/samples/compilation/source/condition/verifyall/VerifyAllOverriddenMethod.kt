open class BaseAssertions {
  @spock.lang.VerifyAll
  open fun isValid(a: Int) {
    a >= 0
  }
}

class SpecificAssertions : BaseAssertions() {
  override fun isValid(a: Int) {
    a > 0
  }
}
