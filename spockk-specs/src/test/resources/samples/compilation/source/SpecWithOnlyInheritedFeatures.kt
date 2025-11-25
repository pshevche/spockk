abstract class BaseSpec : spock.lang.Specification() {
    fun `inherited feature`() {
        io.github.pshevche.spockk.lang.expect
        assert(true)
    }
}
class Spec : BaseSpec()
