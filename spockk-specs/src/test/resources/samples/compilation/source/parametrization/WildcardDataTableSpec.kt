class WildcardDataTableSpec : spock.lang.Specification() {
  fun `can use pseudo-column to enable one-column table`(a: Int) {
    io.github.pshevche.spockk.lang.expect
    a == 1

    io.github.pshevche.spockk.lang.where
    a ; `_`
    1 ; `_`
  }

  fun `pseudo-column can be declared as parameter`(a: Int, `_`: Any) {
    io.github.pshevche.spockk.lang.expect
    a == 3

    io.github.pshevche.spockk.lang.where
    a ; `_`
    3 ; `_`
  }
}
