class BasicDataTableSpec : spock.lang.Specification() {
  fun `basic usage`(a: Int, b: Int, c: Int) {
    io.github.pshevche.spockk.lang.expect
    kotlin.math.max(a, b) == c

    io.github.pshevche.spockk.lang.where
    a ; b ; c
    5 ; 7 ; 7
    3 ; 1 ; 3
    9 ; 9 ; 9
  }
}
