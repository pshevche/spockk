class FeatureWithDataTable : spock.lang.Specification() {
  fun `some feature`(a: Int, b: Int, c: Int) {
    io.github.pshevche.spockk.lang.expect
    assert(a + b == c)

    io.github.pshevche.spockk.lang.where
    a ; b ; c
    1 ; 1 ; 2
    2 ; 2 ; 4
  }
}
