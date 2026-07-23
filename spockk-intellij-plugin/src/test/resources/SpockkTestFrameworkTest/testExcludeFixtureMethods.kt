import io.github.pshevche.spockk.lang.expect

class FixtureMethodSpec : spock.lang.Specification() {
  fun setup() {
    expect
    println("setup")
  }

  fun cleanup() {
    expect
    println("cleanup")
  }

  fun setupSpec() {
    expect
    println("setupSpec")
  }

  fun cleanupSpec() {
    expect
    println("cleanupSpec")
  }

  fun `a real feature`() {
    expect
    assert(true)
  }
}
