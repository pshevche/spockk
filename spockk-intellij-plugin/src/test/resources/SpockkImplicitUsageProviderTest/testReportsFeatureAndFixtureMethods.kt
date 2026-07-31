import io.github.pshevche.spockk.lang.expect

class SampleSpec : spock.lang.Specification() {
  fun setup() {
    println("setup")
  }

  fun cleanup() {
    println("cleanup")
  }

  fun setupSpec() {
    println("setupSpec")
  }

  fun cleanupSpec() {
    println("cleanupSpec")
  }

  fun helperMethod(): Int = 42

  fun `a feature`() {
    expect
    assert(helperMethod() == 42)
  }
}
