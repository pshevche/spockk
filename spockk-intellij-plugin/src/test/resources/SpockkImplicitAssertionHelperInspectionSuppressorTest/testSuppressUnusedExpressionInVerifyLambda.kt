import io.github.pshevche.spockk.lang.verify

class SimpleSpec {

  private fun checkPc(pc: Any) {
    val x = 5
    val y = 10

    verify(pc) {
      x > y
    }
  }
}
