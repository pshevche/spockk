class BasicDataTableSpec : spock.lang.Specification() {
    fun `a feature`(a: String, b: String, c: String, d: String) {
        io.github.pshevche.spockk.lang.expect
        kotlin.test.assertEquals(c, a + b)

        io.github.pshevche.spockk.lang.where
        a ; b ; c ; d
        "first" ; "111111111" ; "1" ; "1"
        "second" ; "2" ; "2" ; "2" // comment after lines
        "third" ; "3" ; "3" ; "3"

        "fourth" ; "4" ; "4" ; "4444444444"
        // comment between lines
        "fifth" ; "5" ; "555555555" ; "5"

        /* block comment and empty line between lines */
        "sixth" ; "6" ; "6" ; ""
    }
}
