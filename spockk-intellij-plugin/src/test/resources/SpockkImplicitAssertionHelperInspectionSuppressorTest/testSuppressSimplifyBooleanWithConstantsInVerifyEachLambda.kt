import io.github.pshevche.spockk.lang.verifyEach

class SimpleSpec {

  private fun checkEach(things: List<Int>) {
    val x = 5
    val y = 10

    verifyEach(things) {
      x > y
    }
  }
}
