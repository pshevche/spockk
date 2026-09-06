import io.github.pshevche.spockk.lang.`when`

class Spec {

  fun `regular feature`() {
    `when`
    throw RuntimeException("Boom!")
    val regularStatement = "hello"
  }
}
