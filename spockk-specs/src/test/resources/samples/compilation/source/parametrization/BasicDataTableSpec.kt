class BasicDataTableSpec : spock.lang.Specification() {
  fun `basic usage`(a: Int, b: Int, c: Int) {
    io.github.pshevche.spockk.lang.expect
    kotlin.test.assertEquals(c, kotlin.math.max(a, b))

    io.github.pshevche.spockk.lang.where
    a ; b ; c
    5 ; 7 ; 7
    3 ; 1 ; 3
    9 ; 9 ; 9
  }
}
