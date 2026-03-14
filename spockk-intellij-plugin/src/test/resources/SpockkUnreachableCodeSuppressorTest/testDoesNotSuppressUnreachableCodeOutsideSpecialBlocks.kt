import io.github.pshevche.spockk.lang.expect

class Spec {

  fun `regular feature`() {
    expect
    throw RuntimeException("Boom!")
    val regularStatement = "hello"
  }
}
