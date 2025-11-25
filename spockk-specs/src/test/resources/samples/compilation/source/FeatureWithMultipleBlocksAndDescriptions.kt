class FeatureWithMultipleBlocksAndDescriptions : spock.lang.Specification() {
    fun `some feature`() {
        io.github.pshevche.spockk.lang.given
        var a = 1

        io.github.pshevche.spockk.lang.`when`
        a += 1

        io.github.pshevche.spockk.lang.then
        assert(a == 2)

        io.github.pshevche.spockk.lang.`when`("incrementing again")
        a += 1

        io.github.pshevche.spockk.lang.then("value is 3")
        assert(a == 3)
    }
}
