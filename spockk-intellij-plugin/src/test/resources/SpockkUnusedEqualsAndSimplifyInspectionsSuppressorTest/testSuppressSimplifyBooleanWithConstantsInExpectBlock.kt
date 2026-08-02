import io.github.pshevche.spockk.lang.expect

class SimpleSpec {

  fun `successful feature`() {
    val x = 5
    val y = 10

    expect
    x > y
  }
}
