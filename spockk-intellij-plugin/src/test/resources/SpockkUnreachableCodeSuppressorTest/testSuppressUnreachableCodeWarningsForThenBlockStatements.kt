import io.github.pshevche.spockk.lang.then
import io.github.pshevche.spockk.lang.`when`

class Spec {

  fun `feature with then`() {
    `when`
    throw RuntimeException("Boom!")

    then
    val thenStatement = "hello"
  }
}
