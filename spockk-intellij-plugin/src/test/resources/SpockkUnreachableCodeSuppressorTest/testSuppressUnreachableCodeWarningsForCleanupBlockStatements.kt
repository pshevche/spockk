import io.github.pshevche.spockk.lang.cleanup

class Spec {

  fun `feature with cleanup`() {
    val resource = AutoCloseable {}
    cleanup
    resource.close()
  }
}
