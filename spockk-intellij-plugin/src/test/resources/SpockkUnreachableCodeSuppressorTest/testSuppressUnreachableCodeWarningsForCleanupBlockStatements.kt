import io.github.pshevche.spockk.lang.cleanup
import io.github.pshevche.spockk.lang.expect

class Spec {

  fun `feature with cleanup`() {
    expect
    throw RuntimeException("Boom!")

    cleanup
    println("cleanup")
  }
}
