import io.github.pshevche.spockk.lang.returned
import io.github.pshevche.spockk.lang.times

interface Counter {
  fun count(): Int
}

class PrimitiveReturnCardinality : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.given
    val obj = Mock(Counter::class.java)

    io.github.pshevche.spockk.lang.`when`
    obj.count()

    io.github.pshevche.spockk.lang.then
    1 * obj.count() returned 42
  }
}
