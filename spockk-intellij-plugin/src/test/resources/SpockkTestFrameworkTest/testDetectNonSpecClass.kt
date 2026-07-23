import spock.lang.Specification

class MySpec : Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.expect
    assert(true)
  }
}

class NotASpec {
  fun regularMethod() {
    println("not a test")
  }
}
