import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.`when`
import io.github.pshevche.spockk.lang.then

class FeatureMethodSpec : spock.lang.Specification() {
  fun `a passing feature`() {
    expect
    assert(true)
  }

  fun `a feature with all blocks`() {
    when:
    val x = 1
    then:
    assert(x == 1)
  }
}
