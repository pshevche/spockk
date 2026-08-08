class BasicDataTableSpec : spock.lang.Specification() {
    fun `basic usage`(a: String, b: String, c: String) {
        io.github.pshevche.spockk.lang.expect
        a + b == c

        io.github.pshevche.spockk.lang.where
        a    ; b    ; c
        "aa" ; "b"  ; "aab"
        "a"  ; "bb" ; "abb"
    }
}
