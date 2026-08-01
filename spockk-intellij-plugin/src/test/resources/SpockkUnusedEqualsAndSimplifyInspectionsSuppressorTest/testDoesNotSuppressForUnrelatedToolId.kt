import io.github.pshevche.spockk.lang.then

class SimpleSpec {

  fun `successful feature`() {
    val x = 5
    val y = 10

    then
    x > y
  }
}
