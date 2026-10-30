import io.github.pshevche.spockk.lang.times

class SimpleInteraction : spock.lang.Specification() {
  fun `some feature`() {
    io.github.pshevche.spockk.lang.given
    val list: MutableList<Int> = Mock()

    io.github.pshevche.spockk.lang.`when`
    list.add(1)

    io.github.pshevche.spockk.lang.then
    1 * list.add(1)
  }
}
