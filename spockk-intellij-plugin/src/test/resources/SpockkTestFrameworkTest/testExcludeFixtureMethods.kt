import io.github.pshevche.spockk.lang.expect

class FixtureMethodSpec : spock.lang.Specification() {
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

  fun `a real feature`() {
    expect
    assert(true)
  }
}
