import io.github.pshevche.spockk.lang.expect

class Spec {

  fun `feature with expect`() {
    expect
    throw RuntimeException("Boom!")
    val expectStatement = "hello"
  }
}
