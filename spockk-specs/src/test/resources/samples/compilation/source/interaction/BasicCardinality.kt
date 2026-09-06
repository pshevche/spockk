import io.github.pshevche.spockk.lang.times

interface Greeter {
  fun greet(name: String)
}

class BasicCardinality : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.given
    val obj = Mock(Greeter::class.java)

    io.github.pshevche.spockk.lang.`when`
    obj.greet("Alice")

    io.github.pshevche.spockk.lang.then
    1 * obj.greet("Alice")
  }
}
