import io.github.pshevche.spockk.lang.verify
import io.github.pshevche.spockk.lang.verifyAll

class SimpleSpec {

  private fun checkNested(pc: Any) {
    val x = 5
    val y = 10

    verifyAll {
      verify(pc) {
        x > y
      }
    }
  }
}
