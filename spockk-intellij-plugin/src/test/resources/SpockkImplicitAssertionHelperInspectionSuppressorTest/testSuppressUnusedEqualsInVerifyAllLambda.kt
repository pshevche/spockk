import io.github.pshevche.spockk.lang.verifyAll

class SimpleSpec {

  private fun checkAll() {
    val x = 5
    val y = 10

    verifyAll {
      x > y
    }
  }
}
